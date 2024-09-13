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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.content.BroadcastReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MusicFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MusicFragment";  // 添加日志TAG
    private ImageView ivMusic;
    private TextView tvProgress, tvTotal;
    private SeekBar sb;
    private Button btnPlay, btnPause, btnNext;
    private Handler handler = new Handler();
    private ObjectAnimator rotationAnimator;
    private boolean isUserSeeking = false; // 用于跟踪用户是否在拖动进度条
    private MusicService musicService;
    private boolean isBound = false; // 用于跟踪 Service 是否已绑定

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;
            Log.d(TAG, "Service connected");  // 调试信息
            updateUI();  // 确保在连接成功后更新UI
            updateSongName(); // 更新歌曲名称
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.d(TAG, "Service disconnected");  // 调试信息
        }
    };

    private BroadcastReceiver songChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();            // 当收到歌曲切换的广播时更新UI
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        ivMusic = view.findViewById(R.id.iv_music);
        //songName = view.findViewById(R.id.song_name);
        tvProgress = view.findViewById(R.id.tv_progress);
        tvTotal = view.findViewById(R.id.tv_total);
        sb = view.findViewById(R.id.sb);
        btnPlay = view.findViewById(R.id.btn_previous);
        btnPause = view.findViewById(R.id.btn_play_pause);
        btnNext = view.findViewById(R.id.btn_next);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
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
                if (isBound) {
                    musicService.seekTo(seekBar.getProgress());
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
        // 注册广播接收器
        IntentFilter filter = new IntentFilter("com.example.mindalert.SONG_CHANGED");
        getActivity().registerReceiver(songChangedReceiver, filter);
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        boolean bindResult = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Service started and bind result: " + bindResult);  // 检查绑定是否成功
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
            Log.d(TAG, "Service unbound");  // 调试信息
        }
        // 注销广播接收器
        getActivity().unregisterReceiver(songChangedReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void setupRotationAnimator() {
        rotationAnimator = ObjectAnimator.ofFloat(ivMusic, "rotation", 0f, 360f);
        rotationAnimator.setDuration(10000);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    }

    private void updateUI() {
        if (musicService != null) {
            sb.setMax(musicService.getDuration());
            tvTotal.setText(formatTime(musicService.getDuration()));
            handler.post(updateSeekBar);
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (isBound && musicService != null && !isUserSeeking) {
                int currentPosition = musicService.getCurrentPosition();
                sb.setProgress(currentPosition);
                tvProgress.setText(formatTime(currentPosition));
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onClick(View v) {
        Button btn_play = getView().findViewById(R.id.btn_play_pause);
        if (isBound) {
            if(v.getId() == R.id.btn_previous){
                musicService.playPreviousSong();
                updateSongName(); // 更新歌曲名称
                btn_play.setText(R.string.music_pause);
                rotationAnimator.start();
                Log.d(TAG, "Previous button clicked");  // 调试信息
            }
            else if(v.getId() == R.id.btn_play_pause){
                if(musicService.isPlaying()){
                    musicService.pauseMusic();
                    btn_play.setText(R.string.music_play);
                    rotationAnimator.pause();
                    Log.d(TAG, "Pause button clicked");  // 调试信息
                }else{
                    musicService.playMusic();
                    btn_play.setText(R.string.music_pause);
                    rotationAnimator.start();
                    Log.d(TAG, "Pause button clicked");  // 调试信息
                }
            }
            else if(v.getId() == R.id.btn_next){
                musicService.playNextSong();
                updateSongName(); // 更新歌曲名称
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
            int currentId = musicService.getCurrentSongResId();
            String title = musicService.getSongList().get(currentId).getTitle();
            //String displayName = getContext().getResources().getString(R.string.now_playing, musicService.getSongList().get(musicService.getCurrentSongResId()).getTitle());
            TextView songNameTextView = getView().findViewById(R.id.song_name);
            if (songNameTextView != null) {
                //songNameTextView.setText(displayName);
                songNameTextView.setText(title);
                tvTotal.setText(formatTime(musicService.getDuration()));
            } else {
                Log.e(TAG, "songNameTextView is null. Check the view initialization.");
            }
        } else {
            Log.e(TAG, "Context is null. Fragment might not be attached to Activity.");
        }
    }
}
