package androidev.thegreenroom
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val USER_TYPE_KEY = stringPreferencesKey("user_type")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    // save user type
    fun setUserType(userType: String) {
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[USER_TYPE_KEY] = userType
            }
        }
    }

    // save onboarding status
    fun setOnboardingComplete(completed: Boolean) {
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[ONBOARDING_COMPLETED_KEY] = completed
            }
        }
    }

    // save user id
    fun setUserId(userId: String) {
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[USER_ID_KEY] = userId
            }
        }
    }


    // read user type
    fun getUserTypeBlocking(): String {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[USER_TYPE_KEY] ?: ""
            }.first()
        }
    }

    // read onboarding status
    fun isOnboardingCompletedBlocking(): Boolean {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[ONBOARDING_COMPLETED_KEY] ?: false
            }.first()
        }
    }

    // read user id
    fun getUserIdBlocking(): String {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[USER_ID_KEY] ?: ""
            }.first()
        }
    }
}