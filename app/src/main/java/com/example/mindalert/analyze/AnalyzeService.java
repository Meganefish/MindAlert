package com.example.mindalert.analyze;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mindalert.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class AnalyzeService extends Service {
    private boolean isReading = false;  // 用来标记是否正在读取文件
    //private boolean isRawReading = false;  // 用来标记是否正在读取文件
    // 读取数据的方法
    private Thread readingThread;  // 用于存储读取文件的线程
    private Thread readingRawThread;  // 用于存储读取文件的线程
    // 数组用于保存最近的8组数据
    // 使用 ArrayList 来存储数据
    ArrayList<Integer> delta = new ArrayList<>();
    ArrayList<Integer> theta = new ArrayList<>();
    ArrayList<Integer> alpha = new ArrayList<>();
    ArrayList<Integer> beta = new ArrayList<>();
    ArrayList<Integer> gamma = new ArrayList<>();
    ArrayList<Integer> perclos = new ArrayList<>();
    ArrayList<Integer> raw = new ArrayList<>();
    private MutableLiveData<ArrayList<Integer>> alphaLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> thetaLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> betaLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> gammaLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> deltaLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> perclosLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> rawLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<Integer>> getAlphaLiveData() {
        return alphaLiveData;
    }

    public LiveData<ArrayList<Integer>> getThetaLiveData() {
        return thetaLiveData;
    }

    public LiveData<ArrayList<Integer>> getBetaLiveData() {
        return betaLiveData;
    }

    public LiveData<ArrayList<Integer>> getGammaLiveData() {
        return gammaLiveData;
    }

    public LiveData<ArrayList<Integer>> getDeltaLiveData() {
        return deltaLiveData;
    }

    public LiveData<ArrayList<Integer>> getPerclosLiveData() {
        return perclosLiveData;
    }

    public LiveData<ArrayList<Integer>> getRawLiveData() {
        return rawLiveData;
    }

    // 更新数据的方法
    public void updateData(ArrayList<Integer> alpha, ArrayList<Integer> theta, ArrayList<Integer> beta, ArrayList<Integer> gamma, ArrayList<Integer> delta) {
        alphaLiveData.postValue(alpha);
        thetaLiveData.postValue(theta);
        betaLiveData.postValue(beta);
        gammaLiveData.postValue(gamma);
        deltaLiveData.postValue(delta);
    }

    public void updatePerclosData(ArrayList<Integer> perclos) {
        perclosLiveData.postValue(perclos);
    }

    public void updateRawData(ArrayList<Integer> raw) {
        rawLiveData.postValue(raw);
    }

    // Binder 类
    public class LocalBinder extends Binder {
        public AnalyzeService getService() {
            return AnalyzeService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // 停止读取操作
        stopReading();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 通过intent来控制是否启动或停止服务
        if (isReading) {
            // 服务已经在读取数据，停止读取
            stopReading();
        } else {
            // 启动读取数据
            readDataFromFile();
        }
        return START_STICKY;  // 保证服务在被杀死时会重新启动
    }


    // 启动读取文件数据
    public void readDataFromFile() {
        if (isReading) {
            return;  // 如果已经在读取数据，直接返回
        }
        isReading = true;
        // 如果没有线程，创建一个新线程来读取文件
        if (readingThread == null || !readingThread.isAlive()) {
            readingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isReading) {
                        try {
                            // 每隔5秒读取一次wave.txt和perclos.txt
                            readWaveFile();  // 自定义方法，读取wave.txt
                            readPerclosFile();  // 自定义方法，读取perclos.txt
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        readingThread.start();  // 启动线程
        if (readingRawThread == null || !readingRawThread.isAlive()) {
            readingRawThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isReading) {
                        try {
                            // 每隔0.05秒读取一次raw.txt
                            readRawFile();  // 自定义方法，读取raw.txt
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        readingRawThread.start();
    }

    // 停止读取文件数据
    public void stopReading() {
        isReading = false;  // 设置标志，停止读取数据
        // 如果线程存在且已启动，则暂停线程
        if (readingThread != null && readingThread.isAlive()) {
            readingThread.interrupt();  // 中断线程，停止读取
        }
        if (readingRawThread != null && readingRawThread.isAlive()) {
            readingRawThread.interrupt();  // 中断线程，停止读取
        }
    }
    int readWaveLine = 0;
    // 读取文件的具体实现
    private void readWaveFile() {
        InputStream fis = null;
        BufferedReader reader = null;

        try {
            fis = getResources().openRawResource(R.raw.wave); // 获取文件输入流
            reader = new BufferedReader(new InputStreamReader(fis));

            String line;
            int k = readWaveLine;
            for(int i = 0;  i < k && (line = reader.readLine()) != null; ++i){
                if (line.startsWith("Delta:")) {
                    int deltaValue = Integer.parseInt(line.split(":")[1].trim());
                    delta.add(deltaValue);
                    // 保证 ArrayList 长度不超过 8
                    if (delta.size() > 8) {
                        delta.remove(0); // 移除最前面的元素
                    }
                } else if (line.startsWith("Theta:")) {
                    int thetaValue = Integer.parseInt(line.split(":")[1].trim());
                    theta.add(thetaValue);
                    if (theta.size() > 8) {
                        theta.remove(0);
                    }
                } else if (line.startsWith("Alpha:")) {
                    int alphaValue = Integer.parseInt(line.split(":")[1].trim());
                    alpha.add(alphaValue);
                    if (alpha.size() > 8) {
                        alpha.remove(0);
                    }
                } else if (line.startsWith("Beta:")) {
                    int betaValue = Integer.parseInt(line.split(":")[1].trim());
                    beta.add(betaValue);
                    if (beta.size() > 8) {
                        beta.remove(0);
                    }
                } else if (line.startsWith("Gamma:")) {
                    int gammaValue = Integer.parseInt(line.split(":")[1].trim());
                    gamma.add(gammaValue);
                    if (gamma.size() > 8) {
                        gamma.remove(0);
                    }
                }
            }
            readWaveLine++;
//            while ((line = reader.readLine()) != null) {
//                // 每次读取一行，解析并更新数组
//                if (line.startsWith("Delta:")) {
//                    int deltaValue = Integer.parseInt(line.split(":")[1].trim());
//                    delta.add(deltaValue);
//                    // 保证 ArrayList 长度不超过 8
//                    if (delta.size() > 8) {
//                        delta.remove(0); // 移除最前面的元素
//                    }
//                } else if (line.startsWith("Theta:")) {
//                    int thetaValue = Integer.parseInt(line.split(":")[1].trim());
//                    theta.add(thetaValue);
//                    if (theta.size() > 8) {
//                        theta.remove(0);
//                    }
//                } else if (line.startsWith("Alpha:")) {
//                    int alphaValue = Integer.parseInt(line.split(":")[1].trim());
//                    alpha.add(alphaValue);
//                    if (alpha.size() > 8) {
//                        alpha.remove(0);
//                    }
//                } else if (line.startsWith("Beta:")) {
//                    int betaValue = Integer.parseInt(line.split(":")[1].trim());
//                    beta.add(betaValue);
//                    if (beta.size() > 8) {
//                        beta.remove(0);
//                    }
//                } else if (line.startsWith("Gamma:")) {
//                    int gammaValue = Integer.parseInt(line.split(":")[1].trim());
//                    gamma.add(gammaValue);
//                    if (gamma.size() > 8) {
//                        gamma.remove(0);
//                    }
//                }
//
//                // 每次读取一行都进行日志输出
//                Log.d("AnalyzeService", "Reading data from /raw/wave.txt: " + line);
//            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateData(alpha, theta, beta, gamma, delta);

        // 输出最后8组数据（调试时查看）
        Log.d("AnalyzeService", "Delta: " + delta);
        Log.d("AnalyzeService", "Theta: " + theta);
        Log.d("AnalyzeService", "Alpha: " + alpha);
        Log.d("AnalyzeService", "Beta: " + beta);
        Log.d("AnalyzeService", "Gamma: " + gamma);
    }
    int readPerclosLine = 0;
    private void readPerclosFile() {
        InputStream fis = null;
        BufferedReader reader = null;

        try {
            fis = getResources().openRawResource(R.raw.perclos); // 获取文件输入流
            reader = new BufferedReader(new InputStreamReader(fis));

            String line;

            int perclosValue = 0;
            int k = readPerclosLine;
            for(int i = 0;  i < k && (line = reader.readLine()) != null; ++i){
                if (line.startsWith("Perclos:")) {
                    perclosValue = Integer.parseInt(line.split(":")[1].trim());
                    perclos.add(perclosValue);
                    if (perclos.size() > 10) {
                        perclos.remove(0); // 移除最前面的元素
                    }
                }
            }
            readPerclosLine++;
//            while ((line = reader.readLine()) != null) {
//                // 每次读取一行，解析并更新数组
//                if (line.startsWith("Perclos:")) {
//                    int perclosValue = Integer.parseInt(line.split(":")[1].trim());
//                    perclos.add(perclosValue);
//                    // 保证 ArrayList 长度不超过 8
//                    if (perclos.size() > 8) {
//                        perclos.remove(0); // 移除最前面的元素
//                    }
//                }
//                // 每次读取一行都进行日志输出
//                Log.d("AnalyzeService", "Reading data from /raw/perclos.txt: " + line);
//            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updatePerclosData(perclos);
        Log.d("AnalyzeService", "Perclos: " + perclos);
    }
    private int readRawLine = 0;
    private void readRawFile() {
        InputStream fis = null;
        BufferedReader reader = null;

        try {
            fis = getResources().openRawResource(R.raw.raw); // 获取文件输入流
            reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            int rawValue = 0;
            int k = readRawLine;
            for(int i = 0;  i < k && (line = reader.readLine()) != null; ++i){
                if (line.startsWith("Raw:")) {
                     rawValue = Integer.parseInt(line.split(":")[1].trim());
                    raw.add(rawValue);
                    if (raw.size() > 40) {
                        raw.remove(0); // 移除最前面的元素
                    }
                }
            }
            readRawLine++;
//            while ((line = reader.readLine()) != null) {
//                if (line.startsWith("Raw:")) {
//                    rawValue = Integer.parseInt(line.replace("Raw:", "").trim());
//                }
//                raw.add(rawValue);
//                if (raw.size() > 8) {
//                    raw.remove(0); // 移除最前面的元素
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateRawData(raw);
        Log.d("AnalyzeService", "raw: " + raw);
    }

}
