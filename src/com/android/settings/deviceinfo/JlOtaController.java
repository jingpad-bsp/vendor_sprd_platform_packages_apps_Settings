package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

public class JlOtaController extends AbstractPreferenceController {
    public static final String KEY_JL_OTA_UPDATE = "JlOtaUpdate";

    public JlOtaController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {

        //mdm
        if(com.jingos.mdm.MdmPolicyIntercept.SystemUpdate_Intercept(mContext))
        {
            return false;
        }

        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        Preference rsup = new Preference(mContext);
        rsup.setTitle(R.string.jl_ota_update_title);
        rsup.setKey(KEY_JL_OTA_UPDATE);
        rsup.setOrder(3);
        rsup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent("com.jingling.action.ota");
                mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                return true;
            }
        });
        screen.addPreference(rsup);
        super.displayPreference(screen);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_JL_OTA_UPDATE;
    }
}
