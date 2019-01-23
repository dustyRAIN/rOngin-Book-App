package com.creation.daguru.ronginbookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.Utils.ExtraWorks;
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.ListBookFullDetails;
import com.creation.daguru.ronginbookapp.data.MultiOwnerDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_OWNERS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_DISTANCE;
import static com.creation.daguru.ronginbookapp.MainActivity.LATITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.LONGITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

public class MultiOwnersBookDetailsActivity extends AppCompatActivity implements MultiOwnerViewAdapter.MultiOwnerAdapterOnClickHandler {



    private TextView mtvBookHeader;
    private TextView mtvAuthorHeader;

    private ChildEventListener mChildEventListener;

    private ProgressBar mLoading;
    private RecyclerView mRecyclerView;
    private MultiOwnerViewAdapter mAdapter;

    private String mBookUId;
    private String mBookName;
    private String mBookAuthor;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private SharedPreferences mSharedPreferences;
    private double mLatitude;
    private double mLongitude;

    private String tOwnerUId;

    private List<MultiOwnerDetails> mOwnerList;

    private boolean mIsIterDone;
    private boolean mIsNameReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_owners_book_details);

        mtvBookHeader = findViewById(R.id.multi_book_name);
        mtvAuthorHeader = findViewById(R.id.multi_book_author);

        mLoading = findViewById(R.id.multi_book_loading);
        mRecyclerView = findViewById(R.id.multi_owners_recycler_view);

        checkAndUseIntentExtras();

        mOwnerList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MultiOwnerViewAdapter(this, mOwnerList, this);
        mRecyclerView.setAdapter(mAdapter);

        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        checkForSharedPref();

        showList();

        mIsIterDone = true;
        mIsNameReady = true;

        attachDatabaseListener();
    }

    private void checkForSharedPref(){
        if(!mSharedPreferences.contains(LATITUDE_KEY + mFirebaseUser.getUid()) ||
                !mSharedPreferences.contains(LONGITUDE_KEY + mFirebaseUser.getUid())){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            mLatitude = mSharedPreferences.getFloat(LATITUDE_KEY + mFirebaseUser.getUid(), 1);
            mLongitude = mSharedPreferences.getFloat(LONGITUDE_KEY + mFirebaseUser.getUid(), 1);
        }
    }



    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }



    private void attachDatabaseListener(){
        DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mBookUId);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    final String ownerUId = dataSnapshot.getKey();
                    Log.d("owner", ownerUId);
                    DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO).child(ownerUId);
                    mIsNameReady = false;
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserBasicInfo ownerBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                            double dis = ExtraWorks.getDistance(mLatitude, mLongitude,
                                    ownerBasicInfo.latitude, ownerBasicInfo.longitude);

                            mOwnerList.add(new MultiOwnerDetails(dataSnapshot.getKey(),
                                    ownerBasicInfo.firstName + " " + ownerBasicInfo.lastName,
                                    dis));

                            Collections.sort(mOwnerList);

                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            reference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                    .child(mBookUId);
            reference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }













    private void checkAndUseIntentExtras(){

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_UID)){
            mBookUId = getIntent().getStringExtra(EXTRA_KEY_BOOK_UID);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_NAME)){
            String bookName = getIntent().getStringExtra(EXTRA_KEY_BOOK_NAME);
            mBookName = bookName;
            mtvBookHeader.setText(bookName);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_AUTHOR)){
            String authorName = getIntent().getStringExtra(EXTRA_KEY_BOOK_AUTHOR);
            mBookAuthor = authorName;
            mtvAuthorHeader.setText(authorName);
        } else {
            finish();
        }
    }

    public void showLoading(){
        mLoading.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    public void showList(){
        mLoading.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void ownerOnClick(String ownerUId, String ownerName, double distance) {
        Intent intent = new Intent(this, SingleOwnerBookDetailsActivity.class);
        intent.putExtra(EXTRA_KEY_BOOK_UID, mBookUId);
        intent.putExtra(EXTRA_KEY_BOOK_OWNER_UID, ownerUId);
        intent.putExtra(EXTRA_KEY_BOOK_NAME, mBookName);
        intent.putExtra(EXTRA_KEY_BOOK_AUTHOR, mBookAuthor);
        intent.putExtra(EXTRA_KEY_BOOK_OWNER_NAME, ownerName);
        intent.putExtra(EXTRA_KEY_DISTANCE, String.format("%.1f km",distance));
        startActivity(intent);
    }
}
