package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.creation.daguru.ronginbookapp.Utils.RonginDateUtils;
import com.creation.daguru.ronginbookapp.data.ListUserBookExchangeDetails;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LENT_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_DAYS_LEFT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_UID;

public class UserBooksLentFragment extends Fragment implements UserExchangeBooksViewAdapter.ExchangeBookOnClickHandler {

    private View mView;


    private ProgressBar mLoading;
    private RecyclerView mRecyclerView;
    private UserExchangeBooksViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private List<ListUserBookExchangeDetails> mBookDetailsList;
    private ChildEventListener mChildEventListener;

    private int mDaysLeft;
    private long mCurrentTimeInMillies;

    private UserExchangeBookDetails mBookDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_books_lent, container, false);

        mLoading = mView.findViewById(R.id.user_books_lent_loading);
        mRecyclerView = mView.findViewById(R.id.user_books_lent_recycler_view);


        showList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);




        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentTimeInMillies = System.currentTimeMillis();
        mBookDetailsList = new ArrayList<>();
        mAdapter = new UserExchangeBooksViewAdapter(getContext(), UserExchangeBooksViewAdapter.LENT_EXCHANGE_TYPE,
                mBookDetailsList, this);
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
            DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS)
                    .child(mFirebaseUser.getUid());

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mBookDetails = dataSnapshot.getValue(UserExchangeBookDetails.class);

                    mDaysLeft = RonginDateUtils.getLeftDaysCount(mBookDetails.exchangeTime, mCurrentTimeInMillies,
                            mBookDetails.dayLimit);

                    if(mBookDetails.otherUserUId.equals(RONGIN_UID)) {
                        mBookDetailsList.add(new ListUserBookExchangeDetails(
                                mBookDetails.bookName,
                                mBookDetails.authorName,
                                mBookDetails.bookUId,
                                mBookDetails.otherUserUId,
                                null,
                                mDaysLeft
                        ));

                        mAdapter.notifyDataSetChanged();
                    } else {
                        mBookDetailsList.add(new ListUserBookExchangeDetails(
                                mBookDetails.bookName,
                                mBookDetails.authorName,
                                mBookDetails.bookUId,
                                mBookDetails.otherUserUId,
                                mBookDetails.otherUserName,
                                mDaysLeft
                        ));

                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserExchangeBookDetails bookDetails = dataSnapshot.getValue(UserExchangeBookDetails.class);
                    int daysLeft = RonginDateUtils.getLeftDaysCount(bookDetails.exchangeTime, System.currentTimeMillis(),
                            bookDetails.dayLimit);

                    ListUserBookExchangeDetails bookExchangeDetails = new ListUserBookExchangeDetails(
                            bookDetails.bookName,
                            bookDetails.authorName,
                            bookDetails.bookUId,
                            bookDetails.otherUserUId,
                            bookDetails.otherUserName,
                            daysLeft
                    );
                    int id = getPositionInList(bookExchangeDetails);

                    mBookDetailsList.set(id, bookExchangeDetails);
                    mAdapter.notifyItemChanged(id);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    UserExchangeBookDetails bookDetails = dataSnapshot.getValue(UserExchangeBookDetails.class);
                    int daysLeft = RonginDateUtils.getLeftDaysCount(bookDetails.exchangeTime, System.currentTimeMillis(),
                            bookDetails.dayLimit);

                    ListUserBookExchangeDetails bookExchangeDetails = new ListUserBookExchangeDetails(
                            bookDetails.bookName,
                            bookDetails.authorName,
                            bookDetails.bookUId,
                            bookDetails.otherUserUId,
                            bookDetails.otherUserName,
                            daysLeft
                    );
                    int id = getPositionInList(bookExchangeDetails);

                    mBookDetailsList.remove(id);
                    mAdapter.notifyItemRemoved(id);
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

    private int getPositionInList(ListUserBookExchangeDetails bookDetails){
        int id = 0;
        for(ListUserBookExchangeDetails details: mBookDetailsList){
            if(details.bookUId.equals(bookDetails.bookUId)){
                return id;
            }
            id++;
        }
        return -1;
    }


    private void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS)
                    .child(mFirebaseUser.getUid());
            reference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }







    private Intent addExtrasToIntent(Intent intent, ListUserBookExchangeDetails bookExchangeDetails){
        intent.putExtra(EXTRA_KEY_EXCHANGE_BOOK_NAME, bookExchangeDetails.bookName);
        intent.putExtra(EXTRA_KEY_EXCHANGE_BOOK_AUTHOR, bookExchangeDetails.authorName);
        intent.putExtra(EXTRA_KEY_EXCHANGE_BOOK_UID, bookExchangeDetails.bookUId);
        intent.putExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_UID, bookExchangeDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_NAME, bookExchangeDetails.otherUserName);
        intent.putExtra(EXTRA_KEY_EXCHANGE_DAYS_LEFT, bookExchangeDetails.daysLeft);

        return intent;
    }


    @Override
    public void bookOnClicked(ListUserBookExchangeDetails bookExchangeDetails) {
        Intent lentDetailsIntent = new Intent(getActivity(), LentBookDetailsActivity.class);
        lentDetailsIntent = addExtrasToIntent(lentDetailsIntent, bookExchangeDetails);
        startActivity(lentDetailsIntent);
    }



    private void showLoading(){
        mLoading.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void showList(){
        mLoading.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}
