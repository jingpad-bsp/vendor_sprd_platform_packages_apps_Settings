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

package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller to battery saver management
 */
public class SprdBatterySaverManagePreferenceController extends BasePreferenceController {

    private static final String KEY_BATTERY_SAVER_MANAGE = "battery_saver_manage";
    private static final String TAG = "SprdBatterySaverManagePreferenceController";
    private final int mUserId;
    private final boolean isSupportSprdPowerManager = (1 == SystemProperties.getInt("persist.sys.pwctl.enable", 1));

    public SprdBatterySaverManagePreferenceController(Context context, String key) {
        super(context, key);
        mUserId = UserHandle.myUserId();
    }

    @Override
    public int getAvailabilityStatus() {
        return isBatterySaverManageAvailable()
                || isSupportSuperResolution()
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }


    private boolean isBatterySaverManageAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_support_batterySaverManage)
                && isSupportSprdPowerManager
                && isUserSupported();
    }

    private boolean isSupportSuperResolution() {
        List<String[]> resolutionMode = new ArrayList<>();
        boolean isShow = mContext.getResources().getBoolean(R.bool.config_support_superResolution);
        boolean isMutiResolutionMode = false;
        final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        try {
            resolutionMode = wm.getResolutions();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException : cannot get resolution mode");
            return false;
        }
        if(resolutionMode != null) {
            int num = resolutionMode.size();
            Log.d(TAG, "resolution mode list num = " + num);
            isMutiResolutionMode = num < 2 ? false : true;
        }
        return isShow && isMutiResolutionMode && isUserSupported();
    }

    private boolean isUserSupported() {
        return mUserId == UserHandle.USER_SYSTEM ? true : false;
    }

    @Override
    public void updateState(Preference preference) {
        final String clazz = SprdBatterySaverManageActivity.class.getName();
        preference.setOnPreferenceClickListener(target -> {
            final Context context = target.getContext();
            final UserManager userManager = UserManager.get(context);
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", clazz);
            context.startActivity(intent);
            return true;
        });
    }
}
