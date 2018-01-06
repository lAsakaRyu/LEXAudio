package com.audio.yametech.lexaudio;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.NoiseSuppressor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class LEXService extends Service {
    private final LocalBinder mBinder = new LocalBinder();
    private int sessionId = 0;
    private Equalizer mEqualizer;
    private EnvironmentalReverb mEReverb = new EnvironmentalReverb(0,0);
    private AudioRecord mRecord;
    private AudioTrack mTrack;
    private SharedPreferences preferences;
    private AudioManager AM;
    private Thread mVL;
    private boolean effectVL;
    private short THRESHOLD;
    private int sampleRate = 44100;
    private int minSize = AudioTrack.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);

    private final BroadcastReceiver mAudioSessionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0);
            if (preferences.getBoolean("MasterSwitch", false)) {
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
            Log.i("LEXAudio","Shared preference changed."+key);
            if (key.equals("MasterSwitch")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (sessionId != 0) {
                        Log.i("LEXAudio", "Effect Started on ID." + sessionId);
                        enableEffect();
                    } else
                        Log.i("LEXAudio", "Effect not started yet / Holding global audio session id." + sessionId);
                } else if (!prefs.getBoolean("MasterSwitch", false)) {
                    disableEffect();
                }
            }
            if (key.equals("ConvolverSwitch")) {
                if(mEReverb!=null)
                    mEReverb.setEnabled(prefs.getBoolean("ConvolverSwitch",false));
            }
            if(key.equals("RoomLevel")){
                if(prefs.getBoolean("MasterSwitch", false)){
                    if(prefs.getBoolean("ConvolverSwitch",false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setRoomLevel((short) prefs.getInt("RoomLevel", 0));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if(key.equals("HFRoomLevel")){
                if(prefs.getBoolean("MasterSwitch", false)){
                    if(prefs.getBoolean("ConvolverSwitch",false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setRoomHFLevel((short) prefs.getInt("HFRoomLevel", -1530));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if(key.equals("ReverbLevel")){
                if(prefs.getBoolean("MasterSwitch", false)){
                    if(prefs.getBoolean("ConvolverSwitch",false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setReverbLevel((short) prefs.getInt("ReverbLevel", -750));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if(key.equals("ReflectionLevel")){
                if(prefs.getBoolean("MasterSwitch", false)){
                    if(prefs.getBoolean("ConvolverSwitch",false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setReflectionsLevel((short) prefs.getInt("ReflectionLevel", -1200));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if(key.equals("DecayTime")){
                if(prefs.getBoolean("MasterSwitch", false)){
                    if(prefs.getBoolean("ConvolverSwitch",false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setDecayTime(prefs.getInt("DecayTime", 1300));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if(key.equals("VLSwitch")){
                if(prefs.getBoolean("VLSwitch", false))
                    effectVLON();
                else
                    effectVLOFF();

            }
            if(key.equals("Threshold")){
                if(prefs.getBoolean("MasterSwitch", false)){
                    if(prefs.getBoolean("VLSwitch",false)) {
                        THRESHOLD = (short)prefs.getInt("Threshold",20000);
                    }
                }
            }
        }
    };


    private void enableEffect() {
        Log.i("LEXAudio", "Effect Enabled.");
        /*mEqualizer = new Equalizer(0, sessionId);
        mEqualizer.setEnabled(false);
        mEqualizer.usePreset((short) 8);
        String str = mEqualizer.getProperties().toString();
        Log.i("LEXEqualizer", str);*/
        mEReverb = new EnvironmentalReverb(0,sessionId);
        mEReverb.setDecayHFRatio((short)1150);
        mEReverb.setDecayTime(preferences.getInt("DecayTime", 1300));
        mEReverb.setDensity((short)1000);
        mEReverb.setDiffusion((short)1000);
        mEReverb.setReflectionsDelay(40);
        mEReverb.setReflectionsLevel((short) preferences.getInt("ReflectionLevel", -1200));
        mEReverb.setReverbDelay(40);
        mEReverb.setReverbLevel((short) preferences.getInt("ReverbLevel", -750));
        mEReverb.setRoomHFLevel((short) preferences.getInt("HFRoomLevel", -1530));
        mEReverb.setRoomLevel((short) preferences.getInt("RoomLevel", 0));
        if(preferences.getBoolean("ConvolverSwitch",false))
            mEReverb.setEnabled(true);
        Log.i("LEXConvolver",mEReverb.getProperties().toString());

        if(preferences.getBoolean("VLSwitch",false)){
            effectVLON();
        }
    }

    private void disableEffect() {
        if(mEReverb != null){
            Log.i("LEXConvolver", "Convolver Effect Released.");
            mEReverb.release();
        }
        if(mVL!=null)
            effectVLOFF();
    }

    private void effectVLON(){
        effectVL = true;
        THRESHOLD = (short)preferences.getInt("Threshold",20000);
        mVL = new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                effectVLModule();
            }

        };
        mVL.start();
    }

    private void effectVLOFF(){
        effectVL = false;
        mVL = null;
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
        mRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        mTrack = new AudioTrack(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
                new AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build(),
                minSize,AudioTrack.MODE_STREAM,sessionId);
        AM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mEReverb!=null) {
            mEReverb.release();
            mEReverb = null;
        }
    }

    private void effectVLModule() {
        short[] lin = new short[minSize];
        int num;
        int count=0;
        mRecord.startRecording();
        //mTrack.play();
        while (effectVL) {
            num = mRecord.read(lin, 0, minSize);
            for(int a=0;a<minSize;a++){
                if(lin[a]>THRESHOLD){
                    count++;
                    Log.i("DATA",Short.toString(lin[a])+" - "+count +" / "+minSize);
                }
            }
            if(count>50){
                int oldVolume = AM.getStreamVolume(AudioManager.STREAM_MUSIC);
                AM.setStreamVolume(AudioManager.STREAM_MUSIC,oldVolume/2,0);
                try {
                    Thread.sleep(preferences.getInt("SleepDuration",5)*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AM.setStreamVolume(AudioManager.STREAM_MUSIC,(int)(oldVolume*0.8),0);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AM.setStreamVolume(AudioManager.STREAM_MUSIC,oldVolume,0);
                try {
                    Thread.sleep(preferences.getInt("SleepInterval",5)*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count = 0;
            //mTrack.write(lin,0, num);
        }
    }

}

