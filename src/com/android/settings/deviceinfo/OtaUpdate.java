package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.Utils;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.logging.MetricsLogger;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class OtaUpdate extends SettingsPreferenceFragment {
    private static final String LOG_TAG = "OtaUpdate";
    private static final String KEY_RECOVERY_SYSTEM_UPDATE = "RecoverySystemUpdate";
    private static final int MINIMUM_LEVEL_POWER = 35;
    private static OtaUpdate mInstance;

    private boolean mInternalUpdateFileExist = false;
    private boolean mSdUpdateFileExist = false;
    private int mUserChoice = 0;
    private Context mContext;
    private List<File> mUpdateItems = new ArrayList<File>();

    public static OtaUpdate getInstance() {
        if (mInstance == null) {
            mInstance = new OtaUpdate();
        }
        return mInstance;
    }

    public boolean isSupport(){
        return true;
    }

    public void initRecoverySystemUpdatePreference(Context context, PreferenceScreen preferenceScreen, Context appContext) {
        mContext = context;
        PreferenceScreen mParentScreen = preferenceScreen;
        Preference rsup = new Preference (mContext);
        rsup.setTitle(R.string.recovery_update_title);
        rsup.setKey(KEY_RECOVERY_SYSTEM_UPDATE);
        rsup.setOrder(2);
        rsup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference){
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.Alertdialog);
                builder.setMessage(mContext.getResources().getString(R.string.recovery_update_message));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        final String [] items = mContext.getResources().getStringArray(R.array.update_choice);
                        String storageState="";
                        String storageDirectory="";
                        //external sdcard
                        storageState = EnvironmentEx.getExternalStoragePathState();
                        storageDirectory = EnvironmentEx.getExternalStoragePath().getAbsolutePath();

                        //clear
                        mUpdateItems.clear();
                        mInternalUpdateFileExist = false;
                        mSdUpdateFileExist = false;

                        // UNISOC:1166404 Ota does not recognize the internal storage mount path
                        // “storage/emulated/0”, only the physical path"data/media/0" is recognized.
                        File internalFile = new File("data/media/0/update.zip");
                        if (internalFile.exists()) {
                            mInternalUpdateFileExist = true;
                            mUpdateItems.add(internalFile);
                        }

                        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                            File file = new File(storageDirectory + "/update.zip");
                            if (file.exists()) {
                                mSdUpdateFileExist = true;
                                mUpdateItems.add(file);
                            }
                        }

                        if (mUpdateItems.size() == 2) {
                            showSingleChoiceDialog(items);
                        } else if (mUpdateItems.size() == 1) {
                            String [] item_temp = new String[1];
                            if (mInternalUpdateFileExist) {
                                item_temp[0] = items[0];
                                showSingleChoiceDialog(item_temp);
                            } else if (mSdUpdateFileExist) {
                                item_temp[0] = items[1];
                                showSingleChoiceDialog(item_temp);
                            }
                        } else {
                            Toast.makeText(mContext, R.string.recovery_no_update_package, Toast.LENGTH_LONG).show();
                        }

                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
                return true;
            }

        });
        mParentScreen.addPreference(rsup);
    }

    private void showSingleChoiceDialog(String [] items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUserChoice = which;
            }
        }).setTitle(R.string.choice_dialog_title).setPositiveButton(R.string.update_choice_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mUserChoice >= 0) {
                    Toast.makeText(mContext, items[mUserChoice], Toast.LENGTH_SHORT).show();
                    tryToRecoverySystem(mUpdateItems.get(mUserChoice));
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    private void tryToRecoverySystem(File file) {
        Intent batteryBroadcast = mContext.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        final int batteryLevel = Utils.getBatteryLevel(batteryBroadcast);
        if (batteryLevel >= MINIMUM_LEVEL_POWER) {
            try {
                 RecoverySystem.installPackage(mContext, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
             Toast.makeText(mContext, R.string.recovery_update_level, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getMetricsCategory() {
        // TODO Auto-generated method stub
        return SettingsEnums.LOCAL_SYSTEM_UPDATE;
    }
}
