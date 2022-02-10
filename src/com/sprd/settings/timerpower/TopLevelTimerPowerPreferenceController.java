/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.sprd.settings.timerpower;

import android.content.Context;
import android.os.UserHandle;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class TopLevelTimerPowerPreferenceController extends BasePreferenceController {

    public TopLevelTimerPowerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        //Add for bug1123651, hide menu when not system user
        boolean isAdmin = UserHandle.myUserId() == UserHandle.USER_SYSTEM;
        return (isAdmin && mContext.getResources().getBoolean(R.bool.config_support_scheduledPowerOnOff))
        ? AVAILABLE
        : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getText(R.string.timer_power_summary);
    }
}
