package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_ACCOUNT_FREEZE;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_ADDED;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_CONFIRMATION;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_GIVING_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_RECEIVED;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_RETURN;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE;

public class NotificationActivity extends AppCompatActivity implements NotificationViewAdapter.NotificationClickHandler {

    private static final String  TAG = "notification";

    private RecyclerView mRecyclerView;
    private TextView mNotificationEmptyMessage;
    private ProgressBar mLoading;

    private NotificationViewAdapter mAdapter;

    private RonginItemTouchHelper mItemTouchHelper;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private Query mQuery;
    private FirebaseUser mFirebaseUser;

    private ValueEventListener mNewNotificationListener;

    private List<NotificationDetails> mNotificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mRecyclerView = findViewById(R.id.noti_recyclerview);
        mNotificationEmptyMessage = findViewById(R.id.noti_no_noti_msg);
        mLoading = findViewById(R.id.noti_loading);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        setItemTouchListener();

        //Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        attachLightListener();
        mNotificationList = new ArrayList<>();

        mAdapter = new NotificationViewAdapter(this, mNotificationList, this);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachDatabaseReadListener();
        detachLightListener();
        super.onPause();
    }

    private void setItemTouchListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mFirebaseUser.getUid());

        mItemTouchHelper = new RonginItemTouchHelper(0, ItemTouchHelper.LEFT,
                this, databaseReference);
        new ItemTouchHelper(mItemTouchHelper).attachToRecyclerView(mRecyclerView);
    }

    private void attachDatabaseListener(){

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mFirebaseUser.getUid());

        Log.d(TAG, databaseReference.getKey());

        mQuery = databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_CREATE_TIME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    NotificationDetails notificationDetails = dataSnapshot.getValue(NotificationDetails.class);

                    //Log.d(TAG, String.valueOf(notificationDetails.isRead));

                    mNotificationList.add(notificationDetails);
                    mAdapter.notifyDataSetChanged();
                    showList();
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
                    showEmptyMessage();
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

    private void detachDatabaseReadListener() {
        //clearAdapter();
        if (mChildEventListener != null) {
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attachLightListener(){
        if(mNewNotificationListener == null){
            mNewNotificationListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state != 0){
                            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                                    .child(mFirebaseUser.getUid());

                            databaseReference.setValue(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mFirebaseUser.getUid());

            databaseReference.addValueEventListener(mNewNotificationListener);
        }
    }

    private void detachLightListener(){
        if(mNewNotificationListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mFirebaseUser.getUid());
            databaseReference.removeEventListener(mNewNotificationListener);

            mNewNotificationListener = null;
        }
    }













    private void showList(){
        mNotificationEmptyMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    private void showLoading(){
        mNotificationEmptyMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
    }

    private void showEmptyMessage(){
        mNotificationEmptyMessage.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
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


    @Override
    public void onClickedNotification(NotificationDetails notificationDetails) {
        switch (notificationDetails.notificationType){
            case TYPE_NOTIFICATION_BOOK_ADDED:
                Intent bookAddedIntent = new Intent(this, NotificationBookAddedActivity.class);
                bookAddedIntent = addExtrasToIntent(bookAddedIntent, notificationDetails);
                startActivity(bookAddedIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_CONFIRMATION:
                Intent bookAddConfirmationIntent = new Intent(this, NotificationBookConfirmActivity.class);
                bookAddConfirmationIntent = addExtrasToIntent(bookAddConfirmationIntent, notificationDetails);
                startActivity(bookAddConfirmationIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_REQUEST:
                Intent bookRequestIntent = new Intent(this, NotificationBookRequestActivity.class);
                bookRequestIntent = addExtrasToIntent(bookRequestIntent, notificationDetails);
                startActivity(bookRequestIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT:
                Intent bookRequestAcceptIntent = new Intent(this, NotificationBookRequestAcceptedActivity.class);
                bookRequestAcceptIntent = addExtrasToIntent(bookRequestAcceptIntent, notificationDetails);
                startActivity(bookRequestAcceptIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY:
                Intent bookRequestDenyIntent = new Intent(this, NotificationBookRequestDeniedActivity.class);
                bookRequestDenyIntent = addExtrasToIntent(bookRequestDenyIntent, notificationDetails);
                startActivity(bookRequestDenyIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_GIVING_REQUEST:
                Intent bookGivingRequestIntent = new Intent(this, NotificationBookGivingRequestActivity.class);
                bookGivingRequestIntent = addExtrasToIntent(bookGivingRequestIntent, notificationDetails);
                startActivity(bookGivingRequestIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_RECEIVED:
                Intent bookReceiveIntent = new Intent(this, NotificationBookReceivedActivity.class);
                bookReceiveIntent = addExtrasToIntent(bookReceiveIntent, notificationDetails);
                startActivity(bookReceiveIntent);
                break;
            case TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE:
                Intent receiveReturnedBookIntent = new Intent(this, NotificationReceiveReturnedBookActivity.class);
                receiveReturnedBookIntent = addExtrasToIntent(receiveReturnedBookIntent, notificationDetails);
                startActivity(receiveReturnedBookIntent);
                break;
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST:
                Intent dayRequestIntent = new Intent(this, NotificationDayRequestActivity.class);
                dayRequestIntent = addExtrasToIntent(dayRequestIntent, notificationDetails);
                startActivity(dayRequestIntent);
                break;
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT:
                Intent dayRequestAcceptIntent = new Intent(this, NotificationDayRequestAcceptedActivity.class);
                dayRequestAcceptIntent = addExtrasToIntent(dayRequestAcceptIntent, notificationDetails);
                startActivity(dayRequestAcceptIntent);
                break;
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY:
                Intent dayRequestDenyIntent = new Intent(this, NotificationDayRequestDeniedActivity.class);
                dayRequestDenyIntent = addExtrasToIntent(dayRequestDenyIntent, notificationDetails);
                startActivity(dayRequestDenyIntent);
                break;
            case TYPE_NOTIFICATION_BOOK_RETURN:
                Intent bookReturnIntent = new Intent(this, NotificationReturnBookActivity.class);
                bookReturnIntent = addExtrasToIntent(bookReturnIntent, notificationDetails);
                startActivity(bookReturnIntent);
                break;
            case TYPE_NOTIFICATION_ACCOUNT_FREEZE:
                Intent accountfreezeIntent = new Intent(this, NotificationAccountFreezedActivity.class);
                startActivity(accountfreezeIntent);
                break;
        }
    }
}
