
package com.sprd.settings.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.sprdpower.PowerManagerEx;
import android.os.sprdpower.IPowerManagerEx;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.PreferenceFragment;
import android.view.Gravity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.ToggleSwitch;
import com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener;
import com.android.settings.R;

public class ToggleAssistantFragment extends SettingsPreferenceFragment {

    private static final String TAG = "ToggleAssistantFragment";

    private static final String KEY_SHORTCUT_TOP = "key_shortcut_top";
    private static final String KEY_SHORTCUT_BOTTOM = "key_shortcut_bottom";
    private static final String KEY_SHORTCUT_LEFT = "key_shortcut_left";
    private static final String KEY_SHORTCUT_RIGHT = "key_shortcut_right";

    private static final String KEY_APP_TOP = "key_app_top";
    private static final String KEY_APP_BOTTOM = "key_app_bottom";
    private static final String KEY_APP_LEFT = "key_app_left";
    private static final String KEY_APP_RIGHT = "key_app_right";

    private ListPreference mShortcutTop;
    private ListPreference mShortcutBottom;
    private ListPreference mShortcutLeft;
    private ListPreference mShortcutRight;

    private ListPreference mAppTop;
    private ListPreference mAppBottom;
    private ListPreference mAppLeft;
    private ListPreference mAppRight;

    private PackageManager mPackagemanager;
    private List<ResolveInfo> mApps;
    private AssistantSecure mAssistantSecure;
    /*UNISOC: Modify for bug 977344@{*/
    private IPowerManagerEx mPowerManagerEx;
    /*}@*/

    private String[] mAppNames;
    private String[] mAppComponentInfo;

    private static final int TOUCH_ASSISTANT = 468;
    private static final boolean SUPPORT_SUPER_POWER_SAVE = (1 == android.os.SystemProperties.getInt("ro.sys.pwctl.ultrasaving",0));

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPackagemanager = this.getActivity().getPackageManager();

        mAssistantSecure = new AssistantSecure(this.getActivity());

        //installActionBarToggleSwitch();

        addPreferencesFromResource(R.xml.assistant_settings);

        mShortcutTop = (ListPreference) this.findPreference(KEY_SHORTCUT_TOP);
        mShortcutBottom = (ListPreference) this.findPreference(KEY_SHORTCUT_BOTTOM);
        mShortcutLeft = (ListPreference) this.findPreference(KEY_SHORTCUT_LEFT);
        mShortcutRight = (ListPreference) this.findPreference(KEY_SHORTCUT_RIGHT);

        mAppTop = (ListPreference) this.findPreference(KEY_APP_TOP);
        mAppBottom = (ListPreference) this.findPreference(KEY_APP_BOTTOM);
        mAppLeft = (ListPreference) this.findPreference(KEY_APP_LEFT);
        mAppRight = (ListPreference) this.findPreference(KEY_APP_RIGHT);

        mShortcutTop.setOnPreferenceChangeListener(mShortcutChangeListener);
        mShortcutBottom.setOnPreferenceChangeListener(mShortcutChangeListener);
        mShortcutLeft.setOnPreferenceChangeListener(mShortcutChangeListener);
        mShortcutRight.setOnPreferenceChangeListener(mShortcutChangeListener);

