package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Fragment used for showing app auto launch and app secondary launch
 */
public class SprdAppAutoRunFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener {

    public static final String TAG = "SprdAppAutoRun";
    public static final String ARGUMENT = "argument";

    private static final String KEY_APP_AUTO_RUN = "app_auto_run";
    private static final String KEY_APP_AS_LUNCH = "app_as_lunch";
    private Preference mAppAutoRunPref;
    private Preference mAppAsLunchPref;

    public static final String APPLICATION_LIST_TYPE = "app_list_type";
    public static final int TAB_INDICATOR_APP_BATTERY_SAVER = 1;
    public static final int LIST_TYPE_APP_BATTERY_SAVER = 2;
    public static final int TAB_INDICATOR_APP_AUTO_RUN = 3;
    public static final int LIST_TYPE_APP_AUTO_RUN= 4;

    public static final String TYPE_APP_CONFIG = "type_app_config";
    public static final int TYPE_APP_WAKEUP = 1000;
    public static final int TYPE_APP_SLEEP = 1001;
    public static final int TYPE_APP_NETWOK_DATA = 1002;
    public static final int TYPE_APP_AUTO_RUN = 1003;
    public static final int TYPE_APP_AS_LUNCH = 1004;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sprd_app_auto_run_fragment);
        mAppAutoRunPref = (Preference) findPreference(KEY_APP_AUTO_RUN);
        mAppAutoRunPref.setOnPreferenceClickListener(this);

        mAppAsLunchPref = (Preference) findPreference(KEY_APP_AS_LUNCH);
        mAppAsLunchPref.setOnPreferenceClickListener(this);
    }

    public static SprdAppAutoRunFragment newInstance() {
        SprdAppAutoRunFragment fragment = new SprdAppAutoRunFragment();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        int resID;
        int appConfigType;
            switch (key) {
                case KEY_APP_AUTO_RUN:
                    resID = R.string.app_auto_run_optimization;
                    appConfigType = TYPE_APP_AUTO_RUN;
                    break;
                case KEY_APP_AS_LUNCH:
                    resID = R.string.app_as_lunch_optimization;
                    appConfigType = TYPE_APP_AS_LUNCH;
                    break;
                default:
                    resID = R.string.app_auto_run_optimization;
                    appConfigType = TYPE_APP_AUTO_RUN;
            }
        Log.d(TAG," onPreferenceClick key:" + key);
        Bundle args = new Bundle();
        args.putInt(APPLICATION_LIST_TYPE, LIST_TYPE_APP_AUTO_RUN);
        args.putInt(TYPE_APP_CONFIG, appConfigType);
        new SubSettingLauncher(getActivity())
                .setDestination(SprdManageApplications.class.getName())
                .setArguments(args)
                .setTitleRes(resID)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
        return true;
    }
}
