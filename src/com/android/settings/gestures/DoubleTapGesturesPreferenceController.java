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

package com.android.settings.gestures;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;
import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_VOLUMEUP_GESTURE_DISABLED;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

public class DoubleTapGesturesPreferenceController extends GesturePreferenceController {

    @VisibleForTesting
    static final int ON = 0;
    @VisibleForTesting
    static final int OFF = 1;

    private static final String PREF_KEY_VIDEO = "gesture_double_tap_power_video";

    private static final String SECURE_KEY = CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;
    private static final String VOLUME_UP_SECURE_KEY = CAMERA_DOUBLE_TAP_VOLUMEUP_GESTURE_DISABLED;
    private static final String KEY = "gesture_double_tap_power";

    private static boolean mDoubleTapPower = false;
    private static boolean mDoubleTapVolumeUp = false;

    public DoubleTapGesturesPreferenceController(Context context, String key) {
        super(context, key);
        mDoubleTapPower = context.getResources()
                .getBoolean(com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
        mDoubleTapVolumeUp = context.getResources()
                .getBoolean(com.android.internal.R.bool.config_cameraDoubleTapVolumeUpGestureEnabled);
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        return !isGestureAvailable(context)
                || prefs.getBoolean(DoubleTapPowerSettings.PREF_KEY_SUGGESTION_COMPLETE, false);
    }

    private static boolean isGestureAvailable(Context context) {
        return mDoubleTapPower || mDoubleTapVolumeUp;
    }

    @Override
    public int getAvailabilityStatus() {
        return isGestureAvailable(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY);
    }

    @Override
    protected String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public boolean isChecked() {
        int cameraDisabled = OFF;
        if (mDoubleTapVolumeUp) {
            cameraDisabled = Settings.Secure.getInt(mContext.getContentResolver(),
                VOLUME_UP_SECURE_KEY, ON);
        } else {
            cameraDisabled = Settings.Secure.getInt(mContext.getContentResolver(),
                SECURE_KEY, ON);
        }
        return cameraDisabled == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return true;
    }
}
