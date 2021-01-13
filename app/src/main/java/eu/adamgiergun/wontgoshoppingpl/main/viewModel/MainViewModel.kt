package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.content.Context
import android.content.Intent
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.*
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.START_FRAGMENT_ID_TAG
import eu.adamgiergun.wontgoshoppingpl.data.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import eu.adamgiergun.wontgoshoppingpl.settings.NotificationPreferenceFragment
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import kotlinx.coroutines.launch

internal class MainViewModel @ViewModelInject constructor(
        @ApplicationContext private val context: Context,
        private val appPreferences: AppPreferences
) : ViewModel() {

    private val isFirstRun: Boolean
        get() = appPreferences.isFirstRun()

    val isLookRich
        get() = appPreferences.isLookRich()
    private var isLastLookRich = false
    val isLookChanged: Boolean
        get() {
            isLookRich.let {
                val result = it != isLastLookRich
                isLastLookRich = it
                return result
            }
        }

    init {
        if (isFirstRun)
            viewModelScope.launch { AppDatabase.getInstance(context).eventDao().initialize() }
        isLastLookRich = isLookRich
    }

    val nextAction: LiveData<MainActivity.Actions>
        get() = MainActivityActions.nextAction

    fun setNextAction(action: MainActivity.Actions) {
        MainActivityActions.setNextAction(action)
    }

    val isCalendarEnabledAndChosen: Boolean
        get() = appPreferences.isUseGoogleCalendarSet() && appPreferences.isCalendarSet()

    val mainNavigationId: Int
        get() = when {
            isFirstRun -> R.navigation.main_navigation_first_run_widget_info
            !appPreferences.wasAppWidgetInfoDisplayed() -> R.navigation.main_navigation_widget_info
            else -> R.navigation.main_navigation
        }

    val wasWidgetInfoDisplayed
        get() = appPreferences.wasAppWidgetInfoDisplayed()

    fun getBannerAd(context: Context): AdRequest {
        return AdsContainer.getBannerAd(context)
    }

    fun initializeInterstitialAd(applicationContext: Context) {
        AdsContainer.initializeInterstitialAd(applicationContext)
    }

    fun getSettingsActivityIntentAtNotifications(): Intent {
        return Intent(context, SettingsActivity::class.java).apply {
            putExtra(START_FRAGMENT_ID_TAG, NotificationPreferenceFragment::class.java.name)
        }
    }

    fun onInitialLookChoice(isLookRich: Boolean) {
        appPreferences.setLookRich(isLookRich)
        setNotFirstRun()
    }

    fun setAppWidgetInfoDisplayed() {
        appPreferences.setAppWidgetInfoDisplayed()
    }

    private fun setNotFirstRun() {
        appPreferences.setFirstRun(false)
    }
}