package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RonginAuthStateChangeListener implements FirebaseAuth.AuthStateListener {

    public FirebaseAuth mFirebaseAuth;
    public OnUserAuthChanged mOnUserAuthChanged;

    public interface OnUserAuthChanged{
        void userAuthinticated();
        void userNotAuthinticated();
    }

    RonginAuthStateChangeListener(FirebaseAuth firebaseAuth, OnUserAuthChanged onUserAuthChanged){
        mFirebaseAuth = firebaseAuth;
        mOnUserAuthChanged = onUserAuthChanged;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if(user != null){
            mOnUserAuthChanged.userAuthinticated();
        } else {
            mOnUserAuthChanged.userNotAuthinticated();
        }
    }
}
