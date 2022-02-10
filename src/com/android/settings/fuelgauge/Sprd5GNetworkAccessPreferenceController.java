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
import android.os.SystemProperties;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

import java.util.List;

/**
 * Controller of 5G limited apps preference .
 * Some applications using 5G network might cause increasement of power comsumption, this feature
 * allows user to control the accessiblity of 5G netwrok.
 */
public class Sprd5GNetworkAccessPreferenceController extends BasePreferenceController {
    private static final String KEY_NETWORK_ACCESS_APPS = "fifth_generation_network_limited_apps";

    public Sprd5GNetworkAccessPreferenceController(Context context) {
        super(context, KEY_NETWORK_ACCESS_APPS);
    }

    public Sprd5GNetworkAccessPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        boolean isSupportedBySettings = mContext.getResources()
                .getBoolean(R.bool.config_support_5g_network_access);
        boolean isShow = isSupportedBySettings && isSupportedByDevice();
        return isShow ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private boolean isSupportedByDevice() {
        boolean supported = SystemProperties.getBoolean("persist.sys.pwctl.net.sa", false);
        return supported;
    }

}
