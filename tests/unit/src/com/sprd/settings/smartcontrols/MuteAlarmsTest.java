package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.MUTE_ALARMS;
import static android.provider.Settings.Global.SMART_MOTION_ENABLED;

import android.content.Context;
import android.provider.Settings;
import android.support.test.uiautomator.UiObjectNotFoundException;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import com.sprd.settings.smartcontrols.MuteAlarmsPreferenceController;
import com.sprd.settings.smartcontrols.SmartMotionFragment;
import com.sprd.settings.smartcontrols.SmartMotionPreferenceController;
import com.sprd.settings.smartcontrols.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MuteAlarmsTest extends SmartControlsTestBase {
    private boolean mIsSmartMotionEnabled;
    private static final String PACK_NAME = "com.android.deskclock";

    @Override
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mContext = InstrumentationRegistry.getTargetContext();
        mParentExist = SmartMotionPreferenceController.isSmartMotionAvailable(mContext);
        mIsSupport = MuteAlarmsPreferenceController.isMuteAlarmsAvailable(mContext) &&
                Utils.isAppInstalled(mContext, PACK_NAME);
        mIsSmartMotionEnabled = SmartMotionFragment.isSmartMotionEnabled(mContext);
        mParentTitle = mContext.getResources().getString(R.string.smart_motion);
        mTitle = mContext.getResources().getString(R.string.mute_alarms);
        mItemLevel = 2;

        Settings.Global.putInt(mContext.getContentResolver(), SMART_MOTION_ENABLED, 1);
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        int smartMotionEnabled = 0;
        if (mIsSmartMotionEnabled) {
            smartMotionEnabled = 1;
        }
        Settings.Global.putInt(mContext.getContentResolver(), SMART_MOTION_ENABLED, smartMotionEnabled);
    }

    @Override
    public void putSettingsProvider(int value) {
        Settings.Global.putInt(mContext.getContentResolver(), MUTE_ALARMS, value);
    }

    @Override
    public int getSettingsProvider() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                MUTE_ALARMS, 0);
    }
}
