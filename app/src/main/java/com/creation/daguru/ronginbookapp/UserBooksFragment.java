package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_AUTHOR_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_COPY_COUNT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_IS_VISIBLE;


public class UserBooksFragment extends Fragment implements UserBooksViewAdapter.BookClickHandler, View.OnClickListener {

    private View mView;


    private FloatingActionButton mfabAdd;
    private RecyclerView mRecyclerView;
    private UserBooksViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;

    private List<UserLibraryBookDetails> mUserLibBookDetails;

    private boolean mIsIterDone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_books, container, false);

        mRecyclerView = mView.findViewById(R.id.user_books_recycler_view);

        showList();

        mfabAdd = mView.findViewById(R.id.user_float_add);
        mfabAdd.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);


        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mIsIterDone = false;

        return mView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        mUserLibBookDetails = new ArrayList<>();
        mAdapter = new UserBooksViewAdapter(getContext(), mUserLibBookDetails, this);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    private void attachDatabaseListener(){
        if(mChildEventListener == null){
            DatabaseReference dbReference = mDatabaseReference
                    .child(DATABASE_DIR_USER_LIB_BOOKS)
                    .child(mFirebaseUser.getUid());

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserLibraryBookDetails bookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    mUserLibBookDetails.add(bookDetails);

                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserLibraryBookDetails bookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    int id = getPositionInList(bookDetails);

                    mUserLibBookDetails.set(id, bookDetails);
                    mAdapter.notifyItemChanged(id);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    UserLibraryBookDetails bookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    int id = getPositionInList(bookDetails);

                    mUserLibBookDetails.remove(id);
                    mAdapter.notifyItemRemoved(id);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            dbReference.addChildEventListener(mChildEventListener);
        }
    }

    private int getPositionInList(UserLibraryBookDetails bookDetails){
        int id = 0;
        for(UserLibraryBookDetails details: mUserLibBookDetails){
            if(details.bookUId.equals(bookDetails.bookUId)){
                return id;
            }
            id++;
        }
        return -1;
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            DatabaseReference reference = mDatabaseReference
                    .child(DATABASE_DIR_USER_LIB_BOOKS)
                    .child(mFirebaseUser.getUid());
            reference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }



    public void showList(){
        mRecyclerView.setVisibility(View.VISIBLE);
    }









    private Intent addExtrasToIntent(Intent intent, UserLibraryBookDetails bookDetails){
        intent.putExtra(EXTRA_KEY_BOOK_NAME, bookDetails.bookName);
        intent.putExtra(EXTRA_KEY_AUTHOR_NAME, bookDetails.authorName);
        intent.putExtra(EXTRA_KEY_BOOK_UID, bookDetails.bookUId);
        intent.putExtra(EXTRA_KEY_IS_VISIBLE, bookDetails.isVisible);
        intent.putExtra(EXTRA_KEY_COPY_COUNT, bookDetails.copyCount);

        return intent;
    }

    @Override
    public void onClickedBook(UserLibraryBookDetails bookDetails) {
        Intent intent = new Intent(getContext(), MyBookDetailsActivity.class);
        intent = addExtrasToIntent(intent, bookDetails);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_float_add:
                Intent intent = new Intent(getContext(), UserLibraryBookAddActivity.class);
                startActivity(intent);
                break;
        }
    }
}
