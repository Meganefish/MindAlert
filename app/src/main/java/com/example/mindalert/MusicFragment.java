package com.example.mindalert;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.content.BroadcastReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MusicFragment";  // 添加日志TAG
    private ImageView ivMusic;
    private TextView tvProgress, tvTotal;
    private SeekBar sb;
    private Button btnPlay, btnPause, btnNext;
    private Handler handler = new Handler();
    private ObjectAnimator rotationAnimator;
    private boolean isUserSeeking = false; // 用于跟踪用户是否在拖动进度条
    private MainActivity mainActivity;
    private ExpandableListView expandableMusicListView;
    private MusicExpandableListAdapter adapter;
    private List<String> groupList; // 组名称，如 "pop", "metal"
    private HashMap<String, List<String>> songMap; // 每个组对应的歌曲列表

    private BroadcastReceiver songChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();            // 当收到歌曲切换的广播时更新UI
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        ivMusic = view.findViewById(R.id.iv_music);
        tvProgress = view.findViewById(R.id.tv_progress);
        tvTotal = view.findViewById(R.id.tv_total);
        sb = view.findViewById(R.id.sb);
        btnPlay = view.findViewById(R.id.btn_previous);
        btnPause = view.findViewById(R.id.btn_play_pause);
        btnNext = view.findViewById(R.id.btn_next);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        expandableMusicListView = view.findViewById(R.id.expandable_music_list);

        // 初始化数据
        groupList = new ArrayList<>();
        songMap = new HashMap<>();
        adapter = new MusicExpandableListAdapter(getContext(), groupList, songMap);
        expandableMusicListView.setAdapter(adapter);

        // ExpandableListView 点击事件处理
        expandableMusicListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if (mainActivity.getIsMusicServiceBound()) {
                String group = groupList.get(groupPosition);
                String songTitle = songMap.get(group).get(childPosition);
                int songIndex = mainActivity.getMusicService().getSongIndexByTitle(songTitle);
                if (songIndex != -1) {
                    mainActivity.getMusicService().playSongAtIndex(songIndex);
                    updateSongName();
                    updateUI();
                }
            }
            return false;
        });

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mainActivity.getIsMusicServiceBound()) {
                    isUserSeeking = true;
                    tvProgress.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mainActivity.getIsMusicServiceBound()) {
                    mainActivity.getMusicService().seekTo(seekBar.getProgress());
                    isUserSeeking = false;
                }
            }
        });

        setupRotationAnimator();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 加载音乐列表并更新UI
        //if (mainActivity.getIsMusicServiceBound()) {
            loadSongList();
        //}
        // 注册广播接收器
        IntentFilter filter = new IntentFilter("com.example.mindalert.SONG_CHANGED");
        getActivity().registerReceiver(songChangedReceiver, filter);
        updateSongName();
        updateUI();
        handler.post(updateSeekBar);
    }

    private void loadSongList() {
        // 从 MusicService 获取歌曲列表
        List<MusicService.Song> songs = mainActivity.getMusicService().getSongList();

        // 清除旧数据
        groupList.clear();
        songMap.clear();

        // 分类歌曲到不同的组
        List<String> popSongs = new ArrayList<>();
        List<String> metalSongs = new ArrayList<>();

        for (MusicService.Song song : songs) {
            if (song.getTitle().startsWith("pop")) {
                popSongs.add(song.getTitle());
            } else if (song.getTitle().startsWith("metal")) {
                metalSongs.add(song.getTitle());
            }
        }

        if (!popSongs.isEmpty()) {
            groupList.add("Pop");
            songMap.put("Pop", popSongs);
        }

        if (!metalSongs.isEmpty()) {
            groupList.add("Metal");
            songMap.put("Metal", metalSongs);
        }

        adapter.notifyDataSetChanged(); // 更新列表显示
    }

    @Override
    public void onResume(){
        super.onResume();
        updateSongName();
        updateUI();
        if(mainActivity.getMusicService().isPlaying()){
            btnPause.setText(R.string.music_pause);
            rotationAnimator.start();
        }else{
            btnPause.setText(R.string.music_play);
        }
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
    }

    private void setupRotationAnimator() {
        rotationAnimator = ObjectAnimator.ofFloat(ivMusic, "rotation", 0f, 360f);
        rotationAnimator.setDuration(10000);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    }

    private void updateUI() {
        if (mainActivity.getMusicService() != null) {
            sb.setMax(mainActivity.getMusicService().getDuration());
            tvTotal.setText(formatTime(mainActivity.getMusicService().getDuration()));

            // 获取当前歌曲并设置 iv_music 的图片
            int currentId = mainActivity.getMusicService().getCurrentSongResId();
            MusicService.Song currentSong = mainActivity.getMusicService().getSongList().get(currentId);

            // 检查图片资源 ID 是否有效
            if (currentSong.getImageResId() != 0) {
                ivMusic.setImageResource(currentSong.getImageResId());  // 设置图片
            } else {
                ivMusic.setImageResource(R.drawable.default_music_image);  // 若无对应图片，设置默认图片
            }
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mainActivity.getIsMusicServiceBound() && mainActivity.getMusicService() != null && !isUserSeeking) {
                int currentPosition = mainActivity.getMusicService().getCurrentPosition();
                sb.setProgress(currentPosition);
                tvProgress.setText(formatTime(currentPosition));
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onClick(View v) {
        Button btn_play = getView().findViewById(R.id.btn_play_pause);
        if (mainActivity.getIsMusicServiceBound()) {
            if(v.getId() == R.id.btn_previous){
                mainActivity.getMusicService().playPreviousSong();
                updateSongName(); // 更新歌曲名称
                updateUI();
                btn_play.setText(R.string.music_pause);
                rotationAnimator.start();
                Log.d(TAG, "Previous button clicked");  // 调试信息
            }
            else if(v.getId() == R.id.btn_play_pause){
                if(mainActivity.getMusicService().isPlaying()){
                    mainActivity.getMusicService().pauseMusic();
                    btn_play.setText(R.string.music_play);
                    rotationAnimator.pause();
                    Log.d(TAG, "Pause button clicked");  // 调试信息
                }else{
                    mainActivity.getMusicService().playMusic();
                    btn_play.setText(R.string.music_pause);
                    rotationAnimator.start();
                    Log.d(TAG, "Pause button clicked");  // 调试信息
                }
            }
            else if(v.getId() == R.id.btn_next){
                mainActivity.getMusicService().playNextSong();
                updateSongName(); // 更新歌曲名称
                updateUI();
                btn_play.setText(R.string.music_pause);
                rotationAnimator.start();
                Log.d(TAG, "Next button clicked");  // 调试信息
            }
        } else {
            Log.d(TAG, "Service not bound, button click ignored");  // 调试信息，确认未绑定时忽略操作
        }
    }

    private String formatTime(int milliseconds) {
        int minutes = milliseconds / 1000 / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // 新增：根据资源ID更新歌曲名称
    private void updateSongName() {
        if (getContext() != null) {
            // 使用 getContext().getResources() 确保获取到资源
            int currentId = mainActivity.getMusicService().getCurrentSongResId();
            String title = mainActivity.getMusicService().getSongList().get(currentId).getTitle();
            //String displayName = getContext().getResources().getString(R.string.now_playing, musicService.getSongList().get(musicService.getCurrentSongResId()).getTitle());
            TextView songNameTextView = getView().findViewById(R.id.song_name);
            if (songNameTextView != null) {
                songNameTextView.setText(title);
            } else {
                Log.e(TAG, "songNameTextView is null. Check the view initialization.");
            }
        } else {
            Log.e(TAG, "Context is null. Fragment might not be attached to Activity.");
        }
    }
}
