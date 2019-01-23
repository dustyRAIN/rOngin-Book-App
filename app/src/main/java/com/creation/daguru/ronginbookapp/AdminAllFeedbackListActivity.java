package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UserFeedbackDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_ALL_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_ALL_FEEDBACK_CHILD_FEEDBACK_TIME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_FEEDBACK_TIME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_FEEDBACK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_IS_READ;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;

public class AdminAllFeedbackListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private AdminFeedbackViewAdapter mAdapter;

    private RonginItemTouchHelper mItemTouchHelper;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mLightListener;
    private Query mQuery;

    private List<UserFeedbackDetails> mFeedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_feedback_list);

        mRecyclerView = findViewById(R.id.admin_feedback_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        setItemTouchListener();
    }

    @Override
    protected void onResume() {
        attachLightListener();
        mFeedbackList = new ArrayList<>();

        mAdapter = new AdminAllFeedbackListActivity.AdminFeedbackViewAdapter(this, mFeedbackList);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachLightListener();
        detachDatabaseReadListener();
        super.onPause();
    }



    private void setItemTouchListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_ALL_FEEDBACK);

        mItemTouchHelper = new RonginItemTouchHelper(0, ItemTouchHelper.LEFT,
                this, databaseReference);
        new ItemTouchHelper(mItemTouchHelper).attachToRecyclerView(mRecyclerView);
    }

    private void attachLightListener(){
        if(mLightListener == null){
            mLightListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state != 0){
                            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_FEEDBACK);

                            databaseReference.setValue(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_FEEDBACK);
            databaseReference.addValueEventListener(mLightListener);
        }
    }

    private void detachLightListener(){
        if(mLightListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_FEEDBACK);
            databaseReference.removeEventListener(mLightListener);
        }
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_ALL_FEEDBACK);

        //Log.d(TAG, databaseReference.getKey());

        mQuery = databaseReference.orderByChild(DATABASE_DIR_USER_ALL_FEEDBACK_CHILD_FEEDBACK_TIME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserFeedbackDetails userFeedbackDetails = dataSnapshot.getValue(UserFeedbackDetails.class);
                    userFeedbackDetails.feedbackUId = dataSnapshot.getKey();

                    //Log.d(TAG, String.valueOf(notificationDetails.isRead));

                    mFeedbackList.add(userFeedbackDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserFeedbackDetails userFeedbackDetails = dataSnapshot.getValue(UserFeedbackDetails.class);
                    int id = getListItemId(userFeedbackDetails);

                    mFeedbackList.set(id, userFeedbackDetails);
                    mAdapter.notifyItemChanged(id);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    UserFeedbackDetails userFeedbackDetails = dataSnapshot.getValue(UserFeedbackDetails.class);
                    int id = getListItemId(userFeedbackDetails);

                    mFeedbackList.remove(id);
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

    private int getListItemId(UserFeedbackDetails userFeedbackDetails){
        int i = 0;
        for(UserFeedbackDetails details: mFeedbackList){
            if(details.userUId.equals(userFeedbackDetails.userUId)){
                return i;
            }
            i++;
        }

        return -1;
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener != null) {
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private Intent addExtrasToIntent(Intent intent, UserFeedbackDetails userFeedbackDetails){
        intent.putExtra(EXTRA_KEY_FEEDBACK, userFeedbackDetails.feedback);
        intent.putExtra(EXTRA_KEY_USER_UID, userFeedbackDetails.userUId);
        intent.putExtra(EXTRA_KEY_USER_NAME, userFeedbackDetails.userName);
        intent.putExtra(EXTRA_KEY_FEEDBACK_TIME, userFeedbackDetails.feedbackTime);
        intent.putExtra(EXTRA_KEY_IS_READ, userFeedbackDetails.isRead);
        intent.putExtra(EXTRA_KEY_FEEDBACK_UID, userFeedbackDetails.feedbackUId);

        return intent;
    }

    private void onFeedbackClicked(UserFeedbackDetails userFeedbackDetails){
        Intent intent = new Intent(this, AdminFeedbackDetailsActivity.class);
        addExtrasToIntent(intent, userFeedbackDetails);
        startActivity(intent);
    }









    private class AdminFeedbackViewAdapter extends
            RecyclerView.Adapter<AdminAllFeedbackListActivity.AdminFeedbackViewAdapter.FeedbackViewHolder> {

        private Context mContext;
        private List<UserFeedbackDetails> mFeedbackList;

        public AdminFeedbackViewAdapter(Context mContext, List<UserFeedbackDetails> mFeedbackList) {
            this.mContext = mContext;
            this.mFeedbackList = mFeedbackList;
        }

        @NonNull
        @Override
        public AdminAllFeedbackListActivity.AdminFeedbackViewAdapter.FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_feedback_list_item, parent, false);
            return new AdminAllFeedbackListActivity.AdminFeedbackViewAdapter.FeedbackViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdminAllFeedbackListActivity.AdminFeedbackViewAdapter.FeedbackViewHolder holder,
                                     int position) {
            UserFeedbackDetails userFeedbackDetails = mFeedbackList.get(position);

            holder.itemView.setTag(userFeedbackDetails.userUId);

            if(userFeedbackDetails.isRead == 0){
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
                holder.userName.setVisibility(View.GONE);
                holder.createTime.setVisibility(View.GONE);

                holder.boldUserName.setVisibility(View.VISIBLE);
                holder.boldCreateTime.setVisibility(View.VISIBLE);

                holder.boldUserName.setText(userFeedbackDetails.userName);
                holder.boldCreateTime.setText(getFriendlyDateString(mContext,
                        getLocalDateFromUTC(userFeedbackDetails.feedbackTime),
                        true));
            } else {
                holder.boldUserName.setVisibility(View.GONE);
                holder.boldCreateTime.setVisibility(View.GONE);

                holder.userName.setVisibility(View.VISIBLE);
                holder.createTime.setVisibility(View.VISIBLE);

                holder.userName.setText(userFeedbackDetails.userName);
                holder.createTime.setText(getFriendlyDateString(mContext,
                        getLocalDateFromUTC(userFeedbackDetails.feedbackTime),
                        true));
            }
        }

        @Override
        public int getItemCount() {
            if(mFeedbackList == null) return 0;
            return mFeedbackList.size();
        }

        public class FeedbackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView userName;
            final TextView createTime;
            final TextView boldUserName;
            final TextView boldCreateTime;

            public FeedbackViewHolder(View itemView) {
                super(itemView);

                userName = itemView.findViewById(R.id.admin_feedback_list_user_name);
                createTime = itemView.findViewById(R.id.admin_feedback_list_date);
                boldUserName = itemView.findViewById(R.id.admin_feedback_list_user_name_bold);
                boldCreateTime = itemView.findViewById(R.id.admin_feedback_list_date_bold);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                onFeedbackClicked(mFeedbackList.get(id));
            }
        }
    }
}
