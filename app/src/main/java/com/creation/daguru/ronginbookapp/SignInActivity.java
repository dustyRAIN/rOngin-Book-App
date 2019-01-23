package com.creation.daguru.ronginbookapp;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.design.widget.Snackbar;

import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;

import android.util.Log;

import android.view.View;

import android.view.ViewGroup;

import android.widget.Button;

import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseException;

import com.google.firebase.FirebaseTooManyRequestsException;

import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.PhoneAuthCredential;

import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.concurrent.TimeUnit;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RANDOM_EMAIL_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RANDOM_EMAIL_INFO_CHILD_EMAIL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EMAIL_ADDRESS;


public class SignInActivity extends AppCompatActivity implements

        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";


    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mSignedInViews;

    private TextView mStatusText;
    private TextView mDetailText;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;



    private TextView mStartButton;
    private TextView mVerifyButton;
    private TextView mResendButton;
    private TextView mCountryCode;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDBReference;

    private String mEditedPhoneNumber;



    @SuppressLint("WrongViewCast")
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign views

        //mPhoneNumberViews = findViewById(R.id.phone_auth_fields);
        //mSignedInViews = findViewById(R.id.signed_in_buttons);

        //mStatusText = findViewById(R.id.status);
        mDetailText = findViewById(R.id.tv_code_sent_notice);

        mDatabase = FirebaseDatabase.getInstance();
        mDBReference = mDatabase.getReference();

        mPhoneNumberField = findViewById(R.id.etv_phone_number);
        mVerificationField = findViewById(R.id.etv_verification_code);

        mCountryCode = findViewById(R.id.tv_country_code);


        mStartButton = findViewById(R.id.tvb_sign_in);
        mVerifyButton = findViewById(R.id.tvb_verify);
        mResendButton = findViewById(R.id.tvb_resend);

        /*mPhoneNumberViews.addView(mPhoneNumberField);
        mPhoneNumberViews.addView(mStartButton);
        mPhoneNumberViews.addView(mCountryCode);

        mSignedInViews.addView(mVerificationField);
        mSignedInViews.addView(mVerifyButton);
        mSignedInViews.addView(mResendButton);
        mSignedInViews.addView(mDetailText);*/
        // Assign click listeners

        mStartButton.setOnClickListener(this);
        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {



            @Override

            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]
                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);

            }



            @Override

            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    mPhoneNumberField.setError("Invalid phone number.");

                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;

                updateUI(STATE_CODE_SENT);
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }



    @Override

    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }





    private void startPhoneNumberVerification(String phoneNumber) {

        Toast.makeText(this, "On progress. Please wait.", Toast.LENGTH_LONG).show();
        mStartButton.setEnabled(false);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        //disableViews(mPhoneNumberField, mStartButton, mCountryCode);
        //enableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);

        mVerificationInProgress = true;
    }



    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        mVerifyButton.setEnabled(false);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {

        mResendButton.setEnabled(false);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mVerificationField.setError("Invalid code.");
                            }

                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    // [END sign_in_with_phone]



    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }



    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }



    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }



    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }



    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }



    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:

                //enableViews(mStartButton, mPhoneNumberField);
                //disableViews(mVerifyButton, mResendButton, mVerificationField);
                //mPhoneNumberViews.setVisibility(View.VISIBLE);
                //mSignedInViews.setVisibility(View.GONE);

                enableViews(mPhoneNumberField, mStartButton, mCountryCode);
                disableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);

                mStartButton.setEnabled(true);

                //Toast.makeText(SignInActivity.this, "initaial", Toast.LENGTH_LONG).show();

                break;

            case STATE_CODE_SENT:

                //enableViews(mVerifyButton, mResendButton, mVerificationField);
                //disableViews(mStartButton, mPhoneNumberField);
                //mPhoneNumberViews.setVisibility(View.GONE);
                //mSignedInViews.setVisibility(View.VISIBLE);

                enableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);
                disableViews(mPhoneNumberField, mStartButton, mCountryCode);

                mVerifyButton.setEnabled(true);
                mResendButton.setEnabled(true);

                Toast.makeText(SignInActivity.this, "Code has been sent.", Toast.LENGTH_LONG).show();

                break;

            case STATE_VERIFY_FAILED:

                /*enableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                        mVerificationField);*/

                //mPhoneNumberViews.setVisibility(View.VISIBLE);
                //mSignedInViews.setVisibility(View.GONE);

                enableViews(mPhoneNumberField, mStartButton, mCountryCode);
                disableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);

                mVerifyButton.setEnabled(true);
                mResendButton.setEnabled(true);

                //mDetailText.setText(R.string.status_verification_failed);
                Toast.makeText(SignInActivity.this, "Verify failed.", Toast.LENGTH_LONG).show();

                break;

            case STATE_VERIFY_SUCCESS:

                // Verification has succeeded, proceed to firebase sign in
                //disableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                //        mVerificationField);

                //mPhoneNumberViews.setVisibility(View.GONE);
                //mSignedInViews.setVisibility(View.VISIBLE);

                disableViews(mPhoneNumberField, mStartButton, mCountryCode);
                enableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);

                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        mVerificationField.setText("Success");
                    }
                }

                break;

            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                //mDetailText.setText(R.string.status_sign_in_failed);
                setResult(RESULT_CANCELED);
                finish();

                break;

            case STATE_SIGNIN_SUCCESS:

                // Np-op, handled by sign-in check
                setResult(RESULT_OK);
                finish();

                break;

        }



        if (user == null) {

            // Signed out
            //mPhoneNumberViews.setVisibility(View.VISIBLE);
            //mSignedInViews.setVisibility(View.GONE);

            //enableViews(mPhoneNumberField, mStartButton, mCountryCode);
            //disableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);

        } else {

            // Signed in
            //mPhoneNumberViews.setVisibility(View.GONE);
            //mSignedInViews.setVisibility(View.VISIBLE);

            enableViews(mVerificationField, mVerifyButton, mResendButton, mDetailText);
            disableViews(mPhoneNumberField, mStartButton, mCountryCode);


            //enableViews(mPhoneNumberField, mVerificationField);

            mPhoneNumberField.setText(null);
            mVerificationField.setText(null);



            //mStatusText.setText(R.string.signed_in);

            //mDetailText.setText(getString(R.string.firebase_status_fmt, user.getUid()));

        }

    }



    private boolean validatePhoneNumber() {

        String phoneNumber = mPhoneNumberField.getText().toString();

        if (TextUtils.isEmpty(phoneNumber)) {

            mPhoneNumberField.setError("Invalid phone number.");

            return false;

        } else {
            return true;
        }

    }

    private void setCodeForPhoneNumber(){
        if(mEditedPhoneNumber.charAt(0) != '+'){
            mEditedPhoneNumber = "+880" + mEditedPhoneNumber;
        }
    }

    private void checkAndStartPhoneNumberVerification(String phoneNumber){
        mStartButton.setEnabled(false);
        phoneNumber = phoneNumber.trim();
        mEditedPhoneNumber = null;
        for(char c: phoneNumber.toCharArray()){
            if(c == '+' || (c >= '0' && c <= '9')){
                if(mEditedPhoneNumber == null) mEditedPhoneNumber = String.valueOf(c);
                else mEditedPhoneNumber += c;
            }
        }

        setCodeForPhoneNumber();

        Log.d("numbur", mEditedPhoneNumber);

        DatabaseReference databaseReference = mDBReference.child(DATABASE_DIR_RANDOM_EMAIL_INFO)
                .child(mEditedPhoneNumber).child(DATABASE_DIR_RANDOM_EMAIL_INFO_CHILD_EMAIL);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mStartButton.setEnabled(true);
                    String email = dataSnapshot.getValue(String.class);
                    Intent intent = new Intent(SignInActivity.this, SignInWithEmailActivity.class);
                    intent.putExtra(EXTRA_KEY_EMAIL_ADDRESS, email);
                    startActivity(intent);
                } else {
                    startPhoneNumberVerification(mEditedPhoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mStartButton.setEnabled(true);
            }
        });
    }



    private void enableViews(View... views) {

        for (View v : views) {

            v.setVisibility(View.VISIBLE);

        }

    }



    private void disableViews(View... views) {

        for (View v : views) {

            v.setVisibility(View.GONE);

        }

    }



    @Override

    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvb_sign_in:

                if (!validatePhoneNumber()) {
                    return;
                }
                checkAndStartPhoneNumberVerification(mPhoneNumberField.getText().toString());

                break;

            case R.id.tvb_verify:

                String code = mVerificationField.getText().toString();

                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);

                break;

            case R.id.tvb_resend:

                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);

                break;
        }

    }

}