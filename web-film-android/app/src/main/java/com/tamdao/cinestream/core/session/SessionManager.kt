package com.tamdao.cinestream.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_INFO = stringPreferencesKey("user_info")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val userInfo: Flow<SessionUser?> = context.dataStore.data.map { preferences ->
        val json = preferences[USER_INFO]
        if (json != null) {
            try {
                gson.fromJson(json, SessionUser::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun saveSession(accessToken: String, refreshToken: String, user: SessionUser) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[IS_LOGGED_IN] = true
            preferences[USER_INFO] = gson.toJson(user)
        }
    }

    suspend fun updateUserInfo(user: SessionUser) {
        context.dataStore.edit { preferences ->
            preferences[USER_INFO] = gson.toJson(user)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class SessionUser(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val avatarUrl: String?,
    val role: String
)
