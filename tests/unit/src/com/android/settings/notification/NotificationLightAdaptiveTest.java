package com.android.settings.notification;

import static com.google.common.truth.Truth.assertThat;
import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;
import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.app.NotificationManager.Policy.SUPPRESSED_EFFECT_BADGE;
import static android.app.NotificationManager.Policy.SUPPRESSED_EFFECT_LIGHTS;
import static android.app.NotificationManager.Policy.SUPPRESSED_EFFECT_NOTIFICATION_LIST;
import static android.app.NotificationManager.Policy.SUPPRESSED_EFFECT_PEEK;
import static android.provider.Settings.System.NOTIFICATION_LIGHT_PULSE;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.AutomaticZenRule;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.notification.ZenPolicy;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.notification.LightsPreferenceController;
import com.android.settings.notification.PulseNotificationPreferenceController;
import com.android.settings.notification.ZenModeVisEffectPreferenceController;
import com.android.settings.notification.ZenRuleVisEffectPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class NotificationLightAdaptiveTest {
    private static final String PREF_KEY = "main_pref";
    public static final String RULE_ID = "test_rule_id";
    private static final int PREF_METRICS = 1;

    private int mDefaultValue;
    private Context mContext;
    private boolean mLedFileIsExists;
    private boolean mIntrusiveNotificationLed;

    private LightsPreferenceController mLightsController;
    private ZenModeVisEffectPreferenceController mZenModeController;
    private ZenRuleVisEffectPreferenceController mZenRuleController;

    @Mock
    private Context mMockContext;

    AutomaticZenRule mRule = new AutomaticZenRule("test", null, null, null, null,
            NotificationManager.INTERRUPTION_FILTER_PRIORITY, true);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = InstrumentationRegistry.getTargetContext();
        mLedFileIsExists = Utils.ledFileIsExists();
        mIntrusiveNotificationLed = mContext.getResources()
                .getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed);
        mDefaultValue = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_PULSE, 0);
        mLightsController = spy(new LightsPreferenceController(mContext, mock(NotificationBackend.class)));
        mZenModeController = new ZenModeVisEffectPreferenceController(mContext, mock(Lifecycle.class),
                PREF_KEY, SUPPRESSED_EFFECT_PEEK, PREF_METRICS, null);
        mZenRuleController = new ZenRuleVisEffectPreferenceController(mContext, mock(Lifecycle.class),
                PREF_KEY, ZenPolicy.VISUAL_EFFECT_PEEK, PREF_METRICS, null);
    }

    @After
    public void tearDown() throws Exception {
        Settings.System.putInt(mContext.getContentResolver(), NOTIFICATION_LIGHT_PULSE, mDefaultValue);
    }

    @Test
    public void testLightsPreferenceController() {
        Settings.System.putInt(mContext.getContentResolver(), NOTIFICATION_LIGHT_PULSE, 1);
        NotificationBackend.AppRow appRow = new NotificationBackend.AppRow();
        NotificationChannel channel = new NotificationChannel("", "", IMPORTANCE_DEFAULT);
        // Set here to ensure that the parent controller isAvailable method returns true
        mLightsController.onResume(appRow, channel, null, null);
        // If the values of mLedFileIsExists and mIntrusiveNotificationLed are both true,
        // the return value depends on the value of NOTIFICATION_LIGHT_PULSE in the database.
        if (mLedFileIsExists && mIntrusiveNotificationLed) {
            assertTrue(mLightsController.isAvailable());
        } else {
            assertFalse(mLightsController.isAvailable());
        }
    }

    @Test
    public void testPulseNotificationPreferenceController() {
       final PulseNotificationPreferenceController controller =
               new PulseNotificationPreferenceController(mMockContext, "testkey");
       Resources mockResources = mock(Resources.class);
       when(mMockContext.getResources()).thenReturn(mockResources);

       when(mockResources.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed))
               .thenReturn(false);
       // If config value is false, mLedFileIsExists is no need to check.
       assertThat(controller.isAvailable()).isFalse();
       assertThat(controller.getAvailabilityStatus()).isEqualTo(
                BasePreferenceController.UNSUPPORTED_ON_DEVICE);

       when(mockResources.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed))
               .thenReturn(true);
       // If the config value is true, the return value depends on the value of mLedFileIsExists
       if (mLedFileIsExists) {
           assertThat(controller.getAvailabilityStatus()).isEqualTo(
                   BasePreferenceController.AVAILABLE);
       } else {
           assertThat(controller.getAvailabilityStatus()).isEqualTo(
                   BasePreferenceController.UNSUPPORTED_ON_DEVICE);
       }
    }

    @Test
    public void testZenModeVisEffectPreferenceController() {
        // SUPPRESSED_EFFECT_PEEK is always available,
        // but now the return value depends on the value of mLedFileIsExists
        if (mLedFileIsExists) {
            assertTrue(mZenModeController.isAvailable());
        } else {
            assertFalse(mZenModeController.isAvailable());
        }

        // SUPPRESSED_EFFECT_LIGHTS is only available if the device has an LED:
        mZenModeController = new ZenModeVisEffectPreferenceController(mMockContext, mock(Lifecycle.class),
                PREF_KEY, SUPPRESSED_EFFECT_LIGHTS, PREF_METRICS, null);
        Resources mockResources = mock(Resources.class);
        when(mMockContext.getResources()).thenReturn(mockResources);

        when(mockResources.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed))
                .thenReturn(false); // no light
        // If config value is false, mLedFileIsExists is no need to check.
        assertFalse(mZenModeController.isAvailable());

        when(mockResources.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed))
                .thenReturn(true); // has light
        // If the config value is true, the return value depends on the value of mLedFileIsExists
        if (mLedFileIsExists) {
            assertTrue(mZenModeController.isAvailable());
        } else {
            assertFalse(mZenModeController.isAvailable());
        }
    }

    @Test
    public void testZenRuleVisEffectPreferenceController() {
        // VISUAL_EFFECT_PEEK isn't available until after onResume is called
        assertFalse(mZenRuleController.isAvailable());
        // Set here to ensure that the parent controller isAvailable method returns true
        updateControllerZenPolicy(new ZenPolicy()); // calls onResume
        assertTrue(mZenRuleController.isAvailable());

        // VISUAL_EFFECT_LIGHTS is only available if the device has an LED:
        mZenRuleController = new ZenRuleVisEffectPreferenceController(mMockContext, mock(Lifecycle.class),
                PREF_KEY, ZenPolicy.VISUAL_EFFECT_LIGHTS, PREF_METRICS, null);
        // Set here to ensure that the parent controller isAvailable method returns true
        updateControllerZenPolicy(new ZenPolicy()); // calls onResume

        Resources mockResources = mock(Resources.class);
        when(mMockContext.getResources()).thenReturn(mockResources);

        // If config value is false, mLedFileIsExists is no need to check.
        when(mockResources.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed))
                .thenReturn(false); // no light
        assertFalse(mZenRuleController.isAvailable());

        // If the config value is true, the return value depends on the value of mLedFileIsExists
        when(mockResources.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed))
                .thenReturn(true); // has light
        // If the config value is true, the return value depends on the value of mLedFileIsExists
        if (mLedFileIsExists) {
            assertTrue(mZenRuleController.isAvailable());
        } else {
            assertFalse(mZenRuleController.isAvailable());
        }
    }

    void updateControllerZenPolicy(ZenPolicy policy) {
        mRule.setZenPolicy(policy);
        mZenRuleController.onResume(mRule, RULE_ID);
    }
}
