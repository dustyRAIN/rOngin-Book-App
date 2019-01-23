package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.UserFeedbackDetails;
import com.creation.daguru.ronginbookapp.data.UserFeedbackInfoDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_ALL_FEEDBACK;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_FEEDBACK_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_FEEDBACK_INFO_CHILD_LAST_FEEDBACK_TIME;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getDayDifference;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtvNotAvailable;
    private TextView mtvbSendFeedback;
    private TextView mtvbRateThisApp;
    private EditText metvFeedback;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private ValueEventListener mFeedbackInfoListener;

    private UserFeedbackInfoDetails mUserFeedbackInfoDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mtvNotAvailable = findViewById(R.id.heading_feed);
        mtvbSendFeedback = findViewById(R.id.send_feed);
        mtvbRateThisApp = findViewById(R.id.rate_app);
        metvFeedback = findViewById(R.id.edit_feed);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserFeedbackInfoDetails = new UserFeedbackInfoDetails();

        mtvbSendFeedback.setOnClickListener(this);
        mtvbRateThisApp.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachUpdateListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableUpdateListener();
    }

    private void attachUpdateListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_FEEDBACK_INFO)
                .child(mFirebaseUser.getUid());

        if(mFeedbackInfoListener == null){
            mFeedbackInfoListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUserFeedbackInfoDetails = dataSnapshot.getValue(UserFeedbackInfoDetails.class);
                    if(mUserFeedbackInfoDetails == null){
                        setUpFeedbackInfo();
                    } else {
                        analyseFeedbackInfo();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        databaseReference.addValueEventListener(mFeedbackInfoListener);
    }

    private void disableUpdateListener(){
        if(mFeedbackInfoListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_FEEDBACK_INFO)
                    .child(mFirebaseUser.getUid());

            databaseReference.removeEventListener(mFeedbackInfoListener);
        }
    }


    private void setUpFeedbackInfo(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_FEEDBACK_INFO)
                .child(mFirebaseUser.getUid());

        mUserFeedbackInfoDetails = new UserFeedbackInfoDetails(1, 0);

        databaseReference.setValue(mUserFeedbackInfoDetails);
    }

    private void analyseFeedbackInfo(){
        if(mUserFeedbackInfoDetails.ability == 0){
            notAbleToSend(1);
        } else {
            int dayNumber = getDayDifference(getUTCDateFromLocal(System.currentTimeMillis()),
                    mUserFeedbackInfoDetails.lastFeedbackTime);

            if(dayNumber<3){
                notAbleToSend(2);
            } else {
                ableToSend();
            }
        }
    }

    private void notAbleToSend(int type){
        mtvbSendFeedback.setEnabled(false);
        mtvNotAvailable.setVisibility(View.VISIBLE);

        if(type == 2){
            mtvNotAvailable.setText("Your feedback was received. Please give us some time to review your previous feedback before you give another.");
        }
    }

    private void ableToSend(){
        mtvbSendFeedback.setEnabled(true);
        mtvNotAvailable.setVisibility(View.INVISIBLE);
    }

    private void sendFeedbackClicked(){
        String typedText = metvFeedback.getText().toString();

        if(typedText.isEmpty() || typedText.equals("")){
            Toast.makeText(this, "Please type something.", Toast.LENGTH_LONG).show();
        } else if(typedText.length()>500){
            Toast.makeText(this, "Text limit exceeded.", Toast.LENGTH_LONG).show();
        } else {
            updateFeedback(typedText);
        }
    }

    private void updateFeedback(String typedText){
        long currentTime = System.currentTimeMillis();

        UserFeedbackDetails userFeedbackDetails = new UserFeedbackDetails(
                typedText,
                mFirebaseUser.getUid(),
                mFirebaseUser.getDisplayName(),
                null,
                getUTCDateFromLocal(currentTime),
                0);

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_ALL_FEEDBACK)
                .child(mFirebaseUser.getUid());

        databaseReference.setValue(userFeedbackDetails);

        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_USER_FEEDBACK_INFO).child(mFirebaseUser.getUid())
                .child(DATABASE_DIR_USER_FEEDBACK_INFO_CHILD_LAST_FEEDBACK_TIME);

        dbRef.setValue(getUTCDateFromLocal(currentTime));

        DatabaseReference dbReference = mDatabaseReference.child(DATABASE_DIR_NEW_FEEDBACK);
        dbReference.setValue(1);

        Toast.makeText(this, "Your feedback is sent.", Toast.LENGTH_LONG).show();
        metvFeedback.setText("");
    }

    private void rateThisApp(){
        Toast.makeText(this, "I will rate this app", Toast.LENGTH_LONG).show();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_feed:
                sendFeedbackClicked();
                break;

            case R.id.rate_app:
                rateThisApp();
                break;
        }
    }
}
