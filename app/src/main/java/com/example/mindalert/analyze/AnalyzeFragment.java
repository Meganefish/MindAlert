package com.example.mindalert.analyze;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;

import android.os.Handler;

import com.example.mindalert.MainActivity;
import com.example.mindalert.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;


public class AnalyzeFragment extends Fragment {
    private View view;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MainActivity mainActivity;
    private Button btnConnect;
    private boolean isReading = false;  // 用于判断文件读取是否进行中
    private ImageView playPauseButton;
    private ImageView nextButton;
    private TextView nowPlayingText;
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

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // 创建 Tab 标题列表
        List<String> titles = new ArrayList<>();
        titles.add("Raw data");
        titles.add("Wave");
        titles.add("Perclos");

        // 创建适配器并设置给 ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(mainActivity, titles);
        viewPager.setAdapter(adapter);

        // 将 TabLayout 和 ViewPager 关联起来
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(titles.get(position));
        }).attach();

        btnConnect = view.findViewById(R.id.btn_connect);

        playPauseButton = view.findViewById(R.id.analyze_play_pause);
        nextButton = view.findViewById(R.id.analyze_next);
        nowPlayingText = view.findViewById(R.id.now_playing);

        btnConnect.setOnClickListener(v -> {
            if (isReading) {
                // 暂停文件读取
                getActivity().stopService(((MainActivity) getActivity()).getAnalyzeIntent());
                btnConnect.setText("Connect");
            } else {
                // 启动文件读取
                getActivity().startService(((MainActivity) getActivity()).getAnalyzeIntent());
                btnConnect.setText("Disconnect");
            }

            // 切换读取状态
            isReading = !isReading;
        });

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
//        updateAnalyzeTask = new Runnable() {
//            @Override
//            public void run() {
//                if(mainActivity.getIsDataRead()){
//                    updateLineChart();
//                }
//                analyzeHandler.postDelayed(this,1000);
//            }
//        };
//        analyzeHandler.post(updateAnalyzeTask);
        updateUI();
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
        updateUI();
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
        String nowPlaying, formattedText;
        if (mainActivity.getIsMusicServiceBound()) {
            if (mainActivity.getMusicService().isPlaying()) {
                playPauseButton.setImageResource(R.drawable.round_pause_icon);
            } else {
                playPauseButton.setImageResource(R.drawable.round_play_icon);
            }

            nowPlaying = mainActivity.getMusicService().getSongList().get(mainActivity.getMusicService().getCurrentSongResId()).getTitle();
            formattedText = String.format(getString(R.string.now_playing), nowPlaying);  // 格式化文本
            nowPlayingText.setText(formattedText);
        } else {
            formattedText = String.format(getString(R.string.now_playing), "");  // 格式化文本
            nowPlayingText.setText(formattedText);
        }
    }
}