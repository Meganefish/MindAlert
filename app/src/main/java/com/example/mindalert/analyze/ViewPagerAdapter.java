package com.example.mindalert.analyze;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mindalert.R;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<String> titles;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> titles) {
        super(fragmentActivity);
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置创建不同的 Fragment
        switch (position) {
            case 0:
                return TabRawFragment.newInstance("Raw data", R.id.lineChart_raw);
            case 1:
                return TabWaveFragment.newInstance("Wave data", R.id.lineChart_wave1,"delta", R.id.lineChart_wave2);
            case 2:
                return TabPerclosFragment.newInstance("Perclos", R.id.lineChart_perclos);
            default:
                return TabRawFragment.newInstance("Raw data", R.id.lineChart_raw);
        }
    }

    @Override
    public int getItemCount() {
        return titles.size(); // 这里返回 Tab 的数量
    }

//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return titles.get(position);
//    }
}

