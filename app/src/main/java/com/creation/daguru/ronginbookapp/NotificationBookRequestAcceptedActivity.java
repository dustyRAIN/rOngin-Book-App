package com.creation.daguru.ronginbookapp;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.MessageQuickDetails;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_UID;
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
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_GIVING_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.isTimeValid;

public class NotificationBookRequestAcceptedActivity extends AppCompatActivity implements View.OnClickListener{

    private NotificationDetails mNotificationDetails;
    private NotificationDetails tNotificationDetails;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvGeetUser;
    private TextView mtvBookName;
    private TextView mtvDetailMsg;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private TextView mtvbAccept;
    private TextView mtvbDecline;

    private int mSelectedDays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_book_request_accepted);

        mNotificationDetails = new NotificationDetails();

        checkForIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mtvGeetUser = findViewById(R.id.noti_book_req_ac_greet_user);
        mtvBookName = findViewById(R.id.noti_book_req_ac_book_name);
        mtvDetailMsg = findViewById(R.id.noti_book_req_ac_description);

        mivUserPic = findViewById(R.id.noti_req_ac_pro_pic);
        mtvUserName = findViewById(R.id.noti_req_ac_user_name);

        mtvbAccept = findViewById(R.id.noti_book_req_ac_true);
        mtvbDecline = findViewById(R.id.noti_book_req_ac_false);

        mtvbAccept.setOnClickListener(this);
        mtvbDecline.setOnClickListener(this);

        checkIfRead();
        setUpUI();
    }


    private void setUpUI(){
        mtvGeetUser.setText("Hey " + mFirebaseUser.getDisplayName() + ",");
        mtvBookName.setText(mNotificationDetails.bookName);
        mtvUserName.setText(mNotificationDetails.otherUserName);
        mtvDetailMsg.setText(String.format(getResources().getString(R.string.noti_details_book_request_reply_accept),
                mNotificationDetails.otherUserName));
        setUpOtherUserPic();
        checkForValidity();
    }

    private void setUpOtherUserPic(){
        //private static final String DATABASE_DIR_USER_BASIC_INFO = "user-basic-info";
        //private static final String DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL = "photoUrl";


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
                            Glide.with(NotificationBookRequestAcceptedActivity.this).load(photoUrl)
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




    private void checkIfRead(){

        if(mNotificationDetails.isRead == 0){
            mNotificationDetails.isRead = 1;

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mFirebaseUser.getUid())
                    .child(mNotificationDetails.notificationUId)
                    .child(DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ);
            databaseReference.setValue(1);
        }
    }

    private void setConnectionToSendMessage(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mFirebaseUser.getUid()).child(mNotificationDetails.otherUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MessageQuickDetails messageQuickDetails = dataSnapshot.getValue(MessageQuickDetails.class);
                if(messageQuickDetails == null){
                    setMessageInformation();
                } else {
                    goToMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setMessageInformation(){
        long currentTime = getUTCDateFromLocal(System.currentTimeMillis());

        MessageQuickDetails ownQuickDetails = new MessageQuickDetails(
                "",
                mNotificationDetails.otherUserName,
                mNotificationDetails.otherUserUId,
                currentTime,
                0,
                0,
                0
        );

        MessageQuickDetails otherQuickDetails = new MessageQuickDetails(
                "",
                mFirebaseUser.getDisplayName(),
                mFirebaseUser.getUid(),
                currentTime,
                0,
                0,
                0
        );

        DatabaseReference ownMessageReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mFirebaseUser.getUid()).child(mNotificationDetails.otherUserUId);

        DatabaseReference otherMessageReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mNotificationDetails.otherUserUId).child(mFirebaseUser.getUid());

        ownMessageReference.setValue(ownQuickDetails);
        otherMessageReference.setValue(otherQuickDetails);

        goToMessage();
    }

    private void goToMessage(){
        Intent intent = new Intent(this, ChatMessagesActivity.class);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_UID, mNotificationDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME, mNotificationDetails.otherUserName);
        startActivity(intent);
    }


    private void checkAndSendNotification(int selectedDays){
        Log.d("Jigshaw", String.valueOf(selectedDays));

        mSelectedDays = selectedDays;

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mNotificationDetails.otherUserUId);

        databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_GIVING_REQUEST ))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int size = 0;
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            size++;
                        }
                        int i = 0;
                        Log.d("hishi", "sa " + String.valueOf(size));
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            Log.d("hishi", String.valueOf(size));
                            NotificationDetails notificationDetails = snapshot.getValue(NotificationDetails.class);
                            if(tNotificationDetails == null ||
                                    tNotificationDetails.createTime <= notificationDetails.createTime){
                                tNotificationDetails = notificationDetails;
                                Log.d("hishi", String.valueOf(size));
                            }
                            i++;
                            if(i == size){
                                decideToSendNotification();
                            }
                        }
                        if(size == 0){
                            decideToSendNotification();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void decideToSendNotification(){
        if(tNotificationDetails == null || !isTimeValid(getUTCDateFromLocal(System.currentTimeMillis()),
                tNotificationDetails.createTime) || tNotificationDetails.isValid == 0){

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mNotificationDetails.otherUserUId);

            NotificationDetails notificationDetails = new NotificationDetails(
                    TYPE_NOTIFICATION_BOOK_GIVING_REQUEST,
                    null,
                    mNotificationDetails.bookUId,
                    mNotificationDetails.bookName,
                    mFirebaseUser.getUid(),
                    mFirebaseUser.getDisplayName(),
                    getUTCDateFromLocal(System.currentTimeMillis()),
                    0,
                    1,
                    -1,
                    mSelectedDays,
                    mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_GIVING_REQUEST)
            );

            String key = databaseReference.push().getKey();
            notificationDetails.notificationUId = key;

            databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(NotificationBookRequestAcceptedActivity.this,
                                "Request has been sent.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(NotificationBookRequestAcceptedActivity.this,
                                "Request was not sent.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mNotificationDetails.otherUserUId);
            dbRef.setValue(1);

        } else {
            Toast.makeText(this, "Another request is still active.", Toast.LENGTH_LONG).show();
        }
    }

    private void showCustomDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pick_day_limit);
        final NumberPicker numberPicker = dialog.findViewById(R.id.pick_day_number_picker);
        numberPicker.setMaxValue(365);
        numberPicker.setMinValue(7);
        TextView tvbPositive = dialog.findViewById(R.id.pick_day_positive_button);
        TextView tvbNegative = dialog.findViewById(R.id.pick_day_negative_button);

        tvbPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedDays = numberPicker.getValue();
                checkAndSendNotification(selectedDays);
                dialog.dismiss();
            }
        });

        tvbNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.noti_book_req_ac_true:
                setConnectionToSendMessage();
                break;
            case R.id.noti_book_req_ac_false:
                showCustomDialog();
                break;
        }
    }
}
