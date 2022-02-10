package com.sprd.settings.timerpower;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.Settings;
import android.os.SystemClock;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.Switch;

import androidx.preference.SwitchPreference;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;
import com.sprd.settings.timerpower.Alarm;
import com.sprd.settings.timerpower.Alarms;
import com.sprd.settings.timerpower.TimerPower;
import com.sprd.settings.timerpower.TopLevelTimerPowerPreferenceController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.util.Log;

public class TimerPowerTest {
    private static final String SWITCH = "android.widget.Switch";
    private static final String PREFERENCE = "androidx.preference.Preference;";
    private static final String TIMER_POWER = "Scheduled power on/off";
    private static final long TIMEOUT = 3000;
    private static final int AVAILABLE = 0;
    private static final int UNSUPPORTED_ON_DEVICE = 3;

    private boolean mIsSupported;
    private UiDevice mDevice;
    private Context mContext;
    private String mTargetPackage;
    private String mTitle;
    private TopLevelTimerPowerPreferenceController mController;

    @Mock
    private Context mMockContext;
    @Mock
    private Resources mResources;

    @Before
    public void setUp() throws UiObjectNotFoundException {
        MockitoAnnotations.initMocks(this);
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        when(mMockContext.getResources()).thenReturn(mResources);
        mController = new TopLevelTimerPowerPreferenceController(mMockContext, "testKey");
        mIsSupported = mContext.getResources().getBoolean(R.bool.config_support_scheduledPowerOnOff);
        launchSettings();
     }

    @Test
    public void testFeatureSwitchOn() {
        // Set feature switch on, test if mController returns the correct status
        when(mResources.getBoolean(anyInt())).thenReturn(true);
        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void testFeatureSwitchOff() {
        // Set feature switch off, test if mController returns the correct statu
        when(mResources.getBoolean(anyInt())).thenReturn(false);
        assertThat(mController.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
        // Just test method getSummary.
        // The context in mController is a mock object, so it returns null.
        assertThat(mController.getSummary()).isEqualTo(null);
    }

    @Test
    public void testTimerPowerExists() {
        // The feature switch is turned on by default.
        // Test if there is a "scheduled power on/off" preference
        final UiObject2 pref = mDevice.wait(Until.findObject(By.text(TIMER_POWER)), TIMEOUT);
        if (mIsSupported) {
            assertNotNull("Scheduled power on/off not display", pref);
        } else {
            assertNull("Scheduled power on/off is showing", pref);
        }
    }

    @Test
    public void testPowerOnSwitchToggle() throws UiObjectNotFoundException {
        if (!mIsSupported) {
            return;
        }
        launchTimerPowerSettings();
        mTitle = "Power on";
        UiObject2 switchPref = getPreferenceSwitch();
        boolean powerOnEnabled = getAlarmEnabled("on");
        // Test switch status is consistent with the status in the database.
        // Test whether the click switch can correctly change the status value in the database.
        if (powerOnEnabled) {
            assertThat(switchPref.isChecked()).isTrue();
            switchPref.click();
            SystemClock.sleep(500);
            assertThat(getAlarmEnabled("on")).isFalse();
        } else {
            assertThat(switchPref.isChecked()).isFalse();
            switchPref.click();
            SystemClock.sleep(500);
            assertThat(getAlarmEnabled("on")).isTrue();
        }
    }

    @Test
    public void testPowerOffSwitchToggle() throws UiObjectNotFoundException {
        if (!mIsSupported) {
            return;
        }
        launchTimerPowerSettings();
        mTitle = "Power off";
        UiObject2 switchPref = getPreferenceSwitch();
        boolean powerOffEnabled = getAlarmEnabled("off");
        // Test switch status is consistent with the status in the database.
        // Test whether the click switch can correctly change the status value in the database.
        if (powerOffEnabled) {
            assertThat(switchPref.isChecked()).isTrue();
            switchPref.click();
            SystemClock.sleep(500);
            assertThat(getAlarmEnabled("off")).isFalse();
        } else {
            assertThat(switchPref.isChecked()).isFalse();
            switchPref.click();
            SystemClock.sleep(500);
            assertThat(getAlarmEnabled("off")).isTrue();
        }
    }


    private UiObject2 getPreference() throws UiObjectNotFoundException {
        try {
            final UiScrollable settings = new UiScrollable(
                    new UiSelector().packageName(mTargetPackage).scrollable(true));
            settings.scrollTextIntoView(mTitle);
        } catch (UiObjectNotFoundException e) {
        }
        return mDevice.wait(Until.findObject(By.text(mTitle)),
                TIMEOUT).getParent().getParent();
    }

    private UiObject2 getPreferenceSwitch() throws UiObjectNotFoundException {
        return getPreference().findObject(By.clazz(SWITCH));
    }

    private void launchSettings() throws UiObjectNotFoundException {
        // launch settings
        Intent settingsIntent = new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(mTargetPackage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(settingsIntent);
    }

    private void launchTimerPowerSettings() throws UiObjectNotFoundException {
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String titleTimerPower = TIMER_POWER;
        settings.scrollTextIntoView(titleTimerPower);
        mDevice.findObject(new UiSelector().text(titleTimerPower)).click();
    }

    private boolean getAlarmEnabled(String label) {
        Cursor cursor = Alarms.getAlarmsCursor(mContext.getContentResolver());
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final Alarm alarm = new Alarm(mContext, cursor);
                if (!alarm.label.equals("") && alarm.label.equals(label)) {
                    return alarm.enabled;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return false;
    }
}
