package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.Utils.ExtraWorks;
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.ListBookFullDetails;
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
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB_CHILD_BOOK_NAME_LOWER;
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



public class BookSearchActivity extends AppCompatActivity implements BooksViewAdapter.BooksAdapterOnClickHandler {

    private String TAG = "SearchBook";



    private EditText metvSearchBook;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoading;
    private TextView mtvNotFoundMessage;

    private List<ListBookFullDetails> mFullDetailsList = new ArrayList<>();
    private List<ListBookFullDetails> mAllBookFullDetails = new ArrayList<>();

    private BooksViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mCurrentReference;
    private Query mCurrentQuery;
    private FirebaseUser mFirebaseUser;
    private ValueEventListener mValueEventListener;
    private TextWatcher mTextChangeListener;

    private int mCurrentQueryCounter;
    private int mQueryNumber;

    private SharedPreferences mSharedPreferences;

    private double mUserLatitude;
    private double mUserLongitude;

    private String mBookToSearch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        mCurrentQueryCounter = 0;

        metvSearchBook = findViewById(R.id.etv_search);
        mRecyclerView = findViewById(R.id.search_recycler_view);
        mLoading = findViewById(R.id.search_loading);
        mtvNotFoundMessage = findViewById(R.id.tv_search_not_found);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new BooksViewAdapter(this, this, mFullDetailsList);
        mRecyclerView.setAdapter(mAdapter);

        checkForLocation();

