package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import eu.adamgiergun.mykotlinlibrary.MyAppPreferences
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.day.Day.Type.Companion.newSet

internal class AppPreferences (private val context: Context) : MyAppPreferences() {

    fun isLookRich(): Boolean {
        val key = context.getString(R.string.app_look_preference_key)
        return getBoolean(context, key, false)
    }

    fun setLookRich(value: Boolean) {
        val key = context.getString(R.string.app_look_preference_key)
        putBoolean(context, key, value)
    }

    fun storedDayTypeSet(): Day.Type.Set {
        return newSet(
                showNonCommerceDays(),
                showHolidays(),
                showCommerceSundays())
    }

    private fun showCommerceSundays(): Boolean {
        val key = context.getString(R.string.include_commerce_sundays_preference_key)
        return getBoolean(context, key, true)
    }

    private fun showHolidays(): Boolean {
        val key = context.getString(R.string.include_holidays_preference_key)
        return getBoolean(context, key, true)
    }

    private fun showNonCommerceDays(): Boolean {
        val key = context.getString(R.string.include_non_commerce_days_preference_key)
        return getBoolean(context, key, true)
    }

    fun wasAppWidgetInfoDisplayed(): Boolean {
        val key = context.getString(R.string.key_app_widget_info_displayed)
        return getBoolean(context, key, false)
    }

    fun setAppWidgetInfoDisplayed() {
        val key = context.getString(R.string.key_app_widget_info_displayed)
        putBoolean(context, key, true)
    }

    fun getAppWidgetTextSize(): String {
        val key = context.getString(R.string.key_app_widget_text_size)
        return getString(context, key, "14")
    }

    fun getUsePolishTimeZone(): Boolean {
        val key = context.getString(R.string.key_use_pl_time_zone)
        return getBoolean(context, key, true)
    }

    fun getReminderDaysBefore(): Int {
        val key = context.getString(R.string.default_reminder_days_before_preference_key)
        val defaultValue = context.resources.getInteger(R.integer.reminder_days_before_default_value)
        return getInt(context, key, defaultValue)
    }

    fun setReminderDaysBefore(value: Int?) {
        value?.let {
            val key = context.getString(R.string.default_reminder_days_before_preference_key)
            putInt(context, key, it)
        }
    }

    fun getReminderTime(): String {
        val key = context.getString(R.string.default_reminder_time_preference_key)
        val defaultValue = context.getString(R.string.default_reminder_time_preference_default_value)
        return getString(context, key, defaultValue)
    }

    fun setReminderTime(value: String?) {
        value?.let {
            val key = context.getString(R.string.default_reminder_time_preference_key)
            putString(context, key, it)
        }
    }

    fun getDefaultCalendarId() = getDefaultCalendarId(context)
    fun isCalendarSet() = isCalendarSet(context)
    fun isFirstRun() = isFirstRun(context)
    fun setFirstRun(value: Boolean) = setFirstRun(context, value)
    fun isUseGoogleCalendarSet() = isUseGoogleCalendarSet(context)
}