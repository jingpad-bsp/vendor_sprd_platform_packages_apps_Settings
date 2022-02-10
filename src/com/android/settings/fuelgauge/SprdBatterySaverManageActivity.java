package com.android.settings.fuelgauge;;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.fragment.app.Fragment;

import java.util.List;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SubSettings;


public class SprdBatterySaverManageActivity extends SubSettings {
    private static final String TAG = "SprdBatterySaverManageActivity";

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof SprdBatterySaverManageFragment) {
                    SprdBatterySaverManageFragment sprdBatterySaverManageFragment = (SprdBatterySaverManageFragment)fragment;
                    sprdBatterySaverManageFragment.onWindowFocusChanged(hasFocus);
                }
            }
        }
    }
}
