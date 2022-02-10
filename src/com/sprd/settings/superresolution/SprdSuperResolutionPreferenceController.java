package com.sprd.settings.superresolution;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

/**
 * Controller to super resolution power saving
 */
public class SprdSuperResolutionPreferenceController extends BasePreferenceController {
    private static final String KEY_SUPER_RESOLUTION = "super_resolution";
    private static final String ACTION_SUPER_RESOLUTION_STATE = "sprd.action.super_resolution_state";
    //notify to prohibit user behavior
    private static final int SUPER_RESOLUTION_STATE_ON = 1;
    //notify to end prohibiting user behavior and dropping frame
    private static final int SUPER_RESOLUTION_STATE_OFF = 0;
    private String TAG = "SprdSuperResolutionPreferenceController";
    private List<String[]> mResolutionMode = new ArrayList<>();
    private Preference mPreference;

    public SprdSuperResolutionPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        boolean isShow = mContext.getResources().getBoolean(R.bool.config_support_superResolution);
        return isSupportSuperResolution() && isShow &&
                isUserSupported()
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private boolean isSupportSuperResolution() {
        final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        try {
            mResolutionMode = wm.getResolutions();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException : cannot get resolution mode");
            return false;
        }
        if(mResolutionMode != null) {
            int num = mResolutionMode.size();
            Log.d(TAG, "resolution mode list num = " + num);
            return num < 2 ? false : true;
        }
        return false;
    }

    private boolean isUserSupported() {
        return UserHandle.myUserId() == UserHandle.USER_OWNER;
    }

    @Override
    public void updateState(Preference preference) {
        final Activity activity = (Activity) mContext;
        mPreference = preference;
        if (activity != null) {
            Log.d(TAG, " activity isInMultiWindowMode : " + activity.isInMultiWindowMode());
            if (activity.isInMultiWindowMode()) {
                setPreferenceEnabled(false);
            } else {
                setPreferenceEnabled(true);
            }
        }
        final String clazz = SprdSuperResolutionSettingsActivity.class.getName();
        preference.setOnPreferenceClickListener(target -> {
            final Context context = target.getContext();
            final UserManager userManager = UserManager.get(context);
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", clazz);
            context.startActivity(intent);
            return true;
        });
    }

    public boolean isSuperResolutionStateOn() {
        return  Settings.System.getInt(mContext.getContentResolver(), ACTION_SUPER_RESOLUTION_STATE, 0)
                == SUPER_RESOLUTION_STATE_ON;
    }

    public void setSuperResolutionStateOff() {
        boolean setValueStatus = Settings.System.putInt(mContext.getContentResolver(), ACTION_SUPER_RESOLUTION_STATE, SUPER_RESOLUTION_STATE_OFF);
        Log.d(TAG, "set ACTION_SUPER_RESOLUTION_STATE SUPER_RESOLUTION_STATE_OFF = " + setValueStatus);
    }

    public void setPreferenceEnabled(boolean enable) {
        if (mPreference != null) {
            mPreference.setEnabled(enable);
        }
    }
}
