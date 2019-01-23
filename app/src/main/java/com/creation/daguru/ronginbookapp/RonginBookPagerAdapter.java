package com.creation.daguru.ronginbookapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.RonginLibraryActivity.allBooksFragment;
import static com.creation.daguru.ronginbookapp.RonginLibraryActivity.ronginBooksFragment;

public class RonginBookPagerAdapter extends FragmentPagerAdapter {

    public RonginBookPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                if(ronginBooksFragment != null) {
                    //Log.d("KISKIS", "ki hoy chata");
                    return ronginBooksFragment;
                }
                return new RonginBooksFragment();
            case 1:
                if(allBooksFragment != null) return allBooksFragment;
                return new AllBooksFragment();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "rOngin Library";
            case 1:
                return "All Books";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createrFragment = (Fragment) super.instantiateItem(container, position);

        switch (position){
            case 0:
                ronginBooksFragment = (RonginBooksFragment) createrFragment;
                break;
            case 1:
                allBooksFragment = (AllBooksFragment) createrFragment;
                break;
            default:
                break;
        }

        return createrFragment;
    }
}
