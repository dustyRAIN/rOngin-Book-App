package com.creation.daguru.ronginbookapp;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UserLibraryActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private UserBookPagerAdapter mAdapter;

    public static UserBooksFragment userBooksFragment;
    public static UserBooksLentFragment userBooksLentFragment;
    public static UserBooksBorrowedFragment userBooksBorrowedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_library);

        mTabLayout = findViewById(R.id.user_lib_tab_layout);
        mViewPager = findViewById(R.id.user_lib_view_pager);
        mAdapter = new UserBookPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
