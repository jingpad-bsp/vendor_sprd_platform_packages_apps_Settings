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
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import android.hardware.display.AmbientDisplayConfiguration;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.widget.SmartSwitchPreference;
import com.android.settings.widget.SmartSwitchPreference.OnPreferenceSwitchChangeListener;
import com.android.settings.widget.SmartSwitchPreference.OnViewClickedListener;

import static android.provider.Settings.Secure.DOZE_PICK_UP_GESTURE;

/**
 * SmartPickUp:To check time, notification icons, and other info, pick up your phone.
 * This feature also exists in the display settings.
 */
public class SmartPickUpPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_SMART_PICK_UP = "smart_pick_up";
    private static final String DIALOG_SMART_PICK_UP_TAG = "smart_pick_up_dialog";
    private SmartSwitchPreference mSmartPickUpPreference;

    private final Fragment mHost;

    public SmartPickUpPreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
    }

    @Override
    public boolean isAvailable() {
        return isSmartPickUpAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SMART_PICK_UP;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mSmartPickUpPreference = (SmartSwitchPreference) screen.findPreference(KEY_SMART_PICK_UP);

            mSmartPickUpPreference.setOnViewClickedListener(new OnViewClickedListener() {
                @Override
                public void OnViewClicked(View v) {
                    showSmartPickUpAnimation();
                }
            });

            mSmartPickUpPreference.setOnPreferenceSwitchCheckedListener(new OnPreferenceSwitchChangeListener() {
                @Override
                public void onPreferenceSwitchChanged(boolean checked) {
                    Settings.Secure.putInt(mContext.getContentResolver(), DOZE_PICK_UP_GESTURE, checked ? 1 : 0);
                }
            });
        }
    }

    @Override
    public void updateState(Preference preference) {
        mSmartPickUpPreference.setChecked(isSmartPickUpEnabled(mContext));
    }

    public static boolean isSmartPickUpAvailable(Context context) {
        AmbientDisplayConfiguration ambientConfig = new AmbientDisplayConfiguration(context);
        return ambientConfig.dozePickupSensorAvailable();
    }

    private static boolean isSmartPickUpEnabled(Context context) {
        AmbientDisplayConfiguration ambientConfig = new AmbientDisplayConfiguration(context);
        return ambientConfig.pickupGestureEnabled(UserHandle.myUserId());
    }

    private void showSmartPickUpAnimation() {
        final FragmentTransaction ft = mHost.getFragmentManager().beginTransaction();
        mHost.getFragmentManager().executePendingTransactions();
        final Fragment prev = mHost.getFragmentManager().findFragmentByTag(DIALOG_SMART_PICK_UP_TAG);
        if (prev != null) {
            if (prev.isAdded()) {
                return;
            }
            ft.remove(prev);
        }
        // ft.addToBackStack(null);
        final SmartPickUpAnimation newFragment = SmartPickUpAnimation.newInstance(mSmartPickUpPreference);
        if (newFragment != null && mHost.getActivity().isResumed() && !newFragment.isAdded()) {
            newFragment.show(ft, DIALOG_SMART_PICK_UP_TAG);
        }
    }
}
