package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.sprdpower.AppPowerSaveConfig;
import android.os.Bundle;
import android.os.sprdpower.IPowerManagerEx;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

/**
 * Fragment used for showing app standy optimization, including app standby wakeup, app standby sleep, app standby network.
 */
public class SprdAppItemBatterySaverFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "SprdAppItemBatterySaver";
    public static final String ARGUMENT = "argument";
    public static final int VALUE_INVALID = -1;
    public static final int VALUE_AUTO = 0;
    public static final int VALUE_OPTIMIZE = 1;
    public static final int VALUE_NO_OPTIMIZE = 2;
    private int mArgument;

    private static final String KEY_APP_OPTIMIZED_PREFERENCE = "app_optimized";
    private static final String KEY_STANDY_WAKEUP_PREFERENCE = "app_standby_wakeup";
    private static final String KEY_STANDY_SLEEP_PREFERENCE = "app_standby_sleep";
    private static final String KEY_STANDY_NETWORK_DATA_PREFERENCE = "app_standby_network";
    private SwitchPreference mAppOptimizedPref;
    private ListPreference mWakeupPreference;
    private ListPreference mSleepPreference;
    private ListPreference mNetworkDataPreference;
    private IPowerManagerEx mPowerManagerEx;
    private AppPowerSaveConfig mAppConfig;
    private String mPkgName;

    public static final String APPLICATION_LIST_TYPE = "app_list_type";
    private int mApplicationListType;
    public static final int TAB_INDICATOR_APP_BATTERY_SAVER = 1;
    public static final int LIST_TYPE_APP_BATTERY_SAVER = 2;
    public static final int TAB_INDICATOR_APP_AUTO_RUN = 3;
    public static final int LIST_TYPE_APP_AUTO_RUN = 4;
    public static final String TYPE_APP_CONFIG = "type_app_config";
    public static final int TYPE_APP_WAKEUP = 1000;
    public static final int TYPE_APP_SLEEP = 1001;
    public static final int TYPE_APP_NETWOK_DATA = 1002;
    public static final int TYPE_NULL = -1;
    public static final int TYPE_OPTIMIZE = 0;
    public static final int TYPE_ALARM = 1;
    public static final int TYPE_WAKELOCK = 2;
    public static final int TYPE_NETWORK = 3;
    public static final int TYPE_AUTOLAUNCH = 4;
    public static final int TYPE_SECONDARYLAUNCH = 5;
    public static final int TYPE_LOCKSCREENCLEANUP = 6;
    public static final int TYPE_MAX = 7;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) mPkgName = bundle.getString("package");
        addPreferencesFromResource(R.xml.sprd_app_item_batterysaver_fragment);
        boolean optimizedEnable = true;
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));
        try {
            mAppConfig = mPowerManagerEx.getAppPowerSaveConfig(mPkgName);
            optimizedEnable = mPowerManagerEx.getAppPowerSaveConfigWithType(
                    mPkgName, TYPE_OPTIMIZE) == 1;
        } catch (RemoteException e) {
            // Not much we can do here
        }
        Log.d(TAG," mPkgName = " + mPkgName + " mAppConfig = " + mAppConfig +
                " optimizedEnable = " + optimizedEnable);

        mAppOptimizedPref = (SwitchPreference) findPreference(KEY_APP_OPTIMIZED_PREFERENCE);
        mAppOptimizedPref.setOnPreferenceChangeListener(this);
        mAppOptimizedPref.setChecked(optimizedEnable);

        mWakeupPreference = (ListPreference) findPreference(KEY_STANDY_WAKEUP_PREFERENCE);
        mWakeupPreference.setOnPreferenceChangeListener(this);

        mSleepPreference = (ListPreference) findPreference(KEY_STANDY_SLEEP_PREFERENCE);
        mSleepPreference.setOnPreferenceChangeListener(this);

        mNetworkDataPreference = (ListPreference) findPreference(KEY_STANDY_NETWORK_DATA_PREFERENCE);
        mNetworkDataPreference.setOnPreferenceChangeListener(this);

        if (mAppConfig != null) {
            mWakeupPreference.setValueIndex(mAppConfig.alarm);
            mWakeupPreference.setSummary(mWakeupPreference.getEntry());
            mSleepPreference.setValueIndex(mAppConfig.wakelock);
            mSleepPreference.setSummary(mSleepPreference.getEntry());
            mNetworkDataPreference.setValueIndex(mAppConfig.network);
            mNetworkDataPreference.setSummary(mNetworkDataPreference.getEntry());
        } else {
            mWakeupPreference.setValueIndex(VALUE_AUTO);
            mWakeupPreference.setSummary(mWakeupPreference.getEntry());
            mSleepPreference.setValueIndex(VALUE_AUTO);
            mSleepPreference.setSummary(mSleepPreference.getEntry());
            mNetworkDataPreference.setValueIndex(VALUE_AUTO);
            mNetworkDataPreference.setSummary(mNetworkDataPreference.getEntry());
        }
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_APP_OPTIMIZED_PREFERENCE.equals(preference.getKey())) {
            boolean enabled = (Boolean) newValue;
            Log.d(TAG,"onPreferenceChange value:" + enabled);
            try {
                mPowerManagerEx.setAppPowerSaveConfigWithType(mPkgName, TYPE_OPTIMIZE,
                        enabled ? 1 : 0);
            } catch (RemoteException e) {
                // Not much we can do here
            }
            return true;
        }
        int type = TYPE_NULL;
        int value = Integer.parseInt((String) newValue);
        if (KEY_STANDY_WAKEUP_PREFERENCE.equals(preference.getKey())) {
            Log.d(TAG,"onPreferenceChange newValue:" + value+ " enty:" +mWakeupPreference.getEntry());
            mWakeupPreference.setSummary(mWakeupPreference.getEntries()[value]);
            type = TYPE_ALARM;
        } else if (KEY_STANDY_SLEEP_PREFERENCE.equals(preference.getKey())) {
            Log.d(TAG,"onPreferenceChange newValue:" +value + " enty:" +mSleepPreference.getEntry());
            mSleepPreference.setSummary(mSleepPreference.getEntries()[value]);
            type = TYPE_WAKELOCK;
        } else if (KEY_STANDY_NETWORK_DATA_PREFERENCE.equals(preference.getKey())) {
            Log.d(TAG,"onPreferenceChange newValue:" + value+ " enty:" + mNetworkDataPreference.getEntry());
            mNetworkDataPreference.setSummary(mNetworkDataPreference.getEntries()[value]);
            type = TYPE_NETWORK;
        }
        if (type != TYPE_NULL) {
            try {
                boolean resp = mPowerManagerEx.setAppPowerSaveConfigWithType(mPkgName, type, value);
                int getType = mPowerManagerEx.getAppPowerSaveConfigWithType(mPkgName, type);
                Log.d(TAG,"type = " + type + " value = " + value + " getType = " + getType);
            } catch (RemoteException e) {
                // Not much we can do here
            }
        }
        return true;
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
}
