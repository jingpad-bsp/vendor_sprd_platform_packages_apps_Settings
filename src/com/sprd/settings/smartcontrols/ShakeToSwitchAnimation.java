package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.SHAKE_TO_SWITCH;
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
 * DialogFragment which show animation of ShakeToSwitch.
 */
public class ShakeToSwitchAnimation extends DialogFragment {
    private ImageView mShakeToSwitchDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "ShakeToSwitchAnimation";

    public static ShakeToSwitchAnimation newInstance(SmartSwitchPreference preference) {
        final ShakeToSwitchAnimation ShakeToSwitchDialog = new ShakeToSwitchAnimation();
        mPreference = preference;
        return ShakeToSwitchDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mShakeToSwitchAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.shake_to_switch, null);

        mShakeToSwitchDisplay = (ImageView) customView.findViewById(R.id.shake_to_switch_display);

        mShakeToSwitchDisplay.setImageResource(R.drawable.shake_to_switch_anim);
        mAnimationDrawable = (AnimationDrawable) mShakeToSwitchDisplay.getDrawable();
        mAnimationDrawable.start();

        mShakeToSwitchAnimationDialog.setView(customView);

        mShakeToSwitchAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), SHAKE_TO_SWITCH, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mShakeToSwitchAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
