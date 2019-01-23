package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.RonginBookDetails;
import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.xml.sax.DTDHandler;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS_CHILD_BOOK_NAME;

public class AdminManageUniqueBooksActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private AdminManageBookAdapter mAdapter;

    private ScrollView mEditSection;
    private EditText metvBookName;
    private EditText metvBookAuthor;
    private EditText metvKeyWord1;
    private EditText metvKeyWord2;
    private EditText metvKeyWord3;
    private TextView mtvbSave;
    private FloatingActionButton mfabAdd;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private UniqueBookDetails mSelectedBook;
    private List<UniqueBookDetails> mBookList;

    private boolean mIsListShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_unique_books);

        mRecyclerView = findViewById(R.id.admin_manage_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mEditSection = findViewById(R.id.admin_manage_edit_section);
        metvBookName = findViewById(R.id.admin_manage_edit_book);
        metvBookAuthor = findViewById(R.id.admin_manage_edit_author);
        metvKeyWord1 = findViewById(R.id.admin_manage_edit_key_1);
        metvKeyWord2 = findViewById(R.id.admin_manage_edit_key_2);
        metvKeyWord3 = findViewById(R.id.admin_manage_edit_key_3);
        mtvbSave = findViewById(R.id.admin_manage_save);
        mfabAdd = findViewById(R.id.admin_manage_float_add);

        mtvbSave.setOnClickListener(this);
        mfabAdd.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        setUpUI();
    }

    @Override
    protected void onResume() {

        mBookList = new ArrayList<>();
        mAdapter = new AdminManageBookAdapter(this, mBookList);
        mRecyclerView.setAdapter(mAdapter);

        attachDatabaseListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachDatabaseListener();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(mIsListShown){
            super.onBackPressed();
        } else {
            initializeRecyclerView();
        }
    }

    private void setUpUI(){
        initializeRecyclerView();
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);

        mQuery = databaseReference.orderByChild(DATABASE_DIR_UNIQUE_BOOK_DETAILS_CHILD_BOOK_NAME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UniqueBookDetails uniqueBookDetails = dataSnapshot.getValue(UniqueBookDetails.class);
                    mBookList.add(uniqueBookDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UniqueBookDetails bookDetails = dataSnapshot.getValue(UniqueBookDetails.class);
                    int id = getBookId(bookDetails);
                    mBookList.set(id, bookDetails);
                    mAdapter.notifyItemChanged(id);
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

    private int getBookId(UniqueBookDetails bookDetails){
        int i = 0;
        for(UniqueBookDetails details: mBookList){
            if(bookDetails.bookUId.equals(details.bookUId)){
                return i;
            }
            i++;
        }

        return -1;
    }

    private void detachDatabaseListener(){
        if(mChildEventListener != null){
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void initializeRecyclerView(){
        mIsListShown = true;
        showRecyclerView();
    }

    private void initializeEditSection(){
        mIsListShown = false;
        metvBookName.setText(mSelectedBook.bookName);
        metvBookAuthor.setText(mSelectedBook.authorName);
        metvKeyWord1.setText(mSelectedBook.keyword1);
        metvKeyWord2.setText(mSelectedBook.keyword2);
        metvKeyWord3.setText(mSelectedBook.keyword3);
        enableButtons();
        showEditSection();
    }

    private void showRecyclerView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mEditSection.setVisibility(View.GONE);
    }

    private void showEditSection(){
        mRecyclerView.setVisibility(View.GONE);
        mEditSection.setVisibility(View.VISIBLE);
    }

    private void enableButtons(){
        mtvbSave.setEnabled(true);
    }

    private void disableButtons(){
        mtvbSave.setEnabled(false);
    }

    private void checkAndSaveEditedDetails(){
        if(isAnyFieldChanged()){
            String book = metvBookName.getText().toString();
            String author = metvBookAuthor.getText().toString();
            String key1 = metvKeyWord1.getText().toString();
            String key2 = metvKeyWord2.getText().toString();
            String key3 = metvKeyWord3.getText().toString();

            UniqueBookDetails bookDetails = new UniqueBookDetails(
                    book,
                    mSelectedBook.bookUId,
                    author,
                    key1,
                    key2,
                    key3
            );

            mSelectedBook = bookDetails;

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS).
                    child(mSelectedBook.bookUId);

            databaseReference.setValue(mSelectedBook);

            DatabaseReference dbAllBookReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                    .child(mSelectedBook.bookUId);

            dbAllBookReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        AllBooksLibBookDetails libBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);
                        libBookDetails.bookName = mSelectedBook.bookName;
                        libBookDetails.authorName = mSelectedBook.authorName;
                        libBookDetails.bookNameLower = mSelectedBook.bookName.toLowerCase();

                        DatabaseReference dbAllBookReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                                .child(mSelectedBook.bookUId);

                        dbAllBookReference.setValue(libBookDetails);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            DatabaseReference dbRonginBookReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                    .child(mSelectedBook.bookUId);

            dbRonginBookReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        RonginBookDetails libBookDetails = dataSnapshot.getValue(RonginBookDetails.class);
                        libBookDetails.bookName = mSelectedBook.bookName;
                        libBookDetails.authorName = mSelectedBook.authorName;

                        DatabaseReference dbRonginBookReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                                .child(mSelectedBook.bookUId);

                        dbRonginBookReference.setValue(libBookDetails);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Toast.makeText(this, "Saved.", Toast.LENGTH_LONG).show();
        } else {
            enableButtons();
            Toast.makeText(this, "Nothing's changed.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isAnyFieldChanged(){
        if(!metvBookName.getText().toString().equals(mSelectedBook.bookName) ||
                !metvBookAuthor.getText().toString().equals(mSelectedBook.authorName) ||
                !metvKeyWord1.getText().toString().equals(mSelectedBook.keyword1) ||
                !metvKeyWord2.getText().toString().equals(mSelectedBook.keyword2) ||
                !metvKeyWord3.getText().toString().equals(mSelectedBook.keyword3)) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.admin_manage_save:
                disableButtons();
                checkAndSaveEditedDetails();
                Log.d("managebook", "Save Clicked");
                break;

            case R.id.admin_manage_float_add:
                Intent intent = new Intent(this, AdminBookAddActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void onBookClicked(UniqueBookDetails bookDetails){
        mSelectedBook = bookDetails;
        initializeEditSection();
    }





    private class AdminManageBookAdapter extends RecyclerView.Adapter<AdminManageBookAdapter.BookViewHolder> {

        private Context mContext;
        private List<UniqueBookDetails> mBookList;

        public AdminManageBookAdapter(Context mContext, List<UniqueBookDetails> mBookList) {
            this.mContext = mContext;
            this.mBookList = mBookList;
        }

        @NonNull
        @Override
        public AdminManageBookAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_manage_book_list_item, parent, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdminManageBookAdapter.BookViewHolder holder, int position) {
            UniqueBookDetails bookDetails = mBookList.get(position);
            holder.bookName.setText(bookDetails.bookName);
            holder.authorName.setText(bookDetails.authorName);
        }

        @Override
        public int getItemCount() {
            if(mBookList == null ) return 0;
            return mBookList.size();
        }

        public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView bookName;
            final TextView authorName;

            public BookViewHolder(View itemView) {
                super(itemView);

                bookName = itemView.findViewById(R.id.admin_manage_book_name);
                authorName = itemView.findViewById(R.id.admin_manage_book_author);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                onBookClicked(mBookList.get(pos));
            }
        }
    }
}
