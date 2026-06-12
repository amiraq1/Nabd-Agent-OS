package com.nabd.ai.local.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * SecureKeyManager handles encrypted storage of LLM API keys.
 */
class SecureKeyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "nabd_provider_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveKey(providerId: String, apiKey: String) {
        sharedPrefs.edit().putString("KEY_$providerId", apiKey).apply()
    }

    fun getKey(providerId: String): String? {
        return sharedPrefs.getString("KEY_$providerId", null)
    }

    fun removeKey(providerId: String) {
        sharedPrefs.edit().remove("KEY_$providerId").apply()
    }
}
