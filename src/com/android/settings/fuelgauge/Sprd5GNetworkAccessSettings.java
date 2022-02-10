package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.sprdpower.AppNetworkConfig;
import android.os.sprdpower.IPowerManagerEx;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import android.util.Log;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.AppSwitchPreference;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment for apps' accessibity control of 5G network
 */
public class Sprd5GNetworkAccessSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Sprd5GNetworkAccessSettings";

    /* PowerManagerEx's remaining parameter for user id, zero by default.*/
    public static final int DEFAULT_UID = 0;

    public PackageManager mPackageManager;

    public IPowerManagerEx mPowerManagerEx;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPackageManager = getContext().getPackageManager();
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuild();
    }

    private void rebuild() {
        List<AppNetworkConfig> apps = null;
        try {
            apps = mPowerManagerEx.getAppNetworkConfigList();
        } catch (RemoteException remoteException) {
            Log.w(TAG,"rebuild with exception :",remoteException);
        }

        if (getContext() == null || apps == null) return;

        cacheRemoveAllPrefs(getPreferenceScreen());
        final int n = apps.size();
        Log.d(TAG, "rebuild: size = " + n);
        for (int i = 0; i < n; i++) {
            AppNetworkConfig appNetworkConfig = apps.get(i);
            String packageName = appNetworkConfig.mPackName;
            if (!shouldAddPreference(packageName)) {
                continue;
            }

            ApplicationInfo appInfo = getApplicationInfo(packageName);
            CharSequence label = mPackageManager.getApplicationLabel(appInfo);
            Drawable icon = getApplicationIcon(packageName);

            String key = packageName + "|" + appInfo.uid;
            //add preference
            NetworkAccessPreference preference = (NetworkAccessPreference) getCachedPreference(key);
            if (preference == null) {
                preference = new NetworkAccessPreference(getPrefContext(), appNetworkConfig);
                preference.setIcon(icon);
                preference.setTitle(label);
                preference.setKey(key);
                preference.setChecked(appNetworkConfig.mUse5G);
                preference.setOnPreferenceChangeListener(this);
                Log.d(TAG, "rebuild: add preference for package: " + packageName);
                getPreferenceScreen().addPreference(preference);
            } else {
                preference.reuse();
            }
            preference.setOrder(i);

            if (appNetworkConfig.mConfigType == AppNetworkConfig.NETWORK_CONGIFG_TYPE_ONLY_PRESET) {
                preference.setEnabled(false);
            }
        }
        removeCachedPrefs(getPreferenceScreen());
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.VIEW_UNKNOWN;
    }

    boolean shouldAddPreference(String packageName) {
        return getApplicationInfo(packageName) != null;
    }

    private ApplicationInfo getApplicationInfo(String packageName) {
        if (packageName == null) return null;
        ApplicationInfo appInfo = null;
        try {
            appInfo = mPackageManager.getApplicationInfo(packageName, 0 /*flag*/);
        } catch (NameNotFoundException exception) {
            Log.w(TAG, "getApplicationInfo " + packageName + " with exception ", exception);
        }
        return appInfo;
    }

    private Drawable getApplicationIcon(String packageName) {
        if (packageName == null) return null;
        Drawable icon = null;
        try {
            icon = mPackageManager.getApplicationIcon(packageName);
        } catch (NameNotFoundException exception) {
            Log.w(TAG, "getApplicationIcon " + packageName + " with exception ", exception);
        }
        return icon;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof NetworkAccessPreference) {
            NetworkAccessPreference accessPreference = (NetworkAccessPreference) preference;
            boolean accessible = newValue == Boolean.TRUE;
            Log.d(TAG, "accessPreference " + accessPreference + " accessible = " + accessible);
            String packageName = accessPreference.mAppConfig.mPackName;
            try {
                mPowerManagerEx.setAppNetworkMode(packageName, DEFAULT_UID, accessible ?
                        AppNetworkConfig.NETWORK_MODE_ENABLE_SA : AppNetworkConfig.NETWORK_MODE_DISABLE_SA);
            } catch (RemoteException remoteException) {
                Log.w(TAG,"setAppNetworkMode with exception :",remoteException);
            }

        }
        return true;
    }

    class NetworkAccessPreference extends AppSwitchPreference {
        private final AppNetworkConfig mAppConfig;

        public NetworkAccessPreference(final Context context, AppNetworkConfig appConfig) {
            super(context);
            setWidgetLayoutResource(R.layout.restricted_switch_widget);
            this.mAppConfig = appConfig;
        }

        public void setState() {
            AppNetworkConfig config = null;
            try {
                config = mPowerManagerEx.getAppNetworkConfig(mAppConfig.mPackName, DEFAULT_UID);
            } catch (RemoteException remoteException) {
                Log.w(TAG,"getAppNetworkConfig with exception :",remoteException);
            }
            if (config != null) {
                Log.d(TAG, "setState: enable5GAccess :" + config.mUse5G + " for " + config.mPackName);
                setChecked(config.mUse5G);
            }
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            holder.findViewById(R.id.restricted_icon).setVisibility(View.GONE);
        }

        public void reuse() {
            setState();
            notifyChanged();
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.sprd_fifth_generation_network_access_settings;
    }

}
