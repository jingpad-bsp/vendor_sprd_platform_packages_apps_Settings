package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.text.TextUtils;
import android.util.Log;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.RemoteException;
import androidx.preference.Preference;
import java.util.NoSuchElementException;

import com.android.settings.deviceinfo.SupportCPVersion;
import com.android.settings.R;

import vendor.sprd.hardware.connmgr.V1_0.IConnmgrCallback;
import vendor.sprd.hardware.connmgr.V1_0.IConnmgr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get CP2 version info
 */
public class SupportCPVersion {

    static SupportCPVersion mInstance;
    private String mBasebandVersion;
    private static final int MSG_UPDATE_BASED_VERSION_SUMMARY = 1;
    private static final String LOG_TAG = "SupportCPVersion";

    private static final String WCND_INFO_COMMAND = "wcn at+spatgetcp2info ";
    private static final String WCND_POWERON_COMMAND ="wcn poweron ";
    private static final String WCND_POWEROFF_COMMAND = "wcn poweroff ";

    private static int lengthOfCp2;

    /* use HIDL to get the cp2 info*/
    private IConnmgr mIConnmgr;

    public static SupportCPVersion getInstance() {
        if (mInstance == null) {
            mInstance = new SupportCPVersion();
        }
        return mInstance;
    }

    public String getBasedSummary(Context context, String property) {
        try {
            String pro = SystemProperties.get(property,
                    context.getResources().getString(R.string.device_info_default));
            String cp2 = "";
            String temp;
            mBasebandVersion = pro;
            Matcher m;
            temp = getCp2Version();
            Log.d(LOG_TAG, "getCp2Version():" + temp);
            if (temp != null) {
                Log.d(LOG_TAG, " temp = " + temp);
                if (temp.startsWith("Platform")) {
                    final String PROC_VERSION_REGEX =
                            "PlatformVersion:(\\S+)" + "ProjectVersion:(\\S+)" + "HWVersion:(\\S+)";
                    temp = temp.replaceAll("\\s+", "");
                    m = Pattern.compile(PROC_VERSION_REGEX).matcher(temp);
                    if (!m.matches()) {
                        Log.e(LOG_TAG, "Regex did not match on cp2 version: ");
                    } else {
                        String dateTime = m.group(3);
                        String modem = "modem";
                        int endIndex = dateTime.indexOf(modem) + modem.length();
                        // String subString1 = dateTime.substring(0, endIndex);
                        String subString = dateTime.substring(endIndex);
                        String time = subString.substring(10);
                        String date = subString.substring(0, 10);
                        cp2 = m.group(1) + "|" + m.group(2) + "|" + date + " "
                                + time;
                    }
                  // new matching rule
                } else if (temp.startsWith("WCN_VER")) {
                    final String UPDATE_PROC_VERSION_REGEX = "WCN_VER:(.*)" + "~(.*)" + "~(.*)" + "~(.*)";
                    m = Pattern.compile(UPDATE_PROC_VERSION_REGEX).matcher(temp);
                    Log.d(LOG_TAG, "temp length=" + temp.length());
                    cp2 = getExactResultForCp2(m, temp);
                } else {
                    Log.e(LOG_TAG, "cp2 version is error");
                }
            }
            if (!TextUtils.isEmpty(cp2)) {
                mBasebandVersion = pro + "\n" + cp2;
            }
            Log.d(LOG_TAG, "pro = " + pro + " cp2 = " + cp2);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exceptipon:" + e.toString());
        }
        return mBasebandVersion;
    }

    /**
     * return the exact string for cp2
     * @param m
     * @param socketCp2
     */
    private String getExactResultForCp2(Matcher m, String socketCp2) {
        StringBuilder cp2 = new StringBuilder("");
        String temp = "";
        if (m.matches()) {
            for (int i = 0; i < m.groupCount() - 1; i++) {
                temp = m.group(i + 1);
                if (temp.contains("Version:")) {
                    Log.d(LOG_TAG, "index of : = " + temp.indexOf(':'));
                    cp2.append(temp.substring(temp.indexOf(':') + 1, temp.length()));
                } else {
                    cp2.append(temp);
                }
                //if the last group is null, no need to show separator "|"
                if (i != (m.groupCount() - 2)) {
                    cp2.append("|");
                }
            }
            Log.d(LOG_TAG, " base wcn_ver cp2 info : " + cp2.toString());
            Log.d(LOG_TAG, " group 4  info :" + m.group(4) + "length () =" + m.group(4).length());
            if (!TextUtils.isEmpty(m.group(m.groupCount()).replaceAll("\\s+", ""))) {
                if (socketCp2.charAt(lengthOfCp2-2) != '~') {
                    cp2.append("|").append(m.group(m.groupCount()));
                }
            }
            Log.d(LOG_TAG," wcn_ver cp2 info : " + cp2.toString());
        }
        return cp2.toString();
    }

    /**
     * get the Iconnmgr service
     */
    private IConnmgr getConnmgrMockable() throws RemoteException {
        return IConnmgr.getService();
    }

    private String SendStringCommand(String cmd) {
        try {
            return mIConnmgr.SendStringCommand(cmd);
        } catch (RemoteException e) {
            // Maybe  do something here ...
            return "";
        }
    }

    public String getCp2Version() {
        String cp2Info = "";

        Log.d(LOG_TAG,"start getting the cp2 info by hidl");

        try {
            mIConnmgr = getConnmgrMockable();
        } catch (RemoteException e) {
            Log.d(LOG_TAG,"remoteexception");
            return cp2Info;
        } catch (NoSuchElementException e) {
            return cp2Info;
        }

        if (mIConnmgr == null) {
            Log.d(LOG_TAG,"mIConnmgr == null");
            return cp2Info;
        }

        //Send the poweron command to start wcnd and get the cp2 info by AT, and then poweroff wcnd
        cp2Info = SendStringCommand(WCND_INFO_COMMAND);

        lengthOfCp2 = cp2Info.length();
        Log.d(LOG_TAG, " has got the cp2 info ");

        return cp2Info;

    }

}
