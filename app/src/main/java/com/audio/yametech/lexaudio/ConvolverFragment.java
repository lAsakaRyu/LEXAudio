package com.audio.yametech.lexaudio;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConvolverFragment extends Fragment {
    private View view;
    private SeekBar seekBarRoomLevel;
    private SeekBar seekBarHFRoomLevel;
    private SeekBar seekBarReverbLevel;
    private SeekBar seekBarReflectionLevel;
    private SeekBar seekBarDecayTime;
    private TextView textViewCurrentRoomLevel;
    private TextView textViewCurrentHFRoomLevel;
    private TextView textViewCurrentReverbLevel;
    private TextView textViewCurrentReflectionLevel;
    private TextView textViewCurrentDecayTime;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Switch switchConvolver;
    private Button buttonDefault;

    public ConvolverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = preferences.edit();
        view = inflater.inflate(R.layout.fragment_convolver, container, false);

        textViewCurrentRoomLevel = view.findViewById(R.id.textViewCurrentRoomLevel);
        textViewCurrentHFRoomLevel = view.findViewById(R.id.textViewCurrentHFRoomLevel);
        textViewCurrentReverbLevel = view.findViewById(R.id.textViewCurrentReverbLevel);
        textViewCurrentReflectionLevel = view.findViewById(R.id.textViewCurrentReflectionLevel);
        textViewCurrentDecayTime = view.findViewById(R.id.textViewCurrentDecayTime);

        switchConvolver = view.findViewById(R.id.switchConvolver);
        switchConvolver.setChecked(preferences.getBoolean("ConvolverSwitch",false));
        switchConvolver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i("LEXAudio","Convolver On.");
                    editor.putBoolean("ConvolverSwitch",true);
                } else {
                    Log.i("LEXAudio","Convolver Off.");
                    editor.putBoolean("ConvolverSwitch",false);
                }
                editor.apply();
            }
        });

        seekBarRoomLevel = view.findViewById(R.id.seekBarRoomLevel);
        seekBarRoomLevel.setProgress((int)(((preferences.getInt("RoomLevel",0)+9000)/(double)10000)*100));
        seekBarRoomLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("RoomLevel",-9000+(int)(((double)seekBar.getProgress()/100)*10000));
                Log.i("LEXConvolver","Room Level = "+progress);
                editor.apply();
                textViewCurrentRoomLevel.setText(Integer.toString(preferences.getInt("RoomLevel",0)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarHFRoomLevel = view.findViewById(R.id.seekBarHFRoomLevel);
        seekBarHFRoomLevel.setProgress((int)(((preferences.getInt("HFRoomLevel",-1530)+9000)/(double)9000)*100));
        seekBarHFRoomLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("HFRoomLevel",-9000+(int)(((double)seekBar.getProgress()/100)*9000));
                Log.i("LEXConvolver","HF Room Level = "+progress);
                editor.apply();
                textViewCurrentHFRoomLevel.setText(Integer.toString(preferences.getInt("HFRoomLevel", -1530)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarReverbLevel = view.findViewById(R.id.seekBarReverbLevel);
        seekBarReverbLevel.setProgress((int)(((preferences.getInt("ReverbLevel",-750)+9000)/(double)11000)*100));
        seekBarReverbLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("ReverbLevel",-9000+(int)(((double)seekBar.getProgress()/100)*11000));
                Log.i("LEXConvolver","Reverb Level = "+progress);
                editor.apply();
                textViewCurrentReverbLevel.setText(Integer.toString(preferences.getInt("ReverbLevel", -750)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarReflectionLevel = view.findViewById(R.id.seekBarReflectionLevel);
        seekBarReflectionLevel.setProgress((int)(((preferences.getInt("ReflectionLevel",-1200)+9000)/(double)10000)*100));
        seekBarReflectionLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("ReflectionLevel",-9000+(int)(((double)seekBar.getProgress()/100)*10000));
                Log.i("LEXConvolver","Reflection Level = "+progress);
                editor.apply();
                textViewCurrentReflectionLevel.setText(Integer.toString(preferences.getInt("ReflectionLevel", -1200)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarDecayTime = view.findViewById(R.id.seekBarDecayTime);
        seekBarDecayTime.setProgress((int)(((preferences.getInt("DecayTime",1300)-100)/(double)19900)*100));
        seekBarDecayTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("DecayTime",100+(int)(((double)seekBar.getProgress()/100)*19900));
                Log.i("LEXConvolver","DecayTime = "+progress);
                editor.apply();
                textViewCurrentDecayTime.setText(Integer.toString(preferences.getInt("DecayTime", 1300)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buttonDefault = view.findViewById(R.id.buttonDefault);
        buttonDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.remove("RoomLevel");
                editor.remove("HFRoomLevel");
                editor.remove("ReverbLevel");
                editor.remove("ReflectionLevel");
                editor.remove("DecayTime");
                editor.apply();
                seekBarRoomLevel.setProgress((int)(((preferences.getInt("RoomLevel",0)+9000)/(double)10000)*100));
                seekBarHFRoomLevel.setProgress((int)(((preferences.getInt("HFRoomLevel",-1530)+9000)/(double)9000)*100));
                seekBarReverbLevel.setProgress((int)(((preferences.getInt("ReverbLevel",-750)+9000)/(double)11000)*100));
                seekBarReflectionLevel.setProgress((int)(((preferences.getInt("ReflectionLevel",-1200)+9000)/(double)10000)*100));
                seekBarDecayTime.setProgress((int)(((preferences.getInt("DecayTime",1300)-100)/(double)19900)*100));
            }
        });


        textViewCurrentRoomLevel.setText(Integer.toString(preferences.getInt("RoomLevel",0)));
        textViewCurrentHFRoomLevel.setText(Integer.toString(preferences.getInt("HFRoomLevel", -1530)));
        textViewCurrentReverbLevel.setText(Integer.toString(preferences.getInt("ReverbLevel", -750)));
        textViewCurrentReflectionLevel.setText(Integer.toString(preferences.getInt("ReflectionLevel", -1200)));
        textViewCurrentDecayTime.setText(Integer.toString(preferences.getInt("DecayTime", 1300)));
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
