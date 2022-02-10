package com.sprd.settings.sprdramdisplay;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.text.TextUtils;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;
import com.sprd.settings.sprdramdisplay.SprdRamDisplayPreferenceController;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RamDisplayTest {

    private UiDevice mDevice;
    public Context mContext;
    private String mTargetPackage;
    private String mTitle;
    private SprdRamDisplayPreferenceController mSprdRamDisplayPreferenceController;
    private static final long START_ACTIVITY_TIMEOUT = 5000;
    private static final String CONFIG_RAM_SIZE = "ro.deviceinfo.ram";
    private static final String SPRD_RAM_SIZE = "ro.boot.ddrsize";


    @Before
    public void setUp() throws UiObjectNotFoundException {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        mTitle = mContext.getResources().getString(R.string.phone_ram);
        mSprdRamDisplayPreferenceController =  new SprdRamDisplayPreferenceController(mContext);
        launchAboutPhoneSettings();
    }

    @Test
    public void testExistAndValue() throws Exception {
        final UiObject2 pref = getPreference();
        String configRam = mSprdRamDisplayPreferenceController.getConfigRam();
        String propertyRam = mSprdRamDisplayPreferenceController.getRamSizeFromProperty();

        if (!mSprdRamDisplayPreferenceController.isPhoneRamSupported()) {
            assertNull(pref);
            return;
        }

        if (configRam == null && propertyRam == null) {
            assertNull(pref);
            return;
        }

        assertNotNull(pref);
        String summary = (pref.findObject(By.res("android:id/summary"))).getText();

        if (!TextUtils.isEmpty(SystemProperties.get(CONFIG_RAM_SIZE))) {
            assertNotNull(configRam);
            assertThat(summary).isEqualTo(configRam);
        } else {
            assertNull(configRam);
            if (!TextUtils.isEmpty(SystemProperties.get(SPRD_RAM_SIZE))) {
                assertNotNull(propertyRam);
                assertThat(summary).isEqualTo(propertyRam);
            } else {
                assertNull(propertyRam);
            }
        }
    }

    private void launchAboutPhoneSettings() {
        Intent aboutIntent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
        aboutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(aboutIntent);
    }

    private UiObject2 getPreference() throws UiObjectNotFoundException {
        try {
            final UiScrollable settings = new UiScrollable(
                    new UiSelector().packageName(mTargetPackage).scrollable(true));
            settings.scrollTextIntoView(mTitle);
        } catch (UiObjectNotFoundException e) {
        }
        UiObject2 obj = mDevice.wait(Until.findObject(By.text(mTitle)),
                START_ACTIVITY_TIMEOUT);
        if (obj == null) {
            return null;
        }
        return obj.getParent().getParent();
    }
}
