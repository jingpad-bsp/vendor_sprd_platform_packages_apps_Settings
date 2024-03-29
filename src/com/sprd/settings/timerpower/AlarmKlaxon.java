package com.sprd.settings.timerpower;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.SubscriptionController;
import android.telephony.SubscriptionManager;

/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play if
 * another activity overrides the AlarmAlert dialog.
 */
public class AlarmKlaxon extends Service {

    private Alarm mCurrentAlarm;
    private long mStartTime;
    private TelephonyManager mTelephonyManager;

    private int mInitialCallState;
    private int mInitialCallState1;
    private boolean isVTCall;

    private static final String ACTION_SHUT_DOWN =
            "sprd.intent.action.ACTION_SHUTDOWN_COUNTDOWN";

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            Log.v("kc  state-->" + state + "  ignored-->" + ignored);
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if (!hasSimCard()) {
                int state_nocard = mTelephonyManager.getCallState();

                if ((state_nocard == TelephonyManager.CALL_STATE_IDLE) && (state_nocard != mInitialCallState)) {
                    startActivityForShutdown();
                    stopSelf();
                }

                return;
            }

            if (TelephonyManager.from(getApplicationContext()).getPhoneCount() > 1) {
                /* Modify for bug1125903, Settings crash when the shutdown alarm starts @{ */
                int state0 = TelephonyManager.getDefault().getCallStateForSlot(0);
                int state1 = TelephonyManager.getDefault().getCallStateForSlot(1);
                /* @} */
                if ((state0 == TelephonyManager.CALL_STATE_IDLE
                        && state1 == TelephonyManager.CALL_STATE_IDLE)
                        && (state0 != mInitialCallState || state1 != mInitialCallState1)) {
                    startActivityForShutdown();
                    stopSelf();
                }
            } else {
                int state0 = mTelephonyManager.getCallState();
                if (state == TelephonyManager.CALL_STATE_IDLE
                        && state0 != mInitialCallState) {
                    startActivityForShutdown();
                    stopSelf();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        Log.v("service onCreate");
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
        // add for check TVCall
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("service onStartCommand");
        // No intent, tell the system not to restart us.
        if (intent == null) {
            Log.v("AlarmKlaxon intent is null");
            stopSelf();
            return START_NOT_STICKY;
        }

        final Alarm alarm = intent
                .getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        if (alarm == null) {
            Log.v("AlarmKlaxon failed to parse the alarm from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }

        play(alarm);
        mCurrentAlarm = alarm;
        // Record the initial call state here so that the new alarm has the
        // newest state.
        if (!hasSimCard()) {
            mInitialCallState = mTelephonyManager.getCallState();
        } else {
            /* Modify for bug1125903, Settings crash when the shutdown alarm starts @{ */
            mInitialCallState = TelephonyManager.getDefault().getCallStateForSlot(0);
            if (TelephonyManager.from(getApplicationContext()).getPhoneCount() > 1) {
                mInitialCallState1 = TelephonyManager.getDefault().getCallStateForSlot(1);
            }
            /* @} */
        }

        return START_STICKY;
    }

    private void play(Alarm alarm) {
        // Check if we are in a call. If we are, don't play alarm
        if (checkCallIsUsing()) {
            Log.v("in-call , AlarmKlaxon don't play alarm");
            return;
        }

        if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            startActivityForShutdown();
            stopSelf();
        }

        mStartTime = System.currentTimeMillis();
    }

    @Override
    public void onDestroy() {
        Log.v("service onDestroy");
        Alarms.FIRST_ALERT = false;
        // Stop listening for incoming calls.
        mTelephonyManager.listen(mPhoneStateListener, 0);
        AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendKillBroadcast(Alarm alarm) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis / 60000.0);
        Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
        alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
        sendBroadcast(alarmKilled);
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    // add for TVCall
    // Do the common stuff when starting the alarm.
    private void startAlarm(MediaPlayer player) throws java.io.IOException,
            IllegalArgumentException, IllegalStateException {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }


    // check the callstate of sim card
    public boolean checkCallIsUsing() {
        /* Modify for bug1125903, Settings crash when the shutdown alarm starts @{ */
        int callstate0 = TelephonyManager.getDefault().getCallStateForSlot(0);
        int callstate1 = TelephonyManager.getDefault().getCallStateForSlot(1);
        if (callstate0 == TelephonyManager.CALL_STATE_IDLE
                && callstate1 == TelephonyManager.CALL_STATE_IDLE) {
            return false;
        } else {
            return true;
        }
        /* @} */
    }

    public void startActivityForShutdown() {
        Intent intent1 = new Intent(ACTION_SHUT_DOWN);
        intent1.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("shutdown_mode", "timer");
        startActivity(intent1);
    }

    private boolean hasSimCard() {
        TelephonyManager mTM;
        int phoneCount;

        mTM = TelephonyManager.from(getApplicationContext());
        phoneCount = mTM.getPhoneCount();

        if (1 == phoneCount) {
            return mTM.hasIccCard(0);
        } else if (2 == phoneCount) {
            if (mTM.hasIccCard(0) || mTM.hasIccCard(1)) {
                return true;
            }
        }

        return false;
    }
}
