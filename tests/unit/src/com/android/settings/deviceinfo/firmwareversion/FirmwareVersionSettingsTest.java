/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.settings.deviceinfo.firmwareversion;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.android.settings.core.BasePreferenceController.UNSUPPORTED_ON_DEVICE;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;
import com.android.settings.deviceinfo.firmwareversion.BasebandVersionPreferenceController;
import com.android.settings.deviceinfo.SupportCPVersion;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FirmwareVersionSettingsTest {

    private static final String ANDROID_VERSION = "Android version";
    private static final String BASEBAND_PROPERTY = "gsm.version.baseband";

    private UiDevice mDevice;
    @Mock
    private Context mMockContext;
    @Mock
    private Resources mResources;
    @Mock
    private SupportCPVersion mInstance;

    private Context mContext;
    private String mTargetPackage;
    private BasebandVersionPreferenceController mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        when(mMockContext.getResources()).thenReturn(mResources);
        mController = new BasebandVersionPreferenceController(mMockContext, "key");
    }

    @Test
    public void testBasebandVersionPreferenceController() {
        // Set feature switch off. Test if getSummary returns the correct summary.
        when(mResources.getBoolean(anyInt())).thenReturn(false);
        final String baseband = SystemProperties.get(BASEBAND_PROPERTY,
                    mContext.getResources().getString(R.string.device_info_default));
        assertThat(mController.getSummary()).isEqualTo(baseband);
    }

    @Test
    public void testBasebandVersionExists() throws UiObjectNotFoundException {
        // Check if the Baseband Version preference exists
        launchFirmwareVersionSettings();
        onView(withText("Baseband version")).check(matches(isDisplayed()));
    }

    /**
     * Feature switch is turned on by default.
     * Check if the version string contains the baseband version.
     * Check if the version string is not the same as the baseband version.
     */
    @Test
    public void testGetCp2VersionInterface() {
        final String cp2 = SupportCPVersion.getInstance().getBasedSummary(mContext,
                BASEBAND_PROPERTY);
        final String baseband = SystemProperties.get(BASEBAND_PROPERTY,
                mContext.getResources().getString(R.string.device_info_default));
        assertThat(cp2.contains(baseband)).isTrue();
        assertThat(cp2).isNotEqualTo(baseband);
    }

    @Test
    public void testCp2DisplayStatus() throws UiObjectNotFoundException {
        boolean supportCp2 = mContext.getResources().getBoolean(R.bool.config_support_showCp2Info);
        String version;
        if (supportCp2) {
            version = SupportCPVersion.getInstance().getBasedSummary(mContext, BASEBAND_PROPERTY);
        } else {
            version = SystemProperties.get(BASEBAND_PROPERTY,
                    mContext.getResources().getString(R.string.device_info_default));
        }
        launchFirmwareVersionSettings();
        // The displayed text must be the same as the version obtained by getBasedSummary
        onView(withText(version)).check(matches(isDisplayed()));
    }

    private void launchFirmwareVersionSettings() throws UiObjectNotFoundException {
        // launch MyDeviceInfoFragment
        Intent settingsIntent = new Intent("android.settings.DEVICE_INFO_SETTINGS")
            .addCategory(Intent.CATEGORY_DEFAULT)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(settingsIntent);

        // select "Android version"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = ANDROID_VERSION;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }
}
