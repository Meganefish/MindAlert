package com.example.mindalert;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeService extends Service {
    public List<Entry> fatigueEntries = new ArrayList<>();
    public List<Entry> wakeEntries = new ArrayList<>();
    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler();
    private Runnable dataReaderRunnable;
    private static final String TAG = "AnalyzeService";

    public class LocalBinder extends Binder {
        public AnalyzeService getService() {
            return AnalyzeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //startReadingData();
    }

    public void startReadingData() {
        dataReaderRunnable = new Runnable() {
            @Override
            public void run() {
                readDataFromFile();
                handler.postDelayed(this, 1000); // 每隔1秒读取一次
            }
        };
        handler.post(dataReaderRunnable);
    }
    private static int readLine = 1;

    public void setReadLine(int readLine) {
        AnalyzeService.readLine = readLine;
    }

    public int getReadLine(){
        return readLine;
    }
    private void readDataFromFile() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.result);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int fatigue = 0;
            int wake = 0;

             //读取文件中的数据
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("wake:")) {
                        wake = Integer.parseInt(line.replace("wake:", "").trim());
                    } else if (line.startsWith("fatigue:")) {
                        fatigue = Integer.parseInt(line.replace("fatigue:", "").trim());
                    }
                }
//
//            //读取文件中的数据
//            int k = getReadLine();
//            for(int i = 0; i < k && (line = reader.readLine()) != null; ++i){
//                wake = Integer.parseInt(line.replace("wake:", "").trim());
//                line = reader.readLine();
//                fatigue = Integer.parseInt(line.replace("fatigue:", "").trim());
//            }
            fatigueEntries.add(new Entry(getReadLine(), fatigue));
            wakeEntries.add(new Entry(getReadLine(), wake));

            // 仅保留8组数据
            if (fatigueEntries.size() > 8) {
                fatigueEntries = fatigueEntries.subList(fatigueEntries.size() - 8, fatigueEntries.size());
                wakeEntries = wakeEntries.subList(wakeEntries.size() - 8, wakeEntries.size());
            }
            //setReadLine(k + 1);
            reader.close();

        } catch (IOException e) {
            Log.e(TAG, "Error reading data from file", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(dataReaderRunnable);
    }
}
