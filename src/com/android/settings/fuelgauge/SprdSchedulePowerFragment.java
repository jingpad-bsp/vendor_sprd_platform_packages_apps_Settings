package com.android.settings.fuelgauge;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.BroadcastReceiver;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.sprdpower.IPowerManagerEx;
import android.os.sprdpower.PowerManagerEx;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedSwitchPreference;

import java.text.NumberFormat;
import java.util.Calendar;

public class SprdSchedulePowerFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener,TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "SchedulePowerMode";

    private static final String KEY_SCHEDULE_SAVER = "schedule_mode";
    private static final String KEY_TURN_ON = "schedule_turn_on_time";
    private static final String KEY_TURN_OFF = "schedule_turn_off_time";
    private static final String KEY_POWER_MODE = "schedule_power_mode";
    private static final String M12 = "h:mm aa";
    private static final String M24 = "kk:mm";
    private static final int START_TIME_TYPE = 1;
    private static final int END_TIME_TYPE = 2;
    private static final int DIALOG_START_TIME = 0;
    private static final int DIALOG_END_TIME = 1;
    private static final boolean UlTRA_SAVING_ENABLED = SystemProperties.getBoolean(
            "ro.sys.pwctl.ultrasaving", false);

    private Context mContext;
    private IPowerManagerEx mPowerManagerEx;
    private RestrictedSwitchPreference mSchedulePowerSaverPre;
    private Preference mScheduleStartPre;
    private Preference mScheduleEndPre;
    private ListPreference mSchedulePowerModePre;
    private boolean mIsScheduleSaverEnable;
    private boolean mplugged;
    private boolean mIsChargeExitLow;
    private int mScheduleStartTime;
    private int mScheduleEndTime;
    private int mScheduleMode;
    private int mStartHour;
    private int mStartMinutes;
    private int mEndHour;
    private int mEndMinutes;
    private int mTimeType;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_BATTERY_SAVER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.schedule_power_saver);

        mContext = getActivity();
        mSchedulePowerSaverPre = (RestrictedSwitchPreference) findPreference(KEY_SCHEDULE_SAVER);
        mSchedulePowerSaverPre.setOnPreferenceChangeListener(this);

        mScheduleStartPre = (Preference) findPreference(KEY_TURN_ON);
        mScheduleStartPre.setOnPreferenceChangeListener(this);

        mScheduleEndPre = (Preference) findPreference(KEY_TURN_OFF);
        mScheduleEndPre.setOnPreferenceChangeListener(this);

        mSchedulePowerModePre = (ListPreference) findPreference(KEY_POWER_MODE);
        Log.d(TAG, "UlTRA_SAVING_ENABLED = " + UlTRA_SAVING_ENABLED);
        if (UlTRA_SAVING_ENABLED) {
            mSchedulePowerModePre.setEntries(R.array.schedule_mode_switch_entries);
            mSchedulePowerModePre.setEntryValues(R.array.schedule_mode_switch_entries_values);
        } else {
            mSchedulePowerModePre.setEntries(R.array.schedule_mode_switch_entries_without_ultrasaving);
            mSchedulePowerModePre.setEntryValues(R.array.schedule_mode_switch_entries_values_without_ultrasaving);
        }
        mSchedulePowerModePre.setOnPreferenceChangeListener(this);

        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));

        mContext = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(PowerManagerEx.ACTION_POWEREX_SAVE_MODE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePrefState(null);
        updatePrefValue();
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(mIntentReceiver);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference pref) {
        Log.d(TAG, "onPreferenceTreeClick(): key-" + pref.getKey() );
        if (pref == mScheduleStartPre) {
            showTimePicker(START_TIME_TYPE);
        } else if (pref == mScheduleEndPre) {
            showTimePicker(END_TIME_TYPE);
        }
        return true;
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        return MetricsEvent.DIALOG_TIME_PICKER;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_START_TIME:
                mTimeType = START_TIME_TYPE;
                return new TimePickerDialog(mContext, this, mStartHour, mStartMinutes,
                    DateFormat.is24HourFormat(mContext));
            case DIALOG_END_TIME:
                mTimeType = END_TIME_TYPE;
                return new TimePickerDialog(mContext, this, mEndHour, mEndMinutes,
                    DateFormat.is24HourFormat(mContext));
            default:
                throw new IllegalArgumentException();
        }
    }

    public void showTimePicker(int type) {
        switch (type) {
            case START_TIME_TYPE:
                removeDialog(DIALOG_START_TIME);
                showDialog(DIALOG_START_TIME);
                return;
            case END_TIME_TYPE:
                removeDialog(DIALOG_END_TIME);
                showDialog(DIALOG_END_TIME);
                return;
            default:
                return;
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        // bug 948347 : power saving optimization should not be set when charging and the switch of ChargeExitLow is opened.
        if (mIsChargeExitLow && mplugged) {
            return;
        }

        try {
            if (mTimeType == START_TIME_TYPE) {
                Log.d(TAG, "onTimeSet() START_TIME_TYPE() hour: " + hour);
                if (hour == mEndHour && minute == mEndMinutes) {
                    Toast.makeText(mContext, getString(R.string.schedule_time_exist), Toast.LENGTH_LONG).show();
                    return;
                }
                mPowerManagerEx.setSchedulePowerMode_StartTime(hour, minute);
                mScheduleStartPre.setSummary(formatTime(hour, minute));
                mStartHour = hour;
                mStartMinutes = minute;
            } else if (mTimeType == END_TIME_TYPE) {
                Log.d(TAG, "onTimeSet() END_TIME_TYPE()");
                if (hour == mStartHour && minute == mStartMinutes) {
                    Toast.makeText(mContext, getString(R.string.schedule_time_exist), Toast.LENGTH_LONG).show();
                    return;
                }
                mPowerManagerEx.setSchedulePowerMode_EndTime(hour, minute);
                mScheduleEndPre.setSummary(formatTime(hour, minute));
                mEndHour = hour;
                mEndMinutes = minute;
            }
        } catch (RemoteException e) {
            // Not much we can do here
        }

    }

    private String formatTime(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        if (c == null) return "";
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        String format = DateFormat.is24HourFormat(mContext) ? M24 : M12;
        return (String)DateFormat.format(format, c);
    }

     @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange(): key-" + preference.getKey() + " newValue - " + newValue);
        // bug 948347 : power saving optimization should not be set when charging and the switch of ChargeExitLow is opened.
        if (mIsChargeExitLow && mplugged) {
            return true;
        }

        if (KEY_SCHEDULE_SAVER.equals(preference.getKey())) {
            final boolean enabled = (Boolean) newValue;
            try {
                mPowerManagerEx.setSchedule_Enable(enabled);
            } catch (RemoteException e) {
                // Not much we can do here
            }
            updatePrefValue();
            return true;
        } else if (KEY_POWER_MODE.equals(preference.getKey())) {
            final int value = Integer.parseInt((String) newValue);
            try {
                mPowerManagerEx.setSchedule_Mode(value);
                mSchedulePowerModePre.setValue((String) newValue);
                mSchedulePowerModePre.setSummary(mSchedulePowerModePre.getEntry());
            } catch (RemoteException e) {
                // Not much we can do here
            }
            return true;
        }
        return true;
    }

    private void updatePrefValue() {
        try {
            mIsScheduleSaverEnable = mPowerManagerEx.getSchedule_Enable();
            mScheduleStartTime = mPowerManagerEx.getSchedulePowerMode_StartTime();
            mScheduleEndTime = mPowerManagerEx.getSchedulePowerMode_EndTime();
            mScheduleMode = mPowerManagerEx.getSchedule_Mode();
        } catch (RemoteException e) {
            // Not much we can do here
        }
        mSchedulePowerSaverPre.setChecked(mIsScheduleSaverEnable);

        mStartHour = mScheduleStartTime / 100;
        mStartMinutes = mScheduleStartTime % 100;
        mEndHour = mScheduleEndTime / 100;
        mEndMinutes = mScheduleEndTime % 100;
        Log.d(TAG,"mScheduleMode: " + mScheduleMode
                + " mStartHour: " + mStartHour + " mStartMinutes: " + mStartMinutes);
        mScheduleStartPre.setSummary(formatTime(mStartHour, mStartMinutes));
        mScheduleEndPre.setSummary(formatTime(mEndHour, mEndMinutes));
        mSchedulePowerModePre.setValue(Integer.toString(mScheduleMode));
        mSchedulePowerModePre.setSummary(mSchedulePowerModePre.getEntry());
    }

    public void updatePrefState(Intent intent) {
        if (null != intent) {
            mplugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
        }
        try {
            mIsChargeExitLow = mPowerManagerEx.getSmartSavingModeWhenCharging();
        } catch (RemoteException e) {
            // Not much we can do here
        }
         Log.d(TAG,"mplugged = " + mplugged + " mIsChargeExitLow= " + mIsChargeExitLow );
        if (mIsChargeExitLow && mplugged) {
            mSchedulePowerSaverPre.setEnabled(false);
            mScheduleStartPre.setEnabled(false);
            mScheduleEndPre.setEnabled(false);
            mSchedulePowerModePre.setEnabled(false);
        } else {
            mSchedulePowerSaverPre.setEnabled(true);
            mScheduleStartPre.setEnabled(true);
            mScheduleEndPre.setEnabled(true);
            mSchedulePowerModePre.setEnabled(true);
        }
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                updatePrefState(intent);
                updatePrefValue();
            } else if (intent.getAction().equals(PowerManagerEx.ACTION_POWEREX_SAVE_MODE_CHANGED)) {
                updatePrefValue();
            }
        }
    };
}

