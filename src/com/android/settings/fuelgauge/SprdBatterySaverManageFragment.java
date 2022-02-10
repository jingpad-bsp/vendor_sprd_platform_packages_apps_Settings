package com.android.settings.fuelgauge;

import android.content.BroadcastReceiver;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.sprdpower.IPowerManagerEx;
import android.util.Log;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;
import com.sprd.settings.superresolution.SprdSuperResolutionPreferenceController;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for showing battery saver, low power saver, schedule power saving and the switch of exiting power saving mode when charging
 */
@SearchIndexable
public class SprdBatterySaverManageFragment extends DashboardFragment {

    public static final String TAG = "SprdBatterySaverManageFragment";
    public static final String ARGUMENT = "argument";

    private static final String KEY_BATTERY_SAVER_OPTIMIZATION = "battery_saver_optimization";
    private static final String KEY_BATTERY_SAVER_MODE = "battery_saver_mode";
    private static final String KEY_AUTO_LOW_POWER = "auto_low_power";
    private static final String KEY_SCHEDULE_MODE = "schedule_mode";
    private static final String KEY_CHARGE_EXIT_LOW_POWER = "charge_exit_low_power";
    private static final String KEY_SUPER_RESOLUTION = "super_resolution";
    private final boolean isSupportSprdPowerManager = (1 == SystemProperties.getInt("persist.sys.pwctl.enable", 1));
    private boolean isSupportSuperResolution;

    private Preference mBatterySaveModePref;
    private Preference mAutoLowPowerPref;
    private Preference mScheduleModePref;
    private Preference mSuperResolutionPref;

    private SprdChargeExitLowPowerPreferenceController mSprdChargeExitLowPowerPreferenceController;
    private SprdSuperResolutionPreferenceController mSprdSuperResolutionPreferenceController;

    private IPowerManagerEx mPowerManagerEx;
    private Context mContext;

    private boolean mIsChargeExitLow;
    private boolean mplugged;

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBatterySaveModePref = (Preference) findPreference (KEY_BATTERY_SAVER_MODE);
        mAutoLowPowerPref = (Preference) findPreference (KEY_AUTO_LOW_POWER);
        mScheduleModePref = (Preference) findPreference (KEY_SCHEDULE_MODE);
        mSuperResolutionPref = (Preference) findPreference (KEY_SUPER_RESOLUTION);
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));
        mContext = getActivity();
        if (!isSupportSprdPowerManager) {
            removePreference(KEY_BATTERY_SAVER_MODE);
            removePreference(KEY_AUTO_LOW_POWER);
            removePreference(KEY_SCHEDULE_MODE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePrefState(null);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Log.d(TAG,"onWindowFocusChanged isSupportSuperResolution :" + isSupportSuperResolution + "    hasWindowFocus : " + hasWindowFocus);
        if (!isSupportSuperResolution || !hasWindowFocus) return;
        if (mSprdSuperResolutionPreferenceController != null
                && mSprdSuperResolutionPreferenceController.isSuperResolutionStateOn()) {
            Log.d(TAG,"onWindowFocusChanged  setSuperResolutionStateOff");
            mSprdSuperResolutionPreferenceController.setSuperResolutionStateOff();
        }
    }

    /* UNISOC : 1073159 Add for super resolution.@ {*/
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.d(TAG, "onMultiWindowModeChanged isInMultiWindowMode = " + isInMultiWindowMode);
        if (isSupportSuperResolution && mSuperResolutionPref != null) {
            mSuperResolutionPref.setEnabled(!isInMultiWindowMode);
        }
    }
    /* @} */

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (KEY_CHARGE_EXIT_LOW_POWER.equals(preference.getKey())) {
            updatePrefState(null);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
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
        Log.d(TAG,"mplugged = " + mplugged + " mIsChargeExitLow = " + mIsChargeExitLow);
        if (mIsChargeExitLow && mplugged) {
            mBatterySaveModePref.setEnabled(false);
            mAutoLowPowerPref.setEnabled(false);
            mScheduleModePref.setEnabled(false);
        } else {
            mBatterySaveModePref.setEnabled(true);
            mAutoLowPowerPref.setEnabled(true);
            mScheduleModePref.setEnabled(true);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.sprd_battery_saver_fragment;
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(mIntentReceiver);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        mSprdChargeExitLowPowerPreferenceController = new SprdChargeExitLowPowerPreferenceController(context);
        mSprdSuperResolutionPreferenceController = new SprdSuperResolutionPreferenceController(context, KEY_SUPER_RESOLUTION);
        isSupportSuperResolution = mSprdSuperResolutionPreferenceController.isAvailable();
        controllers.add(mSprdChargeExitLowPowerPreferenceController);
        return controllers;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                updatePrefState(intent);
            }
        }
    };

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.sprd_battery_saver_fragment;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> niks = super.getNonIndexableKeys(context);
                    if (1 != SystemProperties.getInt("persist.sys.pwctl.enable", 1) ||
                            UserHandle.myUserId() != UserHandle.USER_SYSTEM) {
                        niks.add(KEY_BATTERY_SAVER_MODE);
                        niks.add(KEY_AUTO_LOW_POWER);
                        niks.add(KEY_SCHEDULE_MODE);
                        niks.add(KEY_CHARGE_EXIT_LOW_POWER);
                        niks.add(KEY_BATTERY_SAVER_OPTIMIZATION);
                    }
                    return niks;
                }
        };
}
