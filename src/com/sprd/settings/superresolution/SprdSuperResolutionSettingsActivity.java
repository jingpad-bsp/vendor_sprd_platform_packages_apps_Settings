package com.sprd.settings.superresolution;

import android.content.res.Configuration;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.MenuItem;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SubSettings;


public class SprdSuperResolutionSettingsActivity extends SubSettings {
    private static final String TAG = "SprdSuperResolutionSettingsActivity";
    private static final String ACTION_SUPER_RESOLUTION_STATE = "sprd.action.super_resolution_state";
    private static final int SUPER_RESOLUTION_STATE_OFF = 0;
    private static final int SUPER_RESOLUTION_STATE_ON = 1;

    /* UNISOC: 1152917 finish activity when start super resolution in multi window mode@{ */
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (isInMultiWindowMode) {
            finish();
        }
    }
    /* @} */

    /* UNISOC: 1175284 do not response back acrrow when switching super resolution@{ */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int superResolutionState = Settings.System.getInt(getContentResolver(), ACTION_SUPER_RESOLUTION_STATE, SUPER_RESOLUTION_STATE_OFF);
        if (superResolutionState == SUPER_RESOLUTION_STATE_ON) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /* @} */

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, SprdSuperResolutionSettings.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (SprdSuperResolutionSettings.class.getName().equals(fragmentName)) return true;
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        finish();
    }
}
