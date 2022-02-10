package com.android.settings.display;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.PowerManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import android.app.ActionBar;
import android.view.MenuItem;


public class SprdColorTemperatureActivity extends Activity implements
    RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "SprdColorTemperatureActivity";

    //used for store contrast mode
    private static final String SPRD_DISPLAY_COLOR_TEMPERATURE_MODE =
            "sprd_display_color_temperature_mode";
    //used for back up contrast mode when night display mode is turned on
    private static final String SPRD_DISPLAY_COLOR_TEMPERATURE_MODE_BACKUP =
        "sprd_display_color_temperature_mode_backup";
    //used for store color value
    private static final String SPRD_DISPLAY_COLOR_TEMPERATURE_AUTO_MODE_VALUE =
            "sprd_display_color_temperature_auto_mode_value";
    //contrast mode
    private static final int COLOR_MODE_OFF = 0;
    private static final int COLOR_MODE_INTELLIGENT = 1;
    private static final int COLOR_MODE_ENHANCE = 2;
    private static final int COLOR_MODE_NORMAL = 3;
    //color value, just work on intelligent contrast mode
    private static final int COLOR_VALUE_NATURE = 0xFF000000;
    private static final int COLOR_VALUE_WARM = 0xFF000001;
    private static final int COLOR_VALUE_COLD = 0xFF000002;

    private SprdLabeledSeekBar mSeekBar;
    private RadioGroup mRadioGroup;
    private RadioButton mAutomaticButton;
    private RadioButton mIncreasesButton;
    private RadioButton mStandardButton;
    private TextView mWarmColorTextView;
    private TextView mNormalColorTextView;
    private TextView mCoolColorTextView;
    private TextView mSelectedTextView;
    private TextView mLastTextView;

    private int mColorValue;
    private int mLastColorValue;
    private int mContrastMode;
    private int mCheckButtonId;
    private int mProgress;

    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver= new SettingsObserver(mHandler);
    private int mCurrentUser = UserHandle.USER_NULL;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.sprd_color_temperature_ajusting);

        View decorView = getWindow().getDecorView();
        int option = decorView.getSystemUiVisibility();
        if (isInDarkMode()) {
            option &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        } else {
            option |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decorView.setSystemUiVisibility(option);

        mSeekBar = (SprdLabeledSeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setMax(2);
        mSeekBar.setOnSeekBarChangeListener(new onPreviewSeekBarChangeListener());

        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(this);
        mAutomaticButton = (RadioButton) findViewById(R.id.automatic_button);
        mIncreasesButton = (RadioButton) findViewById(R.id.increased_button);
        mStandardButton = (RadioButton) findViewById(R.id.standard_button);

        mWarmColorTextView = (TextView) findViewById(R.id.color_warm);
        mNormalColorTextView = (TextView) findViewById(R.id.color_normal);
        mCoolColorTextView = (TextView) findViewById(R.id.color_cool);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            getWindow().setAttributes(params);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().setAttributes(params);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            recreate();
        }
    }

    private boolean isInDarkMode() {
        PowerManager powerManager = getSystemService(PowerManager.class);
        UiModeManager uiModeManager = getSystemService(UiModeManager.class);
        int uiMode = uiModeManager.getNightMode();
        if (uiMode == UiModeManager.MODE_NIGHT_YES
                || powerManager.isPowerSaveMode()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettingsObserver.setListening(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContrastMode = getContrastMode();
        mColorValue = mLastColorValue = getColorValue();
        Log.d(TAG, "mContrastMode: " + mContrastMode + " mColorValue: " + mColorValue);
        // when night display mode is turned on, contrast mode is set to off mode and we cannot set contrast mode
        // we need to show the last selected mode from backup contrast mode
        if (mContrastMode == COLOR_MODE_OFF) {
            int contrastModeBackup = getContrastModeBackUp();
            mCheckButtonId = contrastModeToCheckedId(contrastModeBackup);
        } else {
            mCheckButtonId = contrastModeToCheckedId(mContrastMode);
        }
        mRadioGroup.check(mCheckButtonId);
        // when night display is turned on, color temperature should be disabled
        if (isNightDisplayActivated()) {
            mSeekBar.setEnabled(false);
            mAutomaticButton.setEnabled(false);
            mIncreasesButton.setEnabled(false);
            mStandardButton.setEnabled(false);
            //Add for bug 1465880: Toast cannot disappear immediately.
            Toast toast = Toast.makeText(SprdColorTemperatureActivity.this,
                    getString(R.string.color_temperature_disabled_toast), Toast.LENGTH_SHORT);
            ToastManager.setToast(toast);
            toast.show();
        } else {
            mSeekBar.setEnabled(true);
            mAutomaticButton.setEnabled(true);
            mIncreasesButton.setEnabled(true);
            mStandardButton.setEnabled(true);
        }

        setSelectedTextColor(mColorValue);
        if (mContrastMode != COLOR_MODE_INTELLIGENT) {
            setViewEnabled(false);
        }
        mProgress = colorValueToProgress(mColorValue);
        mSeekBar.setProgress(mProgress);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSettingsObserver.setListening(false);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.d(TAG, "onCheckedChanged checkedId = " + checkedId);
        switch (checkedId) {
            case R.id.automatic_button:
                mContrastMode = COLOR_MODE_INTELLIGENT;
                setViewEnabled(true);
                mSeekBar.setProgress(mProgress);
                break;
            case R.id.increased_button:
                mContrastMode = COLOR_MODE_ENHANCE;
                setViewEnabled(false);
                break;
            case R.id.standard_button:
                mContrastMode = COLOR_MODE_NORMAL;
                setViewEnabled(false);
                break;
            default:
                mContrastMode = COLOR_MODE_INTELLIGENT;
                setViewEnabled(true);
        }
        if (isNightDisplayActivated()) {
            mContrastMode = COLOR_MODE_OFF;
        }
        setContrastMode(mContrastMode);
    }

    private class onPreviewSeekBarChangeListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mProgress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mColorValue = progressToColorValue(mProgress);
            Log.d(TAG, "onStopTrackingTouch() mProgress:" + mProgress + " mColorValue:"+ mColorValue);
            switch (mLastColorValue) {
                case COLOR_VALUE_WARM:
                    mLastTextView = mWarmColorTextView;
                    break;
                case COLOR_VALUE_NATURE:
                    mLastTextView = mNormalColorTextView;
                    break;
                case COLOR_VALUE_COLD:
                    mLastTextView = mCoolColorTextView;
                    break;
                default:
                    mLastTextView = mNormalColorTextView;
            }
            final Resources res = getResources();
            if (mLastTextView != null && res != null && mLastColorValue != mColorValue) {
                mLastTextView.setTextColor(res.getColor(R.color.color_temperature_color));
                setSelectedTextColor(mColorValue);
            }
         }
    }

    /**
     * set seekbar and text view enable
     */
    private void setViewEnabled(boolean isIntelligent) {
        mSeekBar.setEnabled(isIntelligent);
        mWarmColorTextView.setEnabled(isIntelligent);
        mNormalColorTextView.setEnabled(isIntelligent);
        mCoolColorTextView.setEnabled(isIntelligent);
    }

    /**
     * highlight text view depending on selected color value
     */
    private void setSelectedTextColor(int value) {
        final Resources res = getResources();
        if (res == null) return;
        switch (value) {
            case COLOR_VALUE_WARM:
                mSelectedTextView = mWarmColorTextView;
                break;
            case COLOR_VALUE_NATURE:
                mSelectedTextView = mNormalColorTextView;
                break;
            case COLOR_VALUE_COLD:
                mSelectedTextView = mCoolColorTextView;
                break;
            default:
                mSelectedTextView = mNormalColorTextView;
        }
        if (mSelectedTextView != null) {
            mSelectedTextView.setTextColor(res.getColor(R.color.selected_color));
            mLastColorValue = mColorValue;
        }
        setColorValue(mColorValue);
    }

    /**
     * convert contrast mode to the selected button id
     */
    private int contrastModeToCheckedId(int mode) {
       int checkId;
        switch (mode) {
            case COLOR_MODE_INTELLIGENT:
                checkId = R.id.automatic_button;
                break;
            case COLOR_MODE_ENHANCE:
                checkId = R.id.increased_button;
                break;
            case COLOR_MODE_NORMAL:
                checkId = R.id.standard_button;
                break;
            default:
                checkId = R.id.automatic_button;
        }
        return checkId;
    }

    /**
     * convert seekbar progress to color value
     */
    private int progressToColorValue(int progress) {
        int colorValue;
        switch (progress) {
            case 0:
                colorValue = COLOR_VALUE_WARM;
                break;
            case 1:
                colorValue = COLOR_VALUE_NATURE;
                break;
            case 2:
                colorValue = COLOR_VALUE_COLD;
                break;
            default:
                colorValue = COLOR_VALUE_NATURE;
        }
        return colorValue;
    }

    /**
     * convert color value to seekbar progress
     */
    private int colorValueToProgress(int value) {
        int progress;
        switch (value) {
            case COLOR_VALUE_WARM:
                progress = 0;
                break;
            case COLOR_VALUE_NATURE:
                progress = 1;
                break;
            case COLOR_VALUE_COLD:
                progress = 2;
                break;
            default:
                progress = 1;
        }
        return progress;
    }

    /**
     * night display and color temperature are mutually exclusive.
     * when night display is turned on , color temperature cannot be set and show disenabled.
     * when night display is turned off, color temperature is set last selected mode and show enabled.
     */
    private final class SettingsObserver extends ContentObserver {
        private final Uri NIGHT_DISPLAY_ACTIVATED
                = Secure.getUriFor (Secure.NIGHT_DISPLAY_ACTIVATED);

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (NIGHT_DISPLAY_ACTIVATED.equals(uri)) {
                if (isNightDisplayActivated()) {
                    mSeekBar.setEnabled(false);
                    mAutomaticButton.setEnabled(false);
                    mIncreasesButton.setEnabled(false);
                    mStandardButton.setEnabled(false);
                    //Add for bug 1465880: Toast cannot disappear immediately.
                    Toast toast = Toast.makeText(SprdColorTemperatureActivity.this,
                            getString(R.string.color_temperature_disabled_toast), Toast.LENGTH_SHORT);
                    ToastManager.setToast(toast);
                    toast.show();
                } else {
                    mContrastMode = getContrastModeBackUp();
                    if (mContrastMode == COLOR_MODE_INTELLIGENT) {
                        mSeekBar.setEnabled(true);
                    }
                    mAutomaticButton.setEnabled(true);
                    mIncreasesButton.setEnabled(true);
                    mStandardButton.setEnabled(true);
                }
            }
        }

        public void setListening(boolean listening){
            final ContentResolver cr = getContentResolver();
            if (listening) {
                cr.registerContentObserver(NIGHT_DISPLAY_ACTIVATED, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }
    }

    private void setColorValue(int value) {
        ContentResolver resolver = getContentResolver();
        Settings.System.putInt(resolver, SPRD_DISPLAY_COLOR_TEMPERATURE_AUTO_MODE_VALUE, value);
    }

    private int getColorValue() {
        ContentResolver resolver = getContentResolver();
        return Settings.System.getInt(resolver, SPRD_DISPLAY_COLOR_TEMPERATURE_AUTO_MODE_VALUE,
                COLOR_VALUE_NATURE);
    }

    private void setContrastMode(int mode) {
        ContentResolver resolver = getContentResolver();
        Settings.System.putInt(resolver, SPRD_DISPLAY_COLOR_TEMPERATURE_MODE, mode);
    }

    private int getContrastMode() {
        ContentResolver resolver = getContentResolver();
        return Settings.System.getInt(resolver, SPRD_DISPLAY_COLOR_TEMPERATURE_MODE,
                COLOR_MODE_INTELLIGENT);
    }

    private int getContrastModeBackUp() {
        ContentResolver resolver = getContentResolver();
        return Settings.System.getInt(resolver, SPRD_DISPLAY_COLOR_TEMPERATURE_MODE_BACKUP,
                COLOR_MODE_INTELLIGENT);
    }

    private boolean isNightDisplayActivated() {
        ContentResolver resolver = getContentResolver();
        return Settings.Secure.getInt(resolver,
                Secure.NIGHT_DISPLAY_ACTIVATED, 0) == 1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
