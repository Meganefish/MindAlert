package com.example.mindalert;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

// 自定义 ViewModelProvider.Factory 类
public class AnalyzeViewModelFactory implements ViewModelProvider.Factory {

    private final AnalyzeService analyzeService;

    // 构造方法，接收 AnalyzeService 实例
    public AnalyzeViewModelFactory(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AnalyzeViewModel.class)) {
            // 使用自定义的构造方法创建 AnalyzeViewModel 实例
            return (T) new AnalyzeViewModel(analyzeService);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
