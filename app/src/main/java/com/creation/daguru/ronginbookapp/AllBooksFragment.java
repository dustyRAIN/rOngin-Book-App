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
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB_CHILD_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_DISTANCE;
import static com.creation.daguru.ronginbookapp.MainActivity.LATITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.LONGITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

public class AllBooksFragment extends Fragment implements BooksViewAdapter.BooksAdapterOnClickHandler{

    private String TAG = "All book new";

    private RecyclerView mRecyclerView;
    private BooksViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private List<ListBookFullDetails> mFullDetailsList;
    private ProgressBar mLoading;
    private SharedPreferences mSharedPreferences;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private double mUserLatitude;
    private double mUserLongitude;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView =  inflater.inflate(R.layout.fragment_all_books, container, false);

        mFullDetailsList = new ArrayList<>();
        mRecyclerView = mView.findViewById(R.id.all_books_lib_recycle_view);
        mLoading = mView.findViewById(R.id.all_books_lib_loading);

        showList();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new BooksViewAdapter(getContext(), this, mFullDetailsList);
        mRecyclerView.setAdapter(mAdapter);

        checkForLocation();

        attachDatabaseListener();

        return mView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
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
        DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB);

        mQuery = reference.orderByChild(DATABASE_DIR_ALL_BOOKS_LIB_CHILD_BOOK_NAME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    AllBooksLibBookDetails libBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);

                    Log.d(TAG, String.valueOf(libBookDetails.ownerCount));

                    if(libBookDetails.ownerCount == 1){
                        double distance = ExtraWorks.getDistance(mUserLatitude, mUserLongitude,
                                libBookDetails.ownerLatitude, libBookDetails.ownerLongitude);

                        if(!libBookDetails.ownerUId.equals(RONGIN_UID)){
                            mFullDetailsList.add(new ListBookFullDetails(
                                    libBookDetails.bookUId,
                                    libBookDetails.ownerUId,
                                    libBookDetails.bookName,
                                    libBookDetails.authorName,
                                    String.format("%.1f km",distance),
                                    libBookDetails.ownerFirstName,
                                    libBookDetails.ownerCount
                            ));

                            mAdapter.notifyDataSetChanged();
                            Log.d(TAG, String.valueOf(libBookDetails.ownerUId));
                        } else {
                            mFullDetailsList.add(new ListBookFullDetails(
                                    libBookDetails.bookUId,
                                    libBookDetails.ownerUId,
                                    libBookDetails.bookName,
                                    libBookDetails.authorName,
                                    String.format("%.1f km",distance),
                                    null,
                                    libBookDetails.ownerCount
                            ));

                            mAdapter.notifyDataSetChanged();
                            Log.d(TAG, String.valueOf(libBookDetails.ownerUId));
                        }
                    } else {
                        mFullDetailsList.add(new ListBookFullDetails(
                                libBookDetails.bookUId,
                                null,
                                libBookDetails.bookName,
                                libBookDetails.authorName,
                                null,
                                null,
                                libBookDetails.ownerCount
                        ));

                        mAdapter.notifyDataSetChanged();
                        Log.d(TAG, String.valueOf(libBookDetails.bookName));
                    }
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
            DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB);
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
    public void bookOnclick(String bookUId, String ownerUId, String bookName, String authorName, String ownerName,
                            String distance) {
        if(ownerUId != null && !ownerUId.equals("") && !ownerUId.isEmpty()){
            Intent intent = new Intent(getActivity(), SingleOwnerBookDetailsActivity.class);
            intent.putExtra(EXTRA_KEY_BOOK_UID, bookUId);
            intent.putExtra(EXTRA_KEY_BOOK_OWNER_UID, ownerUId);
            intent.putExtra(EXTRA_KEY_BOOK_NAME, bookName);
            intent.putExtra(EXTRA_KEY_BOOK_AUTHOR, authorName);
            intent.putExtra(EXTRA_KEY_BOOK_OWNER_NAME, ownerName);
            intent.putExtra(EXTRA_KEY_DISTANCE, distance);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), MultiOwnersBookDetailsActivity.class);
            intent.putExtra(EXTRA_KEY_BOOK_UID, bookUId);
            intent.putExtra(EXTRA_KEY_BOOK_NAME, bookName);
            intent.putExtra(EXTRA_KEY_BOOK_AUTHOR, authorName);
            startActivity(intent);
        }
    }
}
