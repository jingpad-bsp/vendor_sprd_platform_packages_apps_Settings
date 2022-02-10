/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.sprd.settings.smartcontrols;

import android.content.Context;
import android.provider.Settings;
import android.hardware.SprdSensor;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.R;

import static android.provider.Settings.Global.TOUCH_DISABLE;
import static android.provider.Settings.Global.TOUCH_DISABLE_SWITCH;

/**
 * TouchDisable:Prevent misuse when the device in the pocket.
 */
public class TouchDisablePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_TOUCH_DISABLE = "touch_disable";
    private final Fragment mHost;
    private SwitchPreference mPreference;

    public TouchDisablePreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
    }

    @Override
    public boolean isAvailable() {
        return isTouchDisableAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_TOUCH_DISABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = (SwitchPreference) screen.findPreference(KEY_TOUCH_DISABLE);
    }

    @Override
    public void updateState(Preference preference) {
        int setting = 0;
        if (PocketModeFragment.isPocketModeEnabled(mContext)) {
            setting= Settings.Global.getInt(mContext.getContentResolver(),
                TOUCH_DISABLE, 0);
        } else {
            setting= Settings.Global.getInt(mContext.getContentResolver(),
                TOUCH_DISABLE_SWITCH, 0);
        }

        if (preference != null && preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(setting == 1);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean checked = (Boolean) newValue;
        Settings.Global.putInt(mContext.getContentResolver(), TOUCH_DISABLE, checked ? 1 : 0);
        if (preference != null && preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(checked);
        }

        return false;
    }

    public static boolean isTouchDisableAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_support_touchDisable)
                && Utils.isSupportSensor(context, SprdSensor.TYPE_SPRDHUB_POCKET_MODE);
    }

    public void updateOnPocketModeChange(boolean isEnable) {
        if (!isEnable) {
            if (mPreference != null && mPreference.isChecked()) {
                Settings.Global.putInt(mContext.getContentResolver(), TOUCH_DISABLE_SWITCH, 1);
            }
            Settings.Global.putInt(mContext.getContentResolver(), TOUCH_DISABLE, 0);
        } else {
            if (Settings.Global.getInt(mContext.getContentResolver(), TOUCH_DISABLE_SWITCH, 0) == 1) {
                Settings.Global.putInt(mContext.getContentResolver(), TOUCH_DISABLE, 1);
                Settings.Global.putInt(mContext.getContentResolver(), TOUCH_DISABLE_SWITCH, 0);
            }
        }
    }
}
