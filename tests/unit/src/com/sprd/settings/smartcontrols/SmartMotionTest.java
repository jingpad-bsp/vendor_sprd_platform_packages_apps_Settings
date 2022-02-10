package com.sprd.settings.smartcontrols;

import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.UiObjectNotFoundException;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import com.sprd.settings.smartcontrols.SmartControlsSettings;
import com.sprd.settings.smartcontrols.SmartMotionPreferenceController;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SmartMotionTest extends SmartControlsTestBase {
    @Override
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mContext = InstrumentationRegistry.getTargetContext();
        mParentExist = SmartControlsSettings.isSupportSmartControl(mContext);
        mIsSupport = SmartMotionPreferenceController.isSmartMotionAvailable(mContext);
        mParentTitle = mContext.getResources().getString(R.string.smart_controls);
        mTitle = mContext.getResources().getString(R.string.smart_motion);
        mItemLevel = 1;
        super.setUp();
    }

    @Ignore
    @Test
    public void testSwitchToggle() {
    }

    @Ignore
    @Test
    public void testAnimation() {
    }
}
