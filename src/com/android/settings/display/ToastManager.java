package com.android.settings.display;

import android.widget.Toast;
/**
 * Add for bug 1465880: Toast cannot disappear immediately
 */
public class ToastManager {

    private static Toast mToast = null;

    public static void setToast(Toast toast) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = toast;
    }
}