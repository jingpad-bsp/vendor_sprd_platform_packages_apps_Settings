package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.os.sprdpower.AppPowerSaveConfig;
import android.os.Bundle;
import android.os.BatteryStats;
import android.os.Environment;
import android.os.Handler;
import android.os.sprdpower.IPowerManagerEx;
import android.os.sprdpower.PowerManagerEx;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.os.UserHandle;
import android.os.PowerManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;


import android.preference.PreferenceFrameLayout;
import android.graphics.Color;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.applications.AppInfoBase;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStatePowerBridge;
import com.android.settings.applications.manageapplications.ResetAppsHelper;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.fuelgauge.AdvancedPowerUsageDetail;
import com.android.settings.fuelgauge.BatteryEntry;
import com.android.settings.fuelgauge.BatteryStatsHelperLoader;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.LoadingViewController;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.CompoundFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment used for showing application standby optimization, app auto launch and app secondary launch,
 * closing background apps after screen is locked, power intensive apps.
 */
public class SprdManageApplications extends SettingsPreferenceFragment
        implements OnItemClickListener {

    static final String TAG = "SprdManageApplications";
    static final boolean DEBUG = true;

    private static final String EXTRA_SORT_ORDER = "sortOrder";
    private static final String EXTRA_SHOW_SYSTEM = "showSystem";
    private static final String EXTRA_HAS_ENTRIES = "hasEntries";
    private static final String EXTRA_HAS_BRIDGE = "hasBridge";

    public static final String TYPE_APP_CONFIG = "type_app_config";
    private int mArgument;
    public static final int TYPE_APP_WAKEUP = 1000;
    public static final int TYPE_APP_SLEEP = 1001;
    public static final int TYPE_APP_NETWOK_DATA = 1002;
    public static final int TYPE_APP_AUTO_RUN = 1003;
    public static final int TYPE_APP_AS_LUNCH = 1004;
    public static final int TYPE_APP_CLOSE_LOCKED = 1005;
    public static final int TYPE_APP_POWER_INTENSIVE = 1006;

    public static final int SIZE_TOTAL = 0;
    public static final int SIZE_INTERNAL = 1;
    public static final int SIZE_EXTERNAL = 2;
    //auto launch app config value
    public static final int AUTO = 0;//app_switch:setchecked(true),this value is default
    public static final int OPTIMIZE = 1;//app_switch:setchecked(false)
    public static final int DO_NOT_OPTIMIZE = 2;//app_switch:setchecked(true)

    //close after lockscreen app config value
    public static final int DO_NOT_CLOSE = 0;//app_switch:setchecked(false)
    public static final int CLOSE = 1;//app_switch:setchecked(true)

    //the all switch state
    public static final int DEFAULT = 0;
    public static final int SWITCH_STATE_CLOSE = 1;
    public static final int SWITCH_STATE_OPEN = 2;

    // Filter options used for displayed list of applications
    // The order which they appear is the order they will show when spinner is present.
    public static final int FILTER_APPS_POWER_WHITELIST = 0;
    public static final int FILTER_APPS_POWER_WHITELIST_ALL = 1;
    public static final int FILTER_APPS_ALL = 2;
    public static final int FILTER_APPS_ENABLED = 3;

    //Filter options used for the type of power intensive apps
    public static final int POWER_CONSUMER_TYPE_ALARM = 0x01;
    public static final int POWER_CONSUMER_TYPE_WAKELOCK = 0x02;
    public static final int POWER_CONSUMER_TYPE_ALARM_WAKELOCK = 0x03;
    public static final int POWER_CONSUMER_TYPE_GPS = 0x04;
    public static final int POWER_CONSUMER_TYPE_ALARM_GPS = 0x05;
    public static final int POWER_CONSUMER_TYPE_WAKELOCK_GPS = 0x06;
    public static final int POWER_CONSUMER_TYPE_ALL = 0x07;

    private List<BatterySipper> mUsageList;

    private UiModeManager mUiModeManager;
    private PowerManager mPowerManager;


    // This is the actual mapping to filters from FILTER_ constants above, the order must
    // be kept in sync.
    public static final AppFilter[] FILTERS = new AppFilter[] {
            new CompoundFilter(AppStatePowerBridge.FILTER_POWER_WHITELISTED,
                    ApplicationsState.FILTER_ALL_ENABLED),     // High power whitelist, on
            new CompoundFilter(ApplicationsState.FILTER_WITHOUT_DISABLED_UNTIL_USED,
                    ApplicationsState.FILTER_ALL_ENABLED),     // Without disabled until used
            ApplicationsState.SPRD_FILTER_THIRD_PARTY,    //3rd party apps
            ApplicationsState.FILTER_EVERYTHING,  // All apps
            ApplicationsState.FILTER_ALL_ENABLED, // Enabled
    };

    // sort order
    private int mSortOrder = R.id.sort_order_alpha;

    // whether showing system apps.
    private boolean mShowSystem;

    private ApplicationsState mApplicationsState;

    public int mListType = 0;
    public int mFilter;
    private IPowerManagerEx mPowerManagerEx;

    public ApplicationsAdapter mApplications;

    private View mLoadingContainer;

    private View mListContainer;
    private View mRootView;
    private View mSwitchContainer;
    private View mSwitchButton;

    // ListView used to display list
    private ListView mListView;

    //function introduction about this feature
    private TextView mFeatureTitle;
    //the name of control all app button
    private TextView mAllAppTitle;

    // layout inflater object used to inflate views
    private LayoutInflater mInflater;
    private Switch mAllAppSwitch;
    private boolean mIsSelectAll = false;
    private int mSwitchState = DEFAULT;
    private int mAppConfigType;

    public static final int LIST_TYPE_MAIN = 0;

    private ResetAppsHelper mResetAppsHelper;

    public UserManager mUserManager;
    BatterySipper mSipper;
    BatteryStatsHelper mBatteryHelper;
    BatteryUtils mBatteryUtils;
    private String mBatteryPercent;
    private int muid;

    static final int LOADER_BATTERY = 1;

    public enum ConfigType {
        TYPE_NULL(-1),
        TYPE_OPTIMIZE(0),
        TYPE_ALARM(1),
        TYPE_WAKELOCK(2),
        TYPE_NETWORK(3),
        TYPE_AUTOLAUNCH(4),
        TYPE_SECONDARYLAUNCH(5),
        TYPE_LOCKSCREENCLEANUP(6),
        TYPE_POWERCONSUMERTYPE(7),
        TYPE_MAX(8);

        public final int value;
        private ConfigType(int value){
            this.value = value;
        }
    }

    private LoaderManager.LoaderCallbacks<BatteryStatsHelper> mBatteryCallbacks =
            new LoaderManager.LoaderCallbacks<BatteryStatsHelper>() {
                @Override
                public Loader<BatteryStatsHelper> onCreateLoader(int id, Bundle args) {
                    return new BatteryStatsHelperLoader(getContext());
                }

                @Override
                public void onLoadFinished(Loader<BatteryStatsHelper> loader,
                        BatteryStatsHelper batteryHelper) {
                    mBatteryHelper = batteryHelper;
                    if (mBatteryHelper != null) {
                        mUsageList = new ArrayList<>(mBatteryHelper.getUsageList());
                    }
                }

                @Override
                public void onLoaderReset(Loader<BatteryStatsHelper> loader) {
                }
     };

    private void startAdvancedPowerUsageDetail(String appName, int uid) {
        mSipper = findTargetSipper(uid);
        if (isBatteryStatsAvailable()) {
            updateBatteryPercent();
            BatteryEntry entry = new BatteryEntry(getContext(), null, mUserManager, mSipper);
            AdvancedPowerUsageDetail.startBatteryDetailPage((SettingsActivity) getActivity(),
                   this, mBatteryHelper, BatteryStats.STATS_SINCE_CHARGED, entry,
                    mBatteryPercent);
        } else {
            AdvancedPowerUsageDetail.startBatteryDetailPage((SettingsActivity) getActivity(),
                    this, appName);
        }
    }

    private boolean isBatteryStatsAvailable() {
        return mBatteryHelper != null && mSipper != null;
    }

    private BatterySipper findTargetSipper(int uid) {
        if (mUsageList != null) {
            for (int i = 0, size = mUsageList.size(); i < size; i++) {
                BatterySipper sipper = mUsageList.get(i);
                if (sipper.getUid() == uid) {
                    return sipper;
                }
            }
        }
        return null;
    }

    public void updateBatteryPercent() {
        if (isBatteryStatsAvailable()) {
            final int dischargeAmount = mBatteryHelper.getStats().getDischargeAmount(
                    BatteryStats.STATS_SINCE_CHARGED);
            final double hiddenAmount = mBatteryUtils.removeHiddenBatterySippers(mUsageList);
            final int percentOfMax = (int) mBatteryUtils.calculateBatteryPercent(
                    mSipper.totalPowerMah, mBatteryHelper.getTotalPower(), hiddenAmount,
                    dischargeAmount);
            mBatteryPercent = Utils.formatPercentage(percentOfMax);
         }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        Bundle args = getArguments();
        mArgument = args != null ? args.getInt(TYPE_APP_CONFIG) : TYPE_APP_CLOSE_LOCKED;
        mAppConfigType = getCurrentType();
        Log.d(TAG, " mArgument = " + mArgument + " mAppConfigType = " + mAppConfigType);
        mFilter = FILTER_APPS_ALL;
        if (savedInstanceState != null) {
            mSortOrder = savedInstanceState.getInt(EXTRA_SORT_ORDER, mSortOrder);
            mShowSystem = savedInstanceState.getBoolean(EXTRA_SHOW_SYSTEM, mShowSystem);
        }

        mResetAppsHelper = new ResetAppsHelper(getActivity());
        mPowerManagerEx = IPowerManagerEx.Stub.asInterface(ServiceManager.getService("power_ex"));

        final Activity activity = getActivity();
        //bug 1007951 : manageApplications in battery only supports screen orientation portrait.
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mBatteryUtils = BatteryUtils.getInstance(getContext());
        mUserManager = (UserManager) activity.getSystemService(Context.USER_SERVICE);

        //bug 1145153 : update feature title background with ui night mode and power save mode
        mUiModeManager = (UiModeManager) activity.getSystemService(UiModeManager.class);
        mPowerManager = (PowerManager) activity.getSystemService(PowerManager.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // initialize the inflater
        mInflater = inflater;

        mRootView = inflater.inflate(R.layout.sprd_manage_applications_apps, null);
        mLoadingContainer = mRootView.findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mListContainer = mRootView.findViewById(R.id.list_container);
        mSwitchContainer = mRootView.findViewById(R.id.switch_container);
        mSwitchButton = mRootView.findViewById(R.id.switch_button);
        mFeatureTitle = (TextView) mRootView.findViewById(R.id.feature_title);
        mAllAppTitle= (TextView) mRootView.findViewById(com.android.internal.R.id.title);
        mAllAppSwitch = (Switch) mRootView.findViewById(R.id.security_toggle_all);
        //bug 1145153 : update feature title background with ui night mode and power save mode
        updateFeatureTitleBackgoundColor();
        //the text of close app after lock screen and auto lunch is different
        if (mArgument == TYPE_APP_WAKEUP) {
            mFeatureTitle.setVisibility(View.GONE);
            mSwitchContainer.setVisibility(View.GONE);
            //mAllAppTitle.setVisibility(View.GONE);
            //mAllAppSwitch.setVisibility(View.GONE);
        } else if (mArgument == TYPE_APP_AUTO_RUN) {
            mFeatureTitle.setText(R.string.auto_launch_title);
            mAllAppTitle.setText(R.string.not_allow_all_app);
        } else if (mArgument == TYPE_APP_AS_LUNCH) {
            mFeatureTitle.setText(R.string.second_launch_title);
            mAllAppTitle.setText(R.string.not_allow_all_app);
        } else if (mArgument == TYPE_APP_POWER_INTENSIVE) {
            mFeatureTitle.setText(R.string.power_intensive_title);
            mSwitchContainer.setVisibility(View.GONE);
        }
        if (mSwitchButton != null) {
            mSwitchButton.setEnabled(false);
            mSwitchButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mAllAppSwitch != null) {
                        boolean isChecked = mAllAppSwitch.isChecked();
                        mIsSelectAll = !isChecked;
                        Log.d(TAG, "onClick() isChecked = " + isChecked
                                + " mIsSelectAll = " + mIsSelectAll);
                        mAllAppSwitch.setChecked(mIsSelectAll);
                        setAllAppState(mIsSelectAll);
                    }
                }
            });
        }
        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            ListView lv = (ListView) mListContainer.findViewById(android.R.id.list);
            if (emptyView != null) {
                lv.setEmptyView(emptyView);
            }
            lv.setOnItemClickListener(this);
            lv.setSaveEnabled(true);
            lv.setItemsCanFocus(true);
            lv.setTextFilterEnabled(true);
            mListView = lv;
            mApplications = new ApplicationsAdapter(mApplicationsState, this, mFilter);
            if (savedInstanceState != null) {
                mApplications.mHasReceivedLoadEntries =
                        savedInstanceState.getBoolean(EXTRA_HAS_ENTRIES, false);
                mApplications.mHasReceivedBridgeCallback =
                        savedInstanceState.getBoolean(EXTRA_HAS_BRIDGE, false);
            }
            mListView.setAdapter(mApplications);
            mListView.setRecyclerListener(mApplications);
            //SPRD: 819942 remove FastScrollBar in order not to affect the switch state.
            mListView.setVerticalScrollBarEnabled(false);

            Utils.prepareCustomPreferencesList(container, mRootView, mListView, false);
        }

        // We have to do this now because PreferenceFrameLayout looks at it
        // only when the view is added.
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams) mRootView.getLayoutParams()).removeBorders = true;
        }

        mResetAppsHelper.onRestoreInstanceState(savedInstanceState);

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /* bug 1145153 : update feature title background with ui night mode and power save mode @{ */
    private void updateFeatureTitleBackgoundColor() {
        int mode = mUiModeManager.getNightMode();
        if (mFeatureTitle != null) {
            if (mode == UiModeManager.MODE_NIGHT_YES || isPowerSaveMode()) {
                mFeatureTitle.setBackgroundColor(Color.parseColor("#8a000000"));
            } else {
                mFeatureTitle.setBackgroundColor(Color.parseColor("#E1E1E1"));
            }
        }
    }

    boolean isPowerSaveMode() {
        return mPowerManager.isPowerSaveMode();
    }
    /* @} */

    private boolean isFastScrollEnabled() {
        switch (mListType) {
            case LIST_TYPE_MAIN:
                return mSortOrder == R.id.sort_order_alpha;
            default:
                return false;
        }
    }

    @Override
    public int getMetricsCategory() {
        switch (mListType) {
            case LIST_TYPE_MAIN:
                return MetricsEvent.MANAGE_APPLICATIONS;
            default:
                return MetricsEvent.VIEW_UNKNOWN;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView();
        if (mApplications != null) {
            mApplications.resume(mSortOrder);
            mApplications.updateLoading();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_BATTERY, Bundle.EMPTY, mBatteryCallbacks);
    }

    @Override
    public void onPause() {
        super.onPause();
        getLoaderManager().destroyLoader(LOADER_BATTERY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mResetAppsHelper.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SORT_ORDER, mSortOrder);
        outState.putBoolean(EXTRA_SHOW_SYSTEM, mShowSystem);
        outState.putBoolean(EXTRA_HAS_ENTRIES, mApplications.mHasReceivedLoadEntries);
        outState.putBoolean(EXTRA_HAS_BRIDGE, mApplications.mHasReceivedBridgeCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mApplications != null) {
            mApplications.pause();
        }
        mResetAppsHelper.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mApplications != null) {
            mApplications.release();
        }
        mRootView = null;
    }

    /**
     * as ApplicationsAdapter will been used by app close after lock sceen and
     * app as-launch automatically, so use the type to distinguish the different UI.
     */
    public int getCurrentType() {
        switch (mArgument) {
            case TYPE_APP_AUTO_RUN:
                return ConfigType.TYPE_AUTOLAUNCH.value;
            case TYPE_APP_AS_LUNCH:
                return ConfigType.TYPE_SECONDARYLAUNCH.value;
            case TYPE_APP_CLOSE_LOCKED:
                return ConfigType.TYPE_LOCKSCREENCLEANUP.value;
            default:
                return ConfigType.TYPE_AUTOLAUNCH.value;
        }
    }

    public void setAllAppState(boolean flag) {
        if (DEBUG) Log.i(TAG, "closeAllChanged flag:" + flag);
        if (mIsSelectAll) {
            mSwitchState = SWITCH_STATE_OPEN;
        } else {
            mSwitchState = SWITCH_STATE_CLOSE;
        }
        int value = AUTO;
        try {
            if (mArgument == TYPE_APP_CLOSE_LOCKED) {
                value = flag ? CLOSE : DO_NOT_CLOSE;
            } else {
                value = flag ? DO_NOT_OPTIMIZE : OPTIMIZE;
            }
            mPowerManagerEx.setAppPowerSaveConfigListWithType(mApplications.mPkgList,
                    mAppConfigType, value);
        } catch (RemoteException e) {
            // Not much we can do here
        }
        mApplications.notifyDataSetChanged();
    }

    public void updateView() {
        final Activity host = getActivity();
        if (host != null) {
            host.invalidateOptionsMenu();
            Log.d(TAG, "updateView() mode = " + host.isInMultiWindowMode());
            if (host.isInMultiWindowMode()) {
                mFeatureTitle.setVisibility(View.GONE);
            } else {
                if (mArgument == TYPE_APP_WAKEUP) {
                    mFeatureTitle.setVisibility(View.GONE);
                } else {
                    mFeatureTitle.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.d(TAG, "onMultiWindowModeChanged() mode = " + isInMultiWindowMode);
        if (isInMultiWindowMode) {
            mFeatureTitle.setVisibility(View.GONE);
        } else {
            if (mArgument != TYPE_APP_WAKEUP) {
                mFeatureTitle.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Compare by appstate switch, then label.
     */
    public Comparator<AppEntry> APP_STATE_COMPARATOR
            = new Comparator<AppEntry>() {
        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            try {
                int app1Config = mPowerManagerEx.getAppPowerSaveConfigWithType(
                        object1.info.packageName, mAppConfigType);
                int app2Config = mPowerManagerEx.getAppPowerSaveConfigWithType(
                        object2.info.packageName, mAppConfigType);
                // Applications are sorted by check state, then app name
                if (app2Config == app1Config) {
                    return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
                } else {
                    return (app1Config - app2Config);
                }

            } catch (RemoteException e) {
                // Not much we can do here
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };

    private void startAppConfigFragment(Class<?> fragment, String pkg, String label) {
        Bundle args = new Bundle();
        args.putString(AppInfoBase.ARG_PACKAGE_NAME, pkg);
        new SubSettingLauncher(getActivity())
                .setDestination(fragment.getName())
                .setArguments(args)
                .setTitleText(label)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mApplications != null && mApplications.getCount() > position) {
            ApplicationsState.AppEntry entry = mApplications.getAppEntry(position);
            String pkg = entry.info.packageName;
            muid = entry.info.uid;
            //for app standy optimization
            if (mArgument == TYPE_APP_WAKEUP) {
                startAppConfigFragment(SprdAppItemBatterySaverFragment.class, pkg, entry.label);
                return;
            } else if (mArgument == TYPE_APP_POWER_INTENSIVE) {
                startAdvancedPowerUsageDetail(pkg, muid);
                return;
            }

            //for lock screen close-app and app auto-run
            SprdAppViewHolder holder = (SprdAppViewHolder) view.getTag();
            if (holder == null ) return;
            holder.app_switch.toggle();
            boolean isChecked = holder.app_switch.isChecked();
            Log.d(TAG, "isChecked: " + isChecked + " pkg: " + pkg);

            mSwitchState = DEFAULT;
            int configValue = CLOSE;
            if (mArgument == TYPE_APP_CLOSE_LOCKED) {
                configValue = isChecked ? CLOSE : DO_NOT_CLOSE;
            } else {
                configValue = isChecked ? DO_NOT_OPTIMIZE : OPTIMIZE;
            }
            try {
                mPowerManagerEx.setAppPowerSaveConfigWithType(pkg, mAppConfigType, configValue);
                mApplications.setAppStateText(isChecked, holder);
            } catch (RemoteException e) {
                // Not much we can do here
            }
            if (isChecked) {
                mApplications.setSwitchIfAllAppOpen();
            } else {
                if (mAllAppSwitch != null) mAllAppSwitch.setChecked(false);
            }
        }
    }

    /**
     * Custom adapter implementation for the ListView
     * This adapter maintains a map for each displayed application and its properties
     * An index value on each AppInfo object indicates the correct position or index
     * in the list. If the list gets updated dynamically when the user is viewing the list of
     * applications, we need to return the correct index of position. This is done by mapping
     * the getId methods via the package name into the internal maps and indices.
     * The order of applications in the list is mirrored in mAppLocalList
     */
    static class ApplicationsAdapter extends BaseAdapter implements
            ApplicationsState.Callbacks, AppStateBaseBridge.Callback,
            AbsListView.RecyclerListener {

        private final ApplicationsState mState;
        private final ApplicationsState.Session mSession;
        private final SprdManageApplications mManageApplications;
        private final Context mContext;
        private final ArrayList<View> mActive = new ArrayList<View>();
        private final AppStateBaseBridge mExtraInfoBridge = null;
        private final Handler mBgHandler;
        private final Handler mFgHandler;
        private int mFilterMode;
        private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
        private ArrayList<ApplicationsState.AppEntry> mEntries;
        private ArrayList<String> mPkgList;
        private boolean mResumed;
        private int mLastSortMode = -1;
        private boolean mHasReceivedLoadEntries;
        private boolean mHasReceivedBridgeCallback;

        public ApplicationsAdapter(ApplicationsState state,
                SprdManageApplications manageApplications, int filterMode) {
            mState = state;
            mFgHandler = new Handler();
            mBgHandler = new Handler(mState.getBackgroundLooper());
            mSession = state.newSession(this);
            mManageApplications = manageApplications;
            mContext = manageApplications.getActivity();
            mFilterMode = filterMode;
        }

        public void resume(int sort) {
            if (DEBUG) Log.i(TAG, "Resume!  mResumed=" + mResumed);
            if (!mResumed) {
                mResumed = true;
                mSession.onResume();
                mLastSortMode = sort;
                if (mExtraInfoBridge != null) {
                    mExtraInfoBridge.resume();
                }
                rebuild(false);
            } else {
                rebuild(sort);
            }
        }

        public void pause() {
            if (mResumed) {
                mResumed = false;
                mSession.onPause();
                if (mExtraInfoBridge != null) {
                    mExtraInfoBridge.pause();
                }
            }
        }

        public void release() {
            mSession.onDestroy();
            if (mExtraInfoBridge != null) {
                mExtraInfoBridge.release();
            }
        }

        public void rebuild(int sort) {
            if (sort == mLastSortMode) {
                return;
            }
            mLastSortMode = sort;
            rebuild(true);
        }

        public void rebuild(boolean eraseold) {
            if (!mHasReceivedLoadEntries
                    || (mExtraInfoBridge != null && !mHasReceivedBridgeCallback)) {
                // Don't rebuild the list until all the app entries are loaded.
                return;
            }
            if (DEBUG) Log.i(TAG, "Rebuilding app list...mFilterMode = " + mFilterMode
                    + " mShowSystem = " + mManageApplications.mShowSystem);
            ApplicationsState.AppFilter filterObj;
            Comparator<AppEntry> comparatorObj;
            filterObj = FILTERS[mFilterMode];

            comparatorObj = mManageApplications.APP_STATE_COMPARATOR;

            AppFilter finalFilterObj = filterObj;
            mBgHandler.post(() -> {
                final ArrayList<AppEntry> entries = mSession.rebuild(finalFilterObj,
                        comparatorObj, false);
                if (entries != null) {
                    mFgHandler.post(() -> onRebuildComplete(entries));
                }
            });
        }

        @Override
        public void onRebuildComplete(ArrayList<AppEntry> entries) {
            mBaseEntries = entries;
            mPkgList = new ArrayList<String>();
            if (mBaseEntries != null) {
                if (mManageApplications.mArgument == TYPE_APP_POWER_INTENSIVE) {
                    mEntries = powerIntensiveFilter(mBaseEntries);
                } else {
                    mEntries = applyPrefixFilter(mBaseEntries);
                }
            } else {
                mEntries = null;
            }
            mManageApplications.mSwitchButton.setEnabled(true);
            notifyDataSetChanged();

            if (mSession.getAllApps().size() != 0
                    && mManageApplications.mListContainer.getVisibility() != View.VISIBLE) {
                LoadingViewController.handleLoadingContainer(mManageApplications.mLoadingContainer,
                        mManageApplications.mListContainer, true, true);
            }
        }

        private void updateLoading() {
            LoadingViewController.handleLoadingContainer(mManageApplications.mLoadingContainer,
                    mManageApplications.mListContainer,
                    mHasReceivedLoadEntries && mSession.getAllApps().size() != 0, false);
        }

        ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(
                ArrayList<ApplicationsState.AppEntry> origEntries) {
            ArrayList<ApplicationsState.AppEntry> newEntries
                    = new ArrayList<ApplicationsState.AppEntry>();
            int appPowerConfig = 0;
            final int currnetAppType = mManageApplications.mAppConfigType;
            boolean isSelected = true;
            for (int i = 0; i < origEntries.size(); i++) {
                ApplicationsState.AppEntry entry = origEntries.get(i);
                boolean isOwerApp = UserHandle.getUserId(entry.info.uid) == UserHandle.USER_OWNER;
                // true if the application is currently installed for the calling user
                if ((entry.info.flags & ApplicationInfo.FLAG_INSTALLED) != 0 && isOwerApp) {
                    newEntries.add(entry);
                    if (mManageApplications.mArgument == TYPE_APP_WAKEUP
                        || mManageApplications.mArgument == TYPE_APP_POWER_INTENSIVE) {
                        continue;
                    }
                    mPkgList.add(entry.info.packageName);
                    //if one app is closed, mAllAppSwitch will close
                    if (isSelected) {
                        try {
                            appPowerConfig = mManageApplications.mPowerManagerEx.
                                    getAppPowerSaveConfigWithType(entry.info.packageName, currnetAppType);
                        } catch (RemoteException e) {
                            // Not much we can do here
                        }
                        if (mManageApplications.mArgument == TYPE_APP_CLOSE_LOCKED) {
                            isSelected &= appPowerConfig == CLOSE;
                        } else {
                            isSelected &= appPowerConfig == DO_NOT_OPTIMIZE;
                        }
                    }
                    Log.d(TAG,"pkg = " + entry.info + " isOwerApp = " + isOwerApp + " isSelected = " + isSelected);
                }
            }
            if (mManageApplications.mAllAppSwitch != null) {
                mManageApplications.mAllAppSwitch.setChecked(isSelected);
            }
            return newEntries;
        }

         ArrayList<ApplicationsState.AppEntry> powerIntensiveFilter(
                ArrayList<ApplicationsState.AppEntry> origEntries) {
            ArrayList<ApplicationsState.AppEntry> newEntries
                    = new ArrayList<ApplicationsState.AppEntry>();

            for (int i = 0; i < origEntries.size(); i++) {
                ApplicationsState.AppEntry entry = origEntries.get(i);
                boolean isOwerApp = UserHandle.getUserId(entry.info.uid) == UserHandle.USER_OWNER;
                int appPowerComsumerType = -1;
                try {
                    appPowerComsumerType = mManageApplications.mPowerManagerEx.
                            getAppPowerSaveConfigWithType(entry.info.packageName,
                                    ConfigType.TYPE_POWERCONSUMERTYPE.value);
                } catch (RemoteException e) {
                    // Not much we can do here
                }

                if ((entry.info.flags & ApplicationInfo.FLAG_INSTALLED) != 0 && isOwerApp && appPowerComsumerType >0) {
                    newEntries.add(entry);
                }
            }
            return newEntries;
        }

        public void setSwitchIfAllAppOpen() {
            boolean isSelected = true;
            for (int i = 0; i < mEntries.size(); i++) {
                ApplicationsState.AppEntry entry = mEntries.get(i);
                int appPowerConfig = 0;
                final int currnetAppType = mManageApplications.mAppConfigType;
                try {
                    appPowerConfig = mManageApplications.mPowerManagerEx.
                            getAppPowerSaveConfigWithType(entry.info.packageName, currnetAppType);
                } catch (RemoteException e) {
                    // Not much we can do here
                }
                if (DEBUG) Log.i(TAG, "appPowerConfig = " + appPowerConfig);
                if (mManageApplications.mArgument == TYPE_APP_CLOSE_LOCKED) {
                    isSelected &= appPowerConfig == CLOSE;
                } else {
                    isSelected &= appPowerConfig == DO_NOT_OPTIMIZE;
                }
                //if one app is closed, the switch will close, so not need to inquire others app
                if (!isSelected) {
                    break;
                }
            }
            if (mManageApplications.mAllAppSwitch != null) {
                mManageApplications.mAllAppSwitch.setChecked(isSelected);
            }
        }

        @Override
        public void onExtraInfoUpdated() {
            mHasReceivedBridgeCallback = true;
            rebuild(false);
        }

        @Override
        public void onRunningStateChanged(boolean running) {
            mManageApplications.getActivity().setProgressBarIndeterminateVisibility(running);
        }

        @Override
        public void onPackageListChanged() {
            Log.d(TAG,"onPackageListChanged()");
            mManageApplications.mSwitchState = DEFAULT;
            rebuild(false);
        }

        @Override
        public void onPackageIconChanged() {
            // We ensure icons are loaded when their item is displayed, so
            // don't care about icons loaded in the background.
        }

        @Override
        public void onLoadEntriesCompleted() {
            mHasReceivedLoadEntries = true;
            // We may have been skipping rebuilds until this came in, trigger one now.
            rebuild(false);
        }

        @Override
        public void onPackageSizeChanged(String packageName) {
           // Do nothing
        }

        @Override
        public void onLauncherInfoChanged() {
            if (!mManageApplications.mShowSystem) {
                rebuild(false);
            }
        }

        @Override
        public void onAllSizesComputed() {
            if (mLastSortMode == R.id.sort_order_size) {
                rebuild(false);
            }
        }

        public int getCount() {
            int count = 0;
            if (mEntries != null) {
                count = mEntries.size();
            }
            if (mManageApplications.mArgument == TYPE_APP_WAKEUP
                    || mManageApplications.mArgument == TYPE_APP_POWER_INTENSIVE) {
                return count;
            }
            /* bug 1182415 the disabled switch should not be opened  @{ */
            if (mManageApplications.mAllAppTitle != null
                    && mManageApplications.mSwitchContainer != null) {
                if (count > 1) {
                    mManageApplications.mAllAppTitle.setVisibility(View.VISIBLE);
                    mManageApplications.mSwitchContainer.setVisibility(View.VISIBLE);
                } else {
                    mManageApplications.mAllAppTitle.setVisibility(View.GONE);
                    mManageApplications.mSwitchContainer.setVisibility(View.GONE);
                }
            }
            /* @} */
            return count;
        }

        public Object getItem(int position) {
            return mEntries.get(position);
        }

        public ApplicationsState.AppEntry getAppEntry(int position) {
            return mEntries.get(position);
        }

        public long getItemId(int position) {
            return mEntries.get(position).id;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unnecessary calls
            // to findViewById() on each row.
            SprdAppViewHolder holder;
            if (mManageApplications.mArgument == TYPE_APP_WAKEUP) {
                holder = SprdAppViewHolder.createOrRecycle(
                    mManageApplications.mInflater, convertView);
            } else if (mManageApplications.mArgument == TYPE_APP_POWER_INTENSIVE) {
                holder = SprdAppViewHolder.createForPowerIntensiveApps(
                    mManageApplications.mInflater, convertView);
            } else {
                holder = SprdAppViewHolder.createForCloseApp(
                    mManageApplications.mInflater, convertView);
            }
            convertView = holder.rootView;

            // Bind the data efficiently with the holder
            ApplicationsState.AppEntry entry = mEntries.get(position);
            if (mManageApplications.mAllAppSwitch != null) {
                mManageApplications.mAllAppSwitch.setEnabled(true);
            }
            if (DEBUG) Log.d(TAG,"mSwitchState = " + mManageApplications.mSwitchState);
            synchronized (entry) {
                if (entry.label != null) {
                    holder.appName.setText(entry.label);
                }
                mState.ensureIcon(entry);
                if (entry.icon != null) {
                    holder.appIcon.setImageDrawable(entry.icon);
                }

                final int currnetAppType = mManageApplications.mAppConfigType;
                int appPowerComsumerType = -1;
                try {
                    appPowerComsumerType = mManageApplications.mPowerManagerEx.
                            getAppPowerSaveConfigWithType(entry.info.packageName,
                                    ConfigType.TYPE_POWERCONSUMERTYPE.value);
                    if (appPowerComsumerType > 0) {
                        holder.high_usage.setVisibility(View.VISIBLE);
                        if (mManageApplications.mArgument == TYPE_APP_POWER_INTENSIVE) {
                            switch (appPowerComsumerType) {
                                case POWER_CONSUMER_TYPE_ALARM:
                                case POWER_CONSUMER_TYPE_ALARM_WAKELOCK:
                                case POWER_CONSUMER_TYPE_ALARM_GPS:
                                case POWER_CONSUMER_TYPE_ALL:
                                    holder.high_usage_type.setText(R.string.power_consumer_type_alarm);
                                    break;
                                case POWER_CONSUMER_TYPE_WAKELOCK:
                                case POWER_CONSUMER_TYPE_WAKELOCK_GPS:
                                    holder.high_usage_type.setText(R.string.power_consumer_type_wakelock);
                                    break;
                                case POWER_CONSUMER_TYPE_GPS:
                                    holder.high_usage_type.setText(R.string.power_consumer_type_wakelock);
                                    break;
                                default:
                                    break;
                             }
                        }
                    } else {
                        holder.high_usage.setVisibility(View.GONE);
                    }
                } catch (RemoteException e) {
                    // Not much we can do here
                }

                //app standy optimization just need show list all 3rd apps.
                if (mManageApplications.mArgument == TYPE_APP_WAKEUP
                    || mManageApplications.mArgument ==TYPE_APP_POWER_INTENSIVE ) {
                    return convertView;
                }

                if (mManageApplications.mSwitchState == SWITCH_STATE_OPEN) {
                    holder.app_switch.setChecked(true);
                    //holder.app_switch.setEnabled(false);
                    if (mManageApplications.mArgument == TYPE_APP_CLOSE_LOCKED) {
                        holder.appState.setText(R.string.close);
                    } else {
                        holder.appState.setText(R.string.app_allow);
                    }
                } else if (mManageApplications.mSwitchState == SWITCH_STATE_CLOSE) {
                    holder.app_switch.setChecked(false);
                    //holder.app_switch.setEnabled(false);
                    if (mManageApplications.mArgument == TYPE_APP_CLOSE_LOCKED) {
                        holder.appState.setText(R.string.do_not_close);
                    } else {
                        holder.appState.setText(R.string.app_not_allow);
                    }
                } else if (mManageApplications.mSwitchState == DEFAULT) {
                    int appPowerConfig = -1;
                    try {
                        appPowerConfig = mManageApplications.mPowerManagerEx.
                                getAppPowerSaveConfigWithType(entry.info.packageName, currnetAppType);
                    } catch (RemoteException e) {
                        // Not much we can do here
                    }
                    if (mManageApplications.mArgument == TYPE_APP_CLOSE_LOCKED) {
                        holder.app_switch.setChecked(appPowerConfig == CLOSE);
                        setAppStateText(appPowerConfig == CLOSE, holder);
                    } else {
                        holder.app_switch.setChecked(appPowerConfig == DO_NOT_OPTIMIZE);
                        setAppStateText(appPowerConfig == DO_NOT_OPTIMIZE, holder);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "position: " + position + " currnetAppType: " + currnetAppType
                                + " appPowerConfig: " + appPowerConfig + " appPowerComsumerType:"
                                + appPowerComsumerType + " pkg: " + entry.info.packageName);
                    }
                }
            }
            mActive.remove(convertView);
            mActive.add(convertView);
            return convertView;
        }

        @Override
        public void onMovedToScrapHeap(View view) {
            mActive.remove(view);
        }

        public void setAppStateText(boolean isChecked, SprdAppViewHolder holder) {
            if (mManageApplications.mArgument == TYPE_APP_CLOSE_LOCKED) {
                if (isChecked) {
                    holder.appState.setText(R.string.close);
                } else {
                    holder.appState.setText(R.string.do_not_close);
                }
            } else {
                if (isChecked) {
                    holder.appState.setText(R.string.app_allow);
                } else {
                    holder.appState.setText(R.string.app_not_allow);
                }
            }
        }
    }
}
