package com.ahmetakca.omnia.views

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.adapter.ReviewListAdapter
import com.ahmetakca.omnia.databinding.FragmentDetailBinding
import com.ahmetakca.omnia.model.CartAdjust
import com.ahmetakca.omnia.model.CartUpdate
import com.ahmetakca.omnia.model.CartResponse
import com.ahmetakca.omnia.model.FavoriteAdjust
import com.ahmetakca.omnia.model.ProductResponse
import com.ahmetakca.omnia.model.ReviewResponse
import com.ahmetakca.omnia.model.ReviewUpdate
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.internal.format
import org.json.JSONObject
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var reviewListAdapter: ReviewListAdapter
    private val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedReviewId: Int? = null
    private var source: String? = null
    private var product: ProductResponse? = null
    private var cart: CartResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = DetailFragmentArgs.fromBundle(requireArguments())
        source = args.source
        product = args.product
        cart = args.cart
        if (source == "cart") {
            val cartItem = cart ?: return
            handleResponse(cartItem.productId, cartItem.reviews)
            binding.favoriteButton.visibility = View.GONE
            binding.removeCartButton.visibility = View.VISIBLE
            binding.addToCartButton.visibility = View.GONE
            binding.productInfo.visibility = View.VISIBLE
            Glide.with(requireContext()).load(cartItem.imageUrl).into(binding.productImage)
            var productCount = cartItem.quantity
            var totalPrice = cartItem.price * productCount
            binding.removeCartButton.setOnClickListener {
                removeCart(cartItem.productId)
                binding.productPrice.text = (cartItem.totalPrice / cartItem.quantity).toString()
                Toast.makeText(context, "Ürün sepetten kaldırıldı", Toast.LENGTH_SHORT).show()
                binding.removeCartButton.visibility = View.GONE
                binding.favoriteButton.visibility = View.VISIBLE
                binding.productInfo.visibility = View.GONE
                binding.addToCartButton.visibility = View.VISIBLE
            }
            binding.addToCartButton.setOnClickListener {
                addToCart(cartItem.productId, 1)
                Toast.makeText(context, "Ürün sepete eklendi", Toast.LENGTH_SHORT).show()
                binding.removeCartButton.visibility = View.VISIBLE
                binding.favoriteButton.visibility = View.GONE
                binding.productInfo.visibility = View.VISIBLE
                binding.addToCartButton.visibility = View.GONE
            }
            binding.productName.text = cartItem.name
            binding.productDescription.text = cartItem.description
            binding.quantityProduct.text = productCount.toString()
            binding.productPrice.text = totalPrice.toString()
            parentFragmentManager.setFragmentResultListener(
                "quantity_result",
                viewLifecycleOwner
            ) { _, bundle ->
                val selectedQuantity = bundle.getInt("selected_quantity")
                productCount = selectedQuantity
                totalPrice = cartItem.price * productCount
                cartItem.quantity = productCount
                cartItem.totalPrice = totalPrice
                binding.quantityProduct.text = productCount.toString()
                binding.productPrice.text = totalPrice.toString()
                binding.quantityRemove.visibility =
                    if (selectedQuantity > 1) View.VISIBLE else View.GONE
                quantityIncrease(cartItem.productId, productCount)
            }
            binding.quantityProduct.setOnClickListener {
                BottomSheetQuantityFragment().show(parentFragmentManager, "BottomSheetQuantity")
            }
            binding.quantityAdd.setOnClickListener {
                productCount++
                totalPrice = cartItem.price * productCount
                cartItem.quantity = productCount
                cartItem.totalPrice = totalPrice
                quantityIncrease(cartItem.productId, productCount)
                binding.quantityProduct.text = productCount.toString()
                binding.productPrice.text = totalPrice.toString()
                binding.quantityRemove.visibility = View.VISIBLE
            }
            binding.quantityRemove.setOnClickListener {
                if (productCount > 1) {
                    productCount--
                    totalPrice = cartItem.price * productCount
                    cartItem.quantity = productCount
                    cartItem.totalPrice = totalPrice
                    quantityIncrease(cartItem.productId, productCount)
                    binding.quantityProduct.text = productCount.toString()
                    binding.productPrice.text = totalPrice.toString()
                    binding.quantityRemove.visibility =
                        if (productCount > 1) View.VISIBLE else View.GONE
                }
            }
            binding.quantityRemove.visibility =
                if (productCount > 1) View.VISIBLE else View.GONE
            if (cartItem.hasPurchased) {
                binding.commentInputLayout.visibility = View.VISIBLE
            } else {
                binding.commentInputLayout.visibility = View.GONE
            }
            binding.commentInputLayout.setEndIconOnClickListener {
                if (selectedReviewId != null) {
                    editToReview(
                        cartItem.productId,
                        selectedReviewId!!,
                        binding.commentEditText.text.toString()
                    )
                    return@setEndIconOnClickListener
                }
                addToReviews(cartItem.productId, binding.commentEditText.text.toString())
            }
        } else if (source == "home" || source == "favorite") { // home
            val productItem = product ?: return
            handleResponse(productItem.productId, productItem.reviews)
            Glide.with(requireContext()).load(productItem.imageUrl).into(binding.productImage)
            binding.favoriteButton.imageTintList = if (productItem.isFavorite) {
                ColorStateList.valueOf("#EF5350".toColorInt())
            } else {
                ColorStateList.valueOf("#1F1F1F".toColorInt())
            }
            binding.favoriteButton.setOnClickListener {
                if (productItem.isFavorite) {
                    removeToFavorites(productItem.productId)
                    productItem.isFavorite = false
                } else {
                    addToFavorites(productItem.productId)
                    productItem.isFavorite = true
                }
                binding.favoriteButton.imageTintList = if (productItem.isFavorite) {
                    ColorStateList.valueOf("#EF5350".toColorInt())
                } else {
                    ColorStateList.valueOf("#1F1F1F".toColorInt())
                }
            }
            binding.productName.text = productItem.name
            binding.productDescription.text = productItem.description
            binding.productPrice.text = productItem.price.toString()
            binding.addToCartButton.setOnClickListener {
                addToCart(productItem.productId, 1)
            }
            if (productItem.hasPurchased) {
                binding.commentInputLayout.visibility = View.VISIBLE
            } else {
                binding.commentInputLayout.visibility = View.GONE
            }
            binding.commentInputLayout.setEndIconOnClickListener {
                if (selectedReviewId != null) {
                    editToReview(
                        productItem.productId,
                        selectedReviewId!!,
                        binding.commentEditText.text.toString()
                    )
                    return@setEndIconOnClickListener
                }
                addToReviews(productItem.productId, binding.commentEditText.text.toString())
            }
        }
    }

    private fun handleResponse(productId: Int, reviewList: List<ReviewResponse>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewListAdapter =
            ReviewListAdapter(
                reviewList,
                removeToReviewClick = { reviewId -> removeToReview(productId, reviewId) },
                editToReviewClick = { reviewId, comment ->
                    selectedReviewId = reviewId
                    binding.commentEditText.setText(comment)
                },
                formatter
            )
        binding.recyclerView.adapter = reviewListAdapter
    }

    private fun editToReview(productId: Int, reviewId: Int, comment: String) {
        val accessToken = tokenManager.getAccessToken() ?: return
        val review = ReviewUpdate(comment)
        compositeDisposable.add(
            apiService.updateReview("Bearer $accessToken", productId, reviewId, review)
                .subscribeOn(
                    Schedulers.io()
                ).observeOn(AndroidSchedulers.mainThread()).subscribe(
                    { response ->
                        Toast.makeText(context, "Mesaj güncellendi", Toast.LENGTH_SHORT).show()
                    },
                    { error ->
                        if (error is HttpException) {
                            val errorMessage =
                                JSONObject(
                                    error.response()?.errorBody()?.string()
                                ).getString("detail")
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
        )
    }

    private fun removeToReview(productId: Int, reviewId: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.deleteReview("Bearer $accessToken", productId, reviewId).subscribeOn(
                Schedulers.io()
            ).observeOn(AndroidSchedulers.mainThread()).subscribe()
        )
    }

    private fun addToReviews(productId: Int, comment: String) {
        if (comment.trim().isEmpty()) {
            Toast.makeText(context, "Yorum boş bırakılamaz", Toast.LENGTH_SHORT).show()
            return
        }
        val accessToken = tokenManager.getAccessToken() ?: return
        val review = ReviewUpdate(comment)
        compositeDisposable.add(
            apiService.addReview("Bearer $accessToken", productId, review)
                .subscribeOn(Schedulers.io()).observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe({ response ->
                    Toast.makeText(context, "Mesaj paylaşıldı", Toast.LENGTH_SHORT).show()
                }, { error ->
                    if (error is HttpException) {
                        val errorMessage =
                            JSONObject(error.response()?.errorBody()?.string()).getString("detail")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
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

    private fun addToCart(productId: Int, quantity: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        val cart = CartAdjust(productId, quantity)
        compositeDisposable.add(
            apiService.addCart("Bearer $accessToken", cart)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Toast.makeText(context, "Ürün sepete eklendi", Toast.LENGTH_SHORT).show()
                    val action = DetailFragmentDirections.actionDetailFragmentToCartFragment()
                    Navigation.findNavController(requireView()).navigate(action)
                }, { error ->
                    if (error is HttpException) {
                        val errorMessage =
                            JSONObject(error.response()?.errorBody()?.string()).getString("detail")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        )
    }

    private fun quantityIncrease(productId: Int, quantity: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        val cart = CartUpdate(quantity)
        compositeDisposable.add(
            apiService.updateCart("Bearer $accessToken", productId, cart)
                .subscribeOn(Schedulers.io())
                .observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe({ response -> }, { error -> })
        )
    }

    private fun removeCart(productId: Int) {
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.removeCart("Bearer $accessToken", productId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ response -> }, { error -> })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}