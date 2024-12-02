package com.example.mindalert;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mindalert.analyze.AnalyzeFragment;
import com.example.mindalert.analyze.AnalyzeService;
import com.example.mindalert.music.MusicFragment;
import com.example.mindalert.music.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private Button btnAnalyze, btnMap, btnMusic, btnOptions;
    //private FloatingActionButton fabConnect;
    private static Context context;
    //private float dX, dY; // 初始坐标偏移量
    //private float initialX, initialY; // 初始位置
    //private static final int CLICK_THRESHOLD = 100; // 点击的阈值
    private AnalyzeService analyzeService;
    private MusicService musicService;
    //private boolean isDataRead = false;
    private Intent analyzeIntent;
    private boolean isAnalyzeServiceBound = false;
    private boolean isMusicServiceBound = false;

    private ServiceConnection analyzeServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AnalyzeService.LocalBinder binder = (AnalyzeService.LocalBinder) service;
            analyzeService = binder.getService();
            isAnalyzeServiceBound = true;
            if (analyzeService != null) {
                Log.d(TAG,"AnalyzeService Connection Created");
            } else {
                Log.e(TAG, "AnalyzeService is null after onServiceConnected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isAnalyzeServiceBound = false;
            analyzeService = null;  // 确保服务断开时清空
            Log.d(TAG, "AnalyzeService disconnected");  // 调试信息
        }
    };

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isMusicServiceBound = true;
            if (musicService != null) {
                Log.d(TAG,"MusicService Connection Created");
            } else {
                Log.e(TAG, "MusicService is null after onServiceConnected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicServiceBound = false;
            musicService = null;  // 确保服务断开时清空
            Log.d(TAG, "MusicService disconnected");  // 调试信息
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载保存的主题
        loadSavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        analyzeIntent = new Intent(this, AnalyzeService.class);
        boolean analyzeServiceBound = this.bindService(analyzeIntent, analyzeServiceConnection, Context.BIND_AUTO_CREATE);
        if (!analyzeServiceBound) {
            Log.e(TAG, "Failed to bind AnalyzeService");  // 检查服务绑定是否成功
        }

        // 启动 MusicService
        Intent musicIntent = new Intent(this, MusicService.class);
        startService(musicIntent);
        boolean musicServiceBound =  this.bindService(musicIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        if (!musicServiceBound) {
            Log.e(TAG, "Failed to bind MusicService");  // 检查服务绑定是否成功
        }

        btnAnalyze = findViewById(R.id.btn_analyze);
        btnMap = findViewById(R.id.btn_map);
        btnMusic = findViewById(R.id.btn_music);
        btnOptions = findViewById(R.id.btn_options);
        //fabConnect = findViewById(R.id.fab_connect);

        // 设置按钮点击事件
        btnAnalyze.setOnClickListener(v -> replaceFragment(new AnalyzeFragment()));
        btnMap.setOnClickListener(v -> replaceFragment(new MapFragment()));
        btnMusic.setOnClickListener(v -> replaceFragment(new MusicFragment()));
        btnOptions.setOnClickListener(v -> replaceFragment(new OptionsFragment()));

//        fabConnect.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        dX = view.getX() - event.getRawX();
//                        dY = view.getY() - event.getRawY();
//                        initialX = event.getRawX();
//                        initialY = event.getRawY();
//                        Log.d(TAG,"Fab downed");
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        view.animate()
//                                .x(event.getRawX() + dX)
//                                .y(event.getRawY() + dY)
//                                .setDuration(0)
//                                .start();
//
//                        Log.d(TAG,"Fab moved");
//                    case MotionEvent.ACTION_UP:
//                        float finalX = event.getRawX();
//                        float finalY = event.getRawY();
//                        if (Math.abs(finalX - initialX) < CLICK_THRESHOLD && Math.abs(finalY - initialY) < CLICK_THRESHOLD) {
//                            // 如果拖动距离在阈值内，视为点击
//                            Log.d(TAG,"Fab clicked");
//                            view.performClick(); // 调用View的点击方法
//                        }
//                        break;
//                    default:
//                        return false;
//                }
//                return true;
//            }
//        });
//        fabConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!isDataRead)
//                    analyzeService.startReadingData();
//                isDataRead = true;
//            }
//        });
        // 默认显示 AnalyzeFragment
        replaceFragment(new AnalyzeFragment());
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (isAnalyzeServiceBound) {
            this.unbindService(analyzeServiceConnection);
            isAnalyzeServiceBound = false;
            Log.d(TAG, "AnalyzeService Unbound");
        }
        if(isMusicServiceBound){
            this.unbindService(musicServiceConnection);
            isMusicServiceBound = false;
            Log.d(TAG, "MusicService Unbound");
        }
    }


    // 切换Fragment的方法
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public static Context getContext() {
        return context;
    }
    public AnalyzeService getAnalyzeService(){
        return this.analyzeService;
    }
    public Intent getAnalyzeIntent(){ return this.analyzeIntent; }
    public MusicService getMusicService() {return this.musicService; }
    public boolean getIsMusicServiceBound(){
        return this.isMusicServiceBound;
    }

    // 提供一个方法获取LiveData
    private void loadSavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String theme = sharedPreferences.getString("Theme", "Light"); // 默认值为“Light”

        switch (theme) {
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "System":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
