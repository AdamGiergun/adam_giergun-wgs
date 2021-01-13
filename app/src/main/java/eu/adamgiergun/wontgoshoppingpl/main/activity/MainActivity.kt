package eu.adamgiergun.wontgoshoppingpl.main.activity

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityMainBinding
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.DayTypesAddedToCalendarDialog
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.MainViewModel
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderDaysBeforeDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderSettingsAndDateTillDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderSettingsDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderTimeDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.TillDateDialog
import eu.adamgiergun.wontgoshoppingpl.main.fragments.AskForLookFragmentDirections
import eu.adamgiergun.wontgoshoppingpl.main.fragments.WidgetInfoFragmentDirections

/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/
@AndroidEntryPoint
internal class MainActivity : ActivityWithThemeAndPermissions() {

    private val mainViewModel: MainViewModel by viewModels()

    //    private var calStart: Calendar

    //region ActivityLifeCycleRegion
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        calStart = Calendar.getInstance();
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mainViewModel

        mViewModel.allPermissionsGranted.observe(this, {
            if (it) {
                when (mainViewModel.nextAction.value) {
                    Actions.CHECK_PERMISSIONS_LIST ->
                        mainViewModel.setNextAction(Actions.CHECK_CALENDAR_SETTINGS_LIST)
                    Actions.CHECK_PERMISSIONS_DAY ->
                        mainViewModel.setNextAction(Actions.CHECK_CALENDAR_SETTINGS_DAY)
                    else -> Unit
                }
            } else {
                mainViewModel.setNextAction(Actions.NONE)
            }
        })

        mainViewModel.nextAction.observe(this, {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                Actions.CHECK_PERMISSIONS_DAY -> checkCalendarPermissions(Actions.CHECK_CALENDAR_SETTINGS_DAY)
                Actions.CHECK_PERMISSIONS_LIST -> checkCalendarPermissions(Actions.CHECK_CALENDAR_SETTINGS_LIST)
                Actions.CHECK_CALENDAR_SETTINGS_DAY -> checkCalendarSettings(Actions.ASK_FOR_REMINDER_SETTINGS)
                Actions.CHECK_CALENDAR_SETTINGS_LIST -> checkCalendarSettings(Actions.ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR)
                Actions.ASK_FOR_REMINDER_SETTINGS -> {
                    ReminderSettingsDialog().show(supportFragmentManager, "")
                    mainViewModel.setNextAction(Actions.NONE)
                }
                Actions.ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR -> {
                    askForDayTypesAddedToCalendar()
                    mainViewModel.setNextAction(Actions.NONE)
                }
                Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL -> {
                    ReminderSettingsAndDateTillDialog().show(supportFragmentManager, "")
                    mainViewModel.setNextAction(Actions.NONE)
                }
                Actions.START_SETTINGS_ACTIVITY_IN_NOTIFICATIONS -> {
                    startActivity(mainViewModel.getSettingsActivityIntentAtNotifications())
                    mainViewModel.setNextAction(Actions.NONE)
                }
                Actions.NONE -> Unit
            }
        })

        lifecycleScope.launchWhenStarted {
            binding.adView.loadAd(mainViewModel.getBannerAd(this@MainActivity))
            mainViewModel.initializeInterstitialAd(applicationContext)
        }
//        addCrashTestButton();
    }

//        private void addCrashTestButton() {
//            Button crashButton = new Button(this);
//            crashButton.setText("Crash!");
//            crashButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View view) {
//                    throw new RuntimeException("Test Crash"); // Force a crash
//                }
//            });
//
//            addContentView(crashButton, new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//        }

    //    private void showStartTime() {
    //        val calEnd = Calendar.getInstance()
    //        val timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
    //        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
    //    }

    override fun onResume() {
        super.onResume()
        if (mainViewModel.isLookChanged) recreate()
    }
    //endregion

    //region AppMenuHandlingRegion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuLayoutId = if (mainViewModel.wasWidgetInfoDisplayed) R.menu.main_activity_actions else R.menu.main_activity_actions_first_run
        menuInflater.inflate(menuLayoutId, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                mViewModel.startSettingsActivity(this)
                true
            }
            R.id.calendar -> {
                mainViewModel.setNextAction(Actions.CHECK_PERMISSIONS_LIST)
                true
            }
            R.id.help -> {
                mViewModel.startHelpActivity(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkCalendarPermissions(nextActions: Actions) {
        if (mViewModel.allPermissionsGranted.value == true) mainViewModel.setNextAction(nextActions)
        else requestCalendarPermissions()
    }

    private fun checkCalendarSettings(nextActions: Actions) {
        mainViewModel.setNextAction(
                if (mainViewModel.isCalendarEnabledAndChosen)
                    nextActions
                else
                    Actions.START_SETTINGS_ACTIVITY_IN_NOTIFICATIONS
        )
    }

    private fun askForDayTypesAddedToCalendar() {
        DayTypesAddedToCalendarDialog().show(supportFragmentManager, "dialog")
    }
    //endregion

    //region on...ClickRegion
    fun onLookButtonClick(view: View) {
        view.findNavController().navigate(AskForLookFragmentDirections.actionAskForLookFragmentToWidgetInfoFragment())
        (view.id == R.id.imageButtonRich).let { isLookRich ->
            mainViewModel.onInitialLookChoice(isLookRich)
            if (isLookRich) recreate()
        }
    }

    fun onWidgetInfoButtonClick(view: View) {
        view.findNavController().navigate(WidgetInfoFragmentDirections.actionWidgetInfoFragmentToDaysFragment())
        mainViewModel.setAppWidgetInfoDisplayed()
        invalidateOptionsMenu()
    }

    fun showDialog(view: View) {
        when (view.id) {
            R.id.numberOfDaysBeforeView -> ReminderDaysBeforeDialog()
            R.id.timeView -> ReminderTimeDialog()
            else -> TillDateDialog()
        }.show(supportFragmentManager, "")
    }
    //endregion

    enum class Actions {
        CHECK_PERMISSIONS_DAY,
        CHECK_CALENDAR_SETTINGS_DAY,
        ASK_FOR_REMINDER_SETTINGS,
        CHECK_PERMISSIONS_LIST,
        CHECK_CALENDAR_SETTINGS_LIST,
        ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR,
        ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL,
        START_SETTINGS_ACTIVITY_IN_NOTIFICATIONS,
        NONE
    }
}