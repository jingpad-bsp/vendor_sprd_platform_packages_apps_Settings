package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.TOUCH_DISABLE;
import static android.provider.Settings.Global.POCKET_MODE_ENABLED;

import android.content.Context;
import android.provider.Settings;
import android.support.test.uiautomator.UiObjectNotFoundException;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import com.sprd.settings.smartcontrols.PocketModePreferenceController;
import com.sprd.settings.smartcontrols.PocketModeFragment;
import com.sprd.settings.smartcontrols.TouchDisablePreferenceController;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TouchDisableTest extends SmartControlsTestBase {
    private boolean mIsPocketModeEnabled;

    @Override
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mContext = InstrumentationRegistry.getTargetContext();
        mParentExist = PocketModePreferenceController.isPocketModeAvailable(mContext);
        mIsSupport = TouchDisablePreferenceController.isTouchDisableAvailable(mContext);
        mIsPocketModeEnabled = PocketModeFragment.isPocketModeEnabled(mContext);
        mParentTitle = mContext.getResources().getString(R.string.pocket_mode);
        mTitle = mContext.getResources().getString(R.string.touch_disable);
        mItemLevel = 2;

        Settings.Global.putInt(mContext.getContentResolver(), POCKET_MODE_ENABLED, 1);
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        int pocketModeEnabled = 0;
        if (mIsPocketModeEnabled) {
            pocketModeEnabled = 1;
        }
        Settings.Global.putInt(mContext.getContentResolver(), POCKET_MODE_ENABLED, pocketModeEnabled);
    }

    @Override
    public void putSettingsProvider(int value) {
        Settings.Global.putInt(mContext.getContentResolver(), TOUCH_DISABLE, value);
    }

    @Override
    public int getSettingsProvider() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                TOUCH_DISABLE, 0);
    }

    @Ignore
    @Test
    public void testAnimation() {
    }
}
