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
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.DailyUpdateDetails;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.RonginBookDetails;
import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_OWNERS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_UPDATE_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LENT_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
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
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class NotificationBookReceivedActivity extends AppCompatActivity implements View.OnClickListener {

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

    private UserExchangeBookDetails mExchanngeBookDetails;
    private UserBasicInfo mOtherUserBasicInfo;
    private AllBooksLibBookDetails tLibBookDetails;

    private String tUserUId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_book_received);

        mNotificationDetails = new NotificationDetails();

        checkForIntentExtras();

        tLibBookDetails = new AllBooksLibBookDetails();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mtvGeetUser = findViewById(R.id.noti_take_book_greet_user);
        mtvBookName = findViewById(R.id.noti_take_book_book_name);
        mtvDayNumber = findViewById(R.id.noti_take_book_day);
        mtvDetailMsg = findViewById(R.id.noti_take_book_description);

        mivUserPic = findViewById(R.id.noti_take_book_pro_pic);
        mtvUserName = findViewById(R.id.noti_take_book_user_name);

        mtvbAccept = findViewById(R.id.noti_take_book_true);
        mtvbDecline = findViewById(R.id.noti_take_book_false);

        getOtherUserBasicInfo();

        mtvbAccept.setOnClickListener(this);
        mtvbDecline.setOnClickListener(this);

        setUpUI();
    }

    private void getOtherUserBasicInfo(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mNotificationDetails.otherUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOtherUserBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpUI(){
        String greetUser = "Hey " + mFirebaseUser.getDisplayName() + ",";
        mtvGeetUser.setText(greetUser);
        mtvBookName.setText(mNotificationDetails.bookName);
        mtvUserName.setText(mNotificationDetails.otherUserName);
        mtvDayNumber.setText(String.valueOf(mNotificationDetails.requestedDay) + " days");
        mtvDetailMsg.setText(String.format(getResources().getString(R.string.noti_details_book_request_book_take),
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
                            Glide.with(NotificationBookReceivedActivity.this).load(photoUrl)
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



    private void decreaseBookFromOthers(){
        decreaseBookFromOwnerLibrary();
        decreaseBookFromAllLibrary();
    }

    private void decreaseBookFromOwnerLibrary(){

        if(!mNotificationDetails.otherUserUId.equals(RONGIN_UID)){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                    .child(mNotificationDetails.otherUserUId).child(mNotificationDetails.bookUId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserLibraryBookDetails userLibraryBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    if(userLibraryBookDetails.copyCount <= 1){
                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                                .child(mNotificationDetails.otherUserUId).child(mNotificationDetails.bookUId);

                        databaseReference.setValue(null);
                    } else {
                        userLibraryBookDetails.copyCount--;

                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                                .child(mNotificationDetails.otherUserUId).child(mNotificationDetails.bookUId);

                        databaseReference.setValue(userLibraryBookDetails);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                    .child(mNotificationDetails.bookUId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    RonginBookDetails ronginBookDetails = dataSnapshot.getValue(RonginBookDetails.class);
                    if(ronginBookDetails.copyCount <= 1){
                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                                .child(mNotificationDetails.bookUId);

                        databaseReference.setValue(null);
                    } else {
                        ronginBookDetails.copyCount--;

                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                                .child(mNotificationDetails.bookUId);

                        databaseReference.setValue(ronginBookDetails);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void decreaseBookFromAllLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mNotificationDetails.bookUId).child(mNotificationDetails.otherUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer bookCopy = dataSnapshot.getValue(Integer.class);

                if(bookCopy <= 1){
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mNotificationDetails.bookUId).child(mNotificationDetails.otherUserUId);

                    databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                decreaseOwnerCountFromLibrary();
                            }
                        }
                    });
                } else {
                    bookCopy--;
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mNotificationDetails.bookUId).child(mNotificationDetails.otherUserUId);

                    databaseReference.setValue(bookCopy);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void decreaseOwnerCountFromLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mNotificationDetails.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllBooksLibBookDetails libBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);
                if(libBookDetails.ownerCount <= 1){
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                            .child(mNotificationDetails.bookUId);

                    databaseReference.setValue(null);
                } else if(libBookDetails.ownerCount == 2){
                    setDataToAddMoreDetailsToLibBook(libBookDetails);

                    libBookDetails.ownerCount--;
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                            .child(mNotificationDetails.bookUId);

                    databaseReference.setValue(libBookDetails);
                } else {
                    libBookDetails.ownerCount--;
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                            .child(mNotificationDetails.bookUId);

                    databaseReference.setValue(libBookDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDataToAddMoreDetailsToLibBook(AllBooksLibBookDetails libBookDetails){
        tLibBookDetails.bookName = libBookDetails.bookName;
        tLibBookDetails.authorName = libBookDetails.authorName;
        tLibBookDetails.bookUId = libBookDetails.bookUId;
        tLibBookDetails.bookNameLower = libBookDetails.bookNameLower;
        tLibBookDetails.ownerCount = libBookDetails.ownerCount;
        tLibBookDetails.ownerUId = libBookDetails.ownerUId;
        tLibBookDetails.ownerLongitude = libBookDetails.ownerLongitude;
        tLibBookDetails.ownerLatitude = libBookDetails.ownerLatitude;
        tLibBookDetails.ownerFirstName = libBookDetails.ownerFirstName;

        mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mNotificationDetails.bookUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    tUserUId = snapshot.getKey();
                    addMoreDetailsToLibBook();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMoreDetailsToLibBook(){

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(tUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserBasicInfo userBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                tLibBookDetails.ownerFirstName = userBasicInfo.firstName + " " + userBasicInfo.lastName;
                tLibBookDetails.ownerCount = 1;
                tLibBookDetails.ownerLatitude = userBasicInfo.latitude;
                tLibBookDetails.ownerLongitude = userBasicInfo.longitude;
                tLibBookDetails.ownerUId = tUserUId;

                finallyAddDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void finallyAddDetails(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mNotificationDetails.bookUId);

        databaseReference.setValue(tLibBookDetails);
    }


    private void manageReceivedBook(){
        mExchanngeBookDetails = new UserExchangeBookDetails(
                mNotificationDetails.bookName,
                null,
                mNotificationDetails.bookUId,
                null,
                null,
                getUTCDateFromLocal(System.currentTimeMillis()),
                mNotificationDetails.requestedDay
        );

        DatabaseReference dbRefBookAuthor = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS)
                .child(mNotificationDetails.bookUId);

        dbRefBookAuthor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UniqueBookDetails bookDetails = dataSnapshot.getValue(UniqueBookDetails.class);
                mExchanngeBookDetails.authorName = bookDetails.authorName;
                insertToLentSection(mExchanngeBookDetails);
                insertToBorrowedSection(mExchanngeBookDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insertToLentSection(UserExchangeBookDetails bookDetails){
        bookDetails.otherUserUId = mFirebaseUser.getUid();
        bookDetails.otherUserName = mFirebaseUser.getDisplayName();

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LENT_BOOKS)
                .child(mNotificationDetails.otherUserUId);

        databaseReference.child(mFirebaseUser.getUid() + "_" + mNotificationDetails.bookUId).setValue(bookDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NotificationBookReceivedActivity.this, "Successfully taken.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertToBorrowedSection(UserExchangeBookDetails bookDetails){
        bookDetails.otherUserUId = mNotificationDetails.otherUserUId;
        bookDetails.otherUserName = mNotificationDetails.otherUserName;

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                .child(mFirebaseUser.getUid());

        databaseReference.child(mNotificationDetails.otherUserUId + "_" + mNotificationDetails.bookUId).setValue(bookDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NotificationBookReceivedActivity.this, "Successfully taken.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addToUpdateList(){
        String otherUserName = "rOngin";

        if(mNotificationDetails.notificationUId != RONGIN_UID){
            otherUserName = mNotificationDetails.otherUserName;
        }

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
        DailyUpdateDetails updateDetails = new DailyUpdateDetails(
                String.format(getResources().getString(R.string.update_message_book_lent), otherUserName),
                getUTCDateFromLocal(System.currentTimeMillis())
        );

        databaseReference.push().setValue(updateDetails);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.noti_take_book_true:
                readAndDestroyValidity();
                decreaseBookFromOthers();
                manageReceivedBook();
                addToUpdateList();
                break;

            case R.id.noti_take_book_false:
                readAndDestroyValidity();
                break;
        }
    }
}
