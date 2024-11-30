package com.example.doorlock.help;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class PageAdapter extends FragmentStateAdapter{
    ArrayList<Fragment> fragments=new ArrayList<Fragment>();
    public PageAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Fragment>fragments){
        super(fragmentActivity);
        this.fragments=fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new Fragment1();
            case 1:
                return new Fragment2();
            default:
                return new Fragment3();
        }
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}

