package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.Context
import android.content.res.Configuration
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel

internal class SettingsViewModel @ViewModelInject constructor(
        @ApplicationContext private val context: Context
) : ViewModel() {

    private var _settingsActivityBarTitle = MutableLiveData<String>()
    val settingsActivityBarTitle: LiveData<String>
        get() = _settingsActivityBarTitle

    fun setActivityBarTitle(title: String) {
        _settingsActivityBarTitle.value = title
    }

    private var lastIsScreenWideEnough: Boolean

    val isScreenWideEnough: Boolean
        get() = isScreenSizeLarge || isOrientationLandscape

    private val isScreenSizeLarge: Boolean
        get() {
            val screenSize = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            return screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

    private val isOrientationLandscape: Boolean
        get() {
            val orientation = context.resources.configuration.orientation
            return orientation == Configuration.ORIENTATION_LANDSCAPE
        }

    fun hasScreenWideEnoughChanged(): Boolean {
        val result = lastIsScreenWideEnough != isScreenWideEnough
        lastIsScreenWideEnough = isScreenWideEnough
        return result
    }

    init {
        lastIsScreenWideEnough = isScreenWideEnough
    }

    companion object {
        const val START_FRAGMENT_ID_TAG = AppViewModel.START_FRAGMENT_ID_TAG
    }
}
