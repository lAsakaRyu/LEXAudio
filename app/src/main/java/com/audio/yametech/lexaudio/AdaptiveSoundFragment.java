package com.audio.yametech.lexaudio;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdaptiveSoundFragment extends Fragment {
    private View view;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Switch switchAdaptiveSound;
    private SeekBar seekBarMaxLevel;
    private SeekBar seekBarAdaptInterval;
    private TextView textViewCurrentMaxLevel;
    private TextView textViewCurrentAdaptInterval;

    public AdaptiveSoundFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = preferences.edit();
        view = inflater.inflate(R.layout.fragment_adaptive_sound, container, false);
        textViewCurrentMaxLevel = view.findViewById(R.id.textViewCurrentMaxLevel);
        textViewCurrentAdaptInterval = view.findViewById(R.id.textViewCurrentAdaptInterval);

        switchAdaptiveSound = view.findViewById(R.id.switchAdaptiveSound);
        switchAdaptiveSound.setChecked(preferences.getBoolean("ASSwitch",false));
        switchAdaptiveSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i("LEXAudio","Adaptive Sound On.");
                    editor.putBoolean("ASSwitch",true);
                } else {
                    Log.i("LEXAudio","Adaptive Sound Off.");
                    editor.putBoolean("ASSwitch",false);
                }
                editor.apply();
            }
        });

        seekBarMaxLevel = view.findViewById(R.id.seekBarMaxLevel);
        seekBarMaxLevel.setProgress((preferences.getInt("MaxLevel",10000)-3000)/1000);
        seekBarMaxLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("MaxLevel",3000+(seekBar.getProgress()*1000));
                Log.i("LEXASound","Max Level = "+progress);
                editor.apply();
                textViewCurrentMaxLevel.setText(Integer.toString(preferences.getInt("MaxLevel",10000)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarAdaptInterval = view.findViewById(R.id.seekBarAdaptInterval);
        seekBarAdaptInterval.setProgress(preferences.getInt("AdaptInterval",1)-1);
        seekBarAdaptInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("AdaptInterval",1+seekBar.getProgress());
                Log.i("LEXASound","Adapt Interval = "+progress);
                editor.apply();
                textViewCurrentAdaptInterval.setText(Integer.toString(preferences.getInt("AdaptInterval",1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        textViewCurrentMaxLevel.setText(Integer.toString(preferences.getInt("MaxLevel",10000)));
        textViewCurrentAdaptInterval.setText(Integer.toString(preferences.getInt("AdaptInterval",1)));
        return view;
    }

}
