package com.audio.yametech.lexaudio;


import android.content.SharedPreferences;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private Visualizer mVisualizer;
    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private View view;
    private TextView textViewWelcome;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private SharedPreferences preferences;

    private static final float VISUALIZER_HEIGHT_DIP = 100f;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setupVisualizerFxAndUI();
        mVisualizer.setEnabled(true);
        imageView1 = view.findViewById(R.id.imageView1);
        imageView2 = view.findViewById(R.id.imageView2);
        imageView3 = view.findViewById(R.id.imageView3);
        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mVisualizer.getEnabled())
            mVisualizer.setEnabled(true);
        textViewWelcome.setText("Welcome, "+preferences.getString("setting_name","User"));
        if(preferences.getBoolean("ConvolverSwitch",false))
            imageView1.setImageResource(R.drawable.ic_brightness_1_on_24dp);
        else
            imageView1.setImageResource(R.drawable.ic_brightness_1_off_24dp);
        if(preferences.getBoolean("ASSwitch",false))
            imageView2.setImageResource(R.drawable.ic_brightness_1_on_24dp);
        else
            imageView2.setImageResource(R.drawable.ic_brightness_1_off_24dp);
        if(preferences.getBoolean("VLSwitch",false))
            imageView3.setImageResource(R.drawable.ic_brightness_1_on_24dp);
        else
            imageView3.setImageResource(R.drawable.ic_brightness_1_off_24dp);
    }

    private void setupVisualizerFxAndUI() {

        mLinearLayout = view.findViewById(R.id.linearLayoutVisual);

        mVisualizerView = new VisualizerView(view.getContext());
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
        mLinearLayout.addView(mVisualizerView);


        mVisualizer = new Visualizer(0);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                mVisualizerView.updateVisualizer(bytes);
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
        Log.i("LEXVisualizer","Visualizer Initialized.");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mVisualizer.getEnabled())
            mVisualizer.setEnabled(false);
    }
}
