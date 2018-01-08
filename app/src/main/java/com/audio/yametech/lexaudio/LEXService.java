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
    private EnvironmentalReverb mEReverb = new EnvironmentalReverb(0, 0);
    private AudioRecord mRecord;
    private AudioTrack mTrack;
    private SharedPreferences preferences;
    private AudioManager AM;
    private Thread mVL;
    private Thread mAS;
    private boolean effectVL;
    private boolean effectAS;
    private int control = 0;
    private short THRESHOLD;
    private int sampleRate = 44100;
    private int minSize = 1024;
    short[] lin = new short[minSize];

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
            Log.i("LEXAudio", "Shared preference changed." + key);
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
                if (mEReverb != null)
                    mEReverb.setEnabled(prefs.getBoolean("ConvolverSwitch", false));
                if (prefs.getBoolean("ASSwitch", false)&&mEqualizer!=null)
                    mEqualizer.setEnabled(false);
            }
            if (key.equals("RoomLevel")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (prefs.getBoolean("ConvolverSwitch", false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setRoomLevel((short) prefs.getInt("RoomLevel", 0));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if (key.equals("HFRoomLevel")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (prefs.getBoolean("ConvolverSwitch", false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setRoomHFLevel((short) prefs.getInt("HFRoomLevel", -1530));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if (key.equals("ReverbLevel")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (prefs.getBoolean("ConvolverSwitch", false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setReverbLevel((short) prefs.getInt("ReverbLevel", -750));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if (key.equals("ReflectionLevel")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (prefs.getBoolean("ConvolverSwitch", false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setReflectionsLevel((short) prefs.getInt("ReflectionLevel", -1200));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if (key.equals("DecayTime")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (prefs.getBoolean("ConvolverSwitch", false)) {
                        mEReverb.setEnabled(false);
                        mEReverb.setDecayTime(prefs.getInt("DecayTime", 1300));
                        mEReverb.setEnabled(true);
                    }
                }
            }
            if (key.equals("VLSwitch")) {
                if (prefs.getBoolean("VLSwitch", false))
                    effectVLON();
                else
                    effectVLOFF();

            }
            if (key.equals("Threshold")) {
                if (prefs.getBoolean("MasterSwitch", false)) {
                    if (prefs.getBoolean("VLSwitch", false)) {
                        THRESHOLD = (short) prefs.getInt("Threshold", 20000);
                    }
                }
            }
            if (key.equals("ASSwitch")) {
                if (prefs.getBoolean("ASSwitch", false))
                    effectASON();
                else
                    effectASOFF();

            }
            if (key.equals("MaxLevel")) {
                if (mEqualizer != null && prefs.getBoolean("ASSwitch", false)) {
                    mEqualizer.usePreset((short) 3);
                }
            }
        }
    };


    private void enableEffect() {
        Log.i("LEXAudio", "Effect Enabled.");
        mEReverb = new EnvironmentalReverb(0, sessionId);
        mEReverb.setDecayHFRatio((short) 1150);
        mEReverb.setDecayTime(preferences.getInt("DecayTime", 1300));
        mEReverb.setDensity((short) 1000);
        mEReverb.setDiffusion((short) 1000);
        mEReverb.setReflectionsDelay(40);
        mEReverb.setReflectionsLevel((short) preferences.getInt("ReflectionLevel", -1200));
        mEReverb.setReverbDelay(40);
        mEReverb.setReverbLevel((short) preferences.getInt("ReverbLevel", -750));
        mEReverb.setRoomHFLevel((short) preferences.getInt("HFRoomLevel", -1530));
        mEReverb.setRoomLevel((short) preferences.getInt("RoomLevel", 0));
        if (preferences.getBoolean("ConvolverSwitch", false))
            mEReverb.setEnabled(true);
        Log.i("LEXConvolver", mEReverb.getProperties().toString());

        if (preferences.getBoolean("VLSwitch", false)) {
            effectVLON();
        }
        if (preferences.getBoolean("ASSwitch", false)) {
            effectASON();
        }
    }

    private void disableEffect() {
        if (mEReverb != null) {
            Log.i("LEXConvolver", "Convolver Effect Released.");
            mEReverb.release();
        }
        if (mVL != null)
            effectVLOFF();
        if (mAS != null)
            effectASOFF();
    }

    private void effectVLON() {
        effectVL = true;
        THRESHOLD = (short) preferences.getInt("Threshold", 20000);
        mVL = new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                effectVLModule();
            }

        };
        mVL.start();
    }

    private void effectVLOFF() {
        mVL = null;
        effectVL = false;
        mRecord.stop();
        control=0;
    }

    private void effectASON() {
        effectAS = true;
        mAS = new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                effectASModule();
            }
        };
        mAS.start();
    }

    private void effectASOFF() {
        mAS = null;
        effectAS = false;
        if (mEqualizer != null)
            mEqualizer.release();
        mEqualizer = null;
        mRecord.stop();
        control=0;
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
        mRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        /*mTrack = new AudioTrack(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                new AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build(),
                minSize,AudioTrack.MODE_STREAM,sessionId);
        mTrack.play();*/
        AM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEReverb != null) {
            mEReverb.release();
            mEReverb = null;
        }
        if (mVL != null)
            mVL = null;
    }

    private void effectVLModule() {

        int num;
        int count = 0;
        while (effectVL) {
            if (mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
                mRecord.startRecording();
            if(control==0||control==1) {
                num = mRecord.read(lin, 0, minSize);
                control = 1;
            }
            for (int a = 0; a < minSize; a++) {
                if (lin[a] > THRESHOLD) {
                    count++;
                    Log.i("DATA", Short.toString(lin[a]) + " - " + count + " / " + minSize);
                }
            }
            if (count > 50) {
                int oldVolume = AM.getStreamVolume(AudioManager.STREAM_MUSIC);
                AM.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume / 2, 0);
                try {
                    mVL.sleep(preferences.getInt("SleepDuration", 5) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AM.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (oldVolume * 0.8), 0);
                try {
                    mVL.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AM.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
                try {
                    mVL.sleep(preferences.getInt("SleepInterval", 5) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count = 0;
            //mTrack.write(lin,0, num);
        }
    }

    private void effectASModule() {
        mEqualizer = new Equalizer(0, sessionId);
        if (!preferences.getBoolean("ConvolverSwitch", false))
            mEqualizer.setEnabled(true);
        mEqualizer.usePreset((short) 3);
        int num;
        int count = 0;
        short prevband = -1;
        int max_amplitude;
        while (effectAS) {
            if (mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
                mRecord.startRecording();
            if(control==0||control==2) {
                num = mRecord.read(lin, 0, minSize);
                control = 2;
            }
            max_amplitude = 0;
            for (int a = 0; a < minSize; a++) {
                if (lin[a] > max_amplitude) {
                    max_amplitude = lin[a];
                }
            }
            short band;
            try {
                band = mEqualizer.getBand(calculateCenterFreq(lin));
                //Log.i("DATA",Integer.toString(calculateCenterFreq(lin)) + " Count = " + count);
                if (band == prevband)
                    count++;
                else
                    count = 0;
                if (count >= 3 && band >= 0 && band < mEqualizer.getNumberOfBands()) {
                    count = 0;
                    if ((max_amplitude) > preferences.getInt("MaxLevel", 10000))
                        max_amplitude = preferences.getInt("MaxLevel", 10000);
                    Log.i("DATA", Integer.toString(calculateCenterFreq(lin)) + " - " + band + " - " + max_amplitude);
                    mEqualizer.setEnabled(false);
                    mEqualizer.setBandLevel(band, (short) ((max_amplitude / 10)));
                    if (!preferences.getBoolean("ConvolverSwitch", false))
                        mEqualizer.setEnabled(true);
                    Log.i("DATA", mEqualizer.getProperties().toString());
                    try {
                        mAS.sleep(preferences.getInt("AdaptInterval", 1) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                prevband = band;
            } catch (Exception ex) {
                Log.i("Thread exception", "Thread has been stop unexpectedly. minSize = " + minSize);
            }
        }
    }

    private int calculateCenterFreq(short[] lin) {
        double[] magnitude = new double[minSize / 2];

        Complex[] fftTempArray = new Complex[minSize];
        for (int i = 0; i < minSize; i++) {
            fftTempArray[i] = new Complex(lin[i], 0);
        }

        final Complex[] fftArray = FFT.fft(fftTempArray);

        for (int i = 0; i < (minSize / 2) - 1; ++i) {

            double real = fftArray[i].re();
            double imaginary = fftArray[i].im();
            magnitude[i] = Math.sqrt(real * real + imaginary * imaginary);

        }

        double max_magnitude = magnitude[0];
        int max_index = 0;
        for (int i = 0; i < magnitude.length; ++i) {
            if (magnitude[i] > max_magnitude) {
                max_magnitude = (int) magnitude[i];
                max_index = i;
            }
        }
        double freq = sampleRate * max_index / (double) minSize;
        return (int) (freq * 1000);
    }

}

