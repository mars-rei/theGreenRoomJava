package androidev.thegreenroom
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val USER_TYPE_KEY = stringPreferencesKey("user_type")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
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
}