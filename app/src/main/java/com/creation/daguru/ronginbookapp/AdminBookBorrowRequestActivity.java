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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.NotificationDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_CREATE_TIME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CREATE_TIME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_IS_READ;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_IS_VALID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_NOIFICATION_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_NOTIFICATION_REPLY_TYPE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_NOTIFICATION_TYPE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_REQUESTED_DAY;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_GIVING_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.getNotificationDescription;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.getNotificationTitle;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;

public class AdminBookBorrowRequestActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private AdminBookRequestViewAdapter mAdapter;

    private RonginItemTouchHelper mItemTouchHelper;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mLightListener;
    private Query mQuery;

    private List<NotificationDetails> mNotificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_book_borrow_request);

        mRecyclerView = findViewById(R.id.admin_book_request_recycler_view);
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
        mNotificationList = new ArrayList<>();

        mAdapter = new AdminBookRequestViewAdapter(this, mNotificationList);
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
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(RONGIN_UID);

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
                            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                                    .child(RONGIN_UID);

                            databaseReference.setValue(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(RONGIN_UID);
            databaseReference.addValueEventListener(mLightListener);
        }
    }

    private void detachLightListener(){
        if(mLightListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(RONGIN_UID);
            databaseReference.removeEventListener(mLightListener);
        }
    }


    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(RONGIN_UID);

        //Log.d(TAG, databaseReference.getKey());

        mQuery = databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_CREATE_TIME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    NotificationDetails notificationDetails = dataSnapshot.getValue(NotificationDetails.class);

                    //Log.d(TAG, String.valueOf(notificationDetails.isRead));

                    mNotificationList.add(notificationDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    NotificationDetails notificationDetails = dataSnapshot.getValue(NotificationDetails.class);
                    int id = getListItemId(notificationDetails);

                    mNotificationList.set(id, notificationDetails);
                    mAdapter.notifyItemChanged(id);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    NotificationDetails notificationDetails = dataSnapshot.getValue(NotificationDetails.class);
                    int id = getListItemId(notificationDetails);

                    mNotificationList.remove(id);
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

    private int getListItemId(NotificationDetails notificationDetails){
        int i = 0;
        for(NotificationDetails details: mNotificationList){
            if(details.notificationUId.equals(notificationDetails.notificationUId)){
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

    private Intent addExtrasToIntent(Intent intent, NotificationDetails notificationDetails){
        intent.putExtra(EXTRA_KEY_NOTIFICATION_TYPE, notificationDetails.notificationType);
        intent.putExtra(EXTRA_KEY_NOIFICATION_UID, notificationDetails.notificationUId);
        intent.putExtra(EXTRA_KEY_BOOK_UID, notificationDetails.bookUId);
        intent.putExtra(EXTRA_KEY_BOOK_NAME, notificationDetails.bookName);
        intent.putExtra(EXTRA_KEY_OTHER_USER_UID, notificationDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_OTHER_USER_NAME, notificationDetails.otherUserName);
        intent.putExtra(EXTRA_KEY_CREATE_TIME, notificationDetails.createTime);
        intent.putExtra(EXTRA_KEY_IS_READ, notificationDetails.isRead);
        intent.putExtra(EXTRA_KEY_IS_VALID, notificationDetails.isValid);
        intent.putExtra(EXTRA_KEY_NOTIFICATION_REPLY_TYPE, notificationDetails.notificationReplyType);
        intent.putExtra(EXTRA_KEY_REQUESTED_DAY, notificationDetails.requestedDay);

        return intent;
    }

    private void onNotificationClicked(NotificationDetails notificationDetails){
        switch (notificationDetails.notificationType) {
            case TYPE_NOTIFICATION_BOOK_REQUEST:
                Intent bookRequestIntent = new Intent(this, AdminNotificationBookRquestActivity.class);
                bookRequestIntent = addExtrasToIntent(bookRequestIntent, notificationDetails);
                startActivity(bookRequestIntent);
                break;

            case TYPE_NOTIFICATION_BOOK_GIVING_REQUEST:
                Intent bookGivingIntent = new Intent(this, AdminNotificationBookGivingRequestActivity.class);
                bookGivingIntent = addExtrasToIntent(bookGivingIntent, notificationDetails);
                startActivity(bookGivingIntent);
                break;

            case TYPE_NOTIFICATION_MORE_DAY_REQUEST:
                Intent increaseRequestIntent = new Intent(this, AdminNotificationDayRequestActivity.class);
                increaseRequestIntent = addExtrasToIntent(increaseRequestIntent, notificationDetails);
                startActivity(increaseRequestIntent);
                break;

            case TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE:
                Intent bookRecieveIntent = new Intent(this, AdminNotificationRecieveBookActivity.class);
                bookRecieveIntent = addExtrasToIntent(bookRecieveIntent, notificationDetails);
                startActivity(bookRecieveIntent);
                break;
        }
    }








    private class AdminBookRequestViewAdapter extends RecyclerView.Adapter<AdminBookRequestViewAdapter.RequestViewHolder> {

        private Context mContext;
        private List<NotificationDetails> mNotificationList;

        public AdminBookRequestViewAdapter(Context mContext, List<NotificationDetails> mNotificationList) {
            this.mContext = mContext;
            this.mNotificationList = mNotificationList;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_notification_list_item, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            NotificationDetails notificationDetails = mNotificationList.get(position);

            holder.itemView.setTag(notificationDetails.notificationUId);

            if(notificationDetails.isRead == 0){
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
                holder.notificationTitle.setVisibility(View.GONE);
                holder.notificationTime.setVisibility(View.GONE);
                holder.notificationDescription.setVisibility(View.GONE);

                holder.boldNotificationTitle.setVisibility(View.VISIBLE);
                holder.boldNotificationTime.setVisibility(View.VISIBLE);
                holder.boldNotificationDescription.setVisibility(View.VISIBLE);

                holder.boldNotificationTitle.setText(getNotificationTitle(mContext,notificationDetails.notificationType));
                holder.boldNotificationTime.setText(getFriendlyDateString(mContext,
                        getLocalDateFromUTC(notificationDetails.createTime),
                        true));
                holder.boldNotificationDescription.setText(getNotificationDescription(mContext, notificationDetails));

            } else {
                holder.notificationTitle.setVisibility(View.VISIBLE);
                holder.notificationTime.setVisibility(View.VISIBLE);
                holder.notificationDescription.setVisibility(View.VISIBLE);

                holder.boldNotificationTitle.setVisibility(View.GONE);
                holder.boldNotificationTime.setVisibility(View.GONE);
                holder.boldNotificationDescription.setVisibility(View.GONE);

                holder.notificationTitle.setText(getNotificationTitle(mContext,notificationDetails.notificationType));
                holder.notificationTime.setText(getFriendlyDateString(mContext,
                        getLocalDateFromUTC(notificationDetails.createTime),
                        true));
                holder.notificationDescription.setText(getNotificationDescription(mContext, notificationDetails));
            }
        }

        @Override
        public int getItemCount() {
            if(mNotificationList == null) return 0;
            return mNotificationList.size();
        }

        public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView notificationTitle;
            final TextView boldNotificationTitle;
            final TextView notificationTime;
            final TextView boldNotificationTime;
            final TextView notificationDescription;
            final TextView boldNotificationDescription;

            public RequestViewHolder(View itemView) {
                super(itemView);

                notificationTitle = itemView.findViewById(R.id.noti_title);
                boldNotificationTitle = itemView.findViewById(R.id.noti_title_bold);
                notificationTime = itemView.findViewById(R.id.noti_time);
                boldNotificationTime = itemView.findViewById(R.id.noti_time_bold);
                notificationDescription = itemView.findViewById(R.id.noti_description);
                boldNotificationDescription = itemView.findViewById(R.id.noti_description_bold);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                onNotificationClicked(mNotificationList.get(id));
            }
        }
    }
}
