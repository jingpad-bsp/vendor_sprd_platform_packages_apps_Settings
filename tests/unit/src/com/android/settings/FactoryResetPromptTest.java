/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You m/ay obtain a copy of the License at
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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;

import com.android.settings.MasterClear;
import com.android.settings.R;
import com.android.settings.Settings.SystemDashboardActivity;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.Utils;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupdesign.GlifLayout;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collection;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FactoryResetPromptTest {
    private Context mContext;
    private String mTargetPackage;

    private SystemDashboardActivity mActivity;
    private final Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();

    @Rule
    public ActivityTestRule<SystemDashboardActivity> mSystemDashboardActivityRule =
            new ActivityTestRule<>(
                    Settings.SystemDashboardActivity.class,
                    true /* enable touch at launch */,
                    true /* don't launch at every test */);

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();
        mTargetPackage = mContext.getPackageName();
        mActivity = mSystemDashboardActivityRule.getActivity();
    }

    @Test
    public void testLowBatteryPromptBatteryLevel29_switchEnable()
            throws IOException, Throwable {
        launchMasterClear();
        Runtime.getRuntime().exec("dumpsys battery set level 29");
        clickFootButton();
        // If battery level < 30, show dialog with text (R.string.master_clear_level)
        onView(withText(R.string.master_clear_level)).check(matches(isDisplayed()));
    }

    @Test
    public void testLowBatteryPromptBatteryLevel30_switchEnable()
            throws IOException, Throwable {
        launchMasterClear();
        Runtime.getRuntime().exec("dumpsys battery set level 30");
        clickFootButton();
        onView(withText(R.string.master_clear_level)).check(doesNotExist());
    }

    @Test
    public void testLowBatteryPrompt_batteryLevel31_switchEnable()
            throws IOException, Throwable {
        launchMasterClear();
        Runtime.getRuntime().exec("dumpsys battery set level 31");
        clickFootButton();
        onView(withText(R.string.master_clear_level)).check(doesNotExist());
    }

    private void clickFootButton() throws Throwable {
        final Activity activity = getCurrentActivity();
        mInstrumentation.runOnMainSync(() -> {
            final GlifLayout layout = activity.findViewById(R.id.setup_wizard_layout);
            layout.getMixin(FooterBarMixin.class).getPrimaryButtonView().performClick();
        });
    }

    /**
     * Launch MasterClear
     */
    private void startFragment() {
        mInstrumentation.runOnMainSync(() -> {
            new SubSettingLauncher(mActivity)
                    .setDestination(MasterClear.class.getName())
                    .setArguments(new Bundle())
                    .setSourceMetricsCategory(
                            InstrumentedPreferenceFragment.METRICS_CATEGORY_UNKNOWN)
                    .launch();
        });
    }

    private Activity getCurrentActivity() throws Throwable {
        mInstrumentation.waitForIdleSync();
        final Activity[] activity = new Activity[1];
        mInstrumentation.runOnMainSync(() -> {
            Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED);
            activity[0] = activities.iterator().next();
        });
        return activity[0];
    }

    private void launchMasterClear() {
        startFragment();
    }
}
