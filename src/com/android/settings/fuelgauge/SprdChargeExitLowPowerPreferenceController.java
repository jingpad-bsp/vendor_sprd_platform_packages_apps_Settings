package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.RemoteException;
import android.os.sprdpower.IPowerManagerEx;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import android.util.Log;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * Controller to exiting power saving mode when charging
 */
public class SprdChargeExitLowPowerPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_CHARGE_EXIT_LOW_POWER = "charge_exit_low_power";
    private static final String TAG ="SprdChargeExitLowPowerPreferenceController";
    private final boolean isSupportBMFeature = (1 == SystemProperties.getInt("persist.sys.pwctl.enable", 1));
    private SwitchPreference mPreference;
    private boolean mIsChargeExitLow;
    private IPowerManagerEx mPowerManagerEx;

    public SprdChargeExitLowPowerPreferenceController(Context context) {
        super(context);
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));
    }

    @Override
    public boolean isAvailable() {
        if (isSupportBMFeature && UserHandle.myUserId() == UserHandle.USER_OWNER) {
            return true;
        }
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CHARGE_EXIT_LOW_POWER;
    }

    @Override
    public void updateState(Preference preference) {
        try {
            mIsChargeExitLow = mPowerManagerEx.getSmartSavingModeWhenCharging();
            Log.d(TAG, "mIsChargeExitLow= " + mIsChargeExitLow);
        } catch (RemoteException e) {
            // Not much we can do here
        }
        if (preference instanceof SwitchPreference) {
            mPreference = (SwitchPreference) preference;
            mPreference.setChecked(mIsChargeExitLow);
        }
    }

    public boolean isEnabled() {
        if (null != mPreference) {
            return mPreference.isEnabled();
        }
        return false;
    }

    public boolean isChecked() {
        if (null != mPreference) {
            return mPreference.isChecked();
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean config = (Boolean) newValue;
        boolean result = false;
        try {
            result = mPowerManagerEx.setSmartSavingModeWhenCharging(config);
        } catch (RemoteException e){
            // Not much we can do here
        }
        return result;
    }
}
