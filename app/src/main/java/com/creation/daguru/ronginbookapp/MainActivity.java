package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.DailyQuoteDetails;
import com.creation.daguru.ronginbookapp.data.DailyUpdateDetails;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_QUOTE_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_UPDATE_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_UPDATE_LIST_CHILD_CREATE_TIME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_MESSAGE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.STORAGE_DIR_PRO_PIC;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.IS_LOGGED_IN_KEY;
import static com.creation.daguru.ronginbookapp.RonginSplashScreenActivity.IS_ACCOUNT_FREEZED_KEY;
import static com.creation.daguru.ronginbookapp.RonginSplashScreenActivity.SHOULD_WARN_TO_RETURN_KEY;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_ACCOUNT_FREEZE;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_RETURN;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.isDayCountSame;


public class MainActivity extends AppCompatActivity implements
        RonginAuthStateChangeListener.OnUserAuthChanged,
        View.OnClickListener{


    private static final String TAG = "MainActivity";

    protected static final String SHARED_PREFERENCES_NAME = "user-data-preference";
    protected static final String LATITUDE_KEY = "latitude-";
    protected static final String LONGITUDE_KEY = "longitude-";
    protected static final String ATTACH_AUTH_LISTENER = "attach-auth-listener";

    protected static final int RC_SIGN_IN = 1;
    protected static final int RC_GET_INFO = 2;
    protected static final int RC_GET_EMAIL = 3;
    protected static final int RC_SPLASH_SCREEEN = 4;

    private ConstraintLayout mQuoteDescriptionArea;

    private ImageView mivCancelQuoteDescription;

    private TextView mtvQuoteDescription;
    private TextView mtvQuote;
    private TextView mtvQuoteSrc;
    private TextView mtvUserFullName;
    private TextView mtvbRonginLib;
    private TextView mtvbUserLib;

    private TextView mtvBellLight;
    private TextView mtvMessageLight;

    private ImageView mivUserProPic;
    private ImageView mivbIconBell;
    private ImageView mivbIconMessage;

    private RecyclerView mUpdateRecyclerView;
    private UpdateListViewAdapter mUpdateAdapter;
    private List<DailyUpdateDetails> mUpdateList;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private ValueEventListener mNewNotificationListener;
    private ValueEventListener mNewMessageListener;
    private ValueEventListener mDailyQuoteListener;
    private ChildEventListener mDailyUpdateListener;

    private RonginAuthStateChangeListener mAuthStateChangeListener;

    private DailyQuoteDetails mDailyQuoteDetails;

    private double mLatitude;
    private double mLongitude;
    private boolean mIsLocReady;
    private boolean mIsAccountFreezed;
    private boolean mShouldWarn;

    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, RonginSplashScreenActivity.class);
        startActivityForResult(intent, RC_SPLASH_SCREEEN);

        mQuoteDescriptionArea = findViewById(R.id.quote_description_area);
        mivCancelQuoteDescription = findViewById(R.id.cancel_quote_desc);

        mtvQuoteDescription = findViewById(R.id.quote_description);
        mtvQuote = findViewById(R.id.tv_qoute);
        mtvQuoteSrc = findViewById(R.id.tv_qoute_source);
        mtvUserFullName = findViewById(R.id.tv_user_name);

        mivUserProPic = findViewById(R.id.iv_pro_pic);
        mivbIconBell = findViewById(R.id.iv_ic_bell);
        mivbIconMessage = findViewById(R.id.iv_ic_message);

        mtvBellLight = findViewById(R.id.notification_light);
        mtvMessageLight = findViewById(R.id.message_light);

        mtvbRonginLib = findViewById(R.id.tvb_rogin_lib);
        mtvbUserLib = findViewById(R.id.tvb_user_lib);

        mtvBellLight.setVisibility(View.GONE);
        mtvMessageLight.setVisibility(View.GONE);

        mtvQuote.setOnClickListener(this);
        mivCancelQuoteDescription.setOnClickListener(this);

        mtvbRonginLib.setOnClickListener(this);
        mtvbUserLib.setOnClickListener(this);
        mivbIconBell.setOnClickListener(this);
        mivbIconMessage.setOnClickListener(this);
        mtvBellLight.setOnClickListener(this);
        mtvMessageLight.setOnClickListener(this);

        mUpdateRecyclerView = findViewById(R.id.update_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                true);
        layoutManager.setStackFromEnd(true);
        mUpdateRecyclerView.setLayoutManager(layoutManager);
        mUpdateRecyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();

        mIsLocReady = true;

        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        mAuthStateChangeListener = new RonginAuthStateChangeListener(mFirebaseAuth, this);
    }

    @Override
    protected void onResume() {
        Log.d("muha", "onResume");
        super.onResume();

        mUpdateList = new ArrayList<>();
        mUpdateAdapter = new UpdateListViewAdapter(this, mUpdateList);
        mUpdateRecyclerView.setAdapter(mUpdateAdapter);

        if(mSharedPreferences.contains(ATTACH_AUTH_LISTENER)){
            if(mSharedPreferences.getBoolean(ATTACH_AUTH_LISTENER, false)){
                Log.d("muha", "authStateListener Added");
                mFirebaseAuth.addAuthStateListener(mAuthStateChangeListener);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("muha", "onPause");
        if(mAuthStateChangeListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateChangeListener);
        }
        detachLightListener();
        detachQuoteListener();
        detachUpdateListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.edit().putBoolean(ATTACH_AUTH_LISTENER, false).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == RC_SIGN_IN){
                //Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
            } else if(requestCode == RC_GET_INFO){
                //Toast.makeText(this, mFirebaseUser.getDisplayName(), Toast.LENGTH_LONG).show();
            } else if(requestCode == RC_GET_EMAIL){
                Log.d("email", mFirebaseUser.getEmail());
            } else if(requestCode == RC_SPLASH_SCREEEN){
                //mFirebaseAuth.addAuthStateListener(mAuthStateChangeListener);
            }
        } else if(resultCode == RESULT_CANCELED){
            if(requestCode == RC_SIGN_IN){
                //Toast.makeText(this, "not Success", Toast.LENGTH_LONG).show();
                finish();
            } else if(requestCode == RC_GET_INFO){
                finish();
            } else if(requestCode == RC_GET_EMAIL){
                finish();
            } else if(requestCode == RC_SPLASH_SCREEEN){
                finish();
            }
        }
    }

    private void checkForLocation(){
        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!mSharedPreferences.contains(LATITUDE_KEY + mFirebaseUser.getUid()) ||
                !mSharedPreferences.contains(LONGITUDE_KEY + mFirebaseUser.getUid())){
            Log.d("OOOO", "are u crazy bruh?");
            getLocation();
        } else {
            Log.d("OOOO", "it worked?");
        }
    }



    private void getLocation(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mFirebaseUser.getUid());
        mIsLocReady =false;
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserBasicInfo userBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                mLatitude = userBasicInfo.latitude;
                mLongitude = userBasicInfo.longitude;
                mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                mSharedPreferences.edit().putFloat(LATITUDE_KEY + mFirebaseUser.getUid(), (float) mLatitude).apply();
                mSharedPreferences.edit().putFloat(LONGITUDE_KEY + mFirebaseUser.getUid(), (float) mLongitude).apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }












    private void attachLightListener(){
        if(mNewNotificationListener == null){
            mNewNotificationListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state == 0){
                            mtvBellLight.setVisibility(View.GONE);
                        } else {
                            mtvBellLight.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mFirebaseUser.getUid());

            databaseReference.addValueEventListener(mNewNotificationListener);
        }

        if(mNewMessageListener == null){
            mNewMessageListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state == 0){
                            mtvMessageLight.setVisibility(View.GONE);
                        } else {
                            mtvMessageLight.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(mFirebaseUser.getUid());

            databaseReference.addValueEventListener(mNewMessageListener);
        }
    }

    private void detachLightListener(){
        if(mNewNotificationListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(mFirebaseUser.getUid());
            databaseReference.removeEventListener(mNewNotificationListener);

            mNewNotificationListener = null;
        }

        if(mNewMessageListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(mFirebaseUser.getUid());
            databaseReference.removeEventListener(mNewMessageListener);

            mNewMessageListener = null;
        }
    }

    @Override
    public void userAuthinticated() {
        //Toast.makeText(this, "Sign In", Toast.LENGTH_LONG).show();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser.getEmail() == null || mFirebaseUser.getEmail().equals("")
                || mFirebaseUser.getEmail().isEmpty()){
            Intent intent = new Intent(MainActivity.this, GetEmailAddressActivity.class);
            startActivityForResult(intent, RC_GET_EMAIL);
        } else if(mFirebaseUser.getDisplayName() == null || mFirebaseUser.getDisplayName().equals("")
                || mFirebaseUser.getDisplayName().isEmpty()){
            Intent intent = new Intent(MainActivity.this, GetUserInfoActivity.class);
            startActivityForResult(intent, RC_GET_INFO);
        } else {
            //Toast.makeText(this, mFirebaseUser.getDisplayName(), Toast.LENGTH_LONG).show();
            checkForLocation();
            checkForRestrictionAndSetUpUI();
        }
    }

    @Override
    public void userNotAuthinticated() {
        //Toast.makeText(this, "Sign Out", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void checkForRestrictionAndSetUpUI(){
        mIsAccountFreezed = false;
        mShouldWarn = false;
        if(mSharedPreferences.contains(IS_ACCOUNT_FREEZED_KEY)){
            mIsAccountFreezed = mSharedPreferences.getBoolean(IS_ACCOUNT_FREEZED_KEY, false);
        }
        if(mSharedPreferences.contains(SHOULD_WARN_TO_RETURN_KEY)){
            mShouldWarn = mSharedPreferences.getBoolean(SHOULD_WARN_TO_RETURN_KEY, false);
        }
        notifyAboutFreezing();
        notifyToWarnToReturn();
        setUpUI();
    }

    private void notifyAboutFreezing(){
        if(mIsAccountFreezed){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mFirebaseUser.getUid());

            databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                    .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_ACCOUNT_FREEZE))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                NotificationDetails notificationDetails = new NotificationDetails(
                                        TYPE_NOTIFICATION_ACCOUNT_FREEZE,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        getUTCDateFromLocal(System.currentTimeMillis()),
                                        0,
                                        1,
                                        -1,
                                        -1,
                                        mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_ACCOUNT_FREEZE)
                                );

                                DatabaseReference databaseRef = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                                        .child(mFirebaseUser.getUid());

                                String key = databaseRef.push().getKey();
                                notificationDetails.notificationUId = key;
                                databaseRef.child(key).setValue(notificationDetails);

                                DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                                        .child(mFirebaseUser.getUid());
                                dbRef.setValue(1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            deleteFreezeNotification();
        }
    }

    private void deleteFreezeNotification(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mFirebaseUser.getUid());

        databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_ACCOUNT_FREEZE))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            NotificationDetails notificationDetails = snapshot.getValue(NotificationDetails.class);
                            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                                    .child(mFirebaseUser.getUid()).child(notificationDetails.notificationUId);

                            dbRef.setValue(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void notifyToWarnToReturn(){
        if(mShouldWarn){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                    .child(mFirebaseUser.getUid());

            databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                    .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_RETURN))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                NotificationDetails notificationDetails = new NotificationDetails(
                                        TYPE_NOTIFICATION_BOOK_RETURN,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        getUTCDateFromLocal(System.currentTimeMillis()),
                                        0,
                                        1,
                                        -1,
                                        -1,
                                        mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_RETURN)
                                );

                                DatabaseReference databaseRef = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                                        .child(mFirebaseUser.getUid());

                                String key = databaseRef.push().getKey();
                                notificationDetails.notificationUId = key;
                                databaseRef.child(key).setValue(notificationDetails);

                                DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                                        .child(mFirebaseUser.getUid());
                                dbRef.setValue(1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            deleteWarnNotification();
        }
    }

    private void deleteWarnNotification(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mFirebaseUser.getUid());

        databaseReference.orderByChild(DATABASE_DIR_USER_NOTIFICATION_CHILD_OTHER_UID_AND_NOTI_TYPE)
                .equalTo(mFirebaseUser.getUid() + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_RETURN))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            NotificationDetails notificationDetails = snapshot.getValue(NotificationDetails.class);
                            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                                    .child(mFirebaseUser.getUid()).child(notificationDetails.notificationUId);

                            dbRef.setValue(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setUpUI(){
        mQuoteDescriptionArea.setVisibility(View.GONE);
        attachLightListener();
        attachDailyQuoteListener();
        mtvUserFullName.setText(mFirebaseUser.getDisplayName());
        //getSupportLoaderManager().initLoader(LOADER_ID_PRO_PIC, null, this);
        //Toast.makeText(this, "why?" + mFirebaseUser.getPhotoUrl(),Toast.LENGTH_LONG).show();

        if(mFirebaseUser.getPhotoUrl() != null
                && !mFirebaseUser.getPhotoUrl().toString().equals("")
                && !mFirebaseUser.getPhotoUrl().toString().isEmpty()) {

            Log.d("PhotoUrl", mFirebaseUser.getPhotoUrl().toString());

            try {
                Glide.with(MainActivity.this).load(mFirebaseUser.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(mivUserProPic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setUpDailyUpdateList();
    }

    private void setUpDailyUpdateList(){

        attachUpdateListener();
    }

    private void attachUpdateListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
        Query query = databaseReference.orderByChild(DATABASE_DIR_DAILY_UPDATE_LIST_CHILD_CREATE_TIME);
        if(mDailyUpdateListener == null){
            mDailyUpdateListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    DailyUpdateDetails updateDetails = dataSnapshot.getValue(DailyUpdateDetails.class);
                    if(isDayCountSame(updateDetails.createTime, System.currentTimeMillis())){
                        Log.d("haha", "added");

                        mUpdateList.add(updateDetails);
                        mUpdateAdapter.notifyDataSetChanged();

                        Log.d("haha", "added " + String.valueOf(mUpdateList.size()));

                        int position = mUpdateList.size()-1;
                        mUpdateRecyclerView.scrollToPosition(position);
                    } else {
                        String key = dataSnapshot.getKey();
                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
                        databaseReference.child(key).setValue(null);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            query.addChildEventListener(mDailyUpdateListener);
        }
    }

    private void attachDailyQuoteListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_QUOTE_DETAILS);
        if(mDailyQuoteListener == null){
            mDailyQuoteListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mDailyQuoteDetails = dataSnapshot.getValue(DailyQuoteDetails.class);
                    if(mDailyQuoteDetails != null){
                        mtvQuote.setText(mDailyQuoteDetails.dailyQuote);
                        mtvQuoteSrc.setText(mDailyQuoteDetails.quoteSource);
                        mtvQuoteDescription.setText(mDailyQuoteDetails.quoteDescription);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addValueEventListener(mDailyQuoteListener);
        }
    }

    private void detachUpdateListener(){
        if(mDailyUpdateListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
            Query query = databaseReference.orderByChild(DATABASE_DIR_DAILY_UPDATE_LIST_CHILD_CREATE_TIME);
            query.removeEventListener(mDailyUpdateListener);

            mDailyUpdateListener = null;
        }
    }

    private void detachQuoteListener(){
        if(mDailyQuoteListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_QUOTE_DETAILS);
            databaseReference.removeEventListener(mDailyQuoteListener);

            mDailyQuoteListener = null;
        }
    }








    private void clearDataAndDeleteAccount(){
        DatabaseReference ref = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO);
        DatabaseReference reference = ref.child(mFirebaseUser.getUid());
        reference.removeValue().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User Basic Info Deleted.");
                            deleteStorageFile();
                        }
                    }
                });
    }

    private void deleteStorageFile(){
        StorageReference storageReference = mStorageReference.child(STORAGE_DIR_PRO_PIC);
        StorageReference picReference = storageReference.child(mFirebaseUser.getUid());
        picReference.delete().addOnSuccessListener(this,
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User Pro Pic Deleted.");
                    }
                }).addOnFailureListener(this,
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
        deleteAccount();
    }

    private void deleteAccount(){
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseUser.delete().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User Deleted.");
                        } else {
                            Log.d(TAG, "why Not Deleted");
                        }
                    }
                });
    }

    private void startSearchActivity(){
        Intent intent = new Intent(this, BookSearchActivity.class);
        startActivity(intent);
    }

    private void startProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void startFeedbackActivity(){
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }

    private void displayAccountFreezeMessage(){
        if(mToast != null){
            mToast.cancel();
        }

        mToast = Toast.makeText(this,
                "Your Account is frozen. Please return time limit exceeded books that you borrowed.",
                Toast.LENGTH_LONG);

        mToast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_sign_out:
                mSharedPreferences.edit().putBoolean(IS_LOGGED_IN_KEY, false).apply();
                AuthUI.getInstance().signOut(this);
                //Toast.makeText(this, "ei feature ta thakbe na, just for test", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_item_feedback:
                startFeedbackActivity();
                return true;
            case R.id.menu_item_search:
                if(mIsAccountFreezed){
                    displayAccountFreezeMessage();
                } else {
                    startSearchActivity();
                }
                return true;
            case R.id.menu_item_show_profile:
                startProfileActivity();
                return true;
            case R.id.menu_item_about_us:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvb_rogin_lib:
                if(!mIsAccountFreezed){
                    Intent ronginLibIntent = new Intent(MainActivity.this, RonginLibraryActivity.class);
                    startActivity(ronginLibIntent);
                } else {
                    displayAccountFreezeMessage();
                }
                break;

            case R.id.tvb_user_lib:
                Intent userLibIntent = new Intent(MainActivity.this, UserLibraryActivity.class);
                startActivity(userLibIntent);
                break;

            case R.id.iv_ic_bell:
            case R.id.notification_light:
                Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(notificationIntent);
                break;

            case R.id.iv_ic_message:
            case R.id.message_light:
                Intent allMessageIntent = new Intent(MainActivity.this, AllMessageActivity.class);
                startActivity(allMessageIntent);
                break;

            case R.id.tv_qoute:
                mQuoteDescriptionArea.setVisibility(View.VISIBLE);
                break;

            case R.id.cancel_quote_desc:
                mQuoteDescriptionArea.setVisibility(View.GONE);
                break;
        }
    }
}
