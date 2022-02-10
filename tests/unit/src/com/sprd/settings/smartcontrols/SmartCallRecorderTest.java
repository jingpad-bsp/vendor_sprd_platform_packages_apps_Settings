package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.SMART_CALL_RECORDER;
import static android.provider.Settings.Global.SMART_MOTION_ENABLED;

import android.content.Context;
import android.provider.Settings;
import android.support.test.uiautomator.UiObjectNotFoundException;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import com.sprd.settings.smartcontrols.SmartCallRecorderPreferenceController;
import com.sprd.settings.smartcontrols.SmartMotionFragment;
import com.sprd.settings.smartcontrols.SmartMotionPreferenceController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SmartCallRecorderTest extends SmartControlsTestBase {
    private boolean mIsSmartMotionEnabled;

    @Override
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mContext = InstrumentationRegistry.getTargetContext();
        mParentExist = SmartMotionPreferenceController.isSmartMotionAvailable(mContext);
        mIsSupport = SmartCallRecorderPreferenceController.isSmartCallRecorderAvailable(mContext);
        mIsSmartMotionEnabled = SmartMotionFragment.isSmartMotionEnabled(mContext);
        mParentTitle = mContext.getResources().getString(R.string.smart_motion);
        mTitle = mContext.getResources().getString(R.string.smart_call_recorder);
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
        Settings.Global.putInt(mContext.getContentResolver(), SMART_CALL_RECORDER, value);
    }

    @Override
    public int getSettingsProvider() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                SMART_CALL_RECORDER, 0);
    }
}
