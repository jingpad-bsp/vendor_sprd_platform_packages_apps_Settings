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

import static android.provider.Settings.Global.SMART_CALL_RECORDER;
import static android.provider.Settings.Global.SMART_CALL_RECORDER_SWITCH;

/**
 * SmartCallRecorder:Shake the device the first time while in call to start recording.
 */
public class SmartCallRecorderPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_SMART_CALL_RECORDER = "smart_call_recorder";
    private static final String DIALOG_SMART_CALL_RECORDER_TAG = "smart_call_recorder_dialog";
    private final Fragment mHost;
    private SmartSwitchPreference mPreference;

    public SmartCallRecorderPreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
    }

    @Override
    public boolean isAvailable() {
        return isSmartCallRecorderAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SMART_CALL_RECORDER;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        if (isAvailable()) {
            mPreference = (SmartSwitchPreference) screen.findPreference(KEY_SMART_CALL_RECORDER);

            mPreference.setOnViewClickedListener(new OnViewClickedListener() {
                @Override
                public void OnViewClicked(View v) {
                    showSmartCallRecorderAnimation();
                }
            });

            mPreference.setOnPreferenceSwitchCheckedListener(new OnPreferenceSwitchChangeListener() {
                @Override
                public void onPreferenceSwitchChanged(boolean checked) {
                    if (SmartMotionFragment.isSmartMotionEnabled(mContext)) {
                        Settings.Global.putInt(mContext.getContentResolver(), SMART_CALL_RECORDER, checked ? 1 : 0);
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
                SMART_CALL_RECORDER, 0);
        } else {
            setting= Settings.Global.getInt(mContext.getContentResolver(),
                SMART_CALL_RECORDER_SWITCH, 0);
        }

        if (preference != null && preference instanceof SmartSwitchPreference) {
            ((SmartSwitchPreference) preference).setChecked(setting == 1);
        }
    }

    public static boolean isSmartCallRecorderAvailable(Context context) {
        // Modify for bug1170654, Change the sensor type of smartCallRecorder
        return context.getResources().getBoolean(R.bool.config_support_smartCallRecorder)
                && Utils.isSupportSensor(context, SprdSensor.TYPE_SPRDHUB_SHAKE);
    }

    public void updateOnSmartMotionChange(boolean isEnable) {
        if (!isEnable) {
            if (mPreference != null && mPreference.isChecked()) {
                Settings.Global.putInt(mContext.getContentResolver(), SMART_CALL_RECORDER_SWITCH, 1);
            }
            Settings.Global.putInt(mContext.getContentResolver(), SMART_CALL_RECORDER, 0);
        } else {
            if (Settings.Global.getInt(mContext.getContentResolver(), SMART_CALL_RECORDER_SWITCH, 0) == 1) {
                Settings.Global.putInt(mContext.getContentResolver(), SMART_CALL_RECORDER, 1);
                Settings.Global.putInt(mContext.getContentResolver(), SMART_CALL_RECORDER_SWITCH, 0);
            }
        }
    }

    private void showSmartCallRecorderAnimation() {
        final FragmentTransaction ft = mHost.getFragmentManager().beginTransaction();
        mHost.getFragmentManager().executePendingTransactions();
        final Fragment prev = mHost.getFragmentManager().findFragmentByTag(DIALOG_SMART_CALL_RECORDER_TAG);
        if (prev != null) {
            if (prev.isAdded()) {
                return;
            }
            ft.remove(prev);
        }
        // ft.addToBackStack(null);
        final SmartCallRecorderAnimation newFragment = SmartCallRecorderAnimation.newInstance(mPreference);
        if (newFragment != null && mHost.getActivity().isResumed() && !newFragment.isAdded()) {
            newFragment.show(ft, DIALOG_SMART_CALL_RECORDER_TAG);
        }
    }
}
