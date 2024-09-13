package com.example.mindalert;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class OptionsFragment extends Fragment {

    private Spinner spinnerThemes, spinnerLanguage;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        // 初始化控件
        spinnerThemes = view.findViewById(R.id.spinner_themes);
        spinnerLanguage = view.findViewById(R.id.spinner_language);

        // 获取SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);

        // 设置主题 Spinner
        ArrayAdapter<CharSequence> themesAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.themes_options, android.R.layout.simple_spinner_item);
        themesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThemes.setAdapter(themesAdapter);
        spinnerThemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setTheme(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // 设置语言 Spinner
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.language_options, android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setLanguage(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // 加载当前设置
        loadSettings();

        return view;
    }

    private void setTheme(int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (position) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // Light
                editor.putString("Theme", "Light");
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);  // Dark
                editor.putString("Theme", "Dark");
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);  // System Default
                editor.putString("Theme", "System");
                break;
        }
        editor.apply();
    }

    private void setLanguage(int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (position) {
            case 0:
                updateLocale("en");  // English
                editor.putString("Language", "En");
                break;
            case 1:
                updateLocale("zh");  // Chinese
                editor.putString("Language", "Ch");
                break;
        }
        editor.apply();
    }
    private void updateLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());

        // 只刷新当前Fragment
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    private void loadSettings() {
        String theme = sharedPreferences.getString("Theme", "Light");
        String language = sharedPreferences.getString("Language", "En");

        // 设置 Spinner 位置
        if (theme != null) {
            switch (theme) {
                case "Light":
                    spinnerThemes.setSelection(0);
                    break;
                case "Dark":
                    spinnerThemes.setSelection(1);
                    break;
                case "System":
                    spinnerThemes.setSelection(2);
                    break;
            }
        }

        if (language != null) {
            switch (language) {
                case "En":
                    spinnerLanguage.setSelection(0);
                    break;
                case "Ch":
                    spinnerLanguage.setSelection(1);
                    break;
            }
        }
    }
}
