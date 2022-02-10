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
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.R;
import com.android.settings.widget.SmartSwitchPreference;
import com.android.settings.widget.SmartSwitchPreference.OnPreferenceSwitchChangeListener;
import com.android.settings.widget.SmartSwitchPreference.OnViewClickedListener;

import static android.provider.Settings.Global.SHAKE_TO_SWITCH;
import static android.provider.Settings.Global.SHAKE_TO_SWITCH_SWITCH;

/**
 * ShakeToSwitch:Shake the device to switch next or previous wallpaper when not lock screen.
 * Discarded here and implemented by launcher3.
 */
public class ShakeToSwitchPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_SHAKE_TO_SWITCH = "shake_to_switch";
    private static final String DIALOG_SHAKE_TO_SWITCH_TAG = "shake_to_switch_dialog";
    private final Fragment mHost;
    private SmartSwitchPreference mPreference;

    public ShakeToSwitchPreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
    }

    @Override
    public boolean isAvailable() {
        return isShakeToSwitchAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SHAKE_TO_SWITCH;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        if (isAvailable()) {
            mPreference = (SmartSwitchPreference) screen.findPreference(KEY_SHAKE_TO_SWITCH);

            mPreference.setOnViewClickedListener(new OnViewClickedListener() {
                @Override
                public void OnViewClicked(View v) {
                    showShakeToSwitchAnimation();
                }
            });

            mPreference.setOnPreferenceSwitchCheckedListener(new OnPreferenceSwitchChangeListener() {
                @Override
                public void onPreferenceSwitchChanged(boolean checked) {
                    if (SmartMotionFragment.isSmartMotionEnabled(mContext)) {
                        Settings.Global.putInt(mContext.getContentResolver(), SHAKE_TO_SWITCH, checked ? 1 : 0);
                    }
                }
            });
        }
    }

    @Override
    public void updateState(Preference preference) {
        int setting = 0;
        if (SmartMotionFragment.isSmartMotionEnabled(mContext)) {
            setting= Settings.Global.getInt(mContext.getContentResolver(),
                SHAKE_TO_SWITCH, 0);
        } else {
            setting= Settings.Global.getInt(mContext.getContentResolver(),
                SHAKE_TO_SWITCH_SWITCH, 0);
        }

        if (preference != null && preference instanceof SmartSwitchPreference) {
            ((SmartSwitchPreference) preference).setChecked(setting == 1);
        }
    }

    public static boolean isShakeToSwitchAvailable(Context context) {
        //this feature need removed and add the feature on the Launcher3
        return false;
        //return context.getResources().getBoolean(R.bool.config_support_shakeToSwitch)
                //&& Utils.isSupportSensor(context, SprdSensor.TYPE_SPRDHUB_SHAKE);
    }

    public void updateOnSmartMotionChange(boolean isEnable) {
        if (!isEnable) {
            if (mPreference != null && mPreference.isChecked()) {
                Settings.Global.putInt(mContext.getContentResolver(), SHAKE_TO_SWITCH_SWITCH, 1);
            }
            Settings.Global.putInt(mContext.getContentResolver(), SHAKE_TO_SWITCH, 0);
        } else {
            if (Settings.Global.getInt(mContext.getContentResolver(), SHAKE_TO_SWITCH_SWITCH, 0) == 1) {
                Settings.Global.putInt(mContext.getContentResolver(), SHAKE_TO_SWITCH, 1);
                Settings.Global.putInt(mContext.getContentResolver(), SHAKE_TO_SWITCH_SWITCH, 0);
            }
        }
    }

    private void showShakeToSwitchAnimation() {
        final FragmentTransaction ft = mHost.getFragmentManager().beginTransaction();
        mHost.getFragmentManager().executePendingTransactions();
        final Fragment prev = mHost.getFragmentManager().findFragmentByTag(DIALOG_SHAKE_TO_SWITCH_TAG);
        if (prev != null) {
            if (prev.isAdded()) {
                return;
            }
            ft.remove(prev);
        }
        // ft.addToBackStack(null);
        final ShakeToSwitchAnimation newFragment = ShakeToSwitchAnimation.newInstance(mPreference);
        if (newFragment != null && mHost.getActivity().isResumed() && !newFragment.isAdded()) {
            newFragment.show(ft, DIALOG_SHAKE_TO_SWITCH_TAG);
        }
    }
}
