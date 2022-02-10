package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.SMART_CALL_RECORDER;
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
 * DialogFragment which show animation of SmartCallRecorder.
 */
public class SmartCallRecorderAnimation extends DialogFragment {
    private ImageView mSmartCallRecorderDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "SmartCallRecorderAnimation";

    public static SmartCallRecorderAnimation newInstance(SmartSwitchPreference preference) {
        final SmartCallRecorderAnimation SmartCallRecorderDialog = new SmartCallRecorderAnimation();
        mPreference = preference;
        return SmartCallRecorderDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mSmartCallRecorderAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.smart_call_recorder, null);

        mSmartCallRecorderDisplay = (ImageView) customView.findViewById(R.id.smart_call_recorder_display);

        mSmartCallRecorderDisplay.setImageResource(R.drawable.smart_call_recorder_anim);
        mAnimationDrawable = (AnimationDrawable) mSmartCallRecorderDisplay.getDrawable();
        mAnimationDrawable.start();

        mSmartCallRecorderAnimationDialog.setView(customView);

        mSmartCallRecorderAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), SMART_CALL_RECORDER, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mSmartCallRecorderAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
