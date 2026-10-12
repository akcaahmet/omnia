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
import com.ahmetakca.omnia.adapter.CartListAdapter
import com.ahmetakca.omnia.databinding.FragmentCartBinding
import com.ahmetakca.omnia.model.CartAdjust
import com.ahmetakca.omnia.model.CartResponse
import com.ahmetakca.omnia.model.CartUpdate
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var apiService: ApiService
    private lateinit var cartListAdapter: CartListAdapter
    private lateinit var tokenManager: TokenManager
    var totalPrice = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromWeb()
        binding.cartBuyButton.setOnClickListener { cartBuyButton() }
    }

    private fun getFromWeb() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.summaryLinearLayout.visibility = View.GONE
        val accessToken = tokenManager.getAccessToken() ?: run {
            binding.loadingProgressBar.visibility = View.GONE
            return
        }
        compositeDisposable.add(
            apiService.cart("Bearer $accessToken").subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe(
                { response ->
                    handleResponse(response)
                    totalPrice = response.sumOf { it.totalPrice.toInt() }
                    if (totalPrice > 0) {
                        binding.totalPriceTextView.text = totalPrice.toString()
                        binding.summaryLinearLayout.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showClearButtonNavigation(true)
                    } else {
                        binding.summaryLinearLayout.visibility = View.GONE
                        (requireActivity() as MainActivity).showClearButtonNavigation(false)
                    }
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                },
                { error ->
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.summaryLinearLayout.visibility = View.GONE
                    (requireActivity() as MainActivity).showClearButtonNavigation(false)
                    error.printStackTrace()
                })
        )
    }

    private fun handleResponse(cartList: List<CartResponse>) {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        cartListAdapter = CartListAdapter(
            cartList,
            onProductClick = { cart ->
                val action =
                    CartFragmentDirections.actionCartFragmentToDetailFragment(null, cart, "cart")
                Navigation.findNavController(requireView()).navigate(action)
            },
            addToCartClick = { (productId, quantity) ->
                quantityUpdate(productId, quantity)
            },
            removeToCartClick = { (productId, quantity) ->
                quantityUpdate(productId, quantity)
            },
            deleteCartClick = { productId -> deleteToCart(productId) }
        )
        binding.recyclerView.adapter = cartListAdapter
    }

    private fun cartBuyButton() {
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.addOrder("Bearer $accessToken").subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ response ->
                Toast.makeText(
                    context,
                    "Sipariş alındı",
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

    fun clearCart() {
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.deleteCart("Bearer $accessToken").subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ response ->
                Toast.makeText(context, "Sepet temizlendi", Toast.LENGTH_SHORT).show()
            }, { error ->
                error.printStackTrace()
            })
        )
    }

    private fun deleteToCart(productId: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        // Arayüz değiştirildikten sonra tüm sepeti temizlemek için kullanılacak
        compositeDisposable.add(
            // Değiştirilecek, şuan yanlış yapılandırılmış durumda
            apiService.removeCart("Bearer $accessToken", productId).subscribeOn(
                Schedulers.io()
            ).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
                Toast.makeText(context, "Ürün sepetten silindi", Toast.LENGTH_SHORT).show()
            }, { error ->
                if (error is HttpException) {
                    val errorMessage =
                        JSONObject(error.response()?.errorBody()?.string()).getString("detail")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        )
    }

    private fun quantityUpdate(productId: Int, quantity: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        val cart = CartUpdate(quantity)
        compositeDisposable.add(
            apiService.updateCart("Bearer $accessToken", productId, cart)
                .subscribeOn(Schedulers.io())
                .observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe({ response ->
                    // Toast mesaj kaldırılabilir
                    Toast.makeText(context, "Sepet güncellendi", Toast.LENGTH_SHORT).show()
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