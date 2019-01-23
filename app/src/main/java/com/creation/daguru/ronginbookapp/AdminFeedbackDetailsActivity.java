package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.ListUserBasicInfo;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.creation.daguru.ronginbookapp.data.UserFeedbackDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_ALL_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_ALL_FEEDBACK_CHILD_ABILITY;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_FEEDBACK_INFO;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_FEEDBACK_TIME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_FEEDBACK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_IS_READ;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_EMAIL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_LATITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_LONGITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_PHONE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_UID;

public class AdminFeedbackDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtvbSenderName;
    private TextView mtvFeedback;
    private TextView mtvbDelete;
    private TextView mtvbBlock;

    private UserFeedbackDetails mUserFeedbackDetails;
    private UserBasicInfo mSenderInfo;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_feedback_details);

        mUserFeedbackDetails = new UserFeedbackDetails();

        mtvbSenderName = findViewById(R.id.feed_sender_name);
        mtvFeedback = findViewById(R.id.feed_feedback);
        mtvbDelete = findViewById(R.id.feed_delete_button);
        mtvbBlock = findViewById(R.id.feed_block_button);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getIntentExtra();
        updateUI();

        mtvbSenderName.setOnClickListener(this);
        mtvbDelete.setOnClickListener(this);
        mtvbBlock.setOnClickListener(this);
    }

    private void getIntentExtra(){
        if(getIntent().hasExtra(EXTRA_KEY_FEEDBACK)){
            mUserFeedbackDetails.feedback = getIntent().getStringExtra(EXTRA_KEY_FEEDBACK);
        }

        if(getIntent().hasExtra(EXTRA_KEY_USER_UID)){
            mUserFeedbackDetails.userUId = getIntent().getStringExtra(EXTRA_KEY_USER_UID);
        }

        if(getIntent().hasExtra(EXTRA_KEY_USER_NAME)){
            mUserFeedbackDetails.userName = getIntent().getStringExtra(EXTRA_KEY_USER_NAME);
        }

        if(getIntent().hasExtra(EXTRA_KEY_FEEDBACK_TIME)){
            mUserFeedbackDetails.feedbackTime = getIntent().getLongExtra(EXTRA_KEY_FEEDBACK_TIME,
                    System.currentTimeMillis());
        }

        if(getIntent().hasExtra(EXTRA_KEY_IS_READ)){
            mUserFeedbackDetails.isRead = getIntent().getIntExtra(EXTRA_KEY_IS_READ, 0);
        }

        if(getIntent().hasExtra(EXTRA_KEY_FEEDBACK_UID)){
            mUserFeedbackDetails.feedbackUId = getIntent().getStringExtra(EXTRA_KEY_FEEDBACK_UID);
        }
    }

    private void updateUI(){
        mtvbSenderName.setText(mUserFeedbackDetails.userName);
        mtvFeedback.setText(mUserFeedbackDetails.feedback);
    }

    private void goToSenderProfile(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mUserFeedbackDetails.userUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mSenderInfo = dataSnapshot.getValue(UserBasicInfo.class);
                goToSenderDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToSenderDetails(){
        ListUserBasicInfo listUserBasicInfo = new ListUserBasicInfo(
                mSenderInfo.firstName,
                mSenderInfo.lastName,
                mSenderInfo.email,
                mSenderInfo.phoneNumber,
                mSenderInfo.photoUrl,
                mUserFeedbackDetails.userUId,
                mSenderInfo.latitude,
                mSenderInfo.longitude
        );

        Intent intent = new Intent(this, AdminUserDetailsActivity.class);
        intent = addExtrasToIntent(intent, listUserBasicInfo);
        startActivity(intent);
    }

    private Intent addExtrasToIntent(Intent intent, ListUserBasicInfo userBasicInfo){
        intent.putExtra(EXTRA_KEY_USER_NAME, userBasicInfo.firstName + " " + userBasicInfo.lastName);
        intent.putExtra(EXTRA_KEY_USER_EMAIL, userBasicInfo.email);
        intent.putExtra(EXTRA_KEY_USER_PHONE, userBasicInfo.phoneNumber);
        intent.putExtra(EXTRA_KEY_USER_PHOTO_URL, userBasicInfo.photoUrl);
        intent.putExtra(EXTRA_KEY_USER_UID, userBasicInfo.userUId);
        intent.putExtra(EXTRA_KEY_USER_LATITUDE, userBasicInfo.latitude);
        intent.putExtra(EXTRA_KEY_USER_LONGITUDE, userBasicInfo.longitude);

        return intent;
    }

    private void deleteThisFeedback(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_ALL_FEEDBACK)
                .child(mUserFeedbackDetails.feedbackUId);

        databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminFeedbackDetailsActivity.this, "Deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminFeedbackDetailsActivity.this,
                            "Couldn't be deleted.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void blockTheUser(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_FEEDBACK_INFO)
                .child(mUserFeedbackDetails.userUId).child(DATABASE_DIR_USER_ALL_FEEDBACK_CHILD_ABILITY);

        databaseReference.setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminFeedbackDetailsActivity.this, "Blocked.", Toast.LENGTH_SHORT).show();
                    mtvbBlock.setEnabled(false);
                } else {
                    Toast.makeText(AdminFeedbackDetailsActivity.this,
                            "Couldn't be blocked.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.feed_sender_name:
                goToSenderProfile();
                break;

            case R.id.feed_delete_button:
                deleteThisFeedback();
                break;

            case R.id.feed_block_button:
                blockTheUser();
                break;
        }
    }
}
