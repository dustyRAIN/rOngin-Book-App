package com.creation.daguru.ronginbookapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.ListUserBookExchangeDetails;
import com.creation.daguru.ronginbookapp.data.MessageQuickDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
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

public class AdminLentBookDetailsActivity extends AppCompatActivity implements View.OnClickListener {



    private static final int REQUEST_PHONE_CALL = 76;

    private TextView mtvLentBookName;
    private TextView mtvLentBookAuthor;
    private ImageView mivOtherUserPic;
    private TextView mtvOtherUserName;
    private TextView mtvPhoneNumber;
    private TextView mtvEmailAddress;
    private TextView mtvbOpenLocation;
    private TextView mtvbSendMessage;
    private TextView mtvbCall;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private ListUserBookExchangeDetails mBookExchangeDetails;
    private UserBasicInfo mOtherUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lent_book_details);

        mBookExchangeDetails = new ListUserBookExchangeDetails();

        mtvLentBookName = findViewById(R.id.admin_lent_book_name);
        mtvLentBookAuthor = findViewById(R.id.admin_lent_book_author);
        mivOtherUserPic = findViewById(R.id.admin_lent_to_pic);
        mtvOtherUserName = findViewById(R.id.admin_lent_to_name);
        mtvPhoneNumber = findViewById(R.id.admin_lent_to_number);
        mtvEmailAddress = findViewById(R.id.admin_lent_to_email);
        mtvbOpenLocation = findViewById(R.id.admin_lent_to_address);
        mtvbSendMessage = findViewById(R.id.admin_lent_send_message);
        mtvbCall = findViewById(R.id.admin_lent_call);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        disableButtons();
        checkForIntenteExtras();

        mtvbOpenLocation.setOnClickListener(this);
        mtvbSendMessage.setOnClickListener(this);
        mtvbCall.setOnClickListener(this);

        setUpUI();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mOtherUserInfo.phoneNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Permission denied to make call.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void setUpUI(){
        mtvLentBookName.setText(mBookExchangeDetails.bookName);
        mtvLentBookAuthor.setText(mBookExchangeDetails.authorName);
        mtvOtherUserName.setText(mBookExchangeDetails.otherUserName);
    }

    private void setUserInfo(){
        mtvPhoneNumber.setText(mOtherUserInfo.phoneNumber);
        mtvEmailAddress.setText(mOtherUserInfo.email);
    }

    private void disableButtons(){
        mtvbCall.setEnabled(false);
        mtvbOpenLocation.setEnabled(false);
    }

    private void enableButtons(){
        mtvbCall.setEnabled(true);
        mtvbOpenLocation.setEnabled(true);
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
            getOtherUserInfo();
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_NAME)){
            mBookExchangeDetails.otherUserName = getIntent().getStringExtra(EXTRA_KEY_EXCHANGE_OTHER_USER_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_EXCHANGE_DAYS_LEFT)){
            mBookExchangeDetails.daysLeft = getIntent().getIntExtra(EXTRA_KEY_EXCHANGE_DAYS_LEFT, 5);
        }
    }

    private void getOtherUserInfo(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mBookExchangeDetails.otherUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOtherUserInfo = dataSnapshot.getValue(UserBasicInfo.class);
                enableButtons();
                setUserInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showLocationInMap(){
        if(mOtherUserInfo != null){
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mOtherUserInfo.latitude,
                    mOtherUserInfo.longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
    }

    private void callTheUser(){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mOtherUserInfo.phoneNumber));
        if (ContextCompat.checkSelfPermission(AdminLentBookDetailsActivity.this,
                android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AdminLentBookDetailsActivity.this,
                    new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        }
        else
        {
            startActivity(intent);
        }
    }

    private void setConnectionToSendMessage(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID).child(mBookExchangeDetails.otherUserUId);

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
                "rOngin",
                RONGIN_UID,
                currentTime,
                0,
                0,
                0
        );

        DatabaseReference ownMessageReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID).child(mBookExchangeDetails.otherUserUId);

        DatabaseReference otherMessageReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mBookExchangeDetails.otherUserUId).child(RONGIN_UID);

        ownMessageReference.setValue(ownQuickDetails);
        otherMessageReference.setValue(otherQuickDetails);

        goToMessage();
    }

    private void goToMessage(){
        Intent intent = new Intent(this, AdminChatMessagesActivity.class);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_UID, mBookExchangeDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME, mBookExchangeDetails.otherUserName);
        startActivity(intent);
        Toast.makeText(this, "Something to be done.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.admin_lent_send_message:
                setConnectionToSendMessage();
                break;

            case R.id.admin_lent_to_address:
                showLocationInMap();
                break;

            case R.id.admin_lent_call:
                callTheUser();
                break;
        }
    }
}
