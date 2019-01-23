package com.creation.daguru.ronginbookapp;

import android.app.Dialog;
import android.provider.ContactsContract;
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
import com.creation.daguru.ronginbookapp.data.ListUserBookExchangeDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_DAYS_LEFT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_MORE_DAY_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.isTimeValid;

public class BorrowedBookDetailsActivity extends AppCompatActivity implements View.OnClickListener {



    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvBookName;
    private TextView mtvAuthorName;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private TextView mtvDaysLeft;

    private TextView mtvbReturn;
    private TextView mtvbIncreaseDay;

    private NotificationDetails tNotificationDetails;
    private NotificationDetails tReturnNotificationDetails;

    private int mSelectedDays;

    private ListUserBookExchangeDetails mBookExchangeDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_book_details);

        mBookExchangeDetails = new ListUserBookExchangeDetails();

        checkForIntenteExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mtvBookName = findViewById(R.id.borrow_details_book_name);
        mtvAuthorName = findViewById(R.id.borrow_details_author);

        mivUserPic = findViewById(R.id.borrow_details_pro_pic);
        mtvUserName = findViewById(R.id.borrow_details_user_name);

        mtvDaysLeft = findViewById(R.id.borrow_details_day);

        mtvbReturn = findViewById(R.id.borrow_details_true);
        mtvbIncreaseDay = findViewById(R.id.borrow_details_false);

        mtvbReturn.setOnClickListener(this);
        mtvbIncreaseDay.setOnClickListener(this);

        setUpUI();
        setUpOtherUserPic();
    }

    private void setUpUI(){
        mtvBookName.setText(mBookExchangeDetails.bookName);
        mtvAuthorName.setText(mBookExchangeDetails.authorName);
        mtvUserName.setText(mBookExchangeDetails.otherUserName);
        mtvDaysLeft.setText(String.valueOf(mBookExchangeDetails.daysLeft));

        if(mBookExchangeDetails.otherUserUId.equals(RONGIN_UID)){
            mtvUserName.setText("rOngin");
        }
    }

    private void setUpOtherUserPic(){
        //private static final String DATABASE_DIR_USER_BASIC_INFO = "user-basic-info";
        //private static final String DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL = "photoUrl";


        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mBookExchangeDetails.otherUserUId).child(DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String photoUrl = dataSnapshot.getValue(String.class);
                    if(photoUrl != null && !photoUrl.equals("") && !photoUrl.isEmpty()) {

                        Log.d("PhotoUrl", photoUrl);

                        try {
                            Glide.with(BorrowedBookDetailsActivity.this).load(photoUrl)
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


    private void checkForIntenteExtras(){

        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_BOOK_NAME)){
            mBookExchangeDetails.bookName = getIntent().getStringExtra(EXTRA_KEY_EXCHANGE_BOOK_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_BOOK_AUTHOR)){
            mBookExchangeDetails.authorName = getIntent().getStringExtra(EXTRA_KEY_EXCHANGE_BOOK_AUTHOR);
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_BOOK_UID)){
            mBookExchangeDetails.bookUId = getIntent().getStringExtra(EXTRA_KEY_EXCHANGE_BOOK_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_UID)){
            mBookExchangeDetails.otherUserUId = getIntent().getStringExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_NAME)){
            mBookExchangeDetails.otherUserName = getIntent().getStringExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_DAYS_LEFT)){
            mBookExchangeDetails.daysLeft = getIntent().getIntExtra(EXTRA_KEY_EXCHANGE_DAYS_LEFT, 5);
        }
    }


    private void checkAndSendRequest(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mBookExchangeDetails.otherUserUId);

        databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_MORE_DAY_REQUEST))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int size = 0;
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            size++;
                        }

                        int i = 0;

                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            NotificationDetails notificationDetails = snapshot.getValue(NotificationDetails.class);
                            if(tNotificationDetails==null || notificationDetails.createTime >= tNotificationDetails.createTime){
                                tNotificationDetails = notificationDetails;
                            }
                            i++;
                            if(i==size){
                                decideToSendRequest();
                            }
                        }
                        if(size == 0){
                            decideToSendRequest();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void decideToSendRequest(){
        if(tNotificationDetails == null || tNotificationDetails.isValid == 0){
            NotificationDetails notificationDetails = new NotificationDetails(
                    TYPE_NOTIFICATION_MORE_DAY_REQUEST,
                    null,
                    mBookExchangeDetails.bookUId,
                    mBookExchangeDetails.bookName,
                    mFirebaseUser.getUid(),
                    mFirebaseUser.getDisplayName(),
                    getUTCDateFromLocal(System.currentTimeMillis()),
                    0,
                    1,
                    -1,
                    mSelectedDays,
                    mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_MORE_DAY_REQUEST)
            );

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mBookExchangeDetails.otherUserUId);

            String key = databaseReference.push().getKey();
            notificationDetails.notificationUId = key;
            databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(BorrowedBookDetailsActivity.this, "Request Sent.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(BorrowedBookDetailsActivity.this,
                                "Request failed. Check internet connection and try again.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mBookExchangeDetails.otherUserUId);
            dbRef.setValue(1);
        } else {
            Toast.makeText(this, "There is still an active request.", Toast.LENGTH_LONG).show();
        }
    }

    private void showCustomDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pick_day_limit);
        final NumberPicker numberPicker = dialog.findViewById(R.id.pick_day_number_picker);
        TextView tvbPositive = dialog.findViewById(R.id.pick_day_positive_button);
        TextView tvbNegative = dialog.findViewById(R.id.pick_day_negative_button);
        TextView tvTitle = dialog.findViewById(R.id.pick_day_title);
        TextView tvNotice = dialog.findViewById(R.id.pick_day_notice);

        tvTitle.setText("Request to increase");
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(5);
        tvNotice.setVisibility(View.GONE);

        tvbPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedDays = numberPicker.getValue();
                checkAndSendRequest();
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

    private void checkAndNotifyToReturnBook(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mBookExchangeDetails.otherUserUId);

        databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int size = 0;
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            size++;
                        }

                        int i = 0;
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            NotificationDetails notificationDetails = snapshot.getValue(NotificationDetails.class);
                            if(tReturnNotificationDetails == null
                                    || notificationDetails.createTime >= tReturnNotificationDetails.createTime){
                                tReturnNotificationDetails = notificationDetails;
                            }

                            i++;
                            if(i==size)
                                decideToNotify();
                        }

                        if(size == 0){
                            decideToNotify();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void decideToNotify(){
        if(tReturnNotificationDetails == null
                || !isTimeValid(getUTCDateFromLocal(System.currentTimeMillis()), tReturnNotificationDetails.createTime)
                || tReturnNotificationDetails.isValid == 0){

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mBookExchangeDetails.otherUserUId);

            NotificationDetails notificationDetails = new NotificationDetails(
                    TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE,
                    null,
                    mBookExchangeDetails.bookUId,
                    mBookExchangeDetails.bookName,
                    mFirebaseUser.getUid(),
                    mFirebaseUser.getDisplayName(),
                    getUTCDateFromLocal(System.currentTimeMillis()),
                    0,
                    1,
                    -1,
                    -1,
                    mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE)
            );

            String key = databaseReference.push().getKey();
            notificationDetails.notificationUId = key;

            databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(BorrowedBookDetailsActivity.this, "Notification sent.", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(BorrowedBookDetailsActivity.this,
                                "Sending failed. Please check internet connection.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mBookExchangeDetails.otherUserUId);
            dbRef.setValue(1);

        } else {
            Toast.makeText(BorrowedBookDetailsActivity.this, "There is still an active request.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.borrow_details_true:
                checkAndNotifyToReturnBook();
                break;
            case R.id.borrow_details_false:
                showCustomDialog();
                break;
        }
    }
}
