package com.android.settings.deviceinfo;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.deviceinfo.AbstractWifiMacAddressPreferenceController;
import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class CtccWifiMacAddressTest {
    private static final String LOG_TAG = "CtccWifiMacAddressTest";
    private static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    private static final String MACID_FILE_PATH = "/mnt/vendor/wifimac.txt";
    private AbstractWifiMacAddressPreferenceController mController;
    private Context mContext;
    private final Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();

    private WifiManager mWifiManager;

    @Mock
    private Lifecycle mLifecycle;

    private boolean mIsSupport;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = InstrumentationRegistry.getTargetContext();
        mIsSupport = mContext.getResources().getBoolean(R.bool.config_enableGetWifiMacFromFile);
        mWifiManager = mContext.getSystemService(WifiManager.class);
        mController = new ConcreteWifiMacAddressPreferenceController(mContext, mLifecycle);
    }

    @Test
    public void testGetMacAddress_wifi_enable() throws Exception {
        // enable wifi
        mWifiManager.setWifiEnabled(true);
        SystemClock.sleep(2000);
        if (!mWifiManager.isWifiEnabled()) {
            return;
        }
        final String[] macAddresses = mWifiManager.getFactoryMacAddresses();
        String macAddress = null;
        if (macAddresses != null && macAddresses.length > 0) {
            macAddress = macAddresses[0];
        }
        Log.e(LOG_TAG, "macAddress = " + macAddress + ", getMacAddress() = " + mController.getMacAddress());
        assertThat(mController.getMacAddress()).isEqualTo(macAddress);
    }

    @Test
    public void testGetMacAddress_wifi_disable() throws Exception {
        // Disable wifi
        mWifiManager.setWifiEnabled(false);
        SystemClock.sleep(2000);
        if (mWifiManager.isWifiEnabled()) {
            return;
        }

        String mac = getMacFromFile();
        // Turn on the ctcc switch
        if (mIsSupport) {
            // If wifi is disabled, get mac address from file.
            if (!TextUtils.isEmpty(mac)) {
                assertThat(mController.getMacAddress()).isEqualTo(mac);
            } else {
                assertThat(mController.getMacAddress()).isEqualTo("Unavailable");
            }
        } else {
            // Return default value if switch is off
            assertThat(mController.getMacAddress()).isEqualTo("Unavailable");
        }

    }

    private String getMacFromFile() {
        File file = new File(MACID_FILE_PATH);
        BufferedReader reader = null;
        String macAddress = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                macAddress = line;
                break;
            }
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG , "Mac file not exist", e);
        } catch (Exception e) {
            Log.w(LOG_TAG , "get mac from file caught exception", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.w(LOG_TAG, "reader close exception");
            }
        }
        return macAddress;
    }

    private static class ConcreteWifiMacAddressPreferenceController
            extends AbstractWifiMacAddressPreferenceController {

        private ConcreteWifiMacAddressPreferenceController(Context context,
                Lifecycle lifecycle) {
            super(context, lifecycle);
        }
    }
}
