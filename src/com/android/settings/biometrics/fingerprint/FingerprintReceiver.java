package com.android.settings.biometrics.fingerprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;
import java.util.List;

public class FingerprintReceiver extends BroadcastReceiver {

    public static final String ACTION_SPRD_FINGER_BIOMANAGER = "com.sprd.fingerprint.startBIOManager";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("FingerprintReceiver"," onReceive---- intent = " + intent);

        if(ACTION_SPRD_FINGER_BIOMANAGER.equals(intent.getAction())){
            FingerprintManager fpm = (FingerprintManager) context.getSystemService(
                Context.FINGERPRINT_SERVICE);
            Intent activity_intent = new Intent();
            final List<Fingerprint> items = fpm.getEnrolledFingerprints();
            final int fingerprintCount = items != null ? items.size() : 0;
            final String clazz;
            if (fingerprintCount > 0) {
                clazz = FingerprintSettings.class.getName();
            } else {
                clazz = FingerprintEnrollIntroduction.class.getName();
            }
            activity_intent.setClassName("com.android.settings", clazz);
            activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(activity_intent);
        }

    }


}

