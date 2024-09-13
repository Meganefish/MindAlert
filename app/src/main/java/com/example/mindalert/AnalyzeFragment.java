package com.example.mindalert;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.os.Looper;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;


public class AnalyzeFragment extends Fragment {
    private View view;
    private static int height = 0;
    private MainActivity mainActivity;
    private final static String TAG = "AnalyseFragment";
    private ImageView playPauseButton;
    private ImageView nextButton;
    private TextView nowPlayingText;
    private View fatigueBar, wakeBar;
    private TextView state;
    private LineChart lineChart;
    private Handler analyzeHandler = new Handler();
    private Runnable updateAnalyzeTask;
    private BroadcastReceiver songChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();            // 当收到歌曲切换的广播时更新UI
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_analyze, container, false);
        mainActivity = (MainActivity) getActivity();
        super.onViewCreated(view, savedInstanceState);
        // 初始化 LineChart
        lineChart = view.findViewById(R.id.lineChart);
        lineChart.getDescription().setEnabled(false);  // 禁用描述文本
        lineChart.setTouchEnabled(true);  // 启用手势
        lineChart.setPinchZoom(true);  // 启用缩放

        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setAxisMaximum(100);
        lineChart.getAxisLeft().setAxisMinimum(0);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();  // 刷新图表

        fatigueBar = view.findViewById(R.id.fatigue_bar);
        wakeBar = view.findViewById(R.id.wake_bar);
        state = view.findViewById(R.id.tv_state);

        playPauseButton = view.findViewById(R.id.analyze_play_pause);
        nextButton = view.findViewById(R.id.analyze_next);
        nowPlayingText = view.findViewById(R.id.now_playing);

        playPauseButton.setOnClickListener(v -> {
            if (mainActivity.getIsMusicServiceBound()) {
                if (mainActivity.getMusicService().isPlaying()) {
                    mainActivity.getMusicService().pauseMusic();
                    playPauseButton.setImageResource(R.drawable.round_play_icon);
                } else {
                    mainActivity.getMusicService().playMusic();
                    playPauseButton.setImageResource(R.drawable.round_pause_icon);
                }
                updateUI();
            }
        });
        nextButton.setOnClickListener(v -> {
            if (mainActivity.getIsMusicServiceBound()) {
                mainActivity.getMusicService().playNextSong();
                updateUI();
            }
        });
        updateAnalyzeTask = new Runnable() {
            @Override
            public void run() {
                if(mainActivity.getIsDataRead()){
                    updateLineChart();
                    updateBar();
                    updateState();
                }
                analyzeHandler.postDelayed(this,1000);
            }
        };
        analyzeHandler.post(updateAnalyzeTask);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter("com.example.mindalert.SONG_CHANGED");
        getActivity().registerReceiver(songChangedReceiver, filter);
    }


    @Override
    public void onStop() {
        super.onStop();
        // 注销广播接收器
        getActivity().unregisterReceiver(songChangedReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        analyzeHandler.removeCallbacks(updateAnalyzeTask);
    }


    private void updateUI() {
        if (mainActivity.getIsMusicServiceBound()) {
            if (mainActivity.getMusicService().isPlaying()) {
                playPauseButton.setImageResource(R.drawable.round_pause_icon);
            } else {
                playPauseButton.setImageResource(R.drawable.round_play_icon);
            }

            String nowPlaying = mainActivity.getMusicService().getSongList().get(mainActivity.getMusicService().getCurrentSongResId()).getTitle();
            String formattedText = String.format(getString(R.string.now_playing), nowPlaying);  // 格式化文本
            nowPlayingText.setText(formattedText);
        }
    }

    private void updateBar() {
        int index = mainActivity.getAnalyzeService().fatigueEntries.size() - 1;
        float fatigue = mainActivity.getAnalyzeService().fatigueEntries.get(index).getY();
        float wake = mainActivity.getAnalyzeService().wakeEntries.get(index).getY();

        if(height == 0)
            height = view.findViewById(R.id.fatigue_layout).getHeight();

        ViewGroup.LayoutParams fatigueParams = fatigueBar.getLayoutParams();
        fatigueParams.height = (int) (height * (fatigue / 100.0) * 0.7);
        fatigueBar.setLayoutParams(fatigueParams);

        ViewGroup.LayoutParams wakeParams = wakeBar.getLayoutParams();
        wakeParams.height = (int) (height * (wake / 100.0) * 0.7);
        wakeBar.setLayoutParams(wakeParams);
    }

    private void updateState() {
        int index = mainActivity.getAnalyzeService().fatigueEntries.size() - 1;
        float fatigue = mainActivity.getAnalyzeService().fatigueEntries.get(index).getY();
        float wake = mainActivity.getAnalyzeService().wakeEntries.get(index).getY();
        String formattedText;
        if (fatigue > wake) {
            formattedText = String.format(getString(R.string.state), getString(R.string.state_fatigue));  // 格式化文本
            state.setText(formattedText);
        } else {
            formattedText = String.format(getString(R.string.state), getString(R.string.state_wake));  // 格式化文本
            state.setText(formattedText);
        }
    }

    private void updateLineChart() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.getAnalyzeService() == null) {
            Log.e(TAG, "AnalyzeService is not bound yet, cannot update line chart.");
            return;  // 避免空指针异常
        }

        LineDataSet fatigueDataSet = new LineDataSet(mainActivity.getAnalyzeService().fatigueEntries, getString(R.string.fatigue));
        fatigueDataSet.setColor(Color.RED);
        fatigueDataSet.setLineWidth(2f);
        fatigueDataSet.setCircleColor(Color.RED);
        fatigueDataSet.setCircleRadius(3f);

        LineDataSet wakeDataSet = new LineDataSet(mainActivity.getAnalyzeService().wakeEntries, getString(R.string.wake));
        wakeDataSet.setColor(Color.GREEN);
        wakeDataSet.setLineWidth(2f);
        wakeDataSet.setCircleColor(Color.GREEN);
        wakeDataSet.setCircleRadius(3f);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(fatigueDataSet);
        dataSets.add(wakeDataSet);

        LineData data = new LineData(dataSets);


        lineChart.setData(data);
        lineChart.invalidate();  // 刷新图表
    }
}
