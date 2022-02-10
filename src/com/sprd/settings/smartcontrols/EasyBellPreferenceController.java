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
import android.hardware.Sensor;
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

import static android.provider.Settings.Global.EASY_BELL;
import static android.provider.Settings.Global.EASY_BELL_SWITCH;

/**
 * EasyBell:Pick up the device when it is ringing, ring will weaken and device will vibrate.
 */
public class EasyBellPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_EASY_BELL = "easy_bell";
    private static final String DIALOG_EASY_BELL_TAG = "easy_bell_dialog";
    private final Fragment mHost;
    private SmartSwitchPreference mPreference;

    public EasyBellPreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
    }

    @Override
    public boolean isAvailable() {
        return isEasyBellAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_EASY_BELL;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        if (isAvailable()) {
            mPreference = (SmartSwitchPreference) screen.findPreference(KEY_EASY_BELL);

            mPreference.setOnViewClickedListener(new OnViewClickedListener() {
                @Override
                public void OnViewClicked(View v) {
                    showEasyBellAnimation();
                }
            });

            mPreference.setOnPreferenceSwitchCheckedListener(new OnPreferenceSwitchChangeListener() {
                @Override
                public void onPreferenceSwitchChanged(boolean checked) {
                    if (SmartMotionFragment.isSmartMotionEnabled(mContext)) {
                        Settings.Global.putInt(mContext.getContentResolver(), EASY_BELL, checked ? 1 : 0);
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
                EASY_BELL, 0);
        } else {
            setting= Settings.Global.getInt(mContext.getContentResolver(),
                EASY_BELL_SWITCH, 0);
        }

        if (preference != null && preference instanceof SmartSwitchPreference) {
            ((SmartSwitchPreference) preference).setChecked(setting == 1);
        }
    }

    public static boolean isEasyBellAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_support_easyBell)
                && Utils.isSupportSensor(context, Sensor.TYPE_PICK_UP_GESTURE);
    }

    public void updateOnSmartMotionChange(boolean isEnable) {
        if (!isEnable) {
            if (mPreference != null && mPreference.isChecked()) {
                Settings.Global.putInt(mContext.getContentResolver(), EASY_BELL_SWITCH, 1);
            }
            Settings.Global.putInt(mContext.getContentResolver(), EASY_BELL, 0);
        } else {
            if (Settings.Global.getInt(mContext.getContentResolver(), EASY_BELL_SWITCH, 0) == 1) {
                Settings.Global.putInt(mContext.getContentResolver(), EASY_BELL, 1);
                Settings.Global.putInt(mContext.getContentResolver(), EASY_BELL_SWITCH, 0);
            }
        }
    }

    private void showEasyBellAnimation() {
        final FragmentTransaction ft = mHost.getFragmentManager().beginTransaction();
        mHost.getFragmentManager().executePendingTransactions();
        final Fragment prev = mHost.getFragmentManager().findFragmentByTag(DIALOG_EASY_BELL_TAG);
        if (prev != null) {
            if (prev.isAdded()) {
                return;
            }
            ft.remove(prev);
        }
        // ft.addToBackStack(null);
        final EasyBellAnimation newFragment = EasyBellAnimation.newInstance(mPreference);
        if (newFragment != null && mHost.getActivity().isResumed() && !newFragment.isAdded()) {
            newFragment.show(ft, DIALOG_EASY_BELL_TAG);
        }
    }
}
