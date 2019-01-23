package com.creation.daguru.ronginbookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.Utils.ExtraWorks;
import com.creation.daguru.ronginbookapp.Utils.RonginDateUtils;
import com.creation.daguru.ronginbookapp.data.ListBookFullDetails;
import com.creation.daguru.ronginbookapp.data.RonginBookDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_DISTANCE;
import static com.creation.daguru.ronginbookapp.MainActivity.LATITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.LONGITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

public class RonginBooksFragment extends Fragment implements BooksViewAdapter.BooksAdapterOnClickHandler{

    private RecyclerView mRecyclerView;
    private BooksViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private List<ListBookFullDetails> mFullDetailsList;
    private ProgressBar mLoading;

    private SharedPreferences mSharedPreferences;
    private double mUserLatitude;
    private double mUserLongitude;

    private ChildEventListener mChildEventListener;
    private Query mQuery;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView =  inflater.inflate(R.layout.fragment_rongin_books, container, false);
        mRecyclerView = mView.findViewById(R.id.rongin_books_recycler_view);
        mLoading = mView.findViewById(R.id.rongin_books_loading);

        mFullDetailsList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new BooksViewAdapter(getContext(), this, mFullDetailsList);
        mRecyclerView.setAdapter(mAdapter);
        showList();

        checkForLocation();
        attachDatabaseListener();


        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void checkForLocation(){
        mSharedPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(!mSharedPreferences.contains(LATITUDE_KEY + mFirebaseUser.getUid()) ||
                !mSharedPreferences.contains(LONGITUDE_KEY + mFirebaseUser.getUid())){
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        } else {
            mUserLatitude = mSharedPreferences.getFloat(LATITUDE_KEY + mFirebaseUser.getUid(), 1);
            mUserLongitude = mSharedPreferences.getFloat(LONGITUDE_KEY + mFirebaseUser.getUid(), 1);
        }
    }


    private void attachDatabaseListener(){
        DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS);

        mQuery = reference.orderByChild(DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_BOOK_NAME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    RonginBookDetails bookDetails = dataSnapshot.getValue(RonginBookDetails.class);

                    double distance = ExtraWorks.getDistance(mUserLatitude, mUserLongitude,
                            bookDetails.ownerLatitude, bookDetails.ownerLongitude);

                    mFullDetailsList.add(new ListBookFullDetails(
                            bookDetails.bookUId,
                            bookDetails.ownerUId,
                            bookDetails.bookName,
                            bookDetails.authorName,
                            String.format("%.1f km",distance),
                            null,
                            1
                    ));

                    mAdapter.notifyDataSetChanged();
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

            mQuery.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS);
            reference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
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
    public void bookOnclick(String bookUId, String ownerUId, String bookName, String authorName, String ownerName, String distance) {
        if(ownerUId != null && !ownerUId.equals("") && !ownerUId.isEmpty()){
            Intent intent = new Intent(getActivity(), SingleOwnerBookDetailsActivity.class);
            intent.putExtra(EXTRA_KEY_BOOK_UID, bookUId);
            intent.putExtra(EXTRA_KEY_BOOK_OWNER_UID, ownerUId);
            intent.putExtra(EXTRA_KEY_BOOK_NAME, bookName);
            intent.putExtra(EXTRA_KEY_BOOK_AUTHOR, authorName);
            intent.putExtra(EXTRA_KEY_BOOK_OWNER_NAME, ownerName);
            intent.putExtra(EXTRA_KEY_DISTANCE, distance);
            startActivity(intent);
        }
    }
}
