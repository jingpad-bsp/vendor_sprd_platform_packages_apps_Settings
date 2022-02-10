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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsActivity;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import static android.provider.Settings.Global.POCKET_MODE_ENABLED;

import java.util.ArrayList;
import java.util.List;

/**
 * PocketMode consists of 3 parts: SmartBell, TouchDisable and PowerSaving.
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class PocketModeFragment extends DashboardFragment implements
        SwitchBar.OnSwitchChangeListener {

    private static final String TAG = "PocketModeFragment";

    private SwitchBar mSwitchBar;
    private boolean mValidListener = false;

    private static SmartBellPreferenceController mSmartBellPreferenceController;
    private static TouchDisablePreferenceController mTouchDisablePreferenceController;
    private static PowerSavingPreferenceController mPowerSavingPreferenceController;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SettingsActivity activity = (SettingsActivity) getActivity();
        mSwitchBar = activity.getSwitchBar();
        mSwitchBar.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mValidListener = true;
        }
        mSwitchBar.setChecked(isPocketModeEnabled(getActivity()));
        getPreferenceScreen().setEnabled(isPocketModeEnabled(getActivity()));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mValidListener = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.hide();
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.POCKET_MODE;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.pocket_mode;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Fragment host) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        mSmartBellPreferenceController = new SmartBellPreferenceController(context, host);
        controllers.add(mSmartBellPreferenceController);
        mTouchDisablePreferenceController = new TouchDisablePreferenceController(context, host);
        controllers.add(mTouchDisablePreferenceController);
        mPowerSavingPreferenceController = new PowerSavingPreferenceController(context, host);
        controllers.add(mPowerSavingPreferenceController);
        return controllers;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Settings.Global.putInt(getContentResolver(), POCKET_MODE_ENABLED, isChecked ? 1 : 0);
        getPreferenceScreen().setEnabled(isChecked);
        mSmartBellPreferenceController.updateOnPocketModeChange(isChecked);
        mTouchDisablePreferenceController.updateOnPocketModeChange(isChecked);
        mPowerSavingPreferenceController.updateOnPocketModeChange(isChecked);
    }

    public static final boolean isPocketModeEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.POCKET_MODE_ENABLED, 0) == 1;
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
                sir.xmlResId = R.xml.pocket_mode;
                result.add(sir);
            }
            return result;
        }

        @Override
        public List<AbstractPreferenceController> getPreferenceControllers(Context
                context) {
            return buildPreferenceControllers(context, null);
        }
    };
}
