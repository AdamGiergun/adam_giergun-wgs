package eu.adamgiergun.wontgoshoppingpl.settings

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions

internal class SettingsActivity :
        ActivityWithThemeAndPermissions(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val settingsViewModel: SettingsViewModel by viewModels()

    private val layout: Int
        get() = if (settingsViewModel.isScreenWideEnough) R.layout.settings_activity_large else R.layout.settings_activity

    private var navigateToNotifications = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, true)
        setContentView(layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        if (savedInstanceState == null) {
            navigateToNotifications = intent.getStringExtra(SettingsViewModel.START_FRAGMENT_ID_TAG) == NotificationPreferenceFragment::class.java.name
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                onScreenWideEnoughChanged()
            }
        }
        settingsViewModel.settingsActivityBarTitle.observe(this, {
            title = it
        })
        mViewModel.askingForPermissionsNeeded.observe(this, {
            if (it) {
                mViewModel.askingForPermissionsNeeded(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestCalendarPermissions()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (navigateToNotifications) {
            navigateToNotifications = false
            findNavController(R.id.settings_nav_host_fragment).run {
                if (settingsViewModel.isScreenWideEnough) {
                    navigate(LookPreferenceFragmentDirections.actionLookPreferenceFragmentToNotificationPreferenceFragment())
                } else {
                    navigate(HeadersPreferenceFragmentDirections.actionHeadersPreferenceFragmentToNotificationPreferenceFragment())
                }
            }
        }
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        onScreenWideEnoughChanged()
//    }

    private fun onScreenWideEnoughChanged() {
        if (settingsViewModel.hasScreenWideEnoughChanged()) {
            setContentView(layout)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_activity_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.help -> {
                mViewModel.startHelpActivity(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?, pref: Preference?): Boolean {
        caller?.findNavController()?.apply {
            pref?.let { preference ->
                if (preference.fragment == LookPreferenceFragment::class.java.name) {
                    if (settingsViewModel.isScreenWideEnough) {
                        navigate(NotificationPreferenceFragmentDirections.actionNotificationPreferenceFragmentToLookPreferenceFragment())
                    } else {
                        navigate(HeadersPreferenceFragmentDirections.actionHeadersPreferenceFragmentToLookPreferenceFragment())
                    }
                } else {
                    if (settingsViewModel.isScreenWideEnough) {
                        navigate(LookPreferenceFragmentDirections.actionLookPreferenceFragmentToNotificationPreferenceFragment())
                    } else {
                        navigate(HeadersPreferenceFragmentDirections.actionHeadersPreferenceFragmentToNotificationPreferenceFragment())
                    }
                }
            }
        }
        return true
    }
}