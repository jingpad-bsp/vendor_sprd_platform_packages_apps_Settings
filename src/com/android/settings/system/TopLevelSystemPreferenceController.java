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

package com.android.settings.system;

import android.content.Context;
import android.icu.text.ListFormatter;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.util.ArrayList;
import java.util.List;

public class TopLevelSystemPreferenceController extends BasePreferenceController {

    public TopLevelSystemPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE_UNSEARCHABLE;
    }

    @Override
    public CharSequence getSummary() {
        if (SystemProperties.get("ro.com.google.gmsversion").isEmpty()) {
            final String languagesSummary = mContext.getString(
                    R.string.language_picker_title);
            final String gesturesSummary = mContext.getString(
                    R.string.gesture_preference_title).toLowerCase();
            final String timeSummary = mContext.getString(
                    R.string.time_picker_title).toLowerCase();
            final List<String> summaries = new ArrayList<>();
            summaries.add(languagesSummary);
            summaries.add(gesturesSummary);
            summaries.add(timeSummary);
            return ListFormatter.getInstance().format(summaries);
        }
        return mContext.getString(R.string.system_dashboard_summary);
    }
}
