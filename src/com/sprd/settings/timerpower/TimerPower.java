package com.sprd.settings.timerpower;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import android.app.settings.SettingsEnums;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Scheduled power on/off application.
 */
@SearchIndexable
public class TimerPower extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener, Indexable {
    /**
     * This must be false for production.
     * If true, turns on logging, test code, etc.
     */
    static final boolean DEBUG = true;

    private static final String TAG = "TimerPower";

    private static final int POWER_ON_TIMEPICKER = 1;
    private static final int POWER_OFF_TIMEPICKER = 2;
    private static final int DAYS_ERROR = -1;
    private static final int ALARM_ON = 1;
    private static final int ALARM_OFF = 2;

    private static final String KEY_POWER_ON_TIME = "time_power_on";
    private static final String KEY_POWER_OFF_TIME = "time_power_off";
    private static final String KEY_POWER_ON_REPEAT = "repeat_power_on";
    private static final String KEY_POWER_OFF_REPEAT = "repeat_power_off";
    private static final String KEY_POWER_ON_SWTICH = "switch_power_on";
    private static final String KEY_POWER_OFF_SWTICH = "switch_power_off";
    private static final String IS_SAME_TIME = "sameTime";

    private int mCurrentTimePickerDialogId = 0;
    private Context mContext;

    private Alarm mPowerOnAlarm;
    private Alarm mPowerOffAlarm;

