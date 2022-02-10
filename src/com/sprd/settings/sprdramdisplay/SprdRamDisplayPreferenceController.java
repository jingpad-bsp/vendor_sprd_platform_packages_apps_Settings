package com.sprd.settings.sprdramdisplay;

import android.app.Activity;
import android.content.Context;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SprdRamDisplayPreferenceController extends AbstractPreferenceController
            implements PreferenceControllerMixin {
    private Context mContext;

    private static final String TAG = "SprdRamDisplayPreferenceController";

    private static final String KEY_SUPPORT_RAM_DISPLAY = "ram_display";
    private static final String CONFIG_RAM_SIZE = "ro.deviceinfo.ram";
    // Modified for bug1104848, the System property has changed.
    private static final String SPRD_RAM_SIZE = "ro.boot.ddrsize";

    private static final int SI_UNITS = 1000;
    private static final int IEC_UNITS = 1024;

    public SprdRamDisplayPreferenceController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public boolean isAvailable() {
        return isPhoneRamSupported();
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SUPPORT_RAM_DISPLAY;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        String ramSize = getConfigRam();
        if (ramSize == null) {
            ramSize = getRamSizeFromProperty();
        }

        if (ramSize != null) {
            Log.d(TAG, "RAM Size: " + ramSize);
            preference.setSummary(ramSize);
        } else {
            preference.setVisible(false);
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
    }

    public boolean isPhoneRamSupported() {

        boolean mSupport = mContext.getResources().getBoolean(R.bool.config_support_showRam);
        Log.d(TAG, "isPhoneRamSupported: " + mSupport);
        return mSupport;
    }

    public String getConfigRam() {
        String ramConfig = SystemProperties.get(CONFIG_RAM_SIZE, "unconfig");
        if ("unconfig".equals(ramConfig)) {
            Log.d(TAG, "no config ram size.");
            return null;
        } else {
            long configTotalRam = Long.parseLong(ramConfig);
            Log.d(TAG, "config ram to be: " + configTotalRam);
            return Formatter.formatShortFileSize(mContext, configTotalRam);
        }
    }

    public String getRamSizeFromProperty() {
        String size = SystemProperties.get(SPRD_RAM_SIZE, "unconfig");
        if ("unconfig".equals(size)) {
            Log.d(TAG, "can not get ram size from "+SPRD_RAM_SIZE);
            return null;
        } else {
            Log.d(TAG, "property value is:" + size);
            String regEx="[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(size);
            size = m.replaceAll("").trim();
            long ramSize = Long.parseLong(size);
            return Formatter.formatShortFileSize(mContext, covertUnitsToSI(ramSize));
        }
    }

    /**
     * SI_UNITS = 1000bytes; IEC_UNITS = 1024bytes
     * 512MB = 512 * 1000 * 1000
     * 2048MB = 2048/1024 * 1000 * 1000 * 1000
     * 2000MB = 2000 * 1000 * 1000
     */
    private long covertUnitsToSI(long size) {
        if (size > SI_UNITS && size % IEC_UNITS == 0) {
            return size / IEC_UNITS * SI_UNITS * SI_UNITS * SI_UNITS;
        }
        return size * SI_UNITS * SI_UNITS;
    }
}
