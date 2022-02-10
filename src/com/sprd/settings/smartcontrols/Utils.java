/**
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.sprd.settings.smartcontrols;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.net.Uri;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SprdSensor;

import com.android.settings.R;
import java.util.List;

public final class Utils {

    public static final int[] SENSORHUB_LIST = new int[] {
        SprdSensor.TYPE_SPRDHUB_HAND_UP,
        SprdSensor.TYPE_SPRDHUB_SHAKE,
        Sensor.TYPE_PICK_UP_GESTURE,
        SprdSensor.TYPE_SPRDHUB_FLIP,
        SprdSensor.TYPE_SPRDHUB_TAP,
        Sensor.TYPE_WAKE_GESTURE,
        SprdSensor.TYPE_SPRDHUB_POCKET_MODE
    };

    public static boolean isSupportSensor(Context context, int sensorType) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor;
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(sensorType);
            if (sensor != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportSmartControl(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Boolean isSupportSmartControl = false;
        if (sensorManager != null) {
            for (int i = 0; i < SENSORHUB_LIST.length; i++) {
                isSupportSmartControl |= sensorManager.getDefaultSensor(SENSORHUB_LIST[i]) != null;
            }
            return isSupportSmartControl &&
                    context.getResources().getBoolean(R.bool.config_support_smartControls);
        }
        return false;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasGalleryVideo(Context context, String pkg) {
        final Intent intent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("file://"), "video/*");

        intent.setPackage(pkg);
        final List<ResolveInfo> resolveInfos =
                context.getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfos != null && resolveInfos.size() != 0;
    }

}
