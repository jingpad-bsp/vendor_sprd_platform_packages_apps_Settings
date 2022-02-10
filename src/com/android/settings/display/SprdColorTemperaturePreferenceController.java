package com.android.settings.display;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * Controller that show color temperature adjusting
 */
public class SprdColorTemperaturePreferenceController
        extends AbstractPreferenceController implements PreferenceControllerMixin {

    private static final String TAG = "SprdColorTemperaturePreferenceController";
    private static final String KEY_COLORS_CONTRAST = "colors_contrast";
    // used for control if color temperature feature is supported
    private final boolean mIsSupportPQ = SystemProperties.getBoolean("ro.sprd.displayenhance", false);

    public SprdColorTemperaturePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        if (mIsSupportPQ
            && mContext.getResources().getBoolean(R.bool.config_support_colorTemperatureAdjusting)) {
            Log.i(TAG, "ColorTemperatureAdjusting is support");
            return true;
        }
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_COLORS_CONTRAST;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
    }

    @Override
    public void updateState(Preference preference) {
        final String clazz = SprdColorTemperatureActivity.class.getName();
        preference.setOnPreferenceClickListener(target -> {
            final Context context = target.getContext();
            final UserManager userManager = UserManager.get(context);
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", clazz);
            context.startActivity(intent);
            return true;
        });
    }
}
