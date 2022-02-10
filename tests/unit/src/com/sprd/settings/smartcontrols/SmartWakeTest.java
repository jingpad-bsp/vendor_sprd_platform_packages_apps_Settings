package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.test.uiautomator.UiObjectNotFoundException;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import com.sprd.settings.smartcontrols.SmartControlsSettings;
import com.sprd.settings.smartcontrols.SmartWakePreferenceController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SmartWakeTest extends SmartControlsTestBase {
    @Override
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mContext = InstrumentationRegistry.getTargetContext();
        mParentExist = SmartControlsSettings.isSupportSmartControl(mContext);
        mIsSupport = SmartWakePreferenceController.isSmartWakeAvailable(mContext) &&
                SmartWakePreferenceController.isWakeGestureAvailable(mContext);
        mParentTitle = mContext.getResources().getString(R.string.smart_controls);
        mTitle = mContext.getResources().getString(R.string.smart_wake);
        mItemLevel = 1;
        super.setUp();
    }

    @Override
    public void putSettingsProvider(int value) {
        Settings.Secure.putInt(mContext.getContentResolver(), WAKE_GESTURE_ENABLED, value);
    }

    @Override
    public int getSettingsProvider() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                WAKE_GESTURE_ENABLED, 0);
    }
}
