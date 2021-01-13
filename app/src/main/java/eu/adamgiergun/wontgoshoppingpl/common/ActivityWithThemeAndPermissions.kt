package eu.adamgiergun.wontgoshoppingpl.common

import android.content.res.Resources.Theme
import android.os.Build
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import eu.adamgiergun.mykotlinlibrary.AppCompatActivityWithHomeInBar
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.areAllPermissionsGranted
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissions
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissionsCode
import javax.inject.Inject

@AndroidEntryPoint
internal abstract class ActivityWithThemeAndPermissions : AppCompatActivityWithHomeInBar() {

    protected val mViewModel: AppViewModel by viewModels()
    @Inject
    lateinit var appPreferences: AppPreferences

    override fun getTheme(): Theme {
        return super.getTheme().apply {
            val style = if (appPreferences.isLookRich()) R.style.AppThemeCustomizationsRich else R.style.AppThemeCustomizationsMini
            applyStyle(style, true)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected fun requestCalendarPermissions() {
        requestPermissions(calendarPermissions, calendarPermissionsCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == calendarPermissionsCode) {
            if (areAllPermissionsGranted(grantResults)) {
                mViewModel.setAllPermissionsGranted(true)
            } else {
                mViewModel.setAllPermissionsGranted(false)
                Toast.makeText(this, R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show()
            }
        }
    }
}