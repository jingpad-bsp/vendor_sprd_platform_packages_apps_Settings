package com.android.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

public class SmartSwitchPreference extends SwitchPreference {

    private OnPreferenceSwitchChangeListener mOnSwitchChangeListener;
    private OnViewClickedListener mOnViewClickedListener;
    private final Listener mListener = new Listener();
    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }
            mOnSwitchChangeListener.onPreferenceSwitchChanged(isChecked);
            SmartSwitchPreference.this.setChecked(isChecked);
        }
    };

    public interface OnPreferenceSwitchChangeListener {
        public void onPreferenceSwitchChanged(boolean checked);
    }

    public interface OnViewClickedListener {
        public void OnViewClicked(View v);
    }

    public void setOnViewClickedListener(OnViewClickedListener listener) {
        mOnViewClickedListener = listener;
    }

    public void setOnPreferenceSwitchCheckedListener(OnPreferenceSwitchChangeListener listener) {
        mOnSwitchChangeListener = listener;
    }

    public SmartSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWidgetLayoutResource(R.layout.smart_switch_preference);
    }

    public SmartSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.smart_switch_preference);
    }

    public SmartSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.smart_switch_preference);
    }

    public SmartSwitchPreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.smart_switch_preference);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);

        View clickView = (View) view.itemView;
        clickView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mOnViewClickedListener.OnViewClicked(v);
            }
        });

        View switchView = (View) view.findViewById(R.id.prefrence_switch);
        syncSwitchView(switchView);
    }

    private void syncSwitchView(View view) {
        if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
            switchView.setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
            switchView.setOnCheckedChangeListener(mListener);
        }
    }
}
