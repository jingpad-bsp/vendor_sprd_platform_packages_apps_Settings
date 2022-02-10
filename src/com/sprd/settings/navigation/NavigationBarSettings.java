package com.sprd.settings.navigation;

import android.app.ActionBar;
import java.util.ArrayList;
import android.content.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.text.TextUtils;
import android.widget.Switch;
import android.widget.CompoundButton;
import androidx.preference.Preference;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.LayoutInflater;

import com.android.settings.SettingsActivity;
import com.android.settings.Settings.NavigationBarSettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.AppWidgetLoader.ItemConstructor;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import com.android.settings.Utils;
import com.android.settings.overlay.FeatureFactory;

//UNISOC: Add for bug 1122106, 1215321, 1224283
@SearchIndexable
public class NavigationBarSettings extends SettingsPreferenceFragment
            implements OnItemClickListener, Indexable{

    private static final String TAG = "NavigationBarSettings";
    /* UNISOC: Add for bug 1224283 @{ */
    private static final int CATE_ENUM = 1000;
    private View mContentView;
    /* }@ */
    private int mCurrentSelected;
    private String[] mNavigationBarModes = {"RIGHT", "LEFT","RIGHT_NOTI","LEFT_NOTI"};
    private ListView mNavigationBarModeListView;
    private CustomizedNavAdapter mAdapter;
    //UNISOC: Modify for bug 1215321
    private Switch mHideNotiBar;
    private int[] keyImage = { R.drawable.navigation_bar_style_layout1,
            R.drawable.navigation_bar_style_layout2,
            R.drawable.navigation_bar_style_layout3,
            R.drawable.navigation_bar_style_layout4};
    private int[] keyImage1 = { R.drawable.navigation_bar_style_rtl_layout1,
            R.drawable.navigation_bar_style_rtl_layout2,
            R.drawable.navigation_bar_style_rtl_layout3,
            R.drawable.navigation_bar_style_rtl_layout4};

    /* UNISOC: Modify for bug 1224283 @{ */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.hide_navigationbar_preference);
    }
    /* }@ */

    /* UNISOC: Add for bug 1224283 @{ */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.navigation_bar_settings, container, false);
        mNavigationBarModeListView = (ListView) mContentView.findViewById(R.id.softkey_list);
        mHideNotiBar = (Switch) mContentView.findViewById(R.id.hide_navigation_bar);
        int lastConfig =  Settings.System.getInt(getContentResolver(), "navigationbar_config", 0);
        mCurrentSelected = lastConfig & 0x0F;
        mHideNotiBar.setChecked((lastConfig & 0x10) != 0);
        mAdapter = new CustomizedNavAdapter(getActivity(), getListItems(), (isRTL() ? keyImage1 : keyImage));
        return mContentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavigationBarModeListView.setOnItemClickListener(this);
        mNavigationBarModeListView.setAdapter(mAdapter);
        setListViewHeightBasedOnChildren(mNavigationBarModeListView);
        mHideNotiBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int lastConfig = Settings.System.getInt(getContentResolver(), "navigationbar_config", 0);
                if (isChecked) {
                    Settings.System.putInt(getContentResolver(), "navigationbar_config", 0x10 | lastConfig);
                } else {
                    Settings.System.putInt(getContentResolver(), "navigationbar_config", 0x0F & lastConfig);
                }
                Log.d(TAG, "lastConfig = " + lastConfig + " :newConfig = " + Settings.System.getInt(getContentResolver(), "navigationbar_config", 0));
            }
        });
    }

    @Override
    public int getMetricsCategory() {
        return CATE_ENUM;
    }
    /* }@ */

    /*UNISOC: modify by bug938759 {@*/
    private boolean isRTL() {
        return (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL);
    }
    /* @} */

    private List<Map<String, Object>> getListItems() {
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        int index = 0;
        for (String t: mNavigationBarModes) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("text", t);
            if (index == mCurrentSelected) {
                map.put("checked", true);
            } else {
                map.put("checked", false);
            }
            index++;
            listItems.add(map);
        }
        return listItems;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        if (position == mNavigationBarModes.length) {
             return;
        }
        mCurrentSelected = position;
        mAdapter.setListItems(getListItems());
        mAdapter.notifyDataSetChanged();

        int lastConfig = Settings.System.getInt(getContentResolver(), "navigationbar_config", 0);
        Settings.System.putInt(getContentResolver(), "navigationbar_config", (0x10&lastConfig) + mCurrentSelected);
        int newConfig = Settings.System.getInt(getContentResolver(), "navigationbar_config", 0);

        Log.d(TAG, "Style...lastConfig: "+lastConfig + "; newConfig: "+newConfig+"; mCurrentSelected: "+mCurrentSelected);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /* UNISOC: Modify for bug 1111853 @{ */
    public static boolean hasNavigationBar(Context context) {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(context.getDisplayId());
        } catch (RemoteException ex) {
            Log.d(TAG, "RemoteException: "+ex);
        }
        return false;
    }
    /* }@ */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new SprdNavigationSearchIndexProvider();

    private static class SprdNavigationSearchIndexProvider extends BaseSearchIndexProvider {
        //UNISOC: Add for bug 1122106
        private static final String SEARCH_INDEX_KEY = "hide_navigation_bar";

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();
            /* UNISOC: Modify for bug 1111853,1122106 @{ */
            if (hasNavigationBar(context)
                    && context.getResources().getBoolean(com.android.internal.R.bool.config_support_dynamic_navigation_bar)
                    && (context.getResources().getInteger(com.android.internal.R.integer.config_navBarInteractionMode) == NAV_BAR_MODE_3BUTTON)) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                final String screenTitle = context.getResources().getString(R.string.navigation_bar_title);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.intentAction = Intent.ACTION_MAIN;
                data.intentTargetPackage = context.getPackageName();
                //UNISOC: Modify for bug 1224283
                data.intentTargetClass = NavigationBarSettingsActivity.class.getName();
                data.key = SEARCH_INDEX_KEY;
                result.add(data);
            }
            /* }@ */
            return result;
        }

        /* UNISOC: Add for bug 1122106 @{ */
        @Override
        public List<String> getNonIndexableKeys(Context context) {
            final List<String> keys = super.getNonIndexableKeys(context);
            if (!(hasNavigationBar(context)
                    && context.getResources().getBoolean(com.android.internal.R.bool.config_support_dynamic_navigation_bar)
                    && (context.getResources().getInteger(com.android.internal.R.integer.config_navBarInteractionMode) == NAV_BAR_MODE_3BUTTON))) {
                keys.add(SEARCH_INDEX_KEY);
            }
            return keys;
        }
        /* }@ */
    }
}
