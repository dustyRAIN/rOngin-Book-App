package com.creation.daguru.ronginbookapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.Utils.RonginDateUtils;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EMAIL_ADDRESS;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.EMAIL_KEY;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.IS_LOGGED_IN_KEY;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.PASSWORD_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;
import static com.creation.daguru.ronginbookapp.RonginSplashScreenActivity.IS_ACCOUNT_FREEZED_KEY;
import static com.creation.daguru.ronginbookapp.RonginSplashScreenActivity.SHOULD_WARN_TO_RETURN_KEY;

public class SignInWithEmailActivity extends AppCompatActivity implements View.OnClickListener {



    private SharedPreferences mSharedPreferences;

    private ProgressDialog progressDialog;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvEmailAddress;
    private TextView mtvbSignInButton;
    private EditText metvPassword;

    private TextView mtvbForgotPassword;

    private String mCurrentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_with_email);

        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        checkForIntentExtras();

        mtvEmailAddress = findViewById(R.id.email_address);
        metvPassword = findViewById(R.id.password);
        mtvbSignInButton = findViewById(R.id.sign_in_button);

        mtvbForgotPassword = findViewById(R.id.forget_password);

        setEmailAddress();

        mtvbSignInButton.setOnClickListener(this);
        mtvbForgotPassword.setOnClickListener(this);
    }

    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_EMAIL_ADDRESS)){
            mCurrentEmail = getIntent().getStringExtra(EXTRA_KEY_EMAIL_ADDRESS);
        } else {
            finish();
        }
    }

    private void setEmailAddress(){
        String editedEmail = null;
        int charTaken = 0;
        boolean takeAllNow = false;
        for(char c: mCurrentEmail.toCharArray()){
            if(takeAllNow){
                if(editedEmail == null) editedEmail = String.valueOf(c);
                else editedEmail += c;
            } else if(c == '@'){
                if(editedEmail == null) editedEmail = String.valueOf(c);
                else editedEmail += c;
                takeAllNow = true;
            } else if(charTaken < 2){
                if(editedEmail == null) editedEmail = String.valueOf(c);
                else editedEmail += c;
                charTaken++;
            } else {
                editedEmail += '*';
            }
        }

        mtvEmailAddress.setText(editedEmail);
    }

    private void checkAndSignIn(){
        if(metvPassword.getText().toString().isEmpty() || metvPassword.getText().toString().equals("")){
            metvPassword.setError("Type a password first.");
        } else {
            signIn();
        }
    }

    private void signIn(){
        showProgressDialog();
        String password = metvPassword.getText().toString();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mCurrentEmail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    saveInPreference(metvPassword.getText().toString());
                } else {
                    Toast.makeText(SignInWithEmailActivity.this, "Please check your password and internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in..."); // Setting Message
        progressDialog.setTitle("Please Wait"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
    }

    private void saveInPreference(String password){
        mSharedPreferences.edit().putBoolean(IS_LOGGED_IN_KEY, true).apply();
        mSharedPreferences.edit().putString(EMAIL_KEY, mCurrentEmail).apply();
        mSharedPreferences.edit().putString(PASSWORD_KEY, password).apply();
        checkForValidity();
    }

    private void checkForValidity(){
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mSharedPreferences.edit().putBoolean(IS_ACCOUNT_FREEZED_KEY, false).apply();
        mSharedPreferences.edit().putBoolean(SHOULD_WARN_TO_RETURN_KEY, false).apply();

        DatabaseReference reference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                .child(mFirebaseUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int n = 0;
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    n++;
                }

                int i = 0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    UserExchangeBookDetails bookDetails = snapshot.getValue(UserExchangeBookDetails.class);

                    int daysLeft = RonginDateUtils.getLeftDaysCount(bookDetails.exchangeTime, System.currentTimeMillis(),
                            bookDetails.dayLimit);

                    if(daysLeft == 0){
                        mSharedPreferences.edit().putBoolean(IS_ACCOUNT_FREEZED_KEY, true).apply();
                        setResult(RESULT_OK);
                        finish();
                        break;
                    }

                    if(daysLeft <= 3 && daysLeft > 0){
                        mSharedPreferences.edit().putBoolean(SHOULD_WARN_TO_RETURN_KEY, true).apply();
                    }

                    i++;

                    if(i==n){
                        progressDialog.dismiss();
                        finish();
                    }
                }

                if(i==n){
                    progressDialog.dismiss();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void passwordForgotten(){
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        intent.putExtra(EXTRA_KEY_EMAIL_ADDRESS, mCurrentEmail);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                checkAndSignIn();
                break;

            case R.id.forget_password:
                passwordForgotten();
                break;
        }
    }
}
