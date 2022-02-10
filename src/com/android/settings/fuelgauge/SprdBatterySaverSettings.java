package com.android.settings.fuelgauge;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.sprdpower.IPowerManagerEx;
import android.os.sprdpower.PowerManagerEx;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.sprd.settings.RadioButtonPreference;

/**
 * Fragment used for showing smart power saving mode, lowe power mode and super power saving mode.
 */
public class SprdBatterySaverSettings extends SettingsPreferenceFragment {

    private static final String TAG = "BatterySaverSettings";

    private static final String KEY_SMART_POWER_SAVING = "smart_power_saving_mode";
    private static final String KEY_SUPER_POWER_SAVING = "super_power_saving_mode";
    private static final String KEY_LOW_POWER_SAVING = "low_power_mode";
    private static final boolean UlTRA_SAVING_ENABLED = SystemProperties.getBoolean(
            "ro.sys.pwctl.ultrasaving", false);

    public static final int MODE_PERFORMANCE = 0;
    public static final int MODE_SMART = 1;
    public static final int MODE_POWERSAVING = 2;
    public static final int MODE_LOWPOWER = 3;
    public static final int MODE_ULTRASAVING = 4;

    private Context mContext;
    private IPowerManagerEx mPowerManagerEx;
    private String mCurrentSelectedKey;
    private String mLastedSelectedKey;
    private int mMode;
    private boolean mIsChargeExitLow;
    private PreferenceScreen mPreferenceScreen;
    private RadioButtonPreference mSuperPowerModePreference;
    private RadioButtonPreference mLowPowerModePreference;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_BATTERY_SAVER;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.sprd_battery_saver_settings);
        mContext = getActivity();
        mPreferenceScreen = getPreferenceScreen();

        mSuperPowerModePreference = (RadioButtonPreference) findPreference(
                KEY_SUPER_POWER_SAVING);
        if (mSuperPowerModePreference != null && !UlTRA_SAVING_ENABLED) {
            mPreferenceScreen.removePreference(mSuperPowerModePreference);
        }
        mLowPowerModePreference = (RadioButtonPreference) findPreference(
                KEY_LOW_POWER_SAVING);
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
        try {
            mMode = mPowerManagerEx.getPowerSaveMode();
        } catch (RemoteException e) {
            // Not much we can do here
        }
        mCurrentSelectedKey = powerModeToKey(mMode);
        Log.d(TAG, " mCurrent = " + mCurrentSelectedKey + " mLast = "
                + mLastedSelectedKey + " mMode = " + mMode);
        RadioButtonPreference pref = (RadioButtonPreference) findPreference(
                mCurrentSelectedKey);
        if (pref != null) {
            pref.setChecked(true);
            Log.d(TAG,"pref key = " + pref.getKey());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(mIntentReceiver);
    }

    private void refreshUI() {
        int count = mPreferenceScreen.getPreferenceCount();
        RadioButtonPreference pref;
        for (int i = 0; i < count; i++) {
            pref = (RadioButtonPreference) mPreferenceScreen.getPreference(i);
            if (pref != null) {
                pref.setChecked(false);
                Log.d(TAG, "pref key = " + pref.getKey());
            }
        }
    }

    private String powerModeToKey(int mode) {
        String key;
        switch (mode) {
            case MODE_SMART:
                key = KEY_SMART_POWER_SAVING;
                break;
            case MODE_ULTRASAVING:
                key = KEY_SUPER_POWER_SAVING;
                break;
            case MODE_LOWPOWER:
                key = KEY_LOW_POWER_SAVING;
                break;
             default:
                key = KEY_SMART_POWER_SAVING;
        }
        return key;
    }

    private void updateSelectedState(String key) {
        mLastedSelectedKey = mCurrentSelectedKey;
        RadioButtonPreference pref = (RadioButtonPreference) findPreference(
                mLastedSelectedKey);
        if (pref != null) {
            pref.setChecked(false);
            Log.d(TAG,"updateSelectedState last pref key = " + pref.getKey());
        }
        mCurrentSelectedKey = key;
        pref = (RadioButtonPreference) findPreference(mCurrentSelectedKey);
        if (pref != null) {
            pref.setChecked(true);
            Log.d(TAG,"updateSelectedState current pref key = " + pref.getKey());
        }
    }

    private void setPowerSavingMode(String key) {
        int mode;
        switch (key) {
            case KEY_SMART_POWER_SAVING:
                mode = MODE_SMART;
                break;
            case KEY_SUPER_POWER_SAVING:
                mode = MODE_ULTRASAVING;
                break;
            case KEY_LOW_POWER_SAVING:
                mode = MODE_LOWPOWER;
                break;
            default:
                mode = MODE_POWERSAVING;
        }
        Log.d(TAG, "setPowerSavingMode: " + key + " mode:" + mode);
        try {
            mPowerManagerEx.setPowerSaveMode(mode);
        } catch (RemoteException e) {
            // Not much we can do here
        }
    }

     @Override
    public boolean onPreferenceTreeClick(Preference pref) {
        final String key = pref.getKey();
        //customized mode not support currently.
        //if (KEY_CUSTOMIZED_MODE.equals(key)) return true;
        if (pref instanceof RadioButtonPreference) {
            final RadioButtonPreference radioPref = (RadioButtonPreference) pref;
            Log.d(TAG ,"onPreferenceTreeClick pref = " + pref + "checked:"+ radioPref.isChecked());
            if (radioPref.isChecked()) {
                return true;
            }
            /*bug 911188 : Select super power saver or smart power saver and press back, choose wrong mode@{*/
            setPowerSavingMode(key);
            /*@}*/
            return true;
        }
        return false;
     }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                //low power mode should be disable when not charge
                int plugged = intent.getIntExtra("plugged", 0);
                try {
                    mIsChargeExitLow = mPowerManagerEx.getSmartSavingModeWhenCharging();
                } catch (RemoteException e) {
                    // Not much we can do here
                }
                Log.d(TAG,"plugged = " + plugged + " mIsChargeExitLow= " + mIsChargeExitLow);
                if (mIsChargeExitLow && plugged != 0) {
                    mLowPowerModePreference.setEnabled(false);
                    mSuperPowerModePreference.setEnabled(false);
                } else {
                    mLowPowerModePreference.setEnabled(true);
                    mSuperPowerModePreference.setEnabled(true);
                }
            } else if (intent.getAction().equals(
                PowerManagerEx.ACTION_POWEREX_SAVE_MODE_CHANGED)) {
                mMode = intent.getIntExtra(PowerManagerEx.EXTRA_POWEREX_SAVE_MODE,
                        MODE_POWERSAVING);
                Log.d(TAG, " mMode = " + mMode);
                String key = powerModeToKey(mMode);
                RadioButtonPreference pref = (RadioButtonPreference) findPreference(key);
                if (!pref.isChecked()) {
                    updateSelectedState(key);
                }
            }
        }
    };
}
