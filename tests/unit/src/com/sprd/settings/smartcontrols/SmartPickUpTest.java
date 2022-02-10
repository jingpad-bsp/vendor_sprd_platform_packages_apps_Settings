package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Secure.DOZE_PICK_UP_GESTURE;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.test.uiautomator.UiObjectNotFoundException;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import com.sprd.settings.smartcontrols.SmartControlsSettings;
import com.sprd.settings.smartcontrols.SmartPickUpPreferenceController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SmartPickUpTest extends SmartControlsTestBase {
    @Override
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mContext = InstrumentationRegistry.getTargetContext();
        mParentExist = SmartControlsSettings.isSupportSmartControl(mContext);
        mIsSupport = SmartPickUpPreferenceController.isSmartPickUpAvailable(mContext);
        mParentTitle = mContext.getResources().getString(R.string.smart_controls);
        mTitle = mContext.getResources().getString(R.string.ambient_display_pickup_title);
        mItemLevel = 1;
        super.setUp();
    }

    @Override
    public void putSettingsProvider(int value) {
        Settings.Secure.putInt(mContext.getContentResolver(), DOZE_PICK_UP_GESTURE, value);
    }

    @Override
    public int getSettingsProvider() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                DOZE_PICK_UP_GESTURE, 0);
    }
}
