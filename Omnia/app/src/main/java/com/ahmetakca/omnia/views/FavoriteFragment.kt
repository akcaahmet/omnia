package com.ahmetakca.omnia.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.ahmetakca.omnia.MainActivity
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.R
import com.ahmetakca.omnia.adapter.ProductListAdapter
import com.ahmetakca.omnia.databinding.FragmentFavoriteBinding
import com.ahmetakca.omnia.model.FavoriteAdjust
import com.ahmetakca.omnia.model.ProductResponse
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class FavoriteFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteBinding
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
        binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromWeb()
    }

    private fun getFromWeb() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        val accessToken = tokenManager.getAccessToken() ?: run {
            binding.loadingProgressBar.visibility = View.GONE
            return
        }
        compositeDisposable.add(
            apiService.favorite("Bearer $accessToken").subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ response ->
                handleResponse(response)
                if (response.isEmpty()) {
                    (requireActivity() as MainActivity).showClearButtonNavigation(false)
                } else {
                    (requireActivity() as MainActivity).showClearButtonNavigation(true)
                }
                binding.loadingProgressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }, { error ->
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
                val action = FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(
                    product, null, "favorite"
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

    fun clearFavorites() {
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.deleteFavorite("Bearer $accessToken").subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ response ->
                Toast.makeText(
                    context,
                    "Favoriler temizlendi",
                    Toast.LENGTH_SHORT
                ).show()
            }, { error -> error.printStackTrace() })
        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}