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

public class TabPerclosFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CHART_ID = "chart_id";
    private LineChart lineChart;
    private TextView tvTable;
    private ArrayList<Integer> perclos = new ArrayList<>();

    public static TabPerclosFragment newInstance(String title, int chartId) {
        TabPerclosFragment fragment = new TabPerclosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_CHART_ID, chartId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_perclos, container, false);

        // 获取传递过来的参数
        assert getArguments() != null;
        String title = getArguments().getString(ARG_TITLE);

        // 初始化 TextView 和 LineChart
        tvTable = view.findViewById(R.id.tv_table_perclos);
        lineChart = view.findViewById(R.id.lineChart_perclos);

        // 设置 TextView 内容
        tvTable.setText(title);

        setUpLineChart(lineChart);
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

    private void updateLineChart(LineChart lineChart, List<Integer> data, int maxRange) {
        List<Entry> entries = new ArrayList<>();

        // 处理数据，只保留最新的 8 条数据
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Perclos");
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

        analyzeService.getPerclosLiveData().observe(getViewLifecycleOwner(), perclosData -> {
            perclos.clear();
            perclos.addAll(perclosData);
            updateLineChart(lineChart, perclos, 100);
        });
    }
}

