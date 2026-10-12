package com.ahmetakca.omnia.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.adapter.ProductListAdapter
import com.ahmetakca.omnia.databinding.FragmentHomeBinding
import com.ahmetakca.omnia.model.CartAdjust
import com.ahmetakca.omnia.model.FavoriteAdjust
import com.ahmetakca.omnia.model.LogoutRequest
import com.ahmetakca.omnia.model.ProductResponse
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var apiService: ApiService
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromWeb()
        binding.searchInputLayout.setEndIconOnClickListener {
            logout(requireView())
        }
    }

    private fun getFromWeb() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.products("Bearer $accessToken")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response ->
                        handleResponse(response)
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    },
                    { error ->
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        error.printStackTrace()
                    })
        )
    }

    private fun handleResponse(productList: List<ProductResponse>) {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        productListAdapter = ProductListAdapter(
            productList,
            onProductClick = { product ->
                val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    product, null, "home"
                )
                Navigation.findNavController(requireView()).navigate(action)
            },
            addToFavoriteClick = { productId ->
                addToFavorites(productId)
            },
            removeToFavoriteClick = { productId ->
                removeToFavorites(productId)
            }
        )
        binding.recyclerView.adapter = productListAdapter
    }

    private fun removeToFavorites(productId: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.removeFavorite("Bearer $accessToken", productId).subscribeOn(
                Schedulers.io()
            ).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
                Toast.makeText(context, "Ürün favorilerden kaldırıldı", Toast.LENGTH_SHORT).show()
            }, { error ->
                if (error is HttpException) {
                    val errorMessage =
                        JSONObject(error.response()?.errorBody()?.string()).getString("detail")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        )
    }

    private fun addToFavorites(productId: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        val favorite = FavoriteAdjust(productId)
        compositeDisposable.add(
            apiService.addFavorite("Bearer $accessToken", favorite).subscribeOn(Schedulers.io())
                .observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe({ response ->
                    Toast.makeText(
                        context,
                        "Ürün favorilere eklendi",
                        Toast.LENGTH_SHORT
                    ).show()
                }, { error ->
                    if (error is HttpException) {
                        val errorMessage =
                            JSONObject(error.response()?.errorBody()?.string()).getString("detail")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        )
    }

    private fun logout(view: View) {
        val request = LogoutRequest(tokenManager.getRefreshToken()!!)
        compositeDisposable.add(
            apiService.logout(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    tokenManager.clearTokens()
                    val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                    Navigation.findNavController(view).navigate(action)
                    Toast.makeText(context, "Çıkış başarılı", Toast.LENGTH_SHORT).show()
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}