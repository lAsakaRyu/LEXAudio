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
public class VolumeLevelerFragment extends Fragment {
    private View view;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Switch switchVolumeLeveler;
    private SeekBar seekBarSleepDuration;
    private SeekBar seekBarSleepInterval;
    private SeekBar seekBarThreshold;
    private TextView textViewCurrentSleepDuration;
    private TextView textViewCurrentSleepInterval;
    private TextView textViewCurrentThreshold;

    public VolumeLevelerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = preferences.edit();
        view = inflater.inflate(R.layout.fragment_volume_leveler, container, false);

        textViewCurrentSleepDuration = view.findViewById(R.id.textViewCurrentSleepDuration);
        textViewCurrentSleepInterval = view.findViewById(R.id.textViewCurrentSleepInterval);
        textViewCurrentThreshold = view.findViewById(R.id.textViewCurrentThreshold);

        switchVolumeLeveler = view.findViewById(R.id.switchVolumeLeveler);
        switchVolumeLeveler.setChecked(preferences.getBoolean("VLSwitch",false));
        switchVolumeLeveler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i("LEXAudio","Volume Leveler On.");
                    editor.putBoolean("VLSwitch",true);
                } else {
                    Log.i("LEXAudio","Volume Leveler Off.");
                    editor.putBoolean("VLSwitch",false);
                }
                editor.apply();
            }
        });
        seekBarSleepDuration = view.findViewById(R.id.seekBarSleepDuration);
        seekBarSleepDuration.setProgress(preferences.getInt("SleepDuration",5)-1);
        seekBarSleepDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("SleepDuration",1+seekBar.getProgress());
                Log.i("LEXVLeveler","Sleep Duration = "+progress);
                editor.apply();
                textViewCurrentSleepDuration.setText(Integer.toString(preferences.getInt("SleepDuration",5)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarSleepInterval = view.findViewById(R.id.seekBarSleepInterval);
        seekBarSleepInterval.setProgress(preferences.getInt("SleepInterval",5)-1);
        seekBarSleepInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("SleepInterval",1+seekBar.getProgress());
                Log.i("LEXVLeveler","Sleep Interval = "+progress);
                editor.apply();
                textViewCurrentSleepInterval.setText(Integer.toString(preferences.getInt("SleepInterval",5)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarThreshold = view.findViewById(R.id.seekBarThreshold);
        seekBarThreshold.setProgress((int)(((preferences.getInt("Threshold",20000)-10000)/(double)22767)*100));
        seekBarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("Threshold",10000+(int)(((double)seekBar.getProgress()/100)*22767));
                Log.i("LEXVLeveler","Activation Threshold = "+progress);
                editor.apply();
                textViewCurrentThreshold.setText(Integer.toString(preferences.getInt("Threshold",20000)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        textViewCurrentSleepDuration.setText(Integer.toString(preferences.getInt("SleepDuration",5)));
        textViewCurrentSleepInterval.setText(Integer.toString(preferences.getInt("SleepInterval",5)));
        textViewCurrentThreshold.setText(Integer.toString(preferences.getInt("Threshold",20000)));
        return view;
    }

}
