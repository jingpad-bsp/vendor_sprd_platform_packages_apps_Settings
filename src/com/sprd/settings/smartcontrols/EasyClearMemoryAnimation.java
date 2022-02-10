package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.EASY_CLEAR_MEMORY;
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
 * DialogFragment which show animation of EasyClearMemory.
 */
public class EasyClearMemoryAnimation extends DialogFragment {
    private ImageView mEasyClearMemoryDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "EasyClearMemoryAnimation";

    public static EasyClearMemoryAnimation newInstance(SmartSwitchPreference preference) {
        final EasyClearMemoryAnimation EasyClearMemoryDialog = new EasyClearMemoryAnimation();
        mPreference = preference;
        return EasyClearMemoryDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mEasyClearMemoryAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.easy_clear_memory, null);

        mEasyClearMemoryDisplay = (ImageView) customView.findViewById(R.id.easy_clear_memory_display);

        mEasyClearMemoryDisplay.setImageResource(R.drawable.easy_clear_memory_anim);
        mAnimationDrawable = (AnimationDrawable) mEasyClearMemoryDisplay.getDrawable();
        mAnimationDrawable.start();

        mEasyClearMemoryAnimationDialog.setView(customView);

        mEasyClearMemoryAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), EASY_CLEAR_MEMORY, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mEasyClearMemoryAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
