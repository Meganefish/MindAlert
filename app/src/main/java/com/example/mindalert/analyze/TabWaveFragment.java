package com.example.mindalert.analyze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mindalert.MainActivity;
import com.example.mindalert.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class TabWaveFragment extends Fragment {

    private static final String ARG_TITLE1 = "title1";
    private static final String ARG_CHART_ID1 = "chart_id1";
    private static final String ARG_TITLE2 = "title2";
    private static final String ARG_CHART_ID2 = "chart_id2";

    private LineChart lineChart1, lineChart2;
    private TextView tvTable1, tvTable2;

    private ArrayList<Integer> alpha = new ArrayList<>();
    private ArrayList<Integer> theta = new ArrayList<>();
    private ArrayList<Integer> beta = new ArrayList<>();
    private ArrayList<Integer> gamma = new ArrayList<>();
    private ArrayList<Integer> delta = new ArrayList<>();


    public static TabWaveFragment newInstance(String title1, int chartId1, String title2, int chartId2) {
        TabWaveFragment fragment = new TabWaveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE1, title1);
        args.putInt(ARG_CHART_ID1, chartId1);
        args.putString(ARG_TITLE2, title2);
        args.putInt(ARG_CHART_ID2, chartId2);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_wave, container, false);

        // 获取传递过来的参数
        assert getArguments() != null;
        String title1 = getArguments().getString(ARG_TITLE1);
        String title2 = getArguments().getString(ARG_TITLE2);

        // 初始化 TextView 和 LineChart
        tvTable1 = view.findViewById(R.id.tv_table_wave1);
        lineChart1 = view.findViewById(R.id.lineChart_wave1);
        tvTable2 = view.findViewById(R.id.tv_table_wave2);
        lineChart2 = view.findViewById(R.id.lineChart_wave2);

        // 设置 TextView 内容
        tvTable1.setText(title1);
        tvTable2.setText(title2);

        // 初始化 LineChart 数据（这里只是示例，你可以根据需求填充数据）
        setUpLineChart(lineChart1);
        setUpLineChart(lineChart2);

        // 设置观察者，监听 AnalyzeService 中的数据变化
        //observeAnalyzeServiceData();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null && mainActivity.getAnalyzeService() != null) {
            // 服务已初始化，开始观察数据
            observeAnalyzeServiceData();
        } else {
            // 如果服务未初始化，等到后续再绑定
            mainActivity.setAnalyzeServiceReadyListener(new MainActivity.OnAnalyzeServiceReadyListener() {
                @Override
                public void onServiceReady() {
                    observeAnalyzeServiceData(); // 服务准备好后设置观察者
                }
            });
        }
    }

    private void setUpLineChart(LineChart lineChart) {
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
    }

    private void updateLineChart1(LineChart lineChart, List<Integer> alphaData, List<Integer> betaData,
                                  List<Integer> gammaData, List<Integer> thetaData, int maxRange) {

        // 创建 Entry 列表，用于存储每一组数据
        List<Entry> alphaEntries = new ArrayList<>();
        List<Entry> betaEntries = new ArrayList<>();
        List<Entry> gammaEntries = new ArrayList<>();
        List<Entry> thetaEntries = new ArrayList<>();

        // 填充数据，最多保留最新的 8 条数据
        for (int i = 0; i < alphaData.size(); i++) {
            alphaEntries.add(new Entry(i, alphaData.get(i)));
            if (i < betaData.size())
                betaEntries.add(new Entry(i, betaData.get(i)));
            if (i < gammaData.size())
                gammaEntries.add(new Entry(i, gammaData.get(i)));
            if (i < thetaData.size())
                thetaEntries.add(new Entry(i, thetaData.get(i)));
        }

        // 创建 LineDataSet，每一组数据使用不同的颜色
        LineDataSet alphaDataSet = new LineDataSet(alphaEntries, "Alpha");
        alphaDataSet.setColor(getResources().getColor(R.color.alphaColor)); // 颜色
        alphaDataSet.setDrawCircles(true);
        alphaDataSet.setValueTextColor(getResources().getColor(R.color.alphaValueColor));

        LineDataSet betaDataSet = new LineDataSet(betaEntries, "Beta");
        betaDataSet.setColor(getResources().getColor(R.color.betaColor));
        betaDataSet.setDrawCircles(true);
        betaDataSet.setValueTextColor(getResources().getColor(R.color.betaValueColor));

        LineDataSet gammaDataSet = new LineDataSet(gammaEntries, "Gamma");
        gammaDataSet.setColor(getResources().getColor(R.color.gammaColor));
        gammaDataSet.setDrawCircles(true);
        gammaDataSet.setValueTextColor(getResources().getColor(R.color.gammaValueColor));

        LineDataSet thetaDataSet = new LineDataSet(thetaEntries, "Theta");
        thetaDataSet.setColor(getResources().getColor(R.color.thetaColor));
        thetaDataSet.setDrawCircles(true);
        thetaDataSet.setValueTextColor(getResources().getColor(R.color.thetaValueColor));

        // 将所有数据集合添加到 LineData 中
        LineData lineData = new LineData(alphaDataSet, betaDataSet, gammaDataSet, thetaDataSet);

        // 更新 LineChart 数据
        lineChart.setData(lineData);
        lineChart.invalidate(); // 刷新图表
    }

    private void updateLineChart2(LineChart lineChart, List<Integer> data, int maxRange) {
        List<Entry> entries = new ArrayList<>();

        // 处理数据，只保留最新的 8 条数据
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Delta");
        dataSet.setDrawCircles(true);
        dataSet.setColor(getResources().getColor(R.color.black));
        dataSet.setValueTextColor(getResources().getColor(R.color.blue));
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // 刷新图表
    }

    private void observeAnalyzeServiceData() {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        AnalyzeService analyzeService = mainActivity.getAnalyzeService();

        analyzeService.getAlphaLiveData().observe(getViewLifecycleOwner(), alphaData -> {
            // 每隔5秒更新一次 alpha 数据
            alpha.clear();
            alpha.addAll(alphaData);
            updateLineChart1(lineChart1, alpha, beta, gamma, theta, 100);
        });

        analyzeService.getThetaLiveData().observe(getViewLifecycleOwner(), thetaData -> {
            // 每隔5秒更新一次 theta 数据
            theta.clear();
            theta.addAll(thetaData);
            updateLineChart1(lineChart1, alpha, beta, gamma, theta, 100);
        });

        analyzeService.getBetaLiveData().observe(getViewLifecycleOwner(), betaData -> {
            // 每隔5秒更新一次 beta 数据
            beta.clear();
            beta.addAll(betaData);
            updateLineChart1(lineChart1, alpha, beta, gamma, theta, 100);
        });

        analyzeService.getGammaLiveData().observe(getViewLifecycleOwner(), gammaData -> {
            // 每隔5秒更新一次 gamma 数据
            gamma.clear();
            gamma.addAll(gammaData);
            updateLineChart1(lineChart1, alpha, beta, gamma, theta, 100);
        });

        analyzeService.getDeltaLiveData().observe(getViewLifecycleOwner(), deltaData -> {
            // 每隔5秒更新一次 delta 数据
            delta.clear();
            delta.addAll(deltaData);
            updateLineChart2(lineChart2, delta, 2000);
        });
    }
}