package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_VALID;
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
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class NotificationBookRequestActivity extends AppCompatActivity implements View.OnClickListener {


    private NotificationDetails mNotificationDetails;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvGeetUser;
    private TextView mtvBookName;
    private TextView mtvWarnMsg;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private TextView mtvbAccept;
    private TextView mtvbDecline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_book_request);

        mNotificationDetails = new NotificationDetails();

        checkForIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mtvGeetUser = findViewById(R.id.noti_book_req_greet_user);
        mtvBookName = findViewById(R.id.noti_book_req_book_name);

        mivUserPic = findViewById(R.id.noti_req_pro_pic);
        mtvUserName = findViewById(R.id.noti_req_user_name);
        mtvWarnMsg = findViewById(R.id.noti_req_warn_msg);

        mtvbAccept = findViewById(R.id.noti_book_req_true);
        mtvbDecline = findViewById(R.id.noti_book_req_false);

        mtvbAccept.setOnClickListener(this);
        mtvbDecline.setOnClickListener(this);

        setUpUI();
    }


    private void setUpUI(){
        mtvGeetUser.setText("Hey " + mFirebaseUser.getDisplayName() + ",");
        mtvBookName.setText(mNotificationDetails.bookName);
        mtvUserName.setText(mNotificationDetails.otherUserName);
        mtvWarnMsg.setText(String.format("* This person is holding %d borrowed books currently.",
                mNotificationDetails.notificationReplyType));
        if(mNotificationDetails.notificationReplyType > 2){
            mtvWarnMsg.setTextColor(getResources().getColor(R.color.colorPerfectRonginRed));
        }
        setUpOtherUserPic();
        checkForValidity();
    }

    private void setUpOtherUserPic(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mNotificationDetails.otherUserUId).child(DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String photoUrl = dataSnapshot.getValue(String.class);
                    if(photoUrl != null && !photoUrl.equals("") && !photoUrl.isEmpty()) {

                        Log.d("PhotoUrl", photoUrl);

                        try {
                            Glide.with(NotificationBookRequestActivity.this).load(photoUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(mivUserPic);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            mNotificationDetails.notificationReplyType = getIntent().getIntExtra(EXTRA_KEY_NOTIFICATION_REPLY_TYPE, 0);
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
                .child(mFirebaseUser.getUid())
                .child(mNotificationDetails.notificationUId)
                .child(DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ);
        databaseReference.setValue(1);

        databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mFirebaseUser.getUid())
                .child(mNotificationDetails.notificationUId)
                .child(DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_VALID);
        databaseReference.setValue(0);
    }

    private void acceptAndSendNotification(){
        NotificationDetails notificationDetails = new NotificationDetails(
                TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT,
                null,
                mNotificationDetails.bookUId,
                mNotificationDetails.bookName,
                mFirebaseUser.getUid(),
                mFirebaseUser.getDisplayName(),
                getUTCDateFromLocal(System.currentTimeMillis()),
                0,
                1,
                -1,
                -1,
                mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT)
                );

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);

        String key = databaseReference.push().getKey();
        notificationDetails.notificationUId = key;
        databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    readAndDestroyValidity();
                } else {
                    mtvbAccept.setEnabled(true);
                    mtvbDecline.setEnabled(true);
                    Toast.makeText(NotificationBookRequestActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);
        dbRef.setValue(1);
    }

    private void denyAndNotification(){
        NotificationDetails notificationDetails = new NotificationDetails(
                TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY,
                null,
                mNotificationDetails.bookUId,
                mNotificationDetails.bookName,
                mFirebaseUser.getUid(),
                mFirebaseUser.getDisplayName(),
                getUTCDateFromLocal(System.currentTimeMillis()),
                0,
                1,
                -1,
                -1,
                mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY)
        );

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);

        String key = databaseReference.push().getKey();
        notificationDetails.notificationUId = key;
        databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    readAndDestroyValidity();
                } else {
                    mtvbAccept.setEnabled(true);
                    mtvbDecline.setEnabled(true);
                    Toast.makeText(NotificationBookRequestActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
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
            case R.id.noti_book_req_true:
                mtvbAccept.setEnabled(false);
                mtvbDecline.setEnabled(false);
                acceptAndSendNotification();
                break;

            case R.id.noti_book_req_false:
                mtvbAccept.setEnabled(false);
                mtvbDecline.setEnabled(false);
                denyAndNotification();
                break;
        }
    }
}
