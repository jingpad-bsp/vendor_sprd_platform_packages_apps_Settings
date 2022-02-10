package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.EASY_ANSWER;
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
 * DialogFragment which show animation of EasyAnswer.
 */
public class EasyAnswerAnimation extends DialogFragment {
    private ImageView mEasyAnswerDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "EasyAnswerAnimation";

    public static EasyAnswerAnimation newInstance(SmartSwitchPreference preference) {
        final EasyAnswerAnimation EasyAnswerDialog = new EasyAnswerAnimation();
        mPreference = preference;
        return EasyAnswerDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mEasyAnswerAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.easy_answer, null);

        mEasyAnswerDisplay = (ImageView) customView.findViewById(R.id.easy_answer_display);

        mEasyAnswerDisplay.setImageResource(R.drawable.easy_answer_anim);
        mAnimationDrawable = (AnimationDrawable) mEasyAnswerDisplay.getDrawable();
        mAnimationDrawable.start();

        mEasyAnswerAnimationDialog.setView(customView);

        mEasyAnswerAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), EASY_ANSWER, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mEasyAnswerAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
