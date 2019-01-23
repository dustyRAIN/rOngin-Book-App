package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.Utils.RonginDateUtils;
import com.creation.daguru.ronginbookapp.data.ListUserBookExchangeDetails;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LENT_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LENT_BOOKS_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_DAYS_LEFT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class AdminLentBookListActivity extends AppCompatActivity {



    private RecyclerView mRecyclerView;
    private AdminLentListViewAdapter mAdapter;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private List<ListUserBookExchangeDetails> mLentList;

    private int mDaysLeft;
    private long mCurrentTimeInMillies;

    private UserExchangeBookDetails mBookDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lent_book_list);

        mRecyclerView = findViewById(R.id.admin_lent_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onResume() {
        mCurrentTimeInMillies = System.currentTimeMillis();
        mLentList = new ArrayList<>();
        mAdapter = new AdminLentListViewAdapter(this, mLentList);
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
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS).child(RONGIN_UID);
        mQuery = databaseReference.orderByChild(DATABASE_DIR_USER_LENT_BOOKS_BOOK_NAME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserExchangeBookDetails bookDetails = dataSnapshot.getValue(UserExchangeBookDetails.class);
                    mDaysLeft =  RonginDateUtils.getLeftDaysCount(bookDetails.exchangeTime, mCurrentTimeInMillies,
                            bookDetails.dayLimit);

                    mLentList.add(new ListUserBookExchangeDetails(
                            bookDetails.bookName,
                            bookDetails.authorName,
                            bookDetails.bookUId,
                            bookDetails.otherUserUId,
                            bookDetails.otherUserName,
                            mDaysLeft));

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

    private void detachDatabaseListener(){
        if(mChildEventListener != null){
            mQuery.removeEventListener(mChildEventListener);
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

    private void lentBookOnClicked(ListUserBookExchangeDetails bookExchangeDetails){
        Intent lentDetailsIntent = new Intent(this, AdminLentBookDetailsActivity.class);
        lentDetailsIntent = addExtrasToIntent(lentDetailsIntent, bookExchangeDetails);
        startActivity(lentDetailsIntent);
    }














    private class AdminLentListViewAdapter extends RecyclerView.Adapter<AdminLentListViewAdapter.ListViewAdapter> {

        private Context mContext;
        private List<ListUserBookExchangeDetails> mLentList;

        public AdminLentListViewAdapter(Context mContext, List<ListUserBookExchangeDetails> mLentList) {
            this.mContext = mContext;
            this.mLentList = mLentList;
        }

        @NonNull
        @Override
        public ListViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_book_lent_list_item, parent, false);
            return new ListViewAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewAdapter holder, int position) {
            ListUserBookExchangeDetails bookExchangeDetails = mLentList.get(position);
            if(bookExchangeDetails.daysLeft <= 3){

                holder.daysLeft.setVisibility(View.GONE);
                holder.daysLeftRed.setVisibility(View.VISIBLE);

                if(bookExchangeDetails.daysLeft == 0){
                    holder.daysLeftRed.setText("Contract");
                } else if(bookExchangeDetails.daysLeft == 1){
                    holder.daysLeftRed.setText(bookExchangeDetails.daysLeft + " day left");
                } else {
                    holder.daysLeftRed.setText(bookExchangeDetails.daysLeft + " days left");
                }
            } else {
                holder.daysLeft.setVisibility(View.VISIBLE);
                holder.daysLeftRed.setVisibility(View.GONE);

                holder.daysLeft.setText(bookExchangeDetails.daysLeft + " days left");
            }

            holder.bookName.setText(bookExchangeDetails.bookName);
            holder.authorName.setText(bookExchangeDetails.authorName);
            holder.userName.setText(bookExchangeDetails.otherUserName);
        }

        @Override
        public int getItemCount() {
            if(mLentList == null) return 0;
            return mLentList.size();
        }

        public class ListViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView bookName;
            final TextView authorName;
            final TextView userName;
            final TextView daysLeft;
            final TextView daysLeftRed;

            public ListViewAdapter(View itemView) {
                super(itemView);

                bookName = itemView.findViewById(R.id.list_ex_book_name);
                authorName = itemView.findViewById(R.id.list_ex_author_name);
                userName = itemView.findViewById(R.id.list_ex_user_name);
                daysLeft = itemView.findViewById(R.id.list_ex_days_left);
                daysLeftRed = itemView.findViewById(R.id.list_ex_days_left_red);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                lentBookOnClicked(mLentList.get(id));
            }
        }
    }
}
