package com.example.pomodoroapp
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pomodoroapp.service.TimerServiceHelper.sendPreferencesToTimerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesStore(private val context: Context) {
    var appPreferences by mutableStateOf(null as AppPreferences?)
        private set
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

    private fun intKey(preference: PreferenceName) = intPreferencesKey(preference.name)
    private fun booleanKey(preference: PreferenceName) = booleanPreferencesKey(preference.name)

    suspend fun loadAppPreferences() {
        val preferences = context.dataStore.data.first()
        appPreferences = AppPreferences(
            workDuration = preferences[intKey(PreferenceName.WORK_DURATION)]!!,
            restDuration = preferences[intKey(PreferenceName.REST_DURATION)]!!,
            alwaysLightTheme = preferences[booleanKey(PreferenceName.ALWAYS_LIGHT_THEME)]!!,
            alwaysDarkTheme = preferences[booleanKey(PreferenceName.ALWAYS_DARK_THEME)]!!,
            changeTimerTypeOnFinish = preferences[booleanKey(PreferenceName.CHANGE_TIMER_TYPE_ON_FINISH)]!!,
            autostartRestAfterWork = preferences[booleanKey(PreferenceName.AUTOSTART_REST_AFTER_WORK)]!!,
            dndWhileWorking = preferences[booleanKey(PreferenceName.DND_WHILE_WORKING)]!!,
            silentWhileWorking = preferences[booleanKey(PreferenceName.SILENT_WHILE_WORKING)]!!,
            makeDetachedCompletionNotification = preferences[booleanKey(PreferenceName.MAKE_DETACHED_COMPLETION_NOTIFICATION)]!!
        )
    }

    suspend fun writeIntData(value: Int, preference: PreferenceName) {
        val key = intKey(preference)
        context.dataStore.edit { it[key] = value }
        loadAppPreferences()
        sendPreferencesToTimerService(context, appPreferences!!)
    }

    suspend fun writeBooleanData(value: Boolean, preference: PreferenceName) {
        val key = booleanKey(preference)
        context.dataStore.edit { it[key] = value }
        loadAppPreferences()
        sendPreferencesToTimerService(context, appPreferences!!)
    }

    //////////////////////////////////////////////////////////////////

    suspend fun setValuesForFirstLaunch() {
        val key = booleanKey(PreferenceName.APP)
        val checker = context.dataStore.data.map { it[key] }.first()
        if (checker == null)
            setDefaultPreferences()
        else
            loadAppPreferences()
    }

    suspend fun setDefaultPreferences() {
        context.dataStore.edit {
            it[intKey(PreferenceName.WORK_DURATION)] = 30 * 60
            it[intKey(PreferenceName.REST_DURATION)] = 5 * 60

            it[booleanKey(PreferenceName.ALWAYS_LIGHT_THEME)] = false
            it[booleanKey(PreferenceName.ALWAYS_DARK_THEME)] = false
            it[booleanKey(PreferenceName.CHANGE_TIMER_TYPE_ON_FINISH)] = true
            it[booleanKey(PreferenceName.AUTOSTART_REST_AFTER_WORK)] = true
            it[booleanKey(PreferenceName.DND_WHILE_WORKING)] = true
            it[booleanKey(PreferenceName.SILENT_WHILE_WORKING)] = false
            it[booleanKey(PreferenceName.MAKE_DETACHED_COMPLETION_NOTIFICATION)] = true

            it[booleanKey(PreferenceName.APP)] = true
        }
        loadAppPreferences()
    }

    ////////////////////////////////////////////////////////////////////////

    data class AppPreferences(
        val workDuration: Int,  // in seconds
        val restDuration: Int,  // in seconds
        val alwaysLightTheme: Boolean,
        val alwaysDarkTheme: Boolean,
        val changeTimerTypeOnFinish: Boolean,
        val autostartRestAfterWork: Boolean,
        val dndWhileWorking: Boolean,  // DND = do not disturb
        val silentWhileWorking: Boolean,
        val makeDetachedCompletionNotification: Boolean
    )

    enum class PreferenceName {
        WORK_DURATION,
        REST_DURATION,
        ALWAYS_LIGHT_THEME,
        ALWAYS_DARK_THEME,
        CHANGE_TIMER_TYPE_ON_FINISH,
        AUTOSTART_REST_AFTER_WORK,
        DND_WHILE_WORKING,
        SILENT_WHILE_WORKING,
        MAKE_DETACHED_COMPLETION_NOTIFICATION,
        APP
    }
}