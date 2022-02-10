package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.EASY_DIAL;
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
 * DialogFragment which show animation of EasyDial.
 */
public class EasyDialAnimation extends DialogFragment {
    private ImageView mEasyDialDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "EasyDialAnimation";

    public static EasyDialAnimation newInstance(SmartSwitchPreference preference) {
        final EasyDialAnimation EasyDialDialog = new EasyDialAnimation();
        mPreference = preference;
        return EasyDialDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mEasyDialAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.easy_dial, null);

        mEasyDialDisplay = (ImageView) customView.findViewById(R.id.easy_dial_display);

        mEasyDialDisplay.setImageResource(R.drawable.easy_dial_anim);
        mAnimationDrawable = (AnimationDrawable) mEasyDialDisplay.getDrawable();
        mAnimationDrawable.start();

        mEasyDialAnimationDialog.setView(customView);

        mEasyDialAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), EASY_DIAL, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mEasyDialAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
