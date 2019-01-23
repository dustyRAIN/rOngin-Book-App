package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_EMAIL_ADDRESS;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtvForgotHeading;
    private EditText metvEmail;
    private TextView mtvbSendEmail;

    private String mEmailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mtvForgotHeading = findViewById(R.id.forgot_heading);
        mtvbSendEmail = findViewById(R.id.send_email_button);
        metvEmail = findViewById(R.id.email);

        getIntentExtra();

        mtvbSendEmail.setOnClickListener(this);
    }

    private void getIntentExtra(){
        if(getIntent().hasExtra(EXTRA_KEY_EMAIL_ADDRESS)){
            mEmailAddress = getIntent().getStringExtra(EXTRA_KEY_EMAIL_ADDRESS);
        } else {
            finish();
        }
    }

    private void checkAndSendEmail(){
        String email = metvEmail.getText().toString().trim();
        if(email.equals(mEmailAddress)){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.sendPasswordResetEmail(mEmailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mtvForgotHeading.setText("An link to reset your password has been sent to your email address. Please check your email address.");
                        mtvbSendEmail.setEnabled(false);
                    } else {
                        mtvForgotHeading.setText("Email couldn't be sent. Please try again.");
                    }
                }
            });
        } else {
            mtvForgotHeading.setText("Email that you entered didn't match with your original one.");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_email_button:
                checkAndSendEmail();
                break;
        }
    }
}
