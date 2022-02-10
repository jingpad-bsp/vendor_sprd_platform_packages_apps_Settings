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
import android.hardware.SprdSensor;
import android.hardware.SensorManager;
import androidx.preference.Preference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;


public class SmartMotionPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_SMART_MOTION = "smart_motion";

    public static final int[] SMARTMOTION_SENSOR_LIST = new int[] {
        SprdSensor.TYPE_SPRDHUB_HAND_UP,
        SprdSensor.TYPE_SPRDHUB_SHAKE,
        Sensor.TYPE_PICK_UP_GESTURE,
        SprdSensor.TYPE_SPRDHUB_FLIP,
        SprdSensor.TYPE_SPRDHUB_TAP
    };

    public SmartMotionPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return isSmartMotionAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SMART_MOTION;
    }

    public static boolean isSmartMotionAvailable(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Boolean isSupportSmartMotion = false;
        if (sensorManager != null) {
            for (int i = 0; i < SMARTMOTION_SENSOR_LIST.length; i++) {
                isSupportSmartMotion |= sensorManager.getDefaultSensor(SMARTMOTION_SENSOR_LIST[i]) != null;
            }
            return isSupportSmartMotion;
        }
        return false;
    }
}
