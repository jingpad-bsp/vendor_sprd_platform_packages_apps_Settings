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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.provider.Settings;
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

import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;

import static android.provider.Settings.Secure.TP_WAKE_GESTURE_ENABLED;

/**
 * SmartWake:Pick up the device and double-click anywhere on the screen to wake it.
 */
public class SmartWakePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_SMART_WAKE = "smart_wake";
    private static final String DIALOG_SMART_WAKE_TAG = "smart_wake_dialog";
    private SmartSwitchPreference mSmartWakePreference;

    private final Fragment mHost;

    public SmartWakePreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
    }

    @Override
    public boolean isAvailable() {
        if (isSmartWakeAvailable(mContext)
                && isWakeGestureAvailable(mContext)) {
            return true;
        }
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SMART_WAKE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mSmartWakePreference = (SmartSwitchPreference) screen.findPreference(KEY_SMART_WAKE);
    
            mSmartWakePreference.setOnViewClickedListener(new OnViewClickedListener() {
                @Override
                public void OnViewClicked(View v) {
                    showSmartWakeAnimation();
                }
            });

            mSmartWakePreference.setOnPreferenceSwitchCheckedListener(new OnPreferenceSwitchChangeListener() {
                @Override
                public void onPreferenceSwitchChanged(boolean checked) {
                    Settings.Secure.putInt(mContext.getContentResolver(), TP_WAKE_GESTURE_ENABLED, checked ? 1 : 0);
                }
            });
        }
    }

    @Override
    public void updateState(Preference preference) {
        int setting = Settings.Secure.getInt(mContext.getContentResolver(),
        TP_WAKE_GESTURE_ENABLED, 0);

        mSmartWakePreference.setChecked(setting == 1);
    }

    public static boolean isSmartWakeAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_support_smartWake);
    }

    public static boolean isWakeGestureAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_WAKE_GESTURE) != null;
    }

    private void showSmartWakeAnimation() {
        final FragmentTransaction ft = mHost.getFragmentManager().beginTransaction();
        mHost.getFragmentManager().executePendingTransactions();
        final Fragment prev = mHost.getFragmentManager().findFragmentByTag(DIALOG_SMART_WAKE_TAG);
        if (prev != null) {
            if (prev.isAdded()) {
                return;
            }
            ft.remove(prev);
        }
        //ft.addToBackStack(null);
        final SmartWakeAnimation newFragment = SmartWakeAnimation.newInstance(mSmartWakePreference);
        if (newFragment != null && mHost.getActivity().isResumed() && !newFragment.isAdded()) {
            newFragment.show(ft, DIALOG_SMART_WAKE_TAG);
        }
    }
}
