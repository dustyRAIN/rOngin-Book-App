package com.creation.daguru.ronginbookapp;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RonginLibraryActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private RonginBookPagerAdapter mAdapter;

    public static RonginBooksFragment ronginBooksFragment;
    public static AllBooksFragment allBooksFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rongin_library);

        mTabLayout = findViewById(R.id.rong_lib_tab_layout);
        mViewPager = findViewById(R.id.rong_lib_view_pager);
        mAdapter = new RonginBookPagerAdapter(getSupportFragmentManager());

        //addFragments();

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }
}
