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
    private MainActivity mainActivity;
    private final static String TAG = "AnalyseFragment";
    private MusicService musicService;
    private boolean isMusicServiceBound = false;
    private ImageView playPauseButton;
    private ImageView nextButton;
    private TextView nowPlayingText;
    private View fatigueBar, wakeBar;
    private AnalyzeViewModel analyzeViewModel; // ViewModel用于与Service交互
    private TextView state;
    private LineChart lineChart;
    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isMusicServiceBound = true;
            Log.d(TAG, "MusicService connected");  // 调试信息
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isMusicServiceBound = false;
            Log.d(TAG, "MusicService disconnected");  // 调试信息
        }
    };
    private BroadcastReceiver songChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();            // 当收到歌曲切换的广播时更新UI
        }
    };

    // 定义两个观察者
    private final Observer<List<Entry>> fatigueObserver = new Observer<List<Entry>>() {
        @Override
        public void onChanged(List<Entry> entries) {
            mainActivity.getAnalyzeService().fatigueEntries = entries; // 更新数据
            updateLineChart(); // 更新图表
        }
    };

    private final Observer<List<Entry>> wakeObserver = new Observer<List<Entry>>() {
        @Override
        public void onChanged(List<Entry> entries) {
            mainActivity.getAnalyzeService().wakeEntries = entries; // 更新数据
            updateLineChart(); // 更新图表
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent musicIntent = new Intent(getActivity(), MusicService.class);
        boolean musicServiceBound = getActivity().bindService(musicIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        if (!musicServiceBound) {
            Log.e(TAG, "Failed to bind MusicService");  // 检查服务绑定是否成功
        }
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

        mainActivity.getAnalyzeServiceLiveData().observe(getViewLifecycleOwner(), new Observer<AnalyzeService>() {
            @Override
            public void onChanged(AnalyzeService analyzeService) {
                if (analyzeService != null) {
                    // 使用自定义的ViewModelProvider.Factory来获取ViewModel
                    AnalyzeViewModelFactory factory = new AnalyzeViewModelFactory(analyzeService);
                    analyzeViewModel = new ViewModelProvider(AnalyzeFragment.this, factory).get(AnalyzeViewModel.class);

                    // 注册LiveData观察者
                    analyzeViewModel.getFatigueEntries().observe(getViewLifecycleOwner(), fatigueObserver);
                    analyzeViewModel.getWakeEntries().observe(getViewLifecycleOwner(), wakeObserver);
                }
            }
        });
        playPauseButton = view.findViewById(R.id.analyze_play_pause);
        nextButton = view.findViewById(R.id.analyze_next);
        nowPlayingText = view.findViewById(R.id.now_playing);

        playPauseButton.setOnClickListener(v -> {
            if (isMusicServiceBound) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                    playPauseButton.setImageResource(R.drawable.round_play_icon);
                } else {
                    musicService.playMusic();
                    playPauseButton.setImageResource(R.drawable.round_pause_icon);
                }
                updateUI();
            }
        });
        nextButton.setOnClickListener(v -> {
            if (isMusicServiceBound) {
                musicService.playNextSong();
                updateUI();
            }
        });
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
    public void onResume() {
        super.onResume();
        if(analyzeViewModel==null)
            return;
        // 在Fragment可见时，添加观察者
        analyzeViewModel.getFatigueEntries().observe(getViewLifecycleOwner(), fatigueObserver);
        analyzeViewModel.getWakeEntries().observe(getViewLifecycleOwner(), wakeObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 在Fragment不可见时，移除观察者，避免占用计算资源
        analyzeViewModel.getFatigueEntries().removeObserver(fatigueObserver);
        analyzeViewModel.getWakeEntries().removeObserver(wakeObserver);
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
        if (isMusicServiceBound) {
            getActivity().unbindService(musicServiceConnection);
            isMusicServiceBound = false;
            Log.d(TAG, "MusicService Unbound");
        }
    }


    private void updateUI() {
        if (isMusicServiceBound) {
            if (musicService.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.round_pause_icon);
            } else {
                playPauseButton.setImageResource(R.drawable.round_play_icon);
            }

            String nowPlaying = musicService.getSongList().get(musicService.getCurrentSongResId()).getTitle();
            String formattedText = String.format(getString(R.string.now_playing), nowPlaying);  // 格式化文本
            nowPlayingText.setText(formattedText);
        }
    }

    private void updateBar() {
        MainActivity mainActivity = (MainActivity) getActivity();
        float fatigue = mainActivity.getAnalyzeService().fatigueEntries.get(0).getY();
        float wake = mainActivity.getAnalyzeService().wakeEntries.get(0).getY();
        getActivity().runOnUiThread(() -> {
            int height = view.findViewById(R.id.fatigue_layout).getHeight();

            ViewGroup.LayoutParams fatigueParams = fatigueBar.getLayoutParams();
            fatigueParams.height = (int) (height * (fatigue / 100.0) * 0.7);
            fatigueBar.setLayoutParams(fatigueParams);

            ViewGroup.LayoutParams wakeParams = wakeBar.getLayoutParams();
            wakeParams.height = (int) (height * (wake / 100.0) * 0.7);
            wakeBar.setLayoutParams(wakeParams);
        });
    }

    private void updateState() {
        MainActivity mainActivity = (MainActivity) getActivity();
        float fatigue = mainActivity.getAnalyzeService().fatigueEntries.get(0).getY();
        float wake = mainActivity.getAnalyzeService().wakeEntries.get(0).getY();
        getActivity().runOnUiThread(() -> {
            String formattedText;
            if (fatigue > wake) {
                formattedText = String.format(getString(R.string.state), getString(R.string.state_fatigue));  // 格式化文本
                state.setText(formattedText);
            } else {
                formattedText = String.format(getString(R.string.state), getString(R.string.state_wake));  // 格式化文本
                state.setText(formattedText);
            }
        });
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
