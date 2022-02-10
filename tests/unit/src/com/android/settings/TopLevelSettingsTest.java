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
package com.android.settings;

import static com.google.common.truth.Truth.assertThat;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TopLevelSettingsTest {

    private static final String NETWORK_INTERNET = "Network & internet";
    private static final String CONNECTED_DEVICES = "Connected devices";
    private static final String APPS_NOTIFICATIONS = "Apps & notifications";
    private static final String BATTERY = "Battery";
    private static final String DISPLAY = "Display";
    private static final String TIMER_POWER = "Scheduled power on/off";
    private static final String SOUND = "Sound";
    private static final String STORAGE = "Storage";
    private static final String PRIVACY = "Privacy";
    private static final String LOCATION = "Location";
    private static final String SECURITY = "Security";
    private static final String ACCOUNTS = "Accounts";
    private static final String ACCESSIBILITY = "Accessibility";
    private static final String SMART_CONTROLS = "Smart controls";
    private static final String SYSTEM = "System";
    private static final String ABOUT_PHONE = "About phone";
    private static final String SEARCH = "Search settings";

    private UiDevice mDevice;
    private Context mContext;
    private String mTargetPackage;

    @Before
    public void setUp() throws UiObjectNotFoundException {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        launchSettings();
    }

    @Test
    public void testLaunchNetworkAndInternetShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Network & internet"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = NETWORK_INTERNET;
        settings.scrollTextIntoView(preferenceTitle);
        //mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchConnectedDevicesShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Connected devices"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = CONNECTED_DEVICES;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchAppsAndNotificationsShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Apps & notifications"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = APPS_NOTIFICATIONS;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchBatteryShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Battery"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = BATTERY;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchDisplayShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Display"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = DISPLAY;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchTimerPowerShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Scheduled power on/off"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = TIMER_POWER;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchSoundShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Sound"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = SOUND;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchStorageShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Storage"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = STORAGE;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchPrivacyShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Privacy"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = PRIVACY;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchLocationShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Location"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = LOCATION;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchSecurityShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Security"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = SECURITY;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchAccountsShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Accounts"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = ACCOUNTS;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchAccessibilityShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Accessibility"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = ACCESSIBILITY;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchSmartControlsShouldNotCrash() throws UiObjectNotFoundException {
        // selects "Smart controls"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = SMART_CONTROLS;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchSystemShouldNotCrash() throws UiObjectNotFoundException {
        // selects "System"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = SYSTEM;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchAbountPhoneShouldNotCrash() throws UiObjectNotFoundException {
        // selects "About phone"
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = ABOUT_PHONE;
        settings.scrollTextIntoView(preferenceTitle);
        mDevice.findObject(new UiSelector().text(preferenceTitle)).click();
    }

    @Test
    public void testLaunchSearchShouldNotCrash() throws UiObjectNotFoundException {
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String preferenceTitle = NETWORK_INTERNET;
        settings.scrollTextIntoView(preferenceTitle);
        // selects "Search settings"
        onView(withId(R.id.search_action_bar)).perform(click());
    }

    private void launchSettings() throws UiObjectNotFoundException {
        // launch settings
        Intent settingsIntent = new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(mTargetPackage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(settingsIntent);
    }
}