    private Preference mPowerOnTimePref;
    private Preference mPowerOffTimePref;
    private AlarmRepeatPreference mPowerOnRepeatPref;
    private AlarmRepeatPreference mPowerOffRepeatPref;
    private SwitchPreference mPowerOnSwitchPref;
    private SwitchPreference mPowerOffSwitchPref;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.TIMER_POWER;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mContext = getActivity();
        addPreferencesFromResource(R.xml.timer_power);
        initAllPreferences();
    }

    /**
     * Init all the preference
     */
    private void initAllPreferences() {
        mPowerOnTimePref = findPreference(KEY_POWER_ON_TIME);
        mPowerOffTimePref = findPreference(KEY_POWER_OFF_TIME);
        mPowerOnRepeatPref = (AlarmRepeatPreference)findPreference(KEY_POWER_ON_REPEAT);
        mPowerOffRepeatPref = (AlarmRepeatPreference)findPreference(KEY_POWER_OFF_REPEAT);
        mPowerOnSwitchPref = (SwitchPreference)findPreference(KEY_POWER_ON_SWTICH);
        mPowerOffSwitchPref = (SwitchPreference)findPreference(KEY_POWER_OFF_SWTICH);

        mPowerOnRepeatPref.setOnPreferenceChangeListener(this);
        mPowerOffRepeatPref.setOnPreferenceChangeListener(this);
        mPowerOnSwitchPref.setOnPreferenceChangeListener(this);
        mPowerOffSwitchPref.setOnPreferenceChangeListener(this);

        Cursor cursor = Alarms.getAlarmsCursor(mContext.getContentResolver());
        if (cursor != null) {
            // Modify for bug1132841,close cursor object
            if (cursor.moveToFirst()) {
                do {
                    final Alarm alarm = new Alarm(mContext, cursor);
                    if (!alarm.label.equals("") && alarm.label.equals("on")) {
                        mPowerOnAlarm = alarm;
                        mPowerOnSwitchPref.setChecked(alarm.enabled);
                        mPowerOnTimePref.setEnabled(alarm.enabled);
                        mPowerOnTimePref.setSummary(Alarms.formatTime(mContext, alarm.hour,
                            alarm.minutes, alarm.daysOfWeek));
                        mPowerOnRepeatPref.setEnabled(alarm.enabled);
                        mPowerOnRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
                    } else {
                        mPowerOffAlarm = alarm;
                        mPowerOffSwitchPref.setChecked(alarm.enabled);
                        mPowerOffTimePref.setEnabled(alarm.enabled);
                        mPowerOffTimePref.setSummary(Alarms.formatTime(mContext, alarm.hour,
                            alarm.minutes, alarm.daysOfWeek));
                        mPowerOffRepeatPref.setEnabled(alarm.enabled);
                        mPowerOffRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    /**
     * Show time picker dialog
     * @param dialogId: The id of time picker dialog
     */
    public void showTimePicker(int dialogId) {
        removeDialog(dialogId);
        showDialog(dialogId);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        mCurrentTimePickerDialogId = id;
        switch (id) {
            case POWER_ON_TIMEPICKER:
                return new TimePickerDialog(
                mContext,
                this,
                mPowerOnAlarm.hour,
                mPowerOnAlarm.minutes,
                DateFormat.is24HourFormat(mContext));
            case POWER_OFF_TIMEPICKER:
                return new TimePickerDialog(
                mContext,
                this,
                mPowerOffAlarm.hour,
                mPowerOffAlarm.minutes,
                DateFormat.is24HourFormat(mContext));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        return MetricsEvent.DIALOG_TIME_PICKER;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ContentResolver cn = getContentResolver();
        if (cn == null) {
            return;
        }
        Alarm alarm;
        Preference preference;
        if (mCurrentTimePickerDialogId == POWER_ON_TIMEPICKER) {
            alarm = mPowerOnAlarm;
            preference = mPowerOnTimePref;
        } else {
            alarm = mPowerOffAlarm;
            preference = mPowerOffTimePref;
        }
        boolean hasSametimeAlarm = Alarms.isSametimeAlarm(cn, hourOfDay, minute, alarm.id);
        boolean hasDuplicateDate = hasDuplicateAlarmDate(alarm);
        if (isAlarmStateChecked(mContext, alarm.id) && hasSametimeAlarm && hasDuplicateDate) {
            showSameAlarmToast(hourOfDay, minute, mCurrentTimePickerDialogId);
            Log.v(TAG, "onTimeSet : same time can't set, please set again");
            return;
        }
        int sameTimeFlag = (hasDuplicateDate && hasSametimeAlarm) ? 1 : 0;
        Log.v(TAG, "onTimeSet : hasDuplicateDate = " + hasDuplicateDate + ", hasSametimeAlarm = "
                + hasSametimeAlarm + ", sameTimeFlag = " + sameTimeFlag);
        Settings.System.putInt(cn, IS_SAME_TIME, sameTimeFlag);
        alarm.hour = hourOfDay;
        alarm.minutes = minute;
        preference.setSummary(Alarms.formatTime(mContext, alarm.hour,
                alarm.minutes, alarm.daysOfWeek));
        saveAlarm(alarm);
        showAlarmSetToast(alarm);
    }

    /**
     * Get daysCode from database
     * @param label: The label of alarm.
     * The value of label:on/off
     */
    private int getDaysCodeFromDB(String label) {
        int code = DAYS_ERROR;
        //get code from database;
        ContentResolver cr = getContentResolver();
        if (cr != null) {
            Cursor cursor = cr.query(Alarm.Columns.CONTENT_URI, null,
                    Alarm.Columns.MESSAGE + "!=" + "'" + label + "'", null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    code = cursor.getInt(cursor.getColumnIndex(Alarm.Columns.DAYS_OF_WEEK));
                }
                cursor.close();
            }
        }
        return code;
    }

    /**
     * Check if there is an alarm with the same reminder date
     * @param alarm: The alarm to check
     */
    private boolean hasDuplicateAlarmDate(Alarm alarm) {
        int code = DAYS_ERROR;
        int days = DAYS_ERROR;
        boolean hasDuplicateDate = false;

        code = getDaysCodeFromDB(alarm.label);
        if(alarm.daysOfWeek != null) {
            days = alarm.daysOfWeek.getCoded();
            hasDuplicateDate = alarm.daysOfWeek.hasDuplicateDate(days, code);
            Log.v(TAG, "hasDuplicateAlarm --- days = " + days + ", code = " + code
                    + ", hasDuplicateDate = " + hasDuplicateDate);
        }
        return hasDuplicateDate;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_POWER_ON_TIME: {
                showTimePicker(POWER_ON_TIMEPICKER);
                break;
            }
            case KEY_POWER_OFF_TIME: {
                showTimePicker(POWER_OFF_TIMEPICKER);
                break;
            }
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(final Preference p, Object newValue) {
        switch (p.getKey()) {
           case KEY_POWER_ON_SWTICH: {
               boolean enabled = (Boolean) newValue;
               if (!processAlarmSwitchClick(enabled, mPowerOnAlarm)) {
                   return false;
               }
               mPowerOnTimePref.setEnabled(enabled);
               mPowerOnRepeatPref.setEnabled(enabled);
               break;
           }
           case KEY_POWER_OFF_SWTICH: {
               boolean enabled = (Boolean) newValue;
               if (!processAlarmSwitchClick(enabled, mPowerOffAlarm)) {
                   return false;
               }
               mPowerOffTimePref.setEnabled(enabled);
               mPowerOffRepeatPref.setEnabled(enabled);
               break;
           }
           case KEY_POWER_ON_REPEAT: {
               Set<String> value = (Set<String>) newValue;
               mPowerOnAlarm.daysOfWeek = mPowerOnRepeatPref.getDaysOfWeek(value);
               if (processRepeatSet(POWER_ON_TIMEPICKER, mPowerOnAlarm)) {
                   mPowerOnRepeatPref.setDaysOfWeek(mPowerOnAlarm.daysOfWeek);
                   return true;
               }
               return false;
           }
           case KEY_POWER_OFF_REPEAT: {
               Set<String> value = (Set<String>) newValue;
               mPowerOffAlarm.daysOfWeek = mPowerOffRepeatPref.getDaysOfWeek(value);
               if (processRepeatSet(POWER_OFF_TIMEPICKER, mPowerOffAlarm)) {
                   mPowerOffRepeatPref.setDaysOfWeek(mPowerOffAlarm.daysOfWeek);
                   return true;
               }
               return false;
           }
           default:
               break;
        }
        return true;
    }

    /**
     * Check the alarm status when setting the repeat period
     * @param id: The id of timepicker dialog
     * @param alarm: The alarm that is currently setting the repeat period
     */
    private boolean processRepeatSet(int id, Alarm alarm) {
        ContentResolver cn = getContentResolver();
        if (cn == null) {
            return false;
        }
        boolean hasSametimeAlarm = Alarms.isSametimeAlarm(cn,
                alarm.hour, alarm.minutes, alarm.id);
        boolean hasDuplicateDate = hasDuplicateAlarmDate(alarm);
        if (isAlarmStateChecked(mContext, alarm.id) && hasSametimeAlarm && hasDuplicateDate) {
            showSameAlarmToast(alarm.hour, alarm.minutes, id);
            Log.v(TAG, "processRepeatSet : same time can't set, please set again");
            return false;
        }
        int sameTimeFlag = (hasDuplicateDate && hasSametimeAlarm) ? 1 : 0;
        Log.v(TAG, "processRepeatSet : hasDuplicateDate = " + hasDuplicateDate
                + ", hasSametimeAlarm = " + hasSametimeAlarm + ", sameTimeFlag = " + sameTimeFlag);
        Settings.System.putInt(cn, IS_SAME_TIME, sameTimeFlag);
        saveAlarm(alarm);
        showAlarmSetToast(alarm);
        return true;
    }

    /**
     * Check the status of the set alarm to confirm if there is the same alarm
     */
    private boolean processAlarmSwitchClick(boolean enabled, Alarm alarm) {
        ContentResolver cn = getContentResolver();
        if (cn == null) {
            return false;
        }
        boolean isSameTime = (1== Settings.System.getInt(cn,IS_SAME_TIME,0));
        boolean isSameState = isAlarmStateChecked(mContext, alarm.id);
        Log.v(TAG, "AlarmClock : onClick isSameTime = " + isSameTime + ",isSameState= " + isSameState);
        if (enabled && isSameTime && isSameState) {
            showSameAlarmToast(alarm.hour, alarm.minutes);
            return false;
        }
        Alarms.enableAlarm(mContext, alarm.id, enabled);
        alarm.enabled = enabled;
        if (enabled) {
            showAlarmSetToast(alarm);
        }
        return true;
    }

    /**
     * check if ALARM_ON and ALARM_OFF are enabled at the same time.
     */
    private boolean isAlarmStateChecked(Context context, int id) {
        boolean isChecked = false;
        Alarm alarm = null;
        if (id == ALARM_ON) {
            alarm = Alarms.getAlarm(context, context.getContentResolver(), ALARM_OFF);
        } else if (id == ALARM_OFF) {
            alarm = Alarms.getAlarm(context, context.getContentResolver(), ALARM_ON);
        }
        if (alarm != null) {
            isChecked = alarm.enabled;
        }
        return isChecked;
    }

    /**
     * When new set time has already been set by other alarms , show timepicker again
     * @param hour: The new set hour
     * @param minute: The new set minute
     * @param id: The id of timepicker dialog
     */
    private void showSameAlarmToast(int hour, int minute, int id) {
        showSameAlarmToast(hour, minute);
        showTimePicker(id);
    }

    /**
     * When new set time has already been set by other alarms, show Toast
     * @param hour: The new set hour
     * @param minute: The new set minute
     */
    private void showSameAlarmToast(int hour, int minute) {
        String time = null;
        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);
        // the time is 12Hour Format.
        if (!is24HourFormat) {
            // afternoon time
            if (hour > 12) {
                time = (hour - 12) + ":" + minute
                    + mContext.getResources().getString(R.string.timerpower_afternoon);
                if (minute < 10) {
                    time = (hour - 12) + ":0" + minute
                        + mContext.getResources().getString(R.string.timerpower_afternoon);
                }
            } else {
                // morning time
                time = hour + ":" + minute
                    + mContext.getResources().getString(R.string.timerpower_morning);
                if (minute < 10) {
                    time = hour + ":0" + minute
                        + mContext.getResources().getString(R.string.timerpower_morning);
                }
            }
        } else {
            // the time is 24Hour Format.
            time = hour + ":" + minute;
            if (minute < 10) {
                time = hour + ":0" + minute;
            }
        }
        Toast toast = Toast.makeText(mContext,
                mContext.getResources().getString(R.string.alarm_alread_exist, time), Toast.LENGTH_LONG);
        ToastManager.setToast(toast);
        toast.show();
    }

    /**
     * Display a toast that tells the user how long until the alarm
     * goes off.  This helps prevent "am/pm" mistakes.
     */
    private void showAlarmSetToast(Alarm alarm) {
        long timeInMillis = Alarms.calculateAlarm(alarm.hour, alarm.minutes,
                alarm.daysOfWeek).getTimeInMillis();
        String toastText = formatToast(mContext, timeInMillis);
        Toast toast = Toast.makeText(mContext, toastText, Toast.LENGTH_LONG);
        ToastManager.setToast(toast);
        toast.show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from now"
     */
    private String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                    (dispHour ? 2 : 0) |
                    (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    /**
     * Save valid alarm settings
     */
    private void saveAlarm(Alarm am) {
        Alarm alarm = new Alarm();
        alarm.id = am.id;
        alarm.hour = am.hour;
        alarm.minutes = am.minutes;
        alarm.daysOfWeek = am.daysOfWeek;
        alarm.label = am.label;
        alarm.enabled = am.enabled;
        alarm.time = Alarms.calculateAlarm(alarm.hour, alarm.minutes,
            alarm.daysOfWeek).getTimeInMillis();
        if (alarm.id != -1) {
            Alarms.setAlarm(mContext, alarm);
            Alarms.enableAlarm(mContext, alarm.id, alarm.enabled);
        }
    }

    /**
     * Add for search
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new TimerPowerSearchIndexProvider();

    private static class TimerPowerSearchIndexProvider extends BaseSearchIndexProvider {
        private boolean mIsAdmin;

        public TimerPowerSearchIndexProvider() {
            mIsAdmin = UserHandle.myUserId() == UserHandle.USER_SYSTEM;
        }

        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList<>();

            if (!mIsAdmin) {
                return result;
            }

            // The feature switch
            if (!context.getResources().getBoolean(R.bool.config_support_scheduledPowerOnOff)) {
                return result;
            }

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.timer_power;
            result.add(sir);

            return result;
        }
    }
}
