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
 * Controller to manage application standby optimization
 */
public class SprdAppStandbyOptimizerPreferenceController extends BasePreferenceController {

    private static final String TAG = "SprdAppStandbyOptimizerPreferenceController";
    private static final String TYPE_APP_CONFIG = "type_app_config";
    private static final String KEY_APP_BATTERY_SAVER = "app_battery_saver";
    private final boolean isSupportSprdPowerManager = (1 == SystemProperties.getInt("persist.sys.pwctl.enable", 1));
    public static final int TYPE_APP_WAKEUP = 1000;
    private final int mUserId;

    public SprdAppStandbyOptimizerPreferenceController(Context context, String key) {
        super(context, key);
        mUserId = UserHandle.myUserId();
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_APP_BATTERY_SAVER.equals(preference.getKey())) {
            Bundle args = new Bundle();
            args.putInt(TYPE_APP_CONFIG, TYPE_APP_WAKEUP);
            new SubSettingLauncher(mContext)
                    .setDestination(SprdManageApplications.class.getName())
                    .setArguments(args)
                    .setTitleRes(R.string.app_battery_saver_manager)
                    .setSourceMetricsCategory(getMetricsCategory())
                    .launch();
            return true;
        }
        return false;
    }

    @Override
    public int getAvailabilityStatus() {
        return isSupportSprdPowerManager
                && isAppStandbyOptimizerAvailable(mContext)
                && isUserSupported()
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private static boolean isAppStandbyOptimizerAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_support_appStandbyOptimizer);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_APP_BATTERY_SAVER;
    }

    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }

    private boolean isUserSupported() {
        return mUserId == UserHandle.USER_OWNER ? true : false;
    }
}
