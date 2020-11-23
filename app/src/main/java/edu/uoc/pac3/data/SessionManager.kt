package edu.uoc.pac3.data

import android.content.Context
import edu.uoc.pac3.R

/**
 * Created by alex on 06/09/2020.
 */

class SessionManager(private val context: Context) {

    fun isUserAvailable(): Boolean {
        val fileKey = context.getString(R.string.preference_file_key)
        val accessTokenKey = context.getString(R.string.access_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return false
        return sharedPref.getString(accessTokenKey, null) != null
    }

    fun getAccessToken(): String? {
        val fileKey = context.getString(R.string.preference_file_key)
        val accessTokenKey = context.getString(R.string.access_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return null
        return sharedPref.getString(accessTokenKey, null)
    }

    fun saveAccessToken(accessToken: String) {
        val fileKey = context.getString(R.string.preference_file_key)
        val accessTokenKey = context.getString(R.string.access_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(accessTokenKey, accessToken)
            commit()
        }
    }

    fun clearAccessToken() {
        val fileKey = context.getString(R.string.preference_file_key)
        val accessTokenKey = context.getString(R.string.access_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            remove(accessTokenKey)
            commit()
        }
    }

    fun getRefreshToken(): String? {
        val fileKey = context.getString(R.string.preference_file_key)
        val refreshTokenKey = context.getString(R.string.refresh_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return null
        return sharedPref.getString(refreshTokenKey, null)
    }

    fun saveRefreshToken(refreshToken: String) {
        val fileKey = context.getString(R.string.preference_file_key)
        val refreshTokenKey = context.getString(R.string.refresh_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(refreshTokenKey, refreshToken)
            commit()
        }
    }

    fun clearRefreshToken() {
        val fileKey = context.getString(R.string.preference_file_key)
        val refreshTokenKey = context.getString(R.string.refresh_token_key)
        val sharedPref = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            remove(refreshTokenKey)
            commit()
        }
    }

}