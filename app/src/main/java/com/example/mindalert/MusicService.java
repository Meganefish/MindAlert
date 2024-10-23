package com.example.mindalert;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private static int songIndex = 1;
    private int currentSongIndex = 0;
    private List<Song> songList = new ArrayList<>();

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        initializeSongList();
        setupMediaPlayer();
    }

    private void initializeSongList() {
        MediaPlayer tmpMP;
        Context context = getApplicationContext();
        Resources resources = context.getResources();
        Field[] fields = R.raw.class.getFields();

        for (Field field : fields) {
            String resourceName = field.getName();
            Uri songUri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + resourceName);
            int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());

            // 获取对应图片的资源 ID
            int imageResId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

            try {
                tmpMP = MediaPlayer.create(this, resId);
            } catch (Exception e) {
                continue;
            }

            Song newSong = new Song(resourceName, songUri, imageResId);  // 传入图片资源 ID
            songList.add(newSong);
            tmpMP.release();
            Log.d(TAG, "Added song: " + resourceName);
        }
    }


    private void notifySongChanged() {
        Intent intent = new Intent("com.example.mindalert.SONG_CHANGED");
        sendBroadcast(intent);
    }

    // 根据歌曲标题获取歌曲索引
    public int getSongIndexByTitle(String title) {
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1; // 未找到
    }

    // 播放指定索引的歌曲
    public void playSongAtIndex(int songIndex) {
        if (songIndex < 0 || songIndex >= songList.size()) {
            Log.e("MusicService", "Invalid song index");
            return;
        }

        Song song = songList.get(songIndex);
        currentSongIndex = songIndex;

        // 停止当前播放的音乐（如果有的话）
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset(); // 重置 MediaPlayer 以准备播放新歌曲

        try {
            // 设置数据源为指定的歌曲文件
            mediaPlayer.setDataSource(song.getUri().toString());
            mediaPlayer.prepare(); // 准备播放（同步）
            mediaPlayer.start();   // 开始播放

            // 通知 UI 更新当前播放的歌曲信息
            Log.d("MusicService", "Playing song: " + song.getTitle());

        } catch (IOException e) {
            Log.e("MusicService", "Error playing song: " + e.getMessage());
        }
    }

    private void setupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (!songList.isEmpty()) {
            mediaPlayer = MediaPlayer.create(this, songList.get(currentSongIndex).getUri());
            mediaPlayer.setOnCompletionListener(mp -> {
                playNextSong();
                notifySongChanged(); // 发送广播通知歌曲已切换
            });
            Log.d(TAG, "MediaPlayer set up for song index: " + currentSongIndex);
        }
    }

    public void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d(TAG, "Music started");
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d(TAG, "Music paused");
        }
    }

    public void playNextSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        currentSongIndex = (currentSongIndex + 1) % songList.size();
        setupMediaPlayer();
        playMusic();
    }

    public void playPreviousSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
        setupMediaPlayer();
        playMusic();
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public int getCurrentSongResId() {
        return currentSongIndex;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.d(TAG, "Service destroyed");
    }

    public List<Song> getSongList(){
        return this.songList;
    }

    public static class Song {
        private String title;
        private Uri uri;
        private int index;
        private int imageResId;  // 新增字段，保存图片资源 ID

        public Song(String title, Uri uri, int imageResId) {
            this.title = title;
            this.uri = uri;
            this.index = ++songIndex;
            this.imageResId = imageResId;  // 初始化图片资源 ID
        }

        public String getTitle() {
            return title;
        }

        public Uri getUri() {
            return uri;
        }

        public int getIndex() {
            return index;
        }

        public int getImageResId() {
            return imageResId;  // 返回图片资源 ID
        }


    }

}
