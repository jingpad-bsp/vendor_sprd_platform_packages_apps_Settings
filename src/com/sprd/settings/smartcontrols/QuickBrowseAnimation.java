package com.sprd.settings.smartcontrols;

import static android.provider.Settings.Global.QUICK_BROWSE;
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
 * DialogFragment which show animation of QuickBrowse.
 */
public class QuickBrowseAnimation extends DialogFragment {
    private ImageView mQuickBrowseDisplay;
    private AnimationDrawable mAnimationDrawable;
    private static SmartSwitchPreference mPreference;
    private static final String TAG = "QuickBrowseAnimation";

    public static QuickBrowseAnimation newInstance(SmartSwitchPreference preference) {
        final QuickBrowseAnimation QuickBrowseDialog = new QuickBrowseAnimation();
        mPreference = preference;
        return QuickBrowseDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mQuickBrowseAnimationDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.quick_browse, null);

        mQuickBrowseDisplay = (ImageView) customView.findViewById(R.id.quick_browse_display);

        mQuickBrowseDisplay.setImageResource(R.drawable.quick_browse_anim);
        mAnimationDrawable = (AnimationDrawable) mQuickBrowseDisplay.getDrawable();
        mAnimationDrawable.start();

        mQuickBrowseAnimationDialog.setView(customView);

        mQuickBrowseAnimationDialog.setPositiveButton(R.string.smart_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
                        if (mPreference != null) {
                            mPreference.setChecked(turnOn);
                            if (SmartMotionFragment.isSmartMotionEnabled(getActivity())) {
                                Settings.Global.putInt(getActivity().getContentResolver(), QUICK_BROWSE, turnOn ? 1 : 0);
                            }
                        }
                    }
                });

       return mQuickBrowseAnimationDialog.create();
    }

    public void onDismiss(DialogInterface dialog) {
        try {
            super.onDismiss(dialog);
        } catch (Exception e) {
            Log.w(TAG, "ignore a exception that was found when executed onDismiss,exception is:"+e.getMessage());
        }
    }
}
