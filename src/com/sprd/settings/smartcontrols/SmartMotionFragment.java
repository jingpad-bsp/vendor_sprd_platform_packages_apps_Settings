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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.widget.Switch;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceGroup;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsActivity;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import static android.provider.Settings.Global.SMART_MOTION_ENABLED;

import java.util.ArrayList;
import java.util.List;

/**
 * SmartMotion consists of three parts: SmartCall, SmartPlay and More.
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class SmartMotionFragment extends DashboardFragment implements
        SwitchBar.OnSwitchChangeListener {

    private static final String TAG = "SmartMotionFragment";

    private static final String KEY_SMART_CALL = "smart_call";
    private static final String KEY_SMART_PLAY = "smart_play";
    private static final String KEY_MORE = "more";

    private PreferenceGroup mSmartCallCategory;
    private PreferenceGroup mSmartPlayCategory;
    private PreferenceGroup mMoreCategory;

    private SwitchBar mSwitchBar;
    private boolean mValidListener = false;

    //private static EasyDialPreferenceController mEasyDialPreferenceController;
    //private static EasyAnswerPreferenceController mEasyAnswerPreferenceController;
    //private static HandsfreeSwitchPreferenceController mHandsfreeSwitchPreferenceController;
    private static SmartCallRecorderPreferenceController mSmartCallRecorderPreferenceController;
    private static EasyBellPreferenceController mEasyBellPreferenceController;
    private static MuteIncomingCallsPreferenceController mMuteIncomingCallsPreferenceController;
    private static PlayControlPreferenceController mPlayControlPreferenceController;
    private static MusicSwitchPreferenceController mMusicSwitchPreferenceController;
    private static LockMusicSwitchPreferenceController mLockMusicSwitchPreferenceController;
    private static EasyStartPreferenceController mEasyStartPreferenceController;
    private static MuteAlarmsPreferenceController mMuteAlarmsPreferenceController;
    private static ShakeToSwitchPreferenceController mShakeToSwitchPreferenceController;
    private static QuickBrowsePreferenceController mQuickBrowsePreferenceController;
    private static EasyClearMemoryPreferenceController mEasyClearMemoryPreferenceController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeAllPreferences();
    }

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
        mSwitchBar.setChecked(isSmartMotionEnabled(getActivity()));
        getPreferenceScreen().setEnabled(isSmartMotionEnabled(getActivity()));
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
        return SettingsEnums.SMART_MOTION;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.smart_motion;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Fragment host) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        //mEasyDialPreferenceController = new EasyDialPreferenceController(context, host);
        //controllers.add(mEasyDialPreferenceController);

        //mEasyAnswerPreferenceController = new EasyAnswerPreferenceController(context, host);
        //controllers.add(mEasyAnswerPreferenceController);

        //mHandsfreeSwitchPreferenceController = new HandsfreeSwitchPreferenceController(context, host);
        //controllers.add(mHandsfreeSwitchPreferenceController);

        mSmartCallRecorderPreferenceController = new SmartCallRecorderPreferenceController(context, host);
        controllers.add(mSmartCallRecorderPreferenceController);

        mEasyBellPreferenceController = new EasyBellPreferenceController(context, host);
        controllers.add(mEasyBellPreferenceController);

        mMuteIncomingCallsPreferenceController = new MuteIncomingCallsPreferenceController(context, host);
        controllers.add(mMuteIncomingCallsPreferenceController);

        mPlayControlPreferenceController = new PlayControlPreferenceController(context, host);
        controllers.add(mPlayControlPreferenceController);

        mMusicSwitchPreferenceController = new MusicSwitchPreferenceController(context, host);
        controllers.add(mMusicSwitchPreferenceController);

        mLockMusicSwitchPreferenceController = new LockMusicSwitchPreferenceController(context, host);
        controllers.add(mLockMusicSwitchPreferenceController);

        mEasyStartPreferenceController = new EasyStartPreferenceController(context, host);
        controllers.add(mEasyStartPreferenceController);

        mMuteAlarmsPreferenceController = new MuteAlarmsPreferenceController(context, host);
        controllers.add(mMuteAlarmsPreferenceController);

        mShakeToSwitchPreferenceController = new ShakeToSwitchPreferenceController(context, host);
        controllers.add(mShakeToSwitchPreferenceController);

        mQuickBrowsePreferenceController = new QuickBrowsePreferenceController(context, host);
        controllers.add(mQuickBrowsePreferenceController);

        mEasyClearMemoryPreferenceController = new EasyClearMemoryPreferenceController(context, host);
        controllers.add(mEasyClearMemoryPreferenceController);

        return controllers;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Settings.Global.putInt(getContentResolver(), SMART_MOTION_ENABLED, isChecked ? 1 : 0);
        getPreferenceScreen().setEnabled(isChecked);

        //mEasyDialPreferenceController.updateOnSmartMotionChange(isChecked);
        //mEasyAnswerPreferenceController.updateOnSmartMotionChange(isChecked);
        //mHandsfreeSwitchPreferenceController.updateOnSmartMotionChange(isChecked);
        mSmartCallRecorderPreferenceController.updateOnSmartMotionChange(isChecked);
        mEasyBellPreferenceController.updateOnSmartMotionChange(isChecked);
        mMuteIncomingCallsPreferenceController.updateOnSmartMotionChange(isChecked);
        mPlayControlPreferenceController.updateOnSmartMotionChange(isChecked);
        mMusicSwitchPreferenceController.updateOnSmartMotionChange(isChecked);
        mLockMusicSwitchPreferenceController.updateOnSmartMotionChange(isChecked);
        mEasyStartPreferenceController.updateOnSmartMotionChange(isChecked);
        mMuteAlarmsPreferenceController.updateOnSmartMotionChange(isChecked);
        mShakeToSwitchPreferenceController.updateOnSmartMotionChange(isChecked);
        mQuickBrowsePreferenceController.updateOnSmartMotionChange(isChecked);
        mEasyClearMemoryPreferenceController.updateOnSmartMotionChange(isChecked);
    }

    private void initializeAllPreferences() {
        mSmartCallCategory = (PreferenceGroup) findPreference(KEY_SMART_CALL);
        mSmartPlayCategory = (PreferenceGroup) findPreference(KEY_SMART_PLAY);
        mMoreCategory = (PreferenceGroup) findPreference(KEY_MORE);

        if (!(//mEasyDialPreferenceController.isAvailable()
            //|| mEasyAnswerPreferenceController.isAvailable()
            //|| mHandsfreeSwitchPreferenceController.isAvailable()
            mSmartCallRecorderPreferenceController.isAvailable()
            || mEasyBellPreferenceController.isAvailable()
            || mMuteIncomingCallsPreferenceController.isAvailable())
            && mSmartCallCategory != null) {
            getPreferenceScreen().removePreference(mSmartCallCategory);
        }
        if (!(mPlayControlPreferenceController.isAvailable()
            || mMusicSwitchPreferenceController.isAvailable()
            || mLockMusicSwitchPreferenceController.isAvailable())
            && mSmartPlayCategory != null) {
            getPreferenceScreen().removePreference(mSmartPlayCategory);
        }
        if (!(mEasyStartPreferenceController.isAvailable()
            || mMuteAlarmsPreferenceController.isAvailable()
            || mShakeToSwitchPreferenceController.isAvailable()
            || mQuickBrowsePreferenceController.isAvailable()
            || mEasyClearMemoryPreferenceController.isAvailable())
            && mMoreCategory != null) {
            getPreferenceScreen().removePreference(mMoreCategory);
        }
    }

    public static final boolean isSmartMotionEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                SMART_MOTION_ENABLED, 0) == 1;
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
                sir.xmlResId = R.xml.smart_motion;
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
