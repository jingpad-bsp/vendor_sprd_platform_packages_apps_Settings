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

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;

import androidx.fragment.app.Fragment;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main page of SmartControls.
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class SmartControlsSettings extends DashboardFragment {

    private static final String TAG = "SmartControlsSettings";

    private static final String KEY_SMART_WAKE = "smart_wake";
    private static final String KEY_SMART_MOTION = "smart_motion";
    private static final String KEY_POCKET_MODE = "pocket_mode";
    private static final String KEY_SMART_PICK_UP = "smart_pick_up";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SMART_CONTROLS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.smart_controls_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Fragment host) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new SmartWakePreferenceController(context, host));
        controllers.add(new SmartPickUpPreferenceController(context, host));
        controllers.add(new SmartMotionPreferenceController(context));
        controllers.add(new PocketModePreferenceController(context));
        return controllers;
    }

    public static boolean isSupportSmartControl(Context context) {
        return Utils.isSupportSmartControl(context);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                boolean enabled) {
            final ArrayList<SearchIndexableResource> result = new ArrayList<>();
            if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                return result;
            }

            if (Utils.isSupportSmartControl(context)) {
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.smart_controls_settings;
                result.add(sir);
            }
            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            final List<String> keys = super.getNonIndexableKeys(context);
            if (!SmartWakePreferenceController.isSmartWakeAvailable(context)
                    || !SmartWakePreferenceController.isWakeGestureAvailable(context)) {
                keys.add(KEY_SMART_WAKE);
            }
            if (!SmartMotionPreferenceController.isSmartMotionAvailable(context)) {
                keys.add(KEY_SMART_MOTION);
            }
            if (!PocketModePreferenceController.isPocketModeAvailable(context)) {
                keys.add(KEY_POCKET_MODE);
            }

            if (!SmartPickUpPreferenceController.isSmartPickUpAvailable(context)) {
                keys.add(KEY_SMART_PICK_UP);
            }
            return keys;
        }
    };
}
