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

package com.sprd.settings.smartcontrols;

import android.content.Context;
import android.icu.text.ListFormatter;
import android.text.BidiFormatter;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.util.ArrayList;
import java.util.List;

public class TopLevelSmartControlsPreferenceController extends BasePreferenceController {

    public TopLevelSmartControlsPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return SmartControlsSettings.isSupportSmartControl(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        final String smartWakeSummary = mContext.getString(
                R.string.smart_wake);
        /*final String smartMotionSummary = mContext.getString(
                R.string.smart_motion);
        final String pocketModeSummary = mContext.getString(
                R.string.pocket_mode);*/
        final String smartPickUpSummary = mContext.getString(
                R.string.ambient_display_pickup_title);

        final List<String> summaries = new ArrayList<>();
        if (SmartWakePreferenceController.isSmartWakeAvailable(mContext)
                && SmartWakePreferenceController.isWakeGestureAvailable(mContext)
                && !TextUtils.isEmpty(smartWakeSummary)) {
            summaries.add(smartWakeSummary);
        }
        /*if (SmartMotionPreferenceController.isSmartMotionAvailable(mContext)
                && !TextUtils.isEmpty(smartMotionSummary)) {
            summaries.add(smartMotionSummary);
        }
        if (PocketModePreferenceController.isPocketModeAvailable(mContext)
                && !TextUtils.isEmpty(pocketModeSummary)) {
            summaries.add(pocketModeSummary);
        }*/
        if (SmartPickUpPreferenceController.isSmartPickUpAvailable(mContext)
                && !TextUtils.isEmpty(smartPickUpSummary)) {
            //UNISOC:1155097 Change the first character to lowercase
            summaries.add(smartPickUpSummary.toLowerCase());
        }
        return ListFormatter.getInstance().format(summaries);
    }
}
