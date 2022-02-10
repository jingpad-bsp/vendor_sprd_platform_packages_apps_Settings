package com.android.settings.fuelgauge;

import java.text.NumberFormat;

import android.content.BroadcastReceiver;
import android.os.BatteryManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.sprdpower.IPowerManagerEx;
import android.os.sprdpower.PowerManagerEx;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceScreen;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedSwitchPreference;

/**
 * Fragment used for showing low power battery saver settings.
 */
public class SprdAutoLowPowerFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "LowPowerAutoSave";

    private static final String KEY_LOW_POWER = "low_power_battery_saver";
    private static final String KEY_SET_VALUE = "low_power_value";
    private static final String KEY_SWITCH = "low_power_mode_switch";

    private static final boolean UlTRA_SAVING_ENABLED = SystemProperties.getBoolean(
            "ro.sys.pwctl.ultrasaving", false);

    private Context mContext;
    private boolean mIsAutoLowEnable;
    private boolean mIsChargeExitLow;
    private boolean mplugged;
    private int mLowPowerValue;
    private int mLowPowerSwitchMode;
    private IPowerManagerEx mPowerManagerEx;
    private RestrictedSwitchPreference mLowPowerSavePre;
    private ListPreference mLowPowerValuePre;
    private ListPreference mModeSwitchPre;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_BATTERY_SAVER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.low_power_battery_saver);

        mLowPowerSavePre = (RestrictedSwitchPreference) findPreference(KEY_LOW_POWER);
        mLowPowerSavePre.setOnPreferenceChangeListener(this);

        mLowPowerValuePre = (ListPreference) findPreference(KEY_SET_VALUE);
        mLowPowerValuePre.setOnPreferenceChangeListener(this);

        mModeSwitchPre = (ListPreference) findPreference(KEY_SWITCH);
        Log.d(TAG, "UlTRA_SAVING_ENABLED = " + UlTRA_SAVING_ENABLED);
        if (UlTRA_SAVING_ENABLED) {
            mModeSwitchPre.setEntries(R.array.low_power_switch_entries);
            mModeSwitchPre.setEntryValues(R.array.low_power_switch_entries_values);
        } else {
            mModeSwitchPre.setEntries(R.array.low_power_switch_entries_without_ultrasaving);
            mModeSwitchPre.setEntryValues(R.array.low_power_switch_entries_values_without_ultrasaving);
        }
        mModeSwitchPre.setOnPreferenceChangeListener(this);
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange(): key" + preference.getKey() + " newValue - " + newValue);
        // bug 948347 : power saving optimization should not be set when charging and the switch of ChargeExitLow is opened.
        if (mIsChargeExitLow && mplugged) {
            return true;
        }

        if (KEY_LOW_POWER.equals(preference.getKey())) {
            final boolean enabled = (Boolean) newValue;
            try {
                mPowerManagerEx.setAutoLowPower_Enable(enabled);
            } catch (RemoteException e) {
                // Not much we can do here
            }
            updatePrefValue();
            return true;
        } else if (KEY_SET_VALUE.equals(preference.getKey())) {
            final int value = Integer.parseInt((String) newValue);
            try {
                mPowerManagerEx.setAutoLowPower_BattValue(value);
            } catch (RemoteException e) {
                // Not much we can do here
            }
            String label = Utils.formatPercentage(value);
            mLowPowerValuePre.setSummary(label.replace("%","%%"));
            return true;
        } else if (KEY_SWITCH.equals(preference.getKey())) {
            final int value = Integer.parseInt((String) newValue);
            Log.d(TAG, "KEY_SWITCH value = " + value+ "entry = " + mModeSwitchPre.getEntry());
            try {
                mPowerManagerEx.setAutoLowPower_Mode(value);
                mModeSwitchPre.setValue((String) newValue);
                mModeSwitchPre.setSummary(mModeSwitchPre.getEntry());
            } catch (RemoteException e) {
                // Not much we can do here
            }
            return true;
        }
        return true;
    }

    private void updatePrefValue() {
        try {
            mIsAutoLowEnable = mPowerManagerEx.getAutoLowPower_Enable();
            mLowPowerValue = mPowerManagerEx.getAutoLowPower_BattValue();
            mLowPowerSwitchMode = mPowerManagerEx.getAutoLowPower_Mode();
        } catch (RemoteException e) {
            // Not much we can do here
        }
        mLowPowerSavePre.setChecked(mIsAutoLowEnable);

        mLowPowerValuePre.setValue(Integer.toString(mLowPowerValue));
        String label = Utils.formatPercentage(mLowPowerValue);
        mLowPowerValuePre.setSummary(label.replace("%","%%"));

        mModeSwitchPre.setValue(Integer.toString(mLowPowerSwitchMode));
        mModeSwitchPre.setSummary(mModeSwitchPre.getEntry());
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
        Log.d(TAG, "mplugged = " + mplugged + " mIsChargeExitLow="+mIsChargeExitLow );
        if (mIsChargeExitLow && mplugged) {
            mLowPowerSavePre.setEnabled(false);
            mLowPowerValuePre.setEnabled(false);
            mModeSwitchPre.setEnabled(false);
        } else {
            mLowPowerSavePre.setEnabled(true);
            mLowPowerValuePre.setEnabled(true);
            mModeSwitchPre.setEnabled(true);
        }
    }

    private void setVisible(Preference p, boolean visible) {
        final boolean isVisible = getPreferenceScreen().findPreference(p.getKey()) != null;
        if (isVisible == visible) return;
        if (visible) {
            getPreferenceScreen().addPreference(p);
        } else {
            getPreferenceScreen().removePreference(p);
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
