package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_IS_READ;
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
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class NotificationBookRequestDeniedActivity extends AppCompatActivity {

    private NotificationDetails mNotificationDetails;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvGeetUser;
    private TextView mtvBookName;
    private TextView mtvAuthorName;
    private TextView mtvDetailMsg;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private String mAuthorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_book_request_denied);

        mNotificationDetails = new NotificationDetails();
        checkForIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mivUserPic = findViewById(R.id.noti_req_den_pro_pic);
        mtvUserName = findViewById(R.id.noti_req_den_user_name);

        mtvGeetUser = findViewById(R.id.noti_book_req_den_greet_user);
        mtvBookName = findViewById(R.id.noti_book_req_den_book_name);
        mtvAuthorName = findViewById(R.id.noti_book_req_den_author);
        mtvDetailMsg = findViewById(R.id.noti_book_req_den_description);

        getAuthorName();

        setUpUI();

        checkIfRead();
    }

    private void getAuthorName(){
        //private static final String DATABASE_DIR_UNIQUE_BOOK_DETAILS = "unique-book-details";
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS)
                .child(mNotificationDetails.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UniqueBookDetails bookDetails = dataSnapshot.getValue(UniqueBookDetails.class);
                mAuthorName = bookDetails.authorName;
                setAuthorName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setAuthorName(){
        String[] names = mAuthorName.split(",");
        mAuthorName = "";
        for(String name: names){
            mAuthorName += name;
            mAuthorName += "\n";
        }
        mtvAuthorName.setText(mAuthorName);
    }

    private void setUpUI(){
        mtvGeetUser.setText("Hey " + mFirebaseUser.getDisplayName() + ",");
        mtvBookName.setText(mNotificationDetails.bookName);
        mtvUserName.setText(mNotificationDetails.otherUserName);
        mtvDetailMsg.setText(String.format(getResources().getString(R.string.noti_details_book_request_reply_deny),
                mNotificationDetails.otherUserName));

        setUpOtherUserPic();
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
                            Glide.with(NotificationBookRequestDeniedActivity.this).load(photoUrl)
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
}
