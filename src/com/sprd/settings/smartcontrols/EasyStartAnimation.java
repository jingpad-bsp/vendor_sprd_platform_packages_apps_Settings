package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Secure.CAMERA_GESTURE_DISABLED;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.android.settings.R;
import com.android.settings.widget.SmartSwitchPreference;

/**
 * DialogFragment which show animation of EasyStart.
 */
public class EasyStartAnimation extends DialogFragment {
    private ImageView mEasyStartDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "EasyStartAnimation";

    public static EasyStartAnimation newInstance(SmartSwitchPreference preference) {
        final EasyStartAnimation EasyStartDialog = new EasyStartAnimation();
        mPreference = preference;
        return EasyStartDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mEasyStartAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.easy_start, null);

        mEasyStartDisplay = (ImageView) customView.findViewById(R.id.easy_start_display);

        mEasyStartDisplay.setImageResource(R.drawable.easy_start_anim);
        mAnimationDrawable = (AnimationDrawable) mEasyStartDisplay.getDrawable();
        mAnimationDrawable.start();

        mEasyStartAnimationDialog.setView(customView);

        mEasyStartAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Secure.putInt(getActivity().getContentResolver(), CAMERA_GESTURE_DISABLED, turnOn ? 0 : 1);
                            }
                        }
                    }
                });

       return mEasyStartAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
