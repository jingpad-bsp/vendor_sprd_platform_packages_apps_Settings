package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.MUTE_INCOMING_CALLS;
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
 * DialogFragment which show animation of MuteIncomingCalls.
 */
public class MuteIncomingCallsAnimation extends DialogFragment {
    private ImageView mMuteIncomingCallsDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "MuteIncomingCallsAnimation";

    public static MuteIncomingCallsAnimation newInstance(SmartSwitchPreference preference) {
        final MuteIncomingCallsAnimation MuteIncomingCallsDialog = new MuteIncomingCallsAnimation();
        mPreference = preference;
        return MuteIncomingCallsDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mMuteIncomingCallsAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.mute_incoming_calls, null);

        mMuteIncomingCallsDisplay = (ImageView) customView.findViewById(R.id.mute_incoming_calls_display);

        mMuteIncomingCallsDisplay.setImageResource(R.drawable.mute_incoming_calls_anim);
        mAnimationDrawable = (AnimationDrawable) mMuteIncomingCallsDisplay.getDrawable();
        mAnimationDrawable.start();

        mMuteIncomingCallsAnimationDialog.setView(customView);

        mMuteIncomingCallsAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), MUTE_INCOMING_CALLS, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mMuteIncomingCallsAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
