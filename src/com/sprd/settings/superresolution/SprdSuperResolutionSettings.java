package com.sprd.settings.superresolution;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import android.os.SystemProperties;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.sprd.settings.superresolution.RadioButtonPreference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SprdSuperResolutionSettings extends SettingsPreferenceFragment {

    private static final String TAG = "SprdSuperResolutionSettings";
    private static final String ACTION_CHANGE_DISPLAY_CONFIG = "sprd.action.change_display_config";
    private static final String ACTION_SUPER_RESOLUTION_STATE = "sprd.action.super_resolution_state";
    private static final int SUPER_RESOLUTION_STATE_ON = 1;

    private Context mContext;
    private int mModeListNum;
    private PreferenceScreen mPreferenceScreen;
    private String mCurrentSelectedMode;
    private String mLastedSelectedMode;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static TelephonyManager mTelephonyManager;
    private static PhoneStateListener mPhoneStateListener;

    private List<String[]> mResolutionModeList = new ArrayList<>();

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_BATTERY_SAVER;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.sprd_super_resolution_settings);
        mContext = getActivity();
        mPreferenceScreen = getPreferenceScreen();
        mResolutionModeList = getResolutionMode();
        mModeListNum = getModeListNum();

        for (int num = 0; num < mModeListNum; num++) {
            String[] resolution = mResolutionModeList.get(num);
            if (null != resolution && resolution.length == 4) {
                String index = resolution[0];
                String width = resolution[1];
                String height = resolution[2];
                String name = resolution[3];
                Log.d(TAG,"resolution index = " + index + " width = " + width + " height = " + height + " name = " + name);
                RadioButtonPreference pref = new RadioButtonPreference(mContext);
                pref.setChecked(false);
                pref.setTitle(name);
                pref.setSummary(width + " × " + height);
                pref.setKey(index);
                mPreferenceScreen.addPreference(pref);
            }
        }
        /* bug 1181133 ：hide search menu @}*/
        final Bundle args = new Bundle();
        args.putBoolean(SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
        /* @} */
    }

    public void onStart() {
        super.onStart();
        //prohibit user switching resolution on the call
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener =  new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch(state){
                    case TelephonyManager.CALL_STATE_IDLE:
                        setPreferenceViewEnable(true);
                        Log.d(TAG, "PhoneStateListener CALL_STATE_IDLE set preference enable");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        setPreferenceViewEnable(false);
                        Log.d(TAG, "PhoneStateListener CALL_STATE_OFFHOOK set preference disable");
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        setPreferenceViewEnable(false);
                        Log.d(TAG, "PhoneStateListener CALL_STATE_RINGING set preference disable");
                        break;
                }
            }
        };
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void onResume() {
        super.onResume();
        refreshUI();
    }

    public void onStop() {
        super.onStop();
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void updateSelectedState(String mode) {
        mLastedSelectedMode = mCurrentSelectedMode;
        RadioButtonPreference pref = (RadioButtonPreference) findPreference(
                mLastedSelectedMode);
        if (pref != null) {
            pref.setChecked(false);
            Log.d(TAG,"lasted pref mode = " + pref.getKey());
        }
        mCurrentSelectedMode = mode;
        pref = (RadioButtonPreference) findPreference(mCurrentSelectedMode);
        if (pref != null) {
            pref.setChecked(true);
            Log.d(TAG,"current pref mode = " + pref.getKey());
        }
    }

    private void refreshUI() {
        int count = mPreferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = mPreferenceScreen.getPreference(i);
            if (pref != null && pref instanceof RadioButtonPreference) {
                RadioButtonPreference radioPref= (RadioButtonPreference)pref;
                radioPref.setChecked(false);
            }
        }
        mCurrentSelectedMode = getModeIndex();
        updateSelectedState(mCurrentSelectedMode);
    }

    /**
     * get resolution mode list
     * example ResolutionMode={"1","720","1440","HD+"};
     * ResolutionMode[0]:index
     * ResolutionMode[1]:width
     * ResolutionMode[2]:height
     * ResolutionMode[3]:name
     */
    private List<String[]> getResolutionMode() {
        final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        try {
            mResolutionModeList = wm.getResolutions();
            if (mResolutionModeList != null) {
                Collections.sort(mResolutionModeList, new  Comparator<String[]>() {
                    @Override
                    public int compare(String[] mode1, String[] mode2) {
                        return mode1[1].compareTo(mode2[1]);
                    }
                });
                return mResolutionModeList;
            }
        } catch (RemoteException e) {}
        return null;
    }

    private int getModeListNum () {
        if (null != mResolutionModeList) {
            return mResolutionModeList.size();
        }
        return 0;
    }

    private void setPreferenceViewEnable (boolean enabled) {
        int count = mPreferenceScreen.getPreferenceCount();
        for (int i=0; i<count; i++) {
            mPreferenceScreen.getPreference(i).setEnabled(enabled);
        }
    }

    private void setModeIndex(String key) {
        int index = Integer.valueOf(key);
        Settings.System.putInt(mContext.getContentResolver(), ACTION_CHANGE_DISPLAY_CONFIG, index);
    }

    private String getModeIndex() {
        int index = Settings.System.getInt(mContext.getContentResolver(), ACTION_CHANGE_DISPLAY_CONFIG, 0);
        return Integer.toString(index);
    }

     @Override
    public boolean onPreferenceTreeClick(Preference pref) {
        //If in monkey test, don't switch resolution mode.
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        final String key = pref.getKey();
        Log.d(TAG ,"onPreferenceTreeClick pref = " + pref + "   key = " + key);
        if (pref instanceof RadioButtonPreference) {
            updateSelectedState(key);
            if (!mCurrentSelectedMode.equals(mLastedSelectedMode)) {
                //during screen resolution changes, we should notify systemUI and WMS of prohibiting user behavior
                boolean setValueStatus = Settings.System.putInt(mContext.getContentResolver(), ACTION_SUPER_RESOLUTION_STATE, SUPER_RESOLUTION_STATE_ON);
                Log.d(TAG, "set ACTION_SUPER_RESOLUTION_STATE SUPER_RESOLUTION_STATE_ON = " + setValueStatus);
                //prohibit user switching resolution on the call
                setPreferenceViewEnable(false);
                killIncompatibledApps();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setModeIndex(key);
                    }
                }, 500);
            }
            return true;
        }
        return false;
     }

    /**
     * switch resolution, kill running third-party application except inputMethodApp ,installed liveWallpaperApp
     */
    private void killIncompatibledApps() {
        final List<ApplicationInfo> installed = mContext.getPackageManager().getInstalledApplications(0);
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        String[] incompatibledSystemAppArray = mContext.getResources().getStringArray(R.array.incompatibled_system_app);
        String liveWallpaperApp = getLiveWallpaperPackageName();
        final HashSet<String> incompatibledSystemAppHashSet = new HashSet<String>(Arrays.asList(incompatibledSystemAppArray));
        final InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        final List<InputMethodInfo> imis = imm.getInputMethodList();
        final List<String> inputMethodApp = new ArrayList<>();
        for (InputMethodInfo imi : imis) {
            inputMethodApp.add(imi.getPackageName());
        }

        if (installed == null || processes == null) {
            return;
        }
        for (ApplicationInfo app : installed) {
            if (app == null) {
                continue;
            }
            if (inputMethodApp.contains(app.processName)) {
                continue;
            }
            // bug 1116217 : don't kill installed liveWallpaperApp
            if (liveWallpaperApp != null && app.processName.equals(liveWallpaperApp)) {
                Log.d(TAG, "not kill liveWallpaperApp : " + liveWallpaperApp);
                continue;
            }
            if (!app.isSystemApp() ||
                    (incompatibledSystemAppHashSet != null &&
                    incompatibledSystemAppHashSet.contains(app.processName))) {
                for (RunningAppProcessInfo pro: processes) {
                    if (pro != null && app.processName.equals(pro.processName)) {
                        Log.d(TAG," kill app info : " + app.packageName);
                        am.forceStopPackage(app.packageName);
                    }
                }
            }
        }
    }

    //bug 1116217 : don't kill installed liveWallpaperApp
    private String getLiveWallpaperPackageName () {
        final WallpaperManager manager = (WallpaperManager) mContext.getSystemService(
                Context.WALLPAPER_SERVICE);
            if (manager == null) {
                Log.d(TAG, "WallpaperManager not available");
                return null;
            }
        WallpaperInfo info = manager.getWallpaperInfo();
        return info != null ? info.getPackageName() : null;
    }
}
