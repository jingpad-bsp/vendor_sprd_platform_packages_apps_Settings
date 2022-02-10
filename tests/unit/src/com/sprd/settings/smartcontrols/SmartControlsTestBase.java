package com.sprd.settings.smartcontrols;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.sprd.settings.smartcontrols.SmartControlsSettings;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.util.Log;

/**
 * An abstract parent for testing smart controls.
 */
abstract public class SmartControlsTestBase {

    private static final String SWITCH = "android.widget.Switch";
    private static final String SMARTCONTROLS = "Smart controls";
    private static final String OK = "OK";
    private static final long START_ACTIVITY_TIMEOUT = 5000;

    private UiDevice mDevice;
    public Context mContext;
    private String mTargetPackage;
    public int mItemLevel;
    private boolean mIsSupportSmartControl;
    public String mParentTitle;
    public String mTitle;
    public boolean mParentExist = false;
    public boolean mIsSupport = false;

    @Before
    public void setUp() throws UiObjectNotFoundException {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        mIsSupportSmartControl = SmartControlsSettings.isSupportSmartControl(mContext);
        launchSettings();
        if (mItemLevel > 0) {
            launchSmartControlsSettings();
            if (mItemLevel > 1) {
                launchSmartControlsLevel2();
            }
        }
    }

    @Test
    public void testExists() throws Exception {
        if (!mParentExist) {
            return;
        }

        final UiObject2 switchPref = getPreference();

        if (mIsSupport) {
            assertNotNull(switchPref);
        } else {
            assertNull(switchPref);
        }
    }

    @Test
    public void testAnimation() throws Exception {
        if (!mIsSupport) {
            return;
        }

        getPreference().click();
        UiObject2 oj = mDevice.wait(Until.findObject(By.text(OK)), START_ACTIVITY_TIMEOUT);
        assertThat(oj == null).isFalse();
        mDevice.pressBack();
    }

    @Test
    public void testSwitchToggle() throws Exception {
        if (!mIsSupport) {
            return;
        }

        int preValue = getSettingsProvider();
        UiObject2 switchPref = getPreferenceSwitch();

        if (preValue == 1) {
            assertThat(switchPref.isChecked()).isTrue();
        } else {
            assertThat(switchPref.isChecked()).isFalse();
        }

        mDevice.pressBack();
        int newValue = 0;
        if (preValue == 0) {
            newValue = 1;
        }
        putSettingsProvider(newValue);
        if (mItemLevel == 1) {
            launchSmartControlsSettings();
        } else if (mItemLevel == 2) {
            launchSmartControlsLevel2();
        }
        switchPref = getPreferenceSwitch();
        if (newValue == 1) {
            assertThat(switchPref.isChecked()).isTrue();
        } else {
            assertThat(switchPref.isChecked()).isFalse();
        }

        switchPref.click();
        Thread.sleep(500);
        if (newValue == 0) {
            assertThat(getSettingsProvider() == 1).isTrue();
        } else {
            assertThat(getSettingsProvider() == 0).isTrue();
        }

        putSettingsProvider(preValue);
        mDevice.pressBack();
    }

    private void launchSettings() throws UiObjectNotFoundException {
        // launch settings
        Intent settingsIntent = new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(mTargetPackage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(settingsIntent);
    }

    private void launchSmartControlsSettings() throws UiObjectNotFoundException {
        if (!mIsSupportSmartControl) {
            return;
        }
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String titleSmartControls = SMARTCONTROLS;
        settings.scrollTextIntoView(titleSmartControls);
        mDevice.findObject(new UiSelector().text(titleSmartControls)).click();
    }

    private void launchSmartControlsLevel2() throws UiObjectNotFoundException {
        if (!mParentExist) {
            return;
        }
        mDevice.findObject(By.text(mParentTitle)).click();
    }

    private UiObject2 getPreference() throws UiObjectNotFoundException {
        try {
            final UiScrollable settings = new UiScrollable(
                    new UiSelector().packageName(mTargetPackage).scrollable(true));
            settings.scrollTextIntoView(mTitle);
        } catch (UiObjectNotFoundException e) {
        }
        UiObject2 object = mDevice.wait(Until.findObject(By.text(mTitle)),
                START_ACTIVITY_TIMEOUT);
        if (object == null) {
            return null;
        }
        return object.getParent().getParent();
    }

    private UiObject2 getPreferenceSwitch() throws UiObjectNotFoundException {
        return getPreference().findObject(By.clazz(SWITCH));
    }

    public void putSettingsProvider(int value) {}
    public int getSettingsProvider() {return 0;}
}
