package com.audio.yametech.lexaudio;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.media.audiofx.NoiseSuppressor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class LEXService extends Service {
    private final LocalBinder mBinder = new LocalBinder();
    private int sessionId = 0;
    private Equalizer mEqualizer;
    private SharedPreferences preferences;
    public static final UUID ID_V4A_GENERAL_FX = UUID.fromString("41d3c987-e6cf-11e3-a88a-11aba5d5c51b");

    private class LEXModule {
        private final UUID EFFECT_TYPE_NULL = UUID.fromString(
                "ec7178ec-e5e1-4432-a3f4-4657e6795210");
        public AudioEffect mInstance;

        public LEXModule(int nAudioSession) {
            try {
                Log.i("LEXAudio", "Creating module, ");
                mInstance = AudioEffect.class.getConstructor(
                        UUID.class, UUID.class, Integer.TYPE, Integer.TYPE).newInstance(
                        EFFECT_TYPE_NULL, LEXService.ID_V4A_GENERAL_FX, 0, nAudioSession);
            } catch (Exception e) {
                Log.e("ViPER4Android", "Can not create audio effect instance,"
                        + "V4A driver not installed or not supported by this rom");
                mInstance = null;
            }
        }
    }

    public int getSessionId() {
        return this.sessionId;
    }

    private final BroadcastReceiver mAudioSessionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (preferences.getBoolean("MasterSwitch", false)) {
                String action = intent.getAction();
                sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0);
                if (sessionId == 0) {
                    Log.i("LEXEqualizer", String.format("Global session id received: %d", sessionId));
                    Toast.makeText(getApplicationContext(), "Global id received! " + sessionId, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (action.equals(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)) {
                    Log.i("LEXEqualizer", String.format("Audio session open: %d", sessionId));
                    Toast.makeText(getApplicationContext(), "Session Open! " + sessionId, Toast.LENGTH_SHORT).show();
                    enableEffect();
                }

                if (action.equals(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)) {
                    Log.i("LEXEqualizer", String.format("Audio session closed: %d", sessionId));
                    Toast.makeText(getApplicationContext(), "Session Closed! " + sessionId, Toast.LENGTH_SHORT).show();
                    disableEffect();
                }
            } else {
                Log.i("LEXAudio", "Session Received but Master is Off.");
            }
        }
    };
    private final SharedPreferences.OnSharedPreferenceChangeListener PrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals("MasterSwitch")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if(sessionId!=0) {
                        Log.i("LEXAudio","Effect Started on ID."+sessionId);
                        enableEffect();
                    }
                    else
                        Log.i("LEXAudio","Effect not started yet / Holding global audio session id."+sessionId);
                } else if (!prefs.getBoolean("MasterSwitch", false)) {
                    disableEffect();
                }
            }
        }
    };

    private void enableEffect() {
        Log.i("LEXAudio", "Effect Enabled.");
        mEqualizer = new Equalizer(0, sessionId);
        mEqualizer.setEnabled(true);
        mEqualizer.usePreset((short) 8);
        String str = mEqualizer.getProperties().toString();
        Log.i("LEXEqualizer", str);
    }

    private void disableEffect() {
        if (mEqualizer != null){
            Log.i("LEXAudio", "Effect Released.");
            mEqualizer.release();
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public LEXService getService() {
            return LEXService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        IntentFilter audioSessionFilter = new IntentFilter();
        audioSessionFilter.addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        audioSessionFilter.addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        registerReceiver(mAudioSessionReceiver, audioSessionFilter);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(PrefListener);
    }
}

