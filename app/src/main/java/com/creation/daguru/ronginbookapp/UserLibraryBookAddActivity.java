package com.creation.daguru.ronginbookapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.AdminNameRequestDetails;
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.DailyUpdateDetails;
import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_NEW_NAME_REQUEST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_OWNERS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_UPDATE_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.MainActivity.LATITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.LONGITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class UserLibraryBookAddActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    private static final String TAG = "UserLibraryBookAddActivity";

    private AutoCompleteTextView mactvSearchField;
    private EditText metvAuthorName;
    private EditText metvLanguage;

    private TextView mtvSearchedBook;
    private TextView mtvAuthorName;

    private TextView mtvbSearchedBookCancel;
    private TextView mtvbAddNow;

    private CheckBox mcbVisible;

    private AutoSearchBookAdapter mAutoAdapter;
    private List<UniqueBookDetails> mBookList;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mBookChildEventListener;
    private Query mQuery;

    private SharedPreferences mSharedPreferences;
    private double mLatitude;
    private double mLongitude;

    private boolean mIsBookFound;
    private int mIsVisible;

    private UniqueBookDetails mFoundBook;

    private UserLibraryBookDetails mCurrentUserBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_library_book_add);

        mactvSearchField = findViewById(R.id.admin_name_book_search);
        metvAuthorName = findViewById(R.id.admin_name_book_author);
        metvLanguage = findViewById(R.id.user_book_add_language);

        mtvSearchedBook = findViewById(R.id.admin_name_searched_book);
        mtvAuthorName = findViewById(R.id.admin_name_tv_book_author);

        mcbVisible = findViewById(R.id.user_add_check_box);

        mtvbSearchedBookCancel = findViewById(R.id.admin_name_searched_book_cancel);
        mtvbAddNow = findViewById(R.id.admin_name_add_book);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        checkForSharedPref();

        mcbVisible.setOnClickListener(this);
        mactvSearchField.setOnItemClickListener(this);
        mtvbSearchedBookCancel.setOnClickListener(this);
        mtvbAddNow.setOnClickListener(this);

        setUpUI();
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

    private void setUpUI(){
        beforeBookFound();
    }

    @Override
    protected void onResume() {
        mBookList = new ArrayList<>();
        mAutoAdapter = new AutoSearchBookAdapter(this, mBookList);
        mactvSearchField.setAdapter(mAutoAdapter);
        attatchBookListener();

        super.onResume();
    }

    @Override
    protected void onPause() {
        detatchBookListener();
        super.onPause();
    }

    private void attatchBookListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);

        if(mBookChildEventListener == null){
            mBookChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UniqueBookDetails bookDetails = dataSnapshot.getValue(UniqueBookDetails.class);

                    //Toast.makeText(AdminBookNameRequestActivity.this, "Hoise?", Toast.LENGTH_LONG).show();

                    mBookList.add(bookDetails);
                    mAutoAdapter.notifyDataSetChanged();
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

            databaseReference.addChildEventListener(mBookChildEventListener);
        }
    }


    private void detatchBookListener(){
        if(mBookChildEventListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);
            databaseReference.removeEventListener(mBookChildEventListener);
            mBookChildEventListener = null;
        }
    }





    private void beforeBookFound(){
        mIsBookFound = false;
        metvAuthorName.setText("");
        metvLanguage.setText("");
        mcbVisible.setChecked(true);
        mIsVisible = 1;
        enableButtons();
        showEmptyBookDetails();
    }

    private void afterBookFound(UniqueBookDetails bookDetails){
        mIsBookFound = true;
        mFoundBook = bookDetails;

        mtvSearchedBook.setText(mFoundBook.bookName);
        mtvAuthorName.setText(mFoundBook.authorName);

        showFoundBookDetails();
    }

    private void showEmptyBookDetails(){
        mactvSearchField.setVisibility(View.VISIBLE);
        metvAuthorName.setVisibility(View.VISIBLE);
        metvLanguage.setVisibility(View.VISIBLE);

        mtvSearchedBook.setVisibility(View.GONE);
        mtvbSearchedBookCancel.setVisibility(View.GONE);
        mtvAuthorName.setVisibility(View.GONE);
    }

    private void showFoundBookDetails(){
        mactvSearchField.setVisibility(View.GONE);
        metvAuthorName.setVisibility(View.GONE);
        metvLanguage.setVisibility(View.INVISIBLE);

        mtvSearchedBook.setVisibility(View.VISIBLE);
        mtvbSearchedBookCancel.setVisibility(View.VISIBLE);
        mtvAuthorName.setVisibility(View.VISIBLE);
    }

    private void disableButtons(){
        mtvbAddNow.setEnabled(false);
    }

    private void enableButtons(){
        mtvbAddNow.setEnabled(true);
    }


    private void checkAndPreAddBook(){
        if(mIsBookFound){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                    .child(mFirebaseUser.getUid());

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int i = 0, n = 0;
                    boolean isOk = true;

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        n++;
                    }

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        i++;
                        UserExchangeBookDetails bookDetails = snapshot.getValue(UserExchangeBookDetails.class);

                        if(bookDetails.bookUId.equals(mFoundBook.bookUId)){
                            isOk = false;
                        }

                        if(i == n){
                            if(isOk){
                                preAddBook();
                            } else {
                                Toast.makeText(UserLibraryBookAddActivity.this,
                                        "You can't add a borrowed book in your library.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    if(n == 0){
                        preAddBook();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            if(!mactvSearchField.getText().toString().equals("") && !mactvSearchField.getText().toString().isEmpty()){
                if(mactvSearchField.getText().toString().length() > 40){
                    Toast.makeText(this, "Book name is too large.", Toast.LENGTH_LONG).show();
                } else {
                    sendRequestAndShowAlertDialog();
                }
            } else {
                Toast.makeText(this, "Invalid book name.", Toast.LENGTH_LONG).show();
                enableButtons();
            }
        }
    }

    private void preAddBook(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                .child(mFirebaseUser.getUid()).child(mFoundBook.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    UserLibraryBookDetails libraryBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    libraryBookDetails.bookName = mFoundBook.bookName;
                    libraryBookDetails.authorName = mFoundBook.authorName;
                    libraryBookDetails.copyCount++;

                    mCurrentUserBook = libraryBookDetails;
                    addToUserLib();
                } else {
                    UserLibraryBookDetails libraryBookDetails = new UserLibraryBookDetails(
                            mFoundBook.bookName,
                            mFoundBook.authorName,
                            mFoundBook.bookUId,
                            mIsVisible,
                            1
                    );

                    mCurrentUserBook = libraryBookDetails;
                    addToUserLib();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please wait")
                .setMessage(getResources().getString(R.string.book_name_request_dialog_message))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendRequestAndShowAlertDialog(){
        AdminNameRequestDetails nameRequestDetails = new AdminNameRequestDetails(
                mactvSearchField.getText().toString(),
                metvAuthorName.getText().toString(),
                mFirebaseUser.getUid(),
                mFirebaseUser.getDisplayName(),
                null,
                metvLanguage.getText().toString(),
                mIsVisible,
                getUTCDateFromLocal(System.currentTimeMillis()),
                mLatitude,
                mLongitude
        );

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS);
        String key = databaseReference.push().getKey();
        nameRequestDetails.requestUId = key;
        databaseReference.child(key).setValue(nameRequestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_ADMIN_NEW_NAME_REQUEST);
        dbRef.setValue(1);
        showDialog();
        beforeBookFound();
    }

    private void addToUserLib(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                .child(mFirebaseUser.getUid()).child(mCurrentUserBook.bookUId);

        databaseReference.setValue(mCurrentUserBook);

        if(mCurrentUserBook.isVisible == 1){
            addToAllLib();
            addToUpdateList();
        }

        beforeBookFound();
    }

    private void addToAllLib(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mCurrentUserBook.bookUId).child(mFirebaseUser.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int bookCopy = -1;

                if(dataSnapshot.exists()){
                    bookCopy = dataSnapshot.getValue(Integer.class);
                    bookCopy++;

                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mCurrentUserBook.bookUId).child(mFirebaseUser.getUid());

                    databaseReference.setValue(bookCopy);

                } else {
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mCurrentUserBook.bookUId).child(mFirebaseUser.getUid());

                    databaseReference.setValue(1);
                    inreaseOwnerCountInLibrary();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToUpdateList(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
        DailyUpdateDetails updateDetails = new DailyUpdateDetails(
                String.format(getResources().getString(R.string.update_message_book_added), mFirebaseUser.getDisplayName()),
                getUTCDateFromLocal(System.currentTimeMillis())
        );

        databaseReference.push().setValue(updateDetails);
    }

    private void inreaseOwnerCountInLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mCurrentUserBook.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllBooksLibBookDetails booksLibBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);
                if(booksLibBookDetails != null){
                    booksLibBookDetails.ownerCount++;
                } else {
                    booksLibBookDetails = new AllBooksLibBookDetails(
                            mCurrentUserBook.bookName,
                            mCurrentUserBook.authorName,
                            mCurrentUserBook.bookUId,
                            mFirebaseUser.getDisplayName(),
                            mFirebaseUser.getUid(),
                            mCurrentUserBook.bookName.toLowerCase(),
                            1,
                            mLatitude,
                            mLongitude
                    );
                }

                DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                        .child(mCurrentUserBook.bookUId);

                databaseReference.setValue(booksLibBookDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(UserLibraryBookAddActivity.this,
                                    "Book added to 'All books'", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(UserLibraryBookAddActivity.this,
                                    "Book adding failed. Check internet connection.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UniqueBookDetails bookDetails = (UniqueBookDetails) parent.getItemAtPosition(position);
        afterBookFound(bookDetails);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.admin_name_searched_book_cancel:
                beforeBookFound();
                break;

            case R.id.admin_name_add_book:
                disableButtons();
                checkAndPreAddBook();
                break;

            case R.id.user_add_check_box:
                if(((CheckBox)v).isChecked()){
                    mIsVisible = 1;
                } else {
                    mIsVisible = 0;
                }
                break;
        }
    }
}
