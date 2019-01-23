package com.creation.daguru.ronginbookapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import static com.creation.daguru.ronginbookapp.UserLibraryActivity.userBooksBorrowedFragment;
import static com.creation.daguru.ronginbookapp.UserLibraryActivity.userBooksFragment;
import static com.creation.daguru.ronginbookapp.UserLibraryActivity.userBooksLentFragment;

public class UserBookPagerAdapter extends FragmentPagerAdapter {

    public UserBookPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                if(userBooksFragment != null) {
                    //Log.d("KISKIS", "ki hoy chata");
                    return userBooksFragment;
                }
                return new UserBooksFragment();
            case 1:
                if(userBooksLentFragment != null) return userBooksLentFragment;
                return new UserBooksLentFragment();
            case 2:
                if(userBooksBorrowedFragment != null) return userBooksBorrowedFragment;
                return new UserBooksBorrowedFragment();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "My Books";
            case 1:
                return "Books Lent";
            case 2:
                return "Books Borrowed";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createrFragment = (Fragment) super.instantiateItem(container, position);

        switch (position){
            case 0:
                userBooksFragment = (UserBooksFragment) createrFragment;
                break;
            case 1:
                userBooksLentFragment = (UserBooksLentFragment) createrFragment;
                break;
            case 2:
                userBooksBorrowedFragment = (UserBooksBorrowedFragment) createrFragment;
            default:
                break;
        }

        return createrFragment;
    }
}
