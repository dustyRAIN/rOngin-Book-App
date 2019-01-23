package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.ListUserBookExchangeDetails;
import com.creation.daguru.ronginbookapp.data.MessageQuickDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_DAYS_LEFT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EXCHANGE_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class LentBookDetailsActivity extends AppCompatActivity implements View.OnClickListener {




    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvBookName;
    private TextView mtvAuthorName;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private TextView mtvDaysLeft;

    private TextView mtvbSendMessage;

    private ListUserBookExchangeDetails mBookExchangeDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lent_book_details);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mBookExchangeDetails = new ListUserBookExchangeDetails();

        checkForIntenteExtras();

        mtvBookName = findViewById(R.id.lent_details_book_name);
        mtvAuthorName = findViewById(R.id.lent_details_author);

        mivUserPic = findViewById(R.id.lent_details_pro_pic);
        mtvUserName = findViewById(R.id.lent_details_user_name);

        mtvDaysLeft = findViewById(R.id.lent_details_day);

        mtvbSendMessage = findViewById(R.id.lent_details_true);

        mtvbSendMessage.setOnClickListener(this);

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
                            Glide.with(LentBookDetailsActivity.this).load(photoUrl)
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


    private void setConnectionToSendMessage(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mFirebaseUser.getUid()).child(mBookExchangeDetails.otherUserUId);

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
                mBookExchangeDetails.otherUserName,
                mBookExchangeDetails.otherUserUId,
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
                .child(mFirebaseUser.getUid()).child(mBookExchangeDetails.otherUserUId);

        DatabaseReference otherMessageReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mBookExchangeDetails.otherUserUId).child(mFirebaseUser.getUid());

        ownMessageReference.setValue(ownQuickDetails);
        otherMessageReference.setValue(otherQuickDetails);

        goToMessage();
    }

    private void goToMessage(){
        Intent intent = new Intent(this, ChatMessagesActivity.class);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_UID, mBookExchangeDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME, mBookExchangeDetails.otherUserName);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lent_details_true:
                setConnectionToSendMessage();
                break;
        }
    }
}
