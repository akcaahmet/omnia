package com.ahmetakca.omnia.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val masterKey =
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val pref = EncryptedSharedPreferences.create(
        context, "auth_secure", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // private val pref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    fun saveTokens(accessToken: String, refreshToken: String) {
        pref.edit().putString("access_token", accessToken).putString("refresh_token", refreshToken)
            .apply()
    }

    fun saveAccessToken(accessToken: String) {
        pref.edit().putString("access_token", accessToken).apply()
    }

    fun getAccessToken(): String? {
        return pref.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return pref.getString("refresh_token", null)
    }

    fun saveRememberMe(value: Boolean) {
        pref.edit().putBoolean("remember_me", value).apply()
    }

    fun getRememberMe(): Boolean {
        return pref.getBoolean("remember_me", false)
    }

    fun clearTokens() {
        pref.edit().clear().apply()
    }
}