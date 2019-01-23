package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_NEW_NAME_REQUEST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_MESSAGE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.ADMIN_POST_MASTER_ADMIN;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.ADMIN_POST_MODERATOR;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.ADMIN_POST_PRIMARY_ADMIN;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.ADMIN_POST_SECONDARY_ADMIN;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_POST;

public class AdminPanelActivity extends AppCompatActivity implements View.OnClickListener {



    private TextView mtvbBookNameRequest;
    private TextView mtvbManageBook;
    private TextView mtvbRonginLibrary;
    private TextView mtvbBorrowRequest;
    private TextView mtvbLentBooks;
    private TextView mtvbMessages;
    private TextView mtvbUserFeedback;
    private TextView mtvbDailyQuote;
    private TextView mtvbMonitorMods;
    private TextView mtvbAllUsers;

    private TextView mtvNameRequestLight;
    private TextView mtvNotificationLight;
    private TextView mtvMessageLight;
    private TextView mtvFeedbackLight;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    private ValueEventListener mNewNameRequestListener;
    private ValueEventListener mNewNotificationListener;
    private ValueEventListener mNewMessageListener;
    private ValueEventListener mNewFeedbackListener;

    private int mAdminPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        getAdminPost();

        mtvbBookNameRequest = findViewById(R.id.admin_book_name_request);
        mtvbManageBook = findViewById(R.id.admin_manage_book);
        mtvbRonginLibrary = findViewById(R.id.admin_rongin_library);
        mtvbBorrowRequest = findViewById(R.id.admin_book_taking_request);
        mtvbLentBooks = findViewById(R.id.admin_lent_books);
        mtvbMessages = findViewById(R.id.admin_messages);
        mtvbUserFeedback = findViewById(R.id.admin_feedback);
        mtvbDailyQuote = findViewById(R.id.admin_daily_quote);
        mtvbMonitorMods = findViewById(R.id.admin_monitor_mod);
        mtvbAllUsers = findViewById(R.id.admin_all_users);
        mtvNameRequestLight = findViewById(R.id.name_request_light);
        mtvNotificationLight = findViewById(R.id.notification_light);
        mtvMessageLight = findViewById(R.id.message_light);
        mtvFeedbackLight = findViewById(R.id.feedback_light);

        mtvNameRequestLight.setVisibility(View.GONE);
        mtvNotificationLight.setVisibility(View.GONE);
        mtvMessageLight.setVisibility(View.GONE);
        mtvFeedbackLight.setVisibility(View.GONE);


        hideAllAdminOption();
        setUIAccordingToAdminPost();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();

        mtvbBookNameRequest.setOnClickListener(this);
        mtvbManageBook.setOnClickListener(this);
        mtvbRonginLibrary.setOnClickListener(this);
        mtvbBorrowRequest.setOnClickListener(this);
        mtvbLentBooks.setOnClickListener(this);
        mtvbMessages.setOnClickListener(this);
        mtvbUserFeedback.setOnClickListener(this);
        mtvbDailyQuote.setOnClickListener(this);
        mtvbMonitorMods.setOnClickListener(this);
        mtvbAllUsers.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        attachLightListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachLightListener();
        super.onPause();
    }

    private void getAdminPost(){
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_POST)){
            mAdminPost = getIntent().getIntExtra(EXTRA_KEY_ADMIN_POST, ADMIN_POST_MODERATOR);
        }
    }

    private void hideAllAdminOption(){
        mtvbBookNameRequest.setVisibility(View.GONE);
        mtvbManageBook.setVisibility(View.GONE);
        mtvbRonginLibrary.setVisibility(View.GONE);
        mtvbBorrowRequest.setVisibility(View.GONE);
        mtvbLentBooks.setVisibility(View.GONE);
        mtvbMessages.setVisibility(View.GONE);
        mtvbUserFeedback.setVisibility(View.GONE);
        mtvbDailyQuote.setVisibility(View.GONE);
        mtvbMonitorMods.setVisibility(View.GONE);
        mtvbAllUsers.setVisibility(View.GONE);
    }

    private void setUIAccordingToAdminPost(){
        switch (mAdminPost){
            case ADMIN_POST_MASTER_ADMIN:
                mtvbAllUsers.setVisibility(View.VISIBLE);

            case ADMIN_POST_PRIMARY_ADMIN:
                mtvbDailyQuote.setVisibility(View.VISIBLE);
                mtvbMonitorMods.setVisibility(View.VISIBLE);

            case ADMIN_POST_SECONDARY_ADMIN:
                mtvbRonginLibrary.setVisibility(View.VISIBLE);
                mtvbBorrowRequest.setVisibility(View.VISIBLE);
                mtvbLentBooks.setVisibility(View.VISIBLE);
                mtvbMessages.setVisibility(View.VISIBLE);
                mtvbUserFeedback.setVisibility(View.VISIBLE);

            case ADMIN_POST_MODERATOR:
                mtvbBookNameRequest.setVisibility(View.VISIBLE);
                mtvbManageBook.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void attachLightListener(){
        if(mNewNameRequestListener == null){
            mNewNameRequestListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state == 0){
                            mtvNameRequestLight.setVisibility(View.GONE);
                        } else{
                            mtvNameRequestLight.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_NEW_NAME_REQUEST);
            databaseReference.addValueEventListener(mNewNameRequestListener);
        }

        if(mNewNotificationListener == null){
            mNewNotificationListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state == 0){
                            mtvNotificationLight.setVisibility(View.GONE);
                        } else{
                            if(mAdminPost != ADMIN_POST_MODERATOR) {
                                mtvNotificationLight.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(RONGIN_UID);
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
                            if (mAdminPost != ADMIN_POST_MODERATOR){
                                mtvMessageLight.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(RONGIN_UID);
            databaseReference.addValueEventListener(mNewMessageListener);
        }

        if(mNewFeedbackListener == null){
            mNewFeedbackListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state == 0){
                            mtvFeedbackLight.setVisibility(View.GONE);
                        } else {
                            if (mAdminPost != ADMIN_POST_MODERATOR){
                                mtvFeedbackLight.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_FEEDBACK);
            databaseReference.addValueEventListener(mNewFeedbackListener);
        }
    }

    private void detachLightListener(){
        if(mNewNameRequestListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_NEW_NAME_REQUEST);
            databaseReference.removeEventListener(mNewNameRequestListener);

            mNewNameRequestListener = null;
        }

        if(mNewNotificationListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                    .child(RONGIN_UID);
            databaseReference.removeEventListener(mNewNotificationListener);

            mNewNotificationListener = null;
        }

        if(mNewMessageListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(RONGIN_UID);
            databaseReference.removeEventListener(mNewMessageListener);

            mNewMessageListener = null;
        }

        if(mNewFeedbackListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_FEEDBACK);
            databaseReference.removeEventListener(mNewFeedbackListener);

            mNewFeedbackListener = null;
        }
    }








    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.admin_book_name_request:
                Intent nameRequestListIntent = new Intent(this, AdminBookNameRequestActivity.class);
                startActivity(nameRequestListIntent);
                break;

            case R.id.admin_manage_book:
                Intent manageBookIntent = new Intent(this, AdminManageUniqueBooksActivity.class);
                startActivity(manageBookIntent);
                break;

            case R.id.admin_rongin_library:
                Intent ronginLibraryIntent = new Intent(this, AdminRonginLibraryActivity.class);
                startActivity(ronginLibraryIntent);
                break;

            case R.id.admin_book_taking_request:
                Intent adminNotificationIntent = new Intent(this, AdminBookBorrowRequestActivity.class);
                startActivity(adminNotificationIntent);
                break;

            case R.id.admin_lent_books:
                Intent lentBooksIntent = new Intent(this, AdminLentBookListActivity.class);
                startActivity(lentBooksIntent);
                break;

            case R.id.admin_messages:
                Intent messageIntent = new Intent(this, AdminAllMessagesActivity.class);
                startActivity(messageIntent);
                break;

            case R.id.admin_feedback:
                Intent feedbackIntent = new Intent(this, AdminAllFeedbackListActivity.class);
                startActivity(feedbackIntent);
                break;

            case R.id.admin_daily_quote:
                Intent dailyQuoteIntent = new Intent(this, DailyQuoteActivity.class);
                startActivity(dailyQuoteIntent);
                break;

            case R.id.admin_monitor_mod:
                Intent monitoModIntent = new Intent(this, AdminModListActivity.class);
                startActivity(monitoModIntent);
                break;

            case R.id.admin_all_users:
                Intent allUserIntent = new Intent(this, AdminAllUsersActivity.class);
                startActivity(allUserIntent);
                break;
        }
    }
}
