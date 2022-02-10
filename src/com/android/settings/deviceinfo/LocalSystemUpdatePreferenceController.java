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
package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.R;

public class LocalSystemUpdatePreferenceController extends AbstractPreferenceController
            implements PreferenceControllerMixin, LifecycleObserver {

    private static final String LOG_TAG = "LocalSystemUpdatePreferenceController";
    private static String KEY_RECOVERY_SYSTEM_UPDATE = "RecoverySystemUpdate";

    public LocalSystemUpdatePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {

        //mdm
        if(com.jingos.mdm.MdmPolicyIntercept.SystemUpdate_Intercept(mContext))
        {
            return false;
        }

        return mContext.getResources().getBoolean(R.bool.config_support_otaupdate)
                && (UserHandle.myUserId() == UserHandle.USER_OWNER);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_RECOVERY_SYSTEM_UPDATE;
    }
    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            OtaUpdate.getInstance().initRecoverySystemUpdatePreference(
                    mContext, screen, mContext.getApplicationContext());
        }
    }

}
