package eu.adamgiergun.wontgoshoppingpl.common

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.adamgiergun.mykotlinlibrary.PermissionType
import eu.adamgiergun.wontgoshoppingpl.help.HelpActivity
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity

internal class AppViewModel @ViewModelInject constructor(
        @ApplicationContext private val context: Context,
        private val appPreferences: AppPreferences
) : ViewModel() {

    val isLookRich: Boolean
        get() = appPreferences.isLookRich()

//    private var isLastLookRich = false
//    val isLookChanged: Boolean
//        get() {
//            isLookRich.let {
//                val result = it != isLastLookRich
//                isLastLookRich = it
//                return result
//            }
//        }

    private var _allPermissionsGranted = MutableLiveData<Boolean>()
    val allPermissionsGranted: LiveData<Boolean>
        get() = _allPermissionsGranted

    fun setAllPermissionsGranted(value: Boolean) {
        _allPermissionsGranted.value = value
    }

    private var _askingForPermissionsNeeded = MutableLiveData<Boolean>()
    val askingForPermissionsNeeded: LiveData<Boolean>
        get() = _askingForPermissionsNeeded

    fun askingForPermissionsNeeded(value: Boolean) {
        _askingForPermissionsNeeded.value = value
    }

    init {
        _allPermissionsGranted.value =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        || isGranted(Manifest.permission.READ_CALENDAR)
                        && isGranted(Manifest.permission.WRITE_CALENDAR)
//        isLastLookRich = isLookRich
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun startHelpActivity(context: Context) {
        context.startActivity(Intent(context, HelpActivity::class.java))
    }

    fun startSettingsActivity(context: Context) {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    companion object {
        const val START_FRAGMENT_ID_TAG = "start_fragment_id"

        fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
            return grantResults.map { it == PackageManager.PERMISSION_GRANTED }.minOrNull() == true
        }

        val calendarPermissions
            get() = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

        val calendarPermissionsCode
            get() = PermissionType.CALENDAR.code
    }
}