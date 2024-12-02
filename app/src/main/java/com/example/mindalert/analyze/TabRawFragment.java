package com.example.mindalert.analyze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mindalert.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class TabRawFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_CHART_ID = "chart_id";

    public static TabRawFragment newInstance(String title, int chartId) {
        TabRawFragment fragment = new TabRawFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_CHART_ID, chartId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_raw, container, false);

        // 获取传递过来的参数
        assert getArguments() != null;
        String title = getArguments().getString(ARG_TITLE);
        int chartId = getArguments().getInt(ARG_CHART_ID);

        // 初始化 TextView 和 LineChart
        TextView tvTable = view.findViewById(R.id.tv_table_raw);
        LineChart lineChart = view.findViewById(R.id.lineChart_raw);

        // 设置 TextView 内容
        tvTable.setText(title);

        // 初始化 LineChart 数据（这里只是示例，你可以根据需求填充数据）
        // 这里只是简单设置，你可以根据需求配置 LineChart
        setUpLineChart(lineChart);

        return view;
    }

    private void setUpLineChart(LineChart lineChart) {
        // 设置 LineChart 的配置，添加数据
        // 这里只是简单的配置，可以根据需求进一步完善
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(1, 2));
        entries.add(new Entry(2, 3));
        LineDataSet dataSet = new LineDataSet(entries, "Sample Data");
        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate(); // 刷新图表
    }
}