        listenForTextChange();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mTextChangeListener != null){
            metvSearchBook.removeTextChangedListener(mTextChangeListener);
        }
        super.onPause();
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


    private void listenForTextChange(){
        if(mTextChangeListener == null){
            mTextChangeListener = new TextWatcher()
             {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(TAG, "Text changed");
                    mCurrentQueryCounter++;
                    mQueryNumber--;
                    updateList(metvSearchBook.getText().toString());
                }
            };
        }

        metvSearchBook.addTextChangedListener(mTextChangeListener);
    }

    private void updateList(String textToSearch){
        mBookToSearch = textToSearch;

        mQueryNumber--;
        showLoading();
        if(mBookToSearch.isEmpty() || mBookToSearch.equals("")){
            Log.d(TAG, "All size   " + String.valueOf(mAllBookFullDetails.size()));

            if(mAllBookFullDetails.size() > 0 ){
                int size = mFullDetailsList.size();
                mFullDetailsList.clear();
                mAdapter.notifyItemRangeRemoved(0, size);
                for(ListBookFullDetails bookFullDetails: mAllBookFullDetails){
                    mFullDetailsList.add(bookFullDetails);
                    mAdapter.notifyDataSetChanged();
                }
                showList();
            } else {
                updateListWithFullData();
            }
        } else {
            updateListWithSearchedText(mBookToSearch.toLowerCase());
        }
    }


    private void updateListWithSearchedText(String textToSearch){
        Log.d(TAG, "Qc  " + String.valueOf(mCurrentQueryCounter));

        mBookToSearch = textToSearch;

        if(mCurrentQuery != null){
            if(mValueEventListener != null){
                mCurrentQuery.removeEventListener(mValueEventListener);
            }
        }

        int size = mFullDetailsList.size();
        mFullDetailsList.clear();
        mAdapter.notifyItemRangeRemoved(0, size);
        mCurrentReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB);

        mCurrentQuery = mCurrentReference.orderByChild(DATABASE_DIR_ALL_BOOKS_LIB_CHILD_BOOK_NAME_LOWER);

        if(mValueEventListener == null){
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int addCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AllBooksLibBookDetails libBookDetails = snapshot.getValue(AllBooksLibBookDetails.class);
                        if(libBookDetails.bookNameLower.contains(mBookToSearch)){
                            addCount++;
                        } else {
                            continue;
                        }

                        if (libBookDetails.ownerCount == 1) {
                            double distance = ExtraWorks.getDistance(mUserLatitude, mUserLongitude,
                                    libBookDetails.ownerLatitude, libBookDetails.ownerLongitude);

                            if (!libBookDetails.ownerUId.equals(RONGIN_UID)) {
                                if(mQueryNumber == mCurrentQueryCounter && !bookAlreadyShown(libBookDetails)){
                                    Log.d(TAG, "Qn  " + String.valueOf(mQueryNumber));
                                    mFullDetailsList.add(new ListBookFullDetails(
                                            libBookDetails.bookUId,
                                            libBookDetails.ownerUId,
                                            libBookDetails.bookName,
                                            libBookDetails.authorName,
                                            String.format("%.1f km", distance),
                                            libBookDetails.ownerFirstName,
                                            libBookDetails.ownerCount
                                    ));

                                    mAdapter.notifyDataSetChanged();
                                    showList();
                                } else {
                                    break;
                                }
                            } else {
                                if(mQueryNumber == mCurrentQueryCounter && !bookAlreadyShown(libBookDetails)){
                                    Log.d(TAG, "Qn  " + String.valueOf(mQueryNumber));
                                    mFullDetailsList.add(new ListBookFullDetails(
                                        libBookDetails.bookUId,
                                        libBookDetails.ownerUId,
                                        libBookDetails.bookName,
                                        libBookDetails.authorName,
                                        String.format("%.1f km", distance),
                                        null,
                                        libBookDetails.ownerCount
                                    ));

                                    mAdapter.notifyDataSetChanged();
                                    showList();
                                } else {
                                    break;
                                }
                            }
                        } else {
                            if(mQueryNumber == mCurrentQueryCounter && !bookAlreadyShown(libBookDetails)){
                                Log.d(TAG, "Qn  " + String.valueOf(mQueryNumber));
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
                                showList();
                            } else {
                                break;
                            }
                        }

                        if(addCount >= 25)
                            break;
                    }

                    if(mFullDetailsList.size()==0){
                        if(mQueryNumber == mCurrentQueryCounter){
                            showNotFound();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showNotFound();
                }
            };
        }

        mQueryNumber = mCurrentQueryCounter;
        mCurrentQuery.addListenerForSingleValueEvent(mValueEventListener);
    }


    private boolean bookAlreadyShown(AllBooksLibBookDetails bookDetails){
        for(ListBookFullDetails bookFullDetails: mFullDetailsList){
            if(bookDetails.bookUId.equals(bookFullDetails.bookUId)){
                return true;
            }
        }

        return false;
    }









    private void updateListWithFullData(){
        int size = mFullDetailsList.size();
        mFullDetailsList.clear();
        mAdapter.notifyItemRangeRemoved(0, size);

        mAllBookFullDetails.clear();

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AllBooksLibBookDetails libBookDetails = snapshot.getValue(AllBooksLibBookDetails.class);

                    Log.d(TAG, String.valueOf(libBookDetails.ownerCount));

                    if (libBookDetails.ownerCount == 1) {
                        double distance = ExtraWorks.getDistance(mUserLatitude, mUserLongitude,
                                libBookDetails.ownerLatitude, libBookDetails.ownerLongitude);

                        if (!libBookDetails.ownerUId.equals(RONGIN_UID)) {
                            mFullDetailsList.add(new ListBookFullDetails(
                                    libBookDetails.bookUId,
                                    libBookDetails.ownerUId,
                                    libBookDetails.bookName,
                                    libBookDetails.authorName,
                                    String.format("%.1f km", distance),
                                    libBookDetails.ownerFirstName,
                                    libBookDetails.ownerCount
                            ));

                            mAllBookFullDetails.add(new ListBookFullDetails(
                                    libBookDetails.bookUId,
                                    libBookDetails.ownerUId,
                                    libBookDetails.bookName,
                                    libBookDetails.authorName,
                                    String.format("%.1f km", distance),
                                    libBookDetails.ownerFirstName,
                                    libBookDetails.ownerCount
                            ));

                            mAdapter.notifyDataSetChanged();
                            showList();
                            Log.d(TAG, String.valueOf(libBookDetails.ownerUId));
                        } else {
                            mFullDetailsList.add(new ListBookFullDetails(
                                    libBookDetails.bookUId,
                                    libBookDetails.ownerUId,
                                    libBookDetails.bookName,
                                    libBookDetails.authorName,
                                    String.format("%.1f km", distance),
                                    null,
                                    libBookDetails.ownerCount
                            ));

                            mAllBookFullDetails.add(new ListBookFullDetails(
                                    libBookDetails.bookUId,
                                    libBookDetails.ownerUId,
                                    libBookDetails.bookName,
                                    libBookDetails.authorName,
                                    String.format("%.1f km", distance),
                                    null,
                                    libBookDetails.ownerCount
                            ));

                            mAdapter.notifyDataSetChanged();
                            showList();
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

                        mAllBookFullDetails.add(new ListBookFullDetails(
                                libBookDetails.bookUId,
                                null,
                                libBookDetails.bookName,
                                libBookDetails.authorName,
                                null,
                                null,
                                libBookDetails.ownerCount
                        ));

                        mAdapter.notifyDataSetChanged();
                        showList();
                        Log.d(TAG, String.valueOf(libBookDetails.bookName));
                    }
                }

                if(mFullDetailsList.size() == 0){
                    showNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showNotFound();
            }
        });
    }





    public void showNotFound(){
        mLoading.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mtvNotFoundMessage.setVisibility(View.VISIBLE);
    }

    public void showLoading(){
        mLoading.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mtvNotFoundMessage.setVisibility(View.GONE);
    }

    public void showList(){
        mLoading.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mtvNotFoundMessage.setVisibility(View.GONE);
    }










    @Override
    public void bookOnclick(String bookUId, String ownerUId, String bookName, String authorName, String ownerName, String distance) {
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
