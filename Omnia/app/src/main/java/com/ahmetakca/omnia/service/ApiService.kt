package com.ahmetakca.omnia.service

import com.ahmetakca.omnia.model.ActivateRequest
import com.ahmetakca.omnia.model.Cart
import com.ahmetakca.omnia.model.CartAdjust
import com.ahmetakca.omnia.model.CartUpdate
import com.ahmetakca.omnia.model.CartResponse
import com.ahmetakca.omnia.model.FavoriteAdjust
import com.ahmetakca.omnia.model.LoginRequest
import com.ahmetakca.omnia.model.LoginResponse
import com.ahmetakca.omnia.model.LogoutRequest
import com.ahmetakca.omnia.model.OrderResponse
import com.ahmetakca.omnia.model.ProductResponse
import com.ahmetakca.omnia.model.RegisterRequest
import com.ahmetakca.omnia.model.ResendRequest
import com.ahmetakca.omnia.model.ReviewUpdate
import com.ahmetakca.omnia.model.UserResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("/auth/login")
    fun login(@Body loginRequest: LoginRequest): Observable<LoginResponse>

    @POST("/auth/logout")
    fun logout(@Body logoutRequest: LogoutRequest): Observable<Int> // id

    @POST("/auth/register")
    fun register(@Body registerRequest: RegisterRequest): Observable<UserResponse>

    @POST("/users/activate")
    fun activate(@Body activateRequest: ActivateRequest): Observable<UserResponse>

    @POST("/users/resend-activation")
    fun resendCode(@Body resendRequest: ResendRequest): Observable<UserResponse>

    @GET("/products")
    fun products(@Header("Authorization") token: String): Observable<List<ProductResponse>>

    @POST("/cart/items")
    fun addCart(
        @Header("Authorization") token: String,
        @Body cartAdd: CartAdjust
    ): Observable<CartAdjust>

    @GET("/cart")
    fun cart(@Header("Authorization") token: String): Observable<List<CartResponse>>

    @PATCH("/cart/items/{product_id}")
    fun updateCart(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int,
        @Body cartProduct: CartUpdate
    ): Observable<Cart>

    @DELETE("/cart/items/{product_id}")
    fun removeCart(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int
    ): Observable<Int>

    @DELETE("/cart") // tüm sepeti temizleme arayüz güncellenince eklenecek
    fun deleteCart(@Header("Authorization") token: String): Observable<Cart>

    @POST("/favorites")
    fun addFavorite(
        @Header("Authorization") token: String,
        @Body favoriteAdjust: FavoriteAdjust
    ): Observable<Int>

    @GET("/favorites")
    fun favorite(@Header("Authorization") token: String): Observable<List<ProductResponse>>

    @DELETE("/favorites/{product_id}")
    fun removeFavorite(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int
    ): Observable<Int>

    @DELETE("/favorites") // Burda dönen veri tipi değiştirilecek
    fun deleteFavorite(@Header("Authorization") token: String): Observable<Int>

    @POST("/products/{product_id}/reviews")
    fun addReview(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int,
        @Body reviewUpdate: ReviewUpdate
    ): Observable<Int>

    @DELETE("/products/{product_id}/reviews/{review_id}")
    fun deleteReview(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int,
        @Path("review_id") reviewId: Int
    ): Observable<Int>

    @PUT("/products/{product_id}/reviews/{review_id}")
    fun updateReview(
        @Header("Authorization") token: String,
        @Path("product_id") productId: Int,
        @Path("review_id") reviewId: Int,
        @Body reviewUpdate: ReviewUpdate
    ): Observable<Int>

    @POST("/order")
    fun addOrder(@Header("Authorization") token: String): Observable<Int>

    @GET("/order")
    fun order(@Header("Authorization") token: String): Observable<List<OrderResponse>>
}