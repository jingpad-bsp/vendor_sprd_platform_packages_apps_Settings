/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.SystemProperties;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.SettingsActivity;
import com.android.settings.core.BasePreferenceController;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.List;
import android.util.Log;

/**
 * Controller to show power intensive apps
 */
public class SprdPowerIntensiveAppsPreferenceController extends BasePreferenceController {

    private static final String TAG = "SprdPowerIntensiveAppsPreferenceController";
    private static final String TYPE_APP_CONFIG = "type_app_config";
    private static final String KEY_POWER_INTENSIVE_APPS = "power_intensive_apps";
    private final boolean isSupportSprdPowerManager = (1 == SystemProperties.getInt("persist.sys.pwctl.enable", 1));
    private static final int TYPE_APP_POWER_INTENSIVE = 1006;
    private final int mUserId;

    public SprdPowerIntensiveAppsPreferenceController(Context context, String key) {
        super(context, key);
        mUserId = UserHandle.myUserId();
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_POWER_INTENSIVE_APPS.equals(preference.getKey())) {
            Bundle args = new Bundle();
            args.putInt(TYPE_APP_CONFIG, TYPE_APP_POWER_INTENSIVE);
            new SubSettingLauncher(mContext)
                    .setDestination(SprdManageApplications.class.getName())
                    .setSourceMetricsCategory(getMetricsCategory())
                    .setArguments(args)
                    .setTitleRes(R.string.power_intensive_apps)
                    .launch();
            return true;
        }
        return false;
    }

    @Override
    public int getAvailabilityStatus() {
        return isSupportSprdPowerManager
                && isPowerIntensiveAppsrAvailable(mContext)
                && isUserSupported()
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private static boolean isPowerIntensiveAppsrAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_support_powerIntensiveApps);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_POWER_INTENSIVE_APPS;
    }

    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }

    private boolean isUserSupported() {
        return mUserId == UserHandle.USER_OWNER ? true : false;
    }
}
