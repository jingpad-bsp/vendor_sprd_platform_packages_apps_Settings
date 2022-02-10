package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.BasePreferenceController;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.List;

/**
 * Controller to manage application start-up optimization
 */
public class SprdAppAutoRunManagementPreferenceController extends BasePreferenceController {

    private static final String TAG = "SprdAppAutoRunManagementPreferenceController";
    private static final String KEY_APP_AUTO_RUN = "app_auto_run";
    private final boolean isSupportSprdPowerManager = (1 == SystemProperties.getInt("persist.sys.pwctl.enable", 1));
    private final int mUserId;

    public SprdAppAutoRunManagementPreferenceController(Context context, String key) {
        super(context, key);
        mUserId = UserHandle.myUserId();
    }

    @Override
    public int getAvailabilityStatus() {
        return isSupportSprdPowerManager
                && isAppAutoRunManagementAvailable(mContext)
                && isUserSupported()
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private static boolean isAppAutoRunManagementAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_support_appAutoRunManagement);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_APP_AUTO_RUN;
    }

    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }

    private boolean isUserSupported() {
        return mUserId == UserHandle.USER_OWNER ? true : false;
    }
}