        mAppTop.setOnPreferenceChangeListener(mAppChangeListener);
        mAppBottom.setOnPreferenceChangeListener(mAppChangeListener);
        mAppLeft.setOnPreferenceChangeListener(mAppChangeListener);
        mAppRight.setOnPreferenceChangeListener(mAppChangeListener);

    }

    public void onResume() {
        super.onResume();
        installActionBarToggleSwitch();
        updateView();
    }

    public void onPause() {
        super.onPause();
    }

    private void updateView() {

        mShortcutTop.setValue(mAssistantSecure.getShortcutTopValue() + "");
        mShortcutBottom.setValue(mAssistantSecure.getShortcutBottomValue() + "");
        mShortcutLeft.setValue(mAssistantSecure.getShortcutLeftValue() + "");
        mShortcutRight.setValue(mAssistantSecure.getShortcutRightValue() + "");

        mShortcutTop.setTitle(mShortcutTop.getEntry());
        mShortcutBottom.setTitle(mShortcutBottom.getEntry());
        mShortcutLeft.setTitle(mShortcutLeft.getEntry());
        mShortcutRight.setTitle(mShortcutRight.getEntry());

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = mPackagemanager.queryIntentActivities(intent, 0);

        mAppNames = new String[mApps.size()];
        mAppComponentInfo = new String[mApps.size()];

        boolean isAppTopExist = false;
        boolean isAppBottomExist = false;
        boolean isAppLeftExist = false;
        boolean isAppRightExist = false;

        int appTopIndex = 0;
        int appBottomIndex = 0;
        int appLeftIndex = 0;
        int appRightIndex = 0;

        for (int i = 0; i < mApps.size(); i++) {

            mAppNames[i] = mApps.get(i).loadLabel(mPackagemanager).toString();
            mAppComponentInfo[i] = mApps.get(i).activityInfo.packageName + ":"
                    + mApps.get(i).activityInfo.name;

            if(mAssistantSecure.getAppLeftValue().equals(mAppComponentInfo[i])){
                    isAppLeftExist = true;
                    appLeftIndex = i;
               }
              if(mAssistantSecure.getAppTopValue().equals(mAppComponentInfo[i])){
                   isAppTopExist = true;
                   appTopIndex = i;
               }
              if(mAssistantSecure.getAppRightValue().equals(mAppComponentInfo[i])){
                   isAppRightExist = true;
                   appRightIndex = i;
               }
            if(mAssistantSecure.getAppBottomValue().equals(mAppComponentInfo[i])){
                   isAppBottomExist = true;
                   appBottomIndex = i;
               }

            Log.d(TAG, "app name:" + mAppNames[i] + " info:" + mAppComponentInfo[i]);

        }

        Log.d(TAG, "isAppTopExist:" + isAppTopExist + " isAppBottomExist:" + isAppBottomExist
                + " isAppLeftExist:" + isAppLeftExist + " isAppRightExist:" + isAppRightExist);

        /* SPRD: Bug 594274  use the default app if it doesn't exist @{ */
        /* SPRD: modify 20160621 Spreadtrum of 578212 :Assis default applications is same. @{*/
        if (!isAppLeftExist) {
            String leftApp = mAppComponentInfo[0];
            mAssistantSecure.setAppLeftValue(leftApp);
            appLeftIndex = getArrayIndex(mAppComponentInfo, leftApp);
        }
        if (!isAppTopExist) {
            String topApp = mAppComponentInfo[1];
            mAssistantSecure.setAppTopValue(topApp);
            appTopIndex = getArrayIndex(mAppComponentInfo, topApp);
        }
        if (!isAppRightExist) {
            String rightApp = mAppComponentInfo[2];
            mAssistantSecure.setAppRightValue(rightApp);
            appRightIndex = getArrayIndex(mAppComponentInfo, rightApp);
        }
        if (!isAppBottomExist) {
            String bottomApp = mAppComponentInfo[3];
            mAssistantSecure.setAppBottomValue(bottomApp);
            appBottomIndex = getArrayIndex(mAppComponentInfo, bottomApp);
        }
        /*@}*/
        /* @} */

        mAppTop.setEntries(mAppNames);
        mAppTop.setEntryValues(mAppComponentInfo);
        mAppBottom.setEntries(mAppNames);
        mAppBottom.setEntryValues(mAppComponentInfo);
        mAppLeft.setEntries(mAppNames);
        mAppLeft.setEntryValues(mAppComponentInfo);
        mAppRight.setEntries(mAppNames);
        mAppRight.setEntryValues(mAppComponentInfo);

        mAppTop.setValueIndex(appTopIndex);
        mAppBottom.setValueIndex(appBottomIndex);
        mAppLeft.setValueIndex(appLeftIndex);
        mAppRight.setValueIndex(appRightIndex);

        mAppTop.setTitle(mAppTop.getEntry());
        mAppBottom.setTitle(mAppBottom.getEntry());
        mAppLeft.setTitle(mAppLeft.getEntry());
        mAppRight.setTitle(mAppRight.getEntry());

    }

    private int getArrayIndex(CharSequence[] array, CharSequence ch) {
        for (int i = 0; i < array.length; i++) {
            if (ch.equals(array[i])) {
                return i;
            }
        }
        return 0;
    }

    private void installActionBarToggleSwitch() {
        final boolean enabled = mAssistantSecure.getAssistantStatus() != 0;
        SettingsActivity activity = (SettingsActivity) getActivity();
        SwitchBar switchBar = activity.getSwitchBar();
        switchBar.setCheckedInternal(enabled);
        ToggleSwitch toggleSwitch = switchBar.getSwitch();

        switchBar.show();

        toggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {

            @Override
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                toggleSwitch.setCheckedInternal(checked);
                mAssistantSecure.setAssistantStatus(checked ? 1 : 0);
                if (checked) {
                    if (SUPPORT_SUPER_POWER_SAVE && getPowerSaveMode() == PowerManagerEx.MODE_ULTRASAVING) {
                        getActivity().sendBroadcastAsUser(
                            new Intent("com.android.systemui.FLOATKEY_ACTION_STOP"), UserHandle.ALL);
                    } else {
                        getActivity().sendBroadcastAsUser(
                            new Intent("com.android.systemui.FLOATKEY_ACTION_START"), UserHandle.ALL);
                    }
                } else {
                    getActivity().sendBroadcastAsUser(
                            new Intent("com.android.systemui.FLOATKEY_ACTION_STOP"), UserHandle.ALL);
                }

                return false;
            }
        });
    }
    /*UNISOC: Modify for bug 977344@{*/
    private int getPowerSaveMode(){
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));
        try {
            int mode = mPowerManagerEx.getPowerSaveMode();
            return mode;
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    /* @} */
    private OnPreferenceChangeListener mShortcutChangeListener = new OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // TODO Auto-generated method stub
            /* UNISOC: Modify for bug 1160021 @{ */
            boolean ret = false;
            if (preference instanceof ListPreference) {
                ListPreference p = (ListPreference) preference;
                String value = (String)newValue;
                CharSequence[] entries = mShortcutTop.getEntries();
                if (preference == mShortcutTop) {
                    mShortcutTop.setTitle(entries[p.findIndexOfValue(value)]);
                    mAssistantSecure.setShortcutTopValue(Integer.parseInt(value));
                    ret = true;
                }
                if (preference == mShortcutBottom) {
                    mShortcutBottom.setTitle(entries[p.findIndexOfValue(value)]);
                    mAssistantSecure.setShortcutBottomValue(Integer.parseInt(value));
                    ret = true;
                }
                if (preference == mShortcutLeft) {
                    mShortcutLeft.setTitle(entries[p.findIndexOfValue(value)]);
                    mAssistantSecure.setShortcutLeftValue(Integer.parseInt(value));
                    ret = true;
                }
                if (preference == mShortcutRight) {
                    mShortcutRight.setTitle(entries[p.findIndexOfValue(value)]);
                    mAssistantSecure.setShortcutRightValue(Integer.parseInt(value));
                    ret = true;
                }
                if (ret && mAssistantSecure.getAssistantStatus() == 1) {
                    getActivity().sendBroadcastAsUser(
                            new Intent("com.android.systemui.FLOATKEY_ACTION_RESTART"), UserHandle.ALL);
                }
            }
            /* }@ */
            return ret;
        }
    };

    private OnPreferenceChangeListener mAppChangeListener = new OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // TODO Auto-generated method stub
            // CharSequence[] entries = mShortcutTop.getEntries();
            int index = getArrayIndex(mAppComponentInfo, (String) newValue);
            boolean ret = false;

            if (preference == mAppTop) {
                mAppTop.setTitle(mAppNames[index]);
                mAssistantSecure.setAppTopValue(newValue.toString().trim());
                ret = true;
            }
            if (preference == mAppBottom) {
                mAppBottom.setTitle(mAppNames[index]);
                mAssistantSecure.setAppBottomValue(newValue.toString().trim());
                ret = true;
            }
            if (preference == mAppLeft) {
                mAppLeft.setTitle(mAppNames[index]);
                mAssistantSecure.setAppLeftValue(newValue.toString().trim());
                ret = true;
            }
            if (preference == mAppRight) {
                mAppRight.setTitle(mAppNames[index]);
                mAssistantSecure.setAppRightValue(newValue.toString().trim());
                ret = true;
            }
            if (ret && mAssistantSecure.getAssistantStatus() == 1) {
                getActivity().sendBroadcastAsUser(
                        new Intent("com.android.systemui.FLOATKEY_ACTION_RESTART"), UserHandle.ALL);
            }
            return ret;
        }
    };

    public int getMetricsCategory() {
        return TOUCH_ASSISTANT;
    }
}
