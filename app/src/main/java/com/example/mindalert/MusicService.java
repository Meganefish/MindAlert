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
            try{
                 tmpMP = MediaPlayer.create(this,resId);
            }catch (Exception e){
                continue;       // @TODO 未设置对异常的处理，不安全
            }
            Song newSong = new Song(resourceName, songUri);
            songList.add(newSong);
            tmpMP.release();
            Log.d(TAG, "Added song: " + resourceName);
        }
    }

    private void notifySongChanged() {
        Intent intent = new Intent("com.example.mindalert.SONG_CHANGED");
        sendBroadcast(intent);
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

        public Song(String title, Uri uri) {
            this.title = title;
            this.uri = uri;
            this.index = ++songIndex;
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
    }
}
