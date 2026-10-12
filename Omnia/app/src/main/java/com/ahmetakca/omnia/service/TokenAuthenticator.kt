package com.ahmetakca.omnia.service

import com.ahmetakca.omnia.util.TokenManager
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val tokenManager: TokenManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }
        val refreshToken = tokenManager.getRefreshToken() ?: return null
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/auth/refresh")
            .post("".toRequestBody())
            .header("Authorization", "Bearer $refreshToken")
            .build()

        val refreshResponse = client.newCall(request).execute()
        refreshResponse.use {
            if (!it.isSuccessful) {
                tokenManager.clearTokens()
                return null
            }
            val newAccessToken = it.body?.string()?.trim('"') ?: return null
            println(newAccessToken)
            tokenManager.saveAccessToken(newAccessToken)

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}