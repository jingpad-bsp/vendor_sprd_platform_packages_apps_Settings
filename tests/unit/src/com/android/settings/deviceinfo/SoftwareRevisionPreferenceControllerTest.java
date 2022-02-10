package com.android.settings.deviceinfo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.text.TextUtils;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class SoftwareRevisionPreferenceControllerTest {
    private static final String KEY_SOFTWARE_REVISION = "software_revision";
    private static final String ABOUT_PHONE = "About phone";

    private SoftwareRevisionPreferenceController mController;
    private Context mContext;
    private String mTargetPackage;
    private UiDevice mDevice;
    private boolean mIsCtccVersion;

    @Mock
    private Context mMockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mController = new SoftwareRevisionPreferenceController(mMockContext);
        mIsCtccVersion = mContext.getResources().getBoolean(R.bool.config_enableCtccVersion);
    }

    @Test
    public void testIsShowing() throws UiObjectNotFoundException {
        launchAboutPhone();
        // scroll to the bottom of fragment
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        settings.scrollTextIntoView("Build number");
        // If CTCC version, show hardware version
        if (mIsCtccVersion) {
            onView(withText("Software version")).check(matches(isDisplayed()));
        } else {
            onView(withText("Software version")).check(doesNotExist());
        }
    }

    @Test
    public void testIsAvailable() {
        Resources mockResources = mock(Resources.class);
        when(mMockContext.getResources()).thenReturn(mockResources);

        // Turn on the ctcc switch
        when(mMockContext.getResources().getBoolean(R.bool.config_enableCtccVersion))
                .thenReturn(true);
        assertTrue(mController.isAvailable());

        // Turn off the ctcc switch
        when(mMockContext.getResources().getBoolean(R.bool.config_enableCtccVersion))
                .thenReturn(false);
        assertFalse(mController.isAvailable());
    }

    @Test
    public void testPreferenceSummary() {
        Resources mockResources = mock(Resources.class);
        when(mMockContext.getResources()).thenReturn(mockResources);
        when(mMockContext.getResources().getString(R.string.device_info_default))
                .thenReturn("Unknown");

        final String version = SystemProperties.get("ro.version.software");
        // If not empty, the version obtained from the system property is displayed.
        if (!TextUtils.isEmpty(version)) {
            assertThat(mController.getSummary()).isEqualTo(version);
        } else {
            assertThat(mController.getSummary()).isEqualTo("Unknown");
        }
    }

    private void launchAboutPhone() throws UiObjectNotFoundException {
        // launch settings
        Intent settingsIntent = new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(mTargetPackage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(settingsIntent);

        // Select About Phone
        final UiScrollable settings = new UiScrollable(
                new UiSelector().packageName(mTargetPackage).scrollable(true));
        final String titleAboutPhone = ABOUT_PHONE;
        settings.scrollTextIntoView(titleAboutPhone);
        mDevice.findObject(new UiSelector().text(titleAboutPhone)).click();
    }
}
