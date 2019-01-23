package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;

import com.creation.daguru.ronginbookapp.Utils.ExtraWorks;
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.ListBookFullDetails;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_DISTANCE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OTHER_USER_LATITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OTHER_USER_LONGITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.MainActivity.LATITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.LONGITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

public class ShowUserBooksActivity extends AppCompatActivity implements BooksViewAdapter.BooksAdapterOnClickHandler {



    private RecyclerView mRecyclerView;
    private BooksViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private List<ListBookFullDetails> mFullDetailsList;
    private ProgressBar mLoading;
    private SharedPreferences mSharedPreferences;
    private ChildEventListener mChildEventListener;

    private String mOtherUserUId;
    private String mOtherUserName;

    private double mDistance;
    private double mUserLatitude;
    private double mUserLongitude;
    private double mOtherUserLatitude;
    private double mOtherUserLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_books);

        checkForIntentExtras();
        checkForLocation();

        mDistance = ExtraWorks.getDistance(mUserLatitude, mUserLongitude,
                mOtherUserLatitude, mOtherUserLongitude);

        mRecyclerView = findViewById(R.id.all_books_lib_recycle_view);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFullDetailsList = new ArrayList<>();
        mAdapter = new BooksViewAdapter(this, this, mFullDetailsList);
        mRecyclerView.setAdapter(mAdapter);

        attachDatabaseListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    private void checkForLocation(){
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(!mSharedPreferences.contains(LATITUDE_KEY + mFirebaseUser.getUid()) ||
                !mSharedPreferences.contains(LONGITUDE_KEY + mFirebaseUser.getUid())){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            mUserLatitude = mSharedPreferences.getFloat(LATITUDE_KEY + mFirebaseUser.getUid(), 1);
            mUserLongitude = mSharedPreferences.getFloat(LONGITUDE_KEY + mFirebaseUser.getUid(), 1);
        }
    }

    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_OTHER_USER_NAME)){
            mOtherUserName = getIntent().getStringExtra(EXTRA_KEY_OTHER_USER_NAME);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_OTHER_USER_UID)){
            mOtherUserUId = getIntent().getStringExtra(EXTRA_KEY_OTHER_USER_UID);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_OTHER_USER_LATITUDE)){
            mOtherUserLatitude = getIntent().getDoubleExtra(EXTRA_KEY_OTHER_USER_LATITUDE, 1);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_OTHER_USER_LONGITUDE)){
            mOtherUserLongitude = getIntent().getDoubleExtra(EXTRA_KEY_OTHER_USER_LONGITUDE, 1);
        } else {
            finish();
        }
    }

    private void attachDatabaseListener(){
        DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                .child(mOtherUserUId);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserLibraryBookDetails libBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);

                    if(libBookDetails.isVisible == 1){
                        mFullDetailsList.add(new ListBookFullDetails(
                                libBookDetails.bookUId,
                                mOtherUserUId,
                                libBookDetails.bookName,
                                libBookDetails.authorName,
                                String.format("%.1f km",mDistance),
                                mOtherUserName,
                                1
                        ));
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserLibraryBookDetails libBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    ListBookFullDetails bookFullDetails =new ListBookFullDetails(
                            libBookDetails.bookUId,
                            mOtherUserUId,
                            libBookDetails.bookName,
                            libBookDetails.authorName,
                            String.format("%.1f km",mDistance),
                            mOtherUserName,
                            1
                    );
                    int id = getListItemId(bookFullDetails);
                    if(id == -1){
                        if(libBookDetails.isVisible == 1){
                            mFullDetailsList.add(bookFullDetails);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        mFullDetailsList.set(id, bookFullDetails);
                        mAdapter.notifyItemChanged(id);
                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    UserLibraryBookDetails libBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    ListBookFullDetails bookFullDetails =new ListBookFullDetails(
                            libBookDetails.bookUId,
                            mOtherUserUId,
                            libBookDetails.bookName,
                            libBookDetails.authorName,
                            String.format("%.1f km",mDistance),
                            mOtherUserName,
                            1
                    );
                    int id = getListItemId(bookFullDetails);

                    if(id != -1){
                        mFullDetailsList.remove(id);
                        mAdapter.notifyItemRemoved(id);
                    }
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

    private int getListItemId(ListBookFullDetails bookFullDetails){
        int i = 0;
        for(ListBookFullDetails details: mFullDetailsList){
            if(details.bookUId.equals(bookFullDetails.bookUId)){
                return i;
            }
            i++;
        }

        return -1;
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS);
            reference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void bookOnclick(String bookUId, String ownerUId, String bookName, String authorName, String ownerName,
                            String distance) {
        if(ownerUId != null && !ownerUId.equals("") && !ownerUId.isEmpty()){
            Intent intent = new Intent(this, SingleOwnerBookDetailsActivity.class);
            intent.putExtra(EXTRA_KEY_BOOK_UID, bookUId);
            intent.putExtra(EXTRA_KEY_BOOK_OWNER_UID, ownerUId);
            intent.putExtra(EXTRA_KEY_BOOK_NAME, bookName);
            intent.putExtra(EXTRA_KEY_BOOK_AUTHOR, authorName);
            intent.putExtra(EXTRA_KEY_BOOK_OWNER_NAME, ownerName);
            intent.putExtra(EXTRA_KEY_DISTANCE, distance);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MultiOwnersBookDetailsActivity.class);
            intent.putExtra(EXTRA_KEY_BOOK_UID, bookUId);
            intent.putExtra(EXTRA_KEY_BOOK_NAME, bookName);
            intent.putExtra(EXTRA_KEY_BOOK_AUTHOR, authorName);
            startActivity(intent);
        }
    }
}
