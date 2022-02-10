package com.android.settings.display;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.util.List;

/**
 * LabeledSeekBar represent the option assigned with labeled.
 * It pretends to be a group of radio button for color value, in order to adjust the
 * color value on intelligent contrast mode
 */
public class SprdLabeledSeekBar extends SeekBar {

    /** Seek bar change listener set via public method. */
    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    /** Labels for discrete progress values. */
    //private String[] mLabels;

    public SprdLabeledSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.seekBarStyle);
    }

    public SprdLabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SprdLabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        super.setOnSeekBarChangeListener(mProxySeekBarListener);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    /* This method is not used
    public void setLabels(String[] labels) {
        mLabels = labels;
    }*/

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // The callback set in the constructor will proxy calls to this
        // listener.
        mOnSeekBarChangeListener = l;
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return super.dispatchHoverEvent(event);
    }

    private final OnSeekBarChangeListener mProxySeekBarListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
            }
        }
    };
}
