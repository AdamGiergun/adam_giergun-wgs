package eu.adamgiergun.wontgoshoppingpl.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.TypedValue.COMPLEX_UNIT_SP
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.data.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity

internal class WidgetViewModel private constructor(val layoutId: Int, val text: CharSequence, val textSize: Float, val pendingIntent: PendingIntent) {

    internal companion object {
        const val widgetTextViewId = R.id.appwidget_text
        const val widgetTextSizeUnit = COMPLEX_UNIT_SP

        suspend fun newInstance(appContext: Context): WidgetViewModel {
            val appPreferences = AppPreferences(appContext)
            if (appPreferences.isFirstRun()) {
                AppDatabase.getInstance(appContext).eventDao().initialize()
            }
            var layoutId = R.layout.app_widget_no_shopping
            var widgetText: CharSequence = ""
            val textSize = appPreferences.getAppWidgetTextSize().toFloat()
            getNearestDay(appContext)?.let {
                layoutId = if (it.isCommerceSunday) R.layout.app_widget_shopping else R.layout.app_widget_no_shopping
                widgetText = it.widgetText
            }
            val intent = Intent(appContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, 0)
            return WidgetViewModel(layoutId, widgetText, textSize, pendingIntent)
        }

        private suspend fun getNearestDay(appContext: Context): Day? {
            val database = AppDatabase.getInstance(appContext).eventDao()
            return Day.getDayForWidget(database.getFirstEventNotBefore(today))
        }

        fun logUpdated(appContext: Context) {
            FirebaseEvent.logAppWidgetUpdated(appContext)
        }

        fun logFirstAdded(appContext: Context) {
            FirebaseEvent.logAppWidgetFirstAdded(appContext)
        }

        fun logLastRemoved(appContext: Context) {
            FirebaseEvent.logAppWidgetLastRemoved(appContext)
        }
    }
}