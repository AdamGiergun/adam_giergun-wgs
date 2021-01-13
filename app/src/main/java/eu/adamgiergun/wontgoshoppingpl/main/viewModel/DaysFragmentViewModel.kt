package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.content.Context
import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AdsContainer
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.data.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.data.Event
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import kotlinx.coroutines.launch
import java.util.*

internal class DaysFragmentViewModel @ViewModelInject constructor(
        @ApplicationContext private val context: Context,
        private val appPreferences: AppPreferences
) : ViewModel() {

    var isLookRich = false

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    private val _isDbChanged = MutableLiveData<Boolean>()
    val isDbChanged: LiveData<Boolean>
        get() = _isDbChanged

    val lastActionIsAddList
        get() = MainActivityActions.lastActionIsAddList

    val lastTouchedPosition
        get() = LastTouchedDay.position

    var density: Float = 0F

    val tillDate: LiveData<SimpleDate>
        get() = DialogParameters.tillDate

    val lastDateInMillis
        get() = DialogParameters.lastDateInMillis

    private var dayTypesToBeDisplayed = Day.Type.Set()
    private lateinit var dayTypesToBeAddedToCalendar: Day.Type.Set

    init {
        initializeReminder()
        val todayCal = Calendar.getInstance()
        DialogParameters.tillDate.value = SimpleDate.getLastDayOfMonth(todayCal[Calendar.YEAR], todayCal[Calendar.MONTH] + 2)
        viewModelScope.launch { updateDisplayedDataSetIfNeeded() }
        isLookRich = appPreferences.isLookRich()
    }

    fun getDayTypesToBeAddedToCalendarAsArray(): BooleanArray {
        return booleanArrayOf(
                dayTypesToBeAddedToCalendar.contains(Day.Type.NON_COMMERCE_DAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.HOLIDAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.COMMERCE_SUNDAY)
        )
    }

    fun updateDayTypesToBeAddedToCalendar(int: Int, value: Boolean) {
        Day.Type.getType(int)?.let {
            if (value)
                dayTypesToBeAddedToCalendar.add(it)
            else
                dayTypesToBeAddedToCalendar.remove(it)
        }
    }

    val reminderDaysBefore: LiveData<Int>
        get() = DialogParameters.Reminder.daysBefore
    val reminderTime: LiveData<SimpleTime>
        get() = DialogParameters.Reminder.time

    fun initializeReminder() {
        DialogParameters.Reminder.daysBefore.value = appPreferences.getReminderDaysBefore()
        DialogParameters.Reminder.time.value = SimpleTime(appPreferences.getReminderTime())
    }

    fun setReminderDaysBefore(daysBefore: Int) {
        DialogParameters.Reminder.daysBefore.value = daysBefore
    }

    fun setReminderTime(hour: Int, minute: Int) {
        DialogParameters.Reminder.time.value = SimpleTime(hour, minute)
    }

    fun saveCurrentReminderAsDefault() {
        appPreferences.setReminderDaysBefore(DialogParameters.Reminder.daysBefore.value)
        appPreferences.setReminderTime(DialogParameters.Reminder.time.value.toString())
    }

    fun setTillDate(year: Int, month: Int, day: Int) {
        DialogParameters.tillDate.value = SimpleDate(year, month, day)
    }

    fun askForReminderSettingsAndDateTill() {
        MainActivityActions.setNextAction(MainActivity.Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL)
    }

    val dayDescriptionForDialog: String
        get() = LastTouchedDay.value.descriptionForDialog

    fun addToCalendar(isActionAddList: Boolean) {
        MainActivityActions.lastActionIsAddList = isActionAddList
        viewModelScope.launch { AdsContainer.getInterstitialAd(context)?.show() }
        viewModelScope.launch {
            val tillDateValue = DialogParameters.tillDate.value
            when {
                isActionAddList && tillDateValue == null ->
                    false
                isActionAddList && tillDateValue != null ->
                    Day.List.addToCalendar(
                            context,
                            dayTypesToBeAddedToCalendar,
                            DialogParameters.Reminder.get(appPreferences, timeZoneId),
                            timeZoneId,
                            tillDateValue)
                else -> LastTouchedDay.value.run {
                    setReminder(DialogParameters.Reminder.get(appPreferences, timeZoneId))
                    addToGoogleCalendar(context, timeZoneId)
                }
            }.let { result ->
                logCalendarEvent(result)
                showInfo(result)
                if (result) _isDbChanged.value = true
            }
        }
    }

    private val timeZoneId: String
        get() = if (appPreferences.getUsePolishTimeZone()) "Europe/Warsaw" else TimeZone.getDefault().id

    private fun showInfo(resultIsPositive: Boolean) {
        val info = when {
            resultIsPositive -> R.string.info_added_to_calendar
            MainActivityActions.lastActionIsAddList -> R.string.error_sth_went_wrong
            else -> R.string.error_adding_to_calendar_failed
        }
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
    }

    //region Firebase log
    private fun logCalendarEvent(result: Boolean) {
        FirebaseEvent.logCalendarEvent(context, MainActivityActions.lastActionIsAddList, result)
    }

    fun logLook() {
        FirebaseEvent.logAppTheme(context, appPreferences.isLookRich())
    }
    //endregion

    fun setActionNone() {
        MainActivityActions.setNextAction(MainActivity.Actions.NONE)
    }

    fun updateDisplayedDataSetIfNeeded() {
        appPreferences.storedDayTypeSet().let { storedDayTypeSet ->
            if (dayTypesToBeDisplayed != storedDayTypeSet) {
                dayTypesToBeDisplayed = storedDayTypeSet
                dayTypesToBeAddedToCalendar = storedDayTypeSet
                setDaysAndLastDate()
            }
        }
    }

    private fun setDaysAndLastDate() {
        val dayTypes = dayTypesToBeDisplayed.map { it.code.toString() }.toList()
        AppDatabase.getInstance(context).eventDao().run {
            viewModelScope.launch {
                getAllEventsOfTypeNotBefore(dayTypes, today)?.let { list ->
                    _events.value = list
                }

                getLastEvent()?.let {
                    val lastDay = Day(it)
                    DialogParameters.lastDateInMillis = lastDay.dateInMillis
                }
            }
        }
    }

    fun setLastTouchedItem(day: Day?, position: Int, isDbChanged: Boolean) {
        day?.let {
            LastTouchedDay.value = it
            MainActivityActions.setNextAction(MainActivity.Actions.CHECK_PERMISSIONS_DAY)
        }
        LastTouchedDay.position = position
        if (isDbChanged) _isDbChanged.value = true
    }

    private object DialogParameters {
        object Reminder {
            val daysBefore = MutableLiveData<Int>()
            val time = MutableLiveData<SimpleTime>()

            fun get(appPreferences: AppPreferences, timeZoneId: String): SimpleReminder {
                val daysBefore = daysBefore.value
                        ?: appPreferences.getReminderDaysBefore()
                val time = time.value ?: SimpleTime(appPreferences.getReminderTime())
                return SimpleReminder(SimpleReminder.METHOD_ALERT, time, daysBefore, timeZoneId)
            }
        }

        val tillDate = MutableLiveData<SimpleDate>()

        var lastDateInMillis = 0L
    }

    private object LastTouchedDay {
        lateinit var value: Day
        var position = -1
    }
}