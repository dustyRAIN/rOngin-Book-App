package com.creation.daguru.ronginbookapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
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

import java.util.Locale;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_AUTHOR;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_OWNER_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_DISTANCE;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_REQUEST;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class SingleOwnerBookDetailsActivity extends AppCompatActivity implements View.OnClickListener {



    private TextView mtvBookHeader;
    private TextView mtvAuthorHeader;
    private TextView mtvBookName;
    private TextView mtvAuthorName;
    private TextView mtvOwnerName;
    private ImageView mivRonginLogo;
    private TextView mtvDistance;
    private ImageView mivbOpenMap;
    private TextView mtvbSendBookRequest;

    private ProgressDialog mProgressDialog;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private UserBasicInfo mOwnerBasicInfo;
    private UserBasicInfo mUserBasicInfo;

    private NotificationDetails mNotificationDetails;

    private boolean mGotData;

    private String mBookUId;
    private String mOwnerUId;
    private String mBookName;

    private boolean mOwnsRonginBook;
    private boolean mOwnsCurrentBook;
    private boolean mBorrowedCurrentBook;
    private int mBorrowedBookCount;
    private boolean mIsCheckedBorrowedBooks;
    private boolean mIsCheckedUserBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_owner_book_details);
        mtvBookHeader = findViewById(R.id.single_book_name);
        mtvAuthorHeader = findViewById(R.id.single_book_author);
        mtvBookName = findViewById(R.id.single_book_full_name);
        mtvAuthorName = findViewById(R.id.single_book_full_author);
        mtvOwnerName = findViewById(R.id.single_book_full_owner);
        mivRonginLogo = findViewById(R.id.single_owner_rongin_logo);
        mtvDistance = findViewById(R.id.single_book_distance);
        mivbOpenMap = findViewById(R.id.iv_ic_open_map);
        mtvbSendBookRequest = findViewById(R.id.tvb_request_single_book);

        mOwnsRonginBook = false;
        mOwnsCurrentBook = false;
        mBorrowedCurrentBook = false;
        mBorrowedBookCount = 0;
        mIsCheckedBorrowedBooks = false;
        mIsCheckedUserBooks = false;

        checkAndUseIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mOwnerUId.equals(RONGIN_UID)){
            Log.d("SingleDay", mOwnerUId);
            showRonginLogo();
        } else {
            hideRonginLogo();
        }

        getOwnerFullName();

        mivbOpenMap.setOnClickListener(this);
        mtvbSendBookRequest.setOnClickListener(this);
    }



    private void checkAndUseIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_BOOK_OWNER_UID)){
            mOwnerUId = getIntent().getStringExtra(EXTRA_KEY_BOOK_OWNER_UID);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_UID)){

            mBookUId = getIntent().getStringExtra(EXTRA_KEY_BOOK_UID);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_NAME)){
            mBookName = getIntent().getStringExtra(EXTRA_KEY_BOOK_NAME);
            mtvBookHeader.setText(mBookName);
            mtvBookName.setText(mBookName);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_AUTHOR)){
            String authorName = getIntent().getStringExtra(EXTRA_KEY_BOOK_AUTHOR);
            mtvAuthorHeader.setText(authorName);
            mtvAuthorName.setText(authorName);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_BOOK_OWNER_NAME)){
            mtvOwnerName.setText(getIntent().getStringExtra(EXTRA_KEY_BOOK_OWNER_NAME));
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_DISTANCE)){
            mtvDistance.setText(getIntent().getStringExtra(EXTRA_KEY_DISTANCE));
        } else {
            finish();
        }
    }

    private void getOwnerFullName(){
        DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO).child(mOwnerUId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                mtvOwnerName.setText(mUserBasicInfo.firstName + " " + mUserBasicInfo.lastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void showLoacationOnMap(){
        if(mUserBasicInfo != null){
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mUserBasicInfo.latitude, mUserBasicInfo.longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
    }

    private void showRonginLogo(){
        mivRonginLogo.setVisibility(View.VISIBLE);
        mtvOwnerName.setVisibility(View.INVISIBLE);
    }

    private void hideRonginLogo(){
        mivRonginLogo.setVisibility(View.INVISIBLE);
        mtvOwnerName.setVisibility(View.VISIBLE);
    }

    private void showProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Checking eligibility..."); // Setting Message
        mProgressDialog.setTitle("Please Wait"); // Setting Title
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        mProgressDialog.show(); // Display Progress Dialog
        mProgressDialog.setCancelable(false);
    }

    private void hideProgressDialog(){
        mProgressDialog.dismiss();
    }

    private void showDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }





    private void startCheckingEligibility(){
        if(!mFirebaseUser.getUid().equals(mOwnerUId)){
            if(!mIsCheckedBorrowedBooks){
                DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                        .child(mFirebaseUser.getUid());

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int i = 0;
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            mBorrowedBookCount++;
                        }

                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            i++;
                            UserExchangeBookDetails bookDetails = snapshot.getValue(UserExchangeBookDetails.class);
                            if(bookDetails.otherUserUId.equals(RONGIN_UID)){
                                mOwnsRonginBook = true;
                            }
                            if(bookDetails.bookUId.equals(mBookUId)){
                                mBorrowedCurrentBook = true;
                            }

                            if(i == mBorrowedBookCount){
                                mIsCheckedBorrowedBooks = true;
                                searchThisBookInUserLibrary();
                            }
                        }

                        if(mBorrowedBookCount == 0){
                            mIsCheckedBorrowedBooks = true;
                            searchThisBookInUserLibrary();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                searchThisBookInUserLibrary();
            }
        } else {
            hideProgressDialog();
            showDialog("Self Loop!","You can't request for your own book.");
        }
    }

    private void searchThisBookInUserLibrary(){
        if(!mIsCheckedUserBooks){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                    .child(mFirebaseUser.getUid()).child(mBookUId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        mOwnsCurrentBook = true;
                    }
                    mIsCheckedUserBooks = true;
                    analysisTheResult();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            analysisTheResult();
        }
    }

    private void analysisTheResult(){
        if(mOwnsRonginBook && mOwnerUId.equals(RONGIN_UID)){
            hideProgressDialog();
            showDialog("One At a Time!",
                    "You have already borrowed one book from rOngin library. If you want to request for another book, you have to return the borrowed book first.");
        } else if(mOwnsCurrentBook){
            hideProgressDialog();
            showDialog("You Have It!",
                    "You already own this book in your library.");
        } else if(mBorrowedCurrentBook){
            showDialog("You Borrowed It!",
                    "You already have borrowed this book and haven't returned it. You can't borrow the same book multiple times at the same time.");
        } else {
            checkIfRequestAlreadySent();
        }
    }

    private void checkIfRequestAlreadySent(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mOwnerUId);

        databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_REQUEST))
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
                    if(mNotificationDetails == null || mNotificationDetails.createTime <= notificationDetails.createTime){
                        mNotificationDetails = notificationDetails;
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
        if(mNotificationDetails == null || mNotificationDetails.isValid == 0){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mOwnerUId);

            NotificationDetails notificationDetails = new NotificationDetails(
                    TYPE_NOTIFICATION_BOOK_REQUEST,
                    null,
                    mBookUId,
                    mBookName,
                    mFirebaseUser.getUid(),
                    mFirebaseUser.getDisplayName(),
                    getUTCDateFromLocal(System.currentTimeMillis()),
                    0,
                    1,
                    mBorrowedBookCount,
                    -1,
                    mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_REQUEST)
            );

            String key = databaseReference.push().getKey();
            notificationDetails.notificationUId = key;

            databaseReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        hideProgressDialog();
                        showDialog("Done!",
                                "Request has been sent. Please wait for a response.");
                    }
                }
            });

            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mOwnerUId);
            dbRef.setValue(1);
        } else {
            hideProgressDialog();
            showDialog("Be Patient!",
                    "A request has already been sent to this user and the user is yet to respond. Please wait for a response.");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_ic_open_map:
                showLoacationOnMap();
                break;
            case R.id.tvb_request_single_book:
                showProgressDialog();
                startCheckingEligibility();
                break;
        }
    }
}
