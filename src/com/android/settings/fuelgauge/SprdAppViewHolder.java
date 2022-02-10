package com.android.settings.fuelgauge;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState;

// View Holder used when displaying views
public class SprdAppViewHolder {
    public View rootView;
    public TextView appName;
    public ImageView appIcon;
    public TextView appState;
    public Switch app_switch;
    public TextView high_usage;
    public TextView high_usage_type;

    static public SprdAppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sprd_app_battery_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            SprdAppViewHolder holder = new SprdAppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(android.R.id.title);
            holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.high_usage = (TextView) convertView.findViewById(R.id.high_usage);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (SprdAppViewHolder)convertView.getTag();
        }
    }

    static public SprdAppViewHolder createForCloseApp(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sprd_preference_app, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            SprdAppViewHolder holder = new SprdAppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(android.R.id.title);
            holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.app_switch = (Switch) convertView.findViewById(R.id.app_switch);
            holder.appState = (TextView) convertView.findViewById(R.id.app_state);
            holder.high_usage = (TextView) convertView.findViewById(R.id.high_usage);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (SprdAppViewHolder)convertView.getTag();
        }
    }

    static public SprdAppViewHolder createForPowerIntensiveApps(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sprd_app_power_intensive_item, null);
            SprdAppViewHolder holder = new SprdAppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(android.R.id.title);
            holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.high_usage = (TextView) convertView.findViewById(R.id.high_usage);
            holder.high_usage_type = (TextView) convertView.findViewById(R.id.high_usage_type);
            convertView.setTag(holder);
            return holder;
        } else {
            return (SprdAppViewHolder)convertView.getTag();
        }
    }

}
