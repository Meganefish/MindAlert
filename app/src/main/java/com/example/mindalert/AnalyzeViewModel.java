package com.example.mindalert;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.github.mikephil.charting.data.Entry;
import java.util.List;

public class AnalyzeViewModel extends ViewModel {
    private AnalyzeService analyzeService;
    private MutableLiveData<List<Entry>> fatigueEntries = new MutableLiveData<>();
    private MutableLiveData<List<Entry>> wakeEntries = new MutableLiveData<>();


    public AnalyzeViewModel(AnalyzeService service) {
        if(service==null)
            return;
        this.analyzeService = service;
        // 初始化数据，或者可以从AnalyzeService加载数据
        fatigueEntries.setValue(analyzeService.fatigueEntries);
        wakeEntries.setValue(analyzeService.wakeEntries);
    }

    public LiveData<List<Entry>> getFatigueEntries() {
        return fatigueEntries;
    }

    public LiveData<List<Entry>> getWakeEntries() {
        return wakeEntries;
    }

    // 更新fatigueEntries的公共方法
    public void updateFatigueEntries(List<Entry> entries) {
        fatigueEntries.setValue(entries);
    }

    // 更新wakeEntries的公共方法
    public void updateWakeEntries(List<Entry> entries) {
        wakeEntries.setValue(entries);
    }
}
