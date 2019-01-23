package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;

public class AdminBookAddActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private AutoCompleteTextView mactvSearchField;
    private EditText metvAuthorName;
    private EditText metvKeyWord1;
    private EditText metvKeyWord2;
    private EditText metvKeyWord3;

    private TextView mtvSearchedBook;
    private TextView mtvAuthorName;
    private TextView mtvKeyWord1;
    private TextView mtvKeyWord2;
    private TextView mtvKeyWord3;

    private TextView mtvbSearchedBookCancel;
    private TextView mtvbAddNow;

    private AutoSearchBookAdapter mAutoAdapter;
    private List<UniqueBookDetails> mBookList;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mBookChildEventListener;

    private boolean mIsBookFound;

    private UniqueBookDetails mFoundBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_book_add);

        mactvSearchField = findViewById(R.id.admin_name_book_search);
        metvAuthorName = findViewById(R.id.admin_name_book_author);
        metvKeyWord1 = findViewById(R.id.admin_name_keyword_1);
        metvKeyWord2 = findViewById(R.id.admin_name_keyword_2);
        metvKeyWord3 = findViewById(R.id.admin_name_keyword_3);
        mtvSearchedBook = findViewById(R.id.admin_name_searched_book);
        mtvAuthorName = findViewById(R.id.admin_name_tv_book_author);
        mtvKeyWord1 = findViewById(R.id.admin_name_tv_keyword_1);
        mtvKeyWord2 = findViewById(R.id.admin_name_tv_keyword_2);
        mtvKeyWord3 = findViewById(R.id.admin_name_tv_keyword_3);

        mtvbSearchedBookCancel = findViewById(R.id.admin_name_searched_book_cancel);
        mtvbAddNow = findViewById(R.id.admin_name_add_book);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mactvSearchField.setOnItemClickListener(this);
        mtvbSearchedBookCancel.setOnClickListener(this);
        mtvbAddNow.setOnClickListener(this);

        setUpUI();
    }

    private void setUpUI(){
        initializeDetailsView();
    }

    @Override
    protected void onResume() {
        mBookList = new ArrayList<>();
        mAutoAdapter = new AutoSearchBookAdapter(this, mBookList);
        mactvSearchField.setAdapter(mAutoAdapter);
        attachBookListener();

        super.onResume();
    }

    @Override
    protected void onPause() {
        detachBookListener();
        super.onPause();
    }

    private void attachBookListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);

        if(mBookChildEventListener == null){
            mBookChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UniqueBookDetails bookDetails = dataSnapshot.getValue(UniqueBookDetails.class);

                    //Toast.makeText(AdminBookAddActivity.this, "Hoise?", Toast.LENGTH_LONG).show();

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

    private void detachBookListener(){
        if(mBookChildEventListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);
            databaseReference.removeEventListener(mBookChildEventListener);
            mBookChildEventListener = null;
        }
    }

    private void initializeDetailsView(){
        beforeBookFound();
        enableButtons();
    }

    private void beforeBookFound(){
        mIsBookFound = false;

        mactvSearchField.setText("");
        metvAuthorName.setText("");
        metvKeyWord1.setText("");
        metvKeyWord2.setText("");
        metvKeyWord3.setText("");

        showEmptyBookDetails();
    }

    private void afterBookFound(UniqueBookDetails bookDetails){
        mIsBookFound = true;
        mFoundBook = bookDetails;

        mtvSearchedBook.setText(mFoundBook.bookName);
        mtvAuthorName.setText(mFoundBook.authorName);
        mtvKeyWord1.setText(mFoundBook.keyword1);
        mtvKeyWord2.setText(mFoundBook.keyword2);
        mtvKeyWord3.setText(mFoundBook.keyword3);

        showFoundBookDetails();
    }

    private void showEmptyBookDetails(){
        mactvSearchField.setVisibility(View.VISIBLE);
        metvAuthorName.setVisibility(View.VISIBLE);
        metvKeyWord1.setVisibility(View.VISIBLE);
        metvKeyWord2.setVisibility(View.VISIBLE);
        metvKeyWord3.setVisibility(View.VISIBLE);

        mtvSearchedBook.setVisibility(View.GONE);
        mtvbSearchedBookCancel.setVisibility(View.GONE);
        mtvAuthorName.setVisibility(View.GONE);
        mtvKeyWord1.setVisibility(View.GONE);
        mtvKeyWord2.setVisibility(View.GONE);
        mtvKeyWord3.setVisibility(View.GONE);
    }

    private void showFoundBookDetails(){
        mactvSearchField.setVisibility(View.GONE);
        metvAuthorName.setVisibility(View.GONE);
        metvKeyWord1.setVisibility(View.GONE);
        metvKeyWord2.setVisibility(View.GONE);
        metvKeyWord3.setVisibility(View.GONE);

        mtvSearchedBook.setVisibility(View.VISIBLE);
        mtvbSearchedBookCancel.setVisibility(View.VISIBLE);
        mtvAuthorName.setVisibility(View.VISIBLE);
        mtvKeyWord1.setVisibility(View.VISIBLE);
        mtvKeyWord2.setVisibility(View.VISIBLE);
        mtvKeyWord3.setVisibility(View.VISIBLE);
    }

    private void disableButtons(){
        mtvbAddNow.setEnabled(false);
    }

    private void enableButtons(){
        mtvbAddNow.setEnabled(true);
    }



    private void checkAndPreAddBook(){
        if(mIsBookFound){
            Toast.makeText(this, "Book already exists.", Toast.LENGTH_LONG).show();
            enableButtons();
        } else {
            addToUniqueLibrary();
        }
    }

    private void addToUniqueLibrary(){
        String book = mactvSearchField.getText().toString();
        String author = metvAuthorName.getText().toString();
        String key1 = metvKeyWord1.getText().toString();
        String key2 = metvKeyWord2.getText().toString();
        String key3 = metvKeyWord3.getText().toString();

        UniqueBookDetails bookDetails = new UniqueBookDetails(
                book,
                null,
                author,
                key1,
                key2,
                key3
        );

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);
        String key = databaseReference.push().getKey();

        bookDetails.bookUId = key;
        databaseReference.child(key).setValue(bookDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminBookAddActivity.this, "Book Added Successfully.",
                            Toast.LENGTH_LONG).show();

                    initializeDetailsView();
                } else {
                    Toast.makeText(AdminBookAddActivity.this, "Failed. Check internet connection and try again.",
                            Toast.LENGTH_LONG).show();

                    enableButtons();
                }
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
        switch (v.getId()) {
            case R.id.admin_name_searched_book_cancel:
                beforeBookFound();
                break;

            case R.id.admin_name_add_book:
                disableButtons();
                checkAndPreAddBook();
                break;
        }
    }
}
