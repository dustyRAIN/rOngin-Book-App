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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_AUTHOR_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_COPY_COUNT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_LATITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_LONGITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OWNER_UID;

public class AdminRonginLibraryActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private AdminRonginBookViewAdapter mAdapter;

    private FloatingActionButton mfabAdd;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private List<RonginBookDetails> mBookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_rongin_library);

        mRecyclerView = findViewById(R.id.admin_rongin_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mfabAdd = findViewById(R.id.admin_manage_float_add);

        mfabAdd.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onResume() {
        mBookList = new ArrayList<>();
        mAdapter = new AdminRonginBookViewAdapter(this, mBookList);
        mRecyclerView.setAdapter(mAdapter);

        attachDatabaseListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachDatabaseListener();
        super.onPause();
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS);

        mQuery = databaseReference.orderByChild(DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_BOOK_NAME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    RonginBookDetails ronginBookDetails = dataSnapshot.getValue(RonginBookDetails.class);
                    mBookList.add(ronginBookDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    RonginBookDetails bookDetails = dataSnapshot.getValue(RonginBookDetails.class);
                    int id = getBookId(bookDetails);
                    mBookList.set(id, bookDetails);
                    mAdapter.notifyItemChanged(id);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    RonginBookDetails bookDetails = dataSnapshot.getValue(RonginBookDetails.class);
                    int id = getBookId(bookDetails);
                    mBookList.remove(id);
                    mAdapter.notifyItemRemoved(id);
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

    private int getBookId(RonginBookDetails bookDetails){
        int i = 0;
        for(RonginBookDetails details: mBookList){
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.admin_manage_float_add:
                Intent intent = new Intent(this, AdminAddBookToRonginActivity.class);
                startActivity(intent);
                break;
        }
    }

    private Intent addExtrasToIntent(Intent intent, RonginBookDetails bookDetails){
        intent.putExtra(EXTRA_KEY_BOOK_NAME, bookDetails.bookName);
        intent.putExtra(EXTRA_KEY_AUTHOR_NAME, bookDetails.authorName);
        intent.putExtra(EXTRA_KEY_BOOK_UID, bookDetails.bookUId);
        intent.putExtra(EXTRA_KEY_OWNER_UID, bookDetails.ownerUId);
        intent.putExtra(EXTRA_KEY_COPY_COUNT, bookDetails.copyCount);
        intent.putExtra(EXTRA_KEY_LATITUDE, bookDetails.ownerLatitude);
        intent.putExtra(EXTRA_KEY_LONGITUDE, bookDetails.ownerLongitude);

        return intent;
    }

    private void onClickedBook(RonginBookDetails bookDetails){
        Intent intent = new Intent(this, AdminRonginBookDetailsActivity.class);
        intent = addExtrasToIntent(intent, bookDetails);
        startActivity(intent);
    }












    private class AdminRonginBookViewAdapter extends RecyclerView.Adapter<AdminRonginBookViewAdapter.BookViewHolder> {

        private Context mContext;
        private List<RonginBookDetails> mBookList;

        public AdminRonginBookViewAdapter(Context mContext, List<RonginBookDetails> mBookList) {
            this.mContext = mContext;
            this.mBookList = mBookList;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_manage_book_list_item, parent, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            RonginBookDetails ronginBookDetails = mBookList.get(position);
            holder.bookName.setText(ronginBookDetails.bookName);
            holder.authorName.setText(ronginBookDetails.authorName);
        }

        @Override
        public int getItemCount() {
            if(mBookList == null) return 0;
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
                int id = getAdapterPosition();
                onClickedBook(mBookList.get(id));
            }
        }
    }
}
