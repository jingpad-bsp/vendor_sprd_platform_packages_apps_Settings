package com.sprd.settings.timerpower;

import android.widget.Toast;

public class ToastManager {

    private static Toast mToast = null;

    public static void setToast(Toast toast) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = toast;
    }
}
