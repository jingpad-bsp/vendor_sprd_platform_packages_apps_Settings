package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.PLAY_CONTROL;
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
 * DialogFragment which show animation of PlayControl.
 */
public class PlayControlAnimation extends DialogFragment {
    private ImageView mPlayControlDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "PlayControlAnimation";

    public static PlayControlAnimation newInstance(SmartSwitchPreference preference) {
        final PlayControlAnimation PlayControlDialog = new PlayControlAnimation();
        mPreference = preference;
        return PlayControlDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mPlayControlAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.play_control, null);

        mPlayControlDisplay = (ImageView) customView.findViewById(R.id.play_control_display);

        mPlayControlDisplay.setImageResource(R.drawable.play_control_anim);
        mAnimationDrawable = (AnimationDrawable) mPlayControlDisplay.getDrawable();
        mAnimationDrawable.start();

        mPlayControlAnimationDialog.setView(customView);

        mPlayControlAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), PLAY_CONTROL, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mPlayControlAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
