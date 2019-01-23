package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.DailyUpdateDetails;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_OWNERS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_UPDATE_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LENT_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
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
import static com.creation.daguru.ronginbookapp.MainActivity.LATITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.LONGITUDE_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_ADDED;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.isTimeValid;

public class NotificationReceiveReturnedBookActivity extends AppCompatActivity implements View.OnClickListener {

    private NotificationDetails mNotificationDetails;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvGeetUser;
    private TextView mtvBookName;
    private TextView mtvDayNumber;
    private TextView mtvDetailMsg;

    private ImageView mivUserPic;
    private TextView mtvUserName;

    private TextView mtvbAccept;
    private TextView mtvbDecline;

    private String tAuthorName;
    private UserLibraryBookDetails tUserLibraryBookDetails;

    private SharedPreferences mSharedPreferences;
    private double mLatitude;
    private double mLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_receive_returned_book);

        mNotificationDetails = new NotificationDetails();

        checkForIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        checkForSharedPref();

        mtvGeetUser = findViewById(R.id.noti_take_returned_book_greet_user);
        mtvBookName = findViewById(R.id.noti_take_returned_book_book_name);

        mivUserPic = findViewById(R.id.noti_take_returned_book_pro_pic);
        mtvUserName = findViewById(R.id.noti_take_returned_book_user_name);
        mtvDetailMsg = findViewById(R.id.noti_take_returned_book_description);

        mtvbAccept = findViewById(R.id.noti_take_returned_book_true);
        mtvbDecline = findViewById(R.id.noti_take_returned_book_false);

        mtvbAccept.setOnClickListener(this);
        mtvbDecline.setOnClickListener(this);

        checkForValidity();

        setUpUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkTimeValidity();
    }

    private void checkForSharedPref(){
        if(!mSharedPreferences.contains(LATITUDE_KEY + mFirebaseUser.getUid()) ||
                !mSharedPreferences.contains(LONGITUDE_KEY + mFirebaseUser.getUid())){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            mLatitude = mSharedPreferences.getFloat(LATITUDE_KEY + mFirebaseUser.getUid(), 1);
            mLongitude = mSharedPreferences.getFloat(LONGITUDE_KEY + mFirebaseUser.getUid(), 1);
        }
    }


    private void checkTimeValidity(){
        if(!isTimeValid(getUTCDateFromLocal(System.currentTimeMillis()), mNotificationDetails.createTime)
                && mNotificationDetails.isValid == 1){
            Toast.makeText(this, "This request is not valid anymore.", Toast.LENGTH_LONG).show();

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mFirebaseUser.getUid()).child(mNotificationDetails.notificationUId);

            databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    finish();
                }
            });
        }
    }



    private void setUpUI(){
        String greetUser = "Hey " + mFirebaseUser.getDisplayName() + ",";
        mtvGeetUser.setText(greetUser);
        mtvBookName.setText(mNotificationDetails.bookName);
        mtvUserName.setText(mNotificationDetails.otherUserName);
        mtvDetailMsg.setText(String.format(getResources().getString(R.string.noti_details_book_receive_returned),
                mNotificationDetails.otherUserName));
        setUpOtherUserPic();
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
                            Glide.with(NotificationReceiveReturnedBookActivity.this).load(photoUrl)
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

    private void takeTheBook(){
        addBookToOwnLibrary();
        removeFromLentSection();
        removeFromBorrowedSection();
    }

    private void addBookToOwnLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                .child(mFirebaseUser.getUid()).child(mNotificationDetails.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tUserLibraryBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                if(tUserLibraryBookDetails == null){
                    DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS)
                            .child(mNotificationDetails.bookUId);

                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UniqueBookDetails uniqueBookDetails = dataSnapshot.getValue(UniqueBookDetails.class);
                            tAuthorName = uniqueBookDetails.authorName;

                            DatabaseReference dbReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                                    .child(mFirebaseUser.getUid()).child(mNotificationDetails.bookUId);

                            UserLibraryBookDetails userLibraryBookDetails = new UserLibraryBookDetails(
                                    mNotificationDetails.bookName,
                                    tAuthorName,
                                    mNotificationDetails.bookUId,
                                    1,
                                    1
                            );

                            dbReference.setValue(userLibraryBookDetails);

                            addToAllLibrary();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    tUserLibraryBookDetails.copyCount++;
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                            .child(mFirebaseUser.getUid()).child(mNotificationDetails.bookUId);

                    databaseReference.setValue(tUserLibraryBookDetails);
                    tAuthorName = tUserLibraryBookDetails.authorName;

                    addToAllLibrary();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToAllLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mNotificationDetails.bookUId).child(mFirebaseUser.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int bookCopy = -1;

                if(dataSnapshot.exists()){
                    bookCopy = dataSnapshot.getValue(Integer.class);
                    bookCopy++;

                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mNotificationDetails.bookUId).child(mFirebaseUser.getUid());

                    databaseReference.setValue(bookCopy);

                } else {
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mNotificationDetails.bookUId).child(mFirebaseUser.getUid());

                    databaseReference.setValue(1);
                    inreaseOwnerCountInLibrary();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inreaseOwnerCountInLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mNotificationDetails.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllBooksLibBookDetails booksLibBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);
                if(booksLibBookDetails != null){
                    booksLibBookDetails.ownerCount++;
                } else {
                    booksLibBookDetails = new AllBooksLibBookDetails(
                            mNotificationDetails.bookName,
                            tAuthorName,
                            mNotificationDetails.bookUId,
                            mFirebaseUser.getDisplayName(),
                            mFirebaseUser.getUid(),
                            mNotificationDetails.bookName.toLowerCase(),
                            1,
                            mLatitude,
                            mLongitude
                    );
                }

                DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                        .child(mNotificationDetails.bookUId);

                databaseReference.setValue(booksLibBookDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(NotificationReceiveReturnedBookActivity.this,
                                    "Book added to 'All books'", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(NotificationReceiveReturnedBookActivity.this,
                                    "Book adding failed. Check internet connection.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addToUpdateList();
    }

    private void addToUpdateList(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
        DailyUpdateDetails updateDetails = new DailyUpdateDetails(
                String.format(getResources().getString(R.string.update_message_book_added), mFirebaseUser.getDisplayName()),
                getUTCDateFromLocal(System.currentTimeMillis())
        );

        databaseReference.push().setValue(updateDetails);
    }



    private void removeFromLentSection(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS)
                .child(mFirebaseUser.getUid()).child(mNotificationDetails.otherUserUId + "_" + mNotificationDetails.bookUId);

        databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(NotificationReceiveReturnedBookActivity.this,
                            "Successfully Done.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void removeFromBorrowedSection(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                .child(mNotificationDetails.otherUserUId).child(mFirebaseUser.getUid() + "_" + mNotificationDetails.bookUId);

        databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(NotificationReceiveReturnedBookActivity.this,
                            "Successfully Done.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.noti_take_returned_book_true:
                readAndDestroyValidity();
                takeTheBook();
                break;

            case R.id.noti_take_returned_book_false:
                readAndDestroyValidity();
                break;
        }
    }
}
