package com.android.settings.display;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

/**
 * A controller to manage the switch for backlight saving power.
 */

public class BacklightSavingPowerPreferenceController extends BasePreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    private final boolean isSupportBacklightSavingPower = (1 == SystemProperties.getInt("persist.sys.pq.cabc.enabled", 0));
    private final String BACKLIGHT_SAVING_POWER = "backlight_saving_power";
    public SwitchPreference mPreference;

    public BacklightSavingPowerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(
                R.bool.config_backlight_saving_power_setting_available)
                && isSupportBacklightSavingPower? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        int state = Settings.Global.getInt(mContext.getContentResolver(),
                BACKLIGHT_SAVING_POWER, 0);
        if (preference != null && preference instanceof SwitchPreference) {
            mPreference = (SwitchPreference) preference;
            mPreference.setChecked(state == 1);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean setting = (Boolean) newValue;
        Settings.Global.putInt(mContext.getContentResolver(), BACKLIGHT_SAVING_POWER,
                setting ? 1 : 0);
        return true;
    }
}
