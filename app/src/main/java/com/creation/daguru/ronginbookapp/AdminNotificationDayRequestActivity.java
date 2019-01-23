package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LENT_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_VALID;
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
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_ADDED;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getDayDifference;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class AdminNotificationDayRequestActivity extends AppCompatActivity implements View.OnClickListener {

    private NotificationDetails mNotificationDetails;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvGeetUser;
    private TextView mtvBookName;
    private TextView mtvDayNumber;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private TextView mtvbAccept;
    private TextView mtvbDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_day_request);

        mNotificationDetails = new NotificationDetails();

        checkForIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mtvGeetUser = findViewById(R.id.noti_day_req_greet_user);
        mtvBookName = findViewById(R.id.noti_day_req_book_name);
        mtvDayNumber = findViewById(R.id.noti_day_req_day);

        mivUserPic = findViewById(R.id.noti_req_pro_pic);
        mtvUserName = findViewById(R.id.noti_req_user_name);

        mtvbAccept = findViewById(R.id.noti_day_req_true);
        mtvbDecline = findViewById(R.id.noti_day_req_false);

        mtvbAccept.setOnClickListener(this);
        mtvbDecline.setOnClickListener(this);

        setUpUI();
    }


    private void setUpUI(){
        mtvGeetUser.setText("Hey " + mFirebaseUser.getDisplayName() + ",");
        mtvBookName.setText(mNotificationDetails.bookName);
        mtvUserName.setText(mNotificationDetails.otherUserName);
        mtvDayNumber.setText(String.valueOf(mNotificationDetails.requestedDay) + " more days");
        checkForValidity();
    }

    private void checkForValidity(){
        if(mNotificationDetails.isValid == 0){
            mtvbAccept.setEnabled(false);
            mtvbDecline.setEnabled(false);
        } else {
            mtvbAccept.setEnabled(true);
            mtvbDecline.setEnabled(true);
        }
    }




    private void checkForIntentExtras(){

        if(getIntent().hasExtra(EXTRA_KEY_NOTIFICATION_TYPE)){
            mNotificationDetails.notificationType = getIntent().getIntExtra(EXTRA_KEY_NOTIFICATION_TYPE,
                    TYPE_NOTIFICATION_BOOK_ADDED);
        }
        if(getIntent().hasExtra(EXTRA_KEY_NOIFICATION_UID)){
            mNotificationDetails.notificationUId = getIntent().getStringExtra(EXTRA_KEY_NOIFICATION_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_BOOK_UID)){
            mNotificationDetails.bookUId = getIntent().getStringExtra(EXTRA_KEY_BOOK_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_BOOK_NAME)){
            mNotificationDetails.bookName = getIntent().getStringExtra(EXTRA_KEY_BOOK_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_OTHER_USER_UID)){
            mNotificationDetails.otherUserUId = getIntent().getStringExtra(EXTRA_KEY_OTHER_USER_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_OTHER_USER_NAME)){
            mNotificationDetails.otherUserName = getIntent().getStringExtra(EXTRA_KEY_OTHER_USER_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_CREATE_TIME)){
            mNotificationDetails.createTime = getIntent().getLongExtra(EXTRA_KEY_CREATE_TIME,
                    getUTCDateFromLocal(System.currentTimeMillis()));
        }
        if(getIntent().hasExtra(EXTRA_KEY_IS_READ)){
            mNotificationDetails.isRead = getIntent().getIntExtra(EXTRA_KEY_IS_READ, 1);
        }
        if(getIntent().hasExtra(EXTRA_KEY_IS_VALID)){
            mNotificationDetails.isValid = getIntent().getIntExtra(EXTRA_KEY_IS_VALID, 0);
        }
        if(getIntent().hasExtra(EXTRA_KEY_NOTIFICATION_REPLY_TYPE)){
            mNotificationDetails.notificationReplyType = getIntent().getIntExtra(EXTRA_KEY_NOTIFICATION_REPLY_TYPE,
                    0);
        }
        if(getIntent().hasExtra(EXTRA_KEY_REQUESTED_DAY)){
            mNotificationDetails.requestedDay = getIntent().getIntExtra(EXTRA_KEY_REQUESTED_DAY, 0);
        }
    }




    private void readAndDestroyValidity(){

        mNotificationDetails.isValid = 0;

        checkForValidity();

        mNotificationDetails.isRead = 1;
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(RONGIN_UID)
                .child(mNotificationDetails.notificationUId)
                .child(DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ);
        databaseReference.setValue(1);

        databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(RONGIN_UID)
                .child(mNotificationDetails.notificationUId)
                .child(DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_VALID);
        databaseReference.setValue(0);
    }



    private void increaseDayAndNotify(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS)
                .child(RONGIN_UID).child(mNotificationDetails.otherUserUId + "_" + mNotificationDetails.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserExchangeBookDetails bookDetails = dataSnapshot.getValue(UserExchangeBookDetails.class);
                int dayTillNow = getDayDifference(getUTCDateFromLocal(System.currentTimeMillis()),
                        bookDetails.exchangeTime);

                if(dayTillNow < bookDetails.dayLimit){
                    dayTillNow = bookDetails.dayLimit;
                }

                setDayLimit(bookDetails, dayTillNow + mNotificationDetails.requestedDay);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDayLimit(UserExchangeBookDetails bookDetails, int day){
        bookDetails.dayLimit = day;

        setOwnDayLimit(bookDetails);
        setOtherDayLimit(bookDetails);

        sendPositiveNotification();
    }

    private void setOwnDayLimit(UserExchangeBookDetails bookDetails){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS)
                .child(RONGIN_UID).child(mNotificationDetails.otherUserUId + "_" + mNotificationDetails.bookUId);

        databaseReference.setValue(bookDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminNotificationDayRequestActivity.this,
                            "Day Increased", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setOtherDayLimit(UserExchangeBookDetails bookDetails){
        bookDetails.otherUserUId = RONGIN_UID;
        bookDetails.otherUserName = "rOngin";

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                .child(mNotificationDetails.otherUserUId).child(RONGIN_UID + "_" + mNotificationDetails.bookUId);

        databaseReference.setValue(bookDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminNotificationDayRequestActivity.this,
                            "Day Increased", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendPositiveNotification(){
        NotificationDetails notificationDetails = new NotificationDetails();
        notificationDetails.bookName = mNotificationDetails.bookName;
        notificationDetails.bookUId = mNotificationDetails.bookUId;
        notificationDetails.requestedDay = mNotificationDetails.requestedDay;
        notificationDetails.notificationType = TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT;
        notificationDetails.otherUId_notiType = RONGIN_UID + "_" +
                String.valueOf(TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT);
        notificationDetails.notificationReplyType = mNotificationDetails.notificationReplyType;
        notificationDetails.otherUserUId = RONGIN_UID;
        notificationDetails.otherUserName = "rOngin";
        notificationDetails.createTime = getUTCDateFromLocal(System.currentTimeMillis());
        notificationDetails.isValid = 1;
        notificationDetails.isRead = 0;

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);

        Log.d("notify", mNotificationDetails.otherUserUId);

        String key = databaseReference.push().getKey();
        notificationDetails.notificationUId = key;

        databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminNotificationDayRequestActivity.this, "Notification Sent.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminNotificationDayRequestActivity.this,
                            "Request failed. Check internet connection and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);
        dbRef.setValue(1);
    }

    private void sendNegativeNotification(){
        NotificationDetails notificationDetails = new NotificationDetails();
        notificationDetails.notificationType = TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY;
        notificationDetails.bookName = mNotificationDetails.bookName;
        notificationDetails.bookUId = mNotificationDetails.bookUId;
        notificationDetails.otherUId_notiType = RONGIN_UID + "_" +
                String.valueOf(TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT);
        notificationDetails.notificationReplyType = mNotificationDetails.notificationReplyType;
        notificationDetails.otherUserUId = RONGIN_UID;
        notificationDetails.otherUserName = "rOngin";
        notificationDetails.createTime = getUTCDateFromLocal(System.currentTimeMillis());
        notificationDetails.isValid = 1;
        notificationDetails.isRead = 0;
        notificationDetails.requestedDay = -1;

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);

        String key = databaseReference.push().getKey();
        notificationDetails.notificationUId = key;

        databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminNotificationDayRequestActivity.this, "Notification Sent.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminNotificationDayRequestActivity.this,
                            "Request failed. Check internet connection and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);
        dbRef.setValue(1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.noti_day_req_true:
                readAndDestroyValidity();
                increaseDayAndNotify();
                break;

            case R.id.noti_day_req_false:
                readAndDestroyValidity();
                sendNegativeNotification();
                break;
        }
    }
}
