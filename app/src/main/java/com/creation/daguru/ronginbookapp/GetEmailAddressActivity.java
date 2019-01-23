package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RANDOM_EMAIL_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RANDOM_EMAIL_INFO_CHILD_EMAIL;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_EMAIL;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

public class GetEmailAddressActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final String IS_LOGGED_IN_KEY = "rongin-current-user-logged-in";
    protected static final String EMAIL_KEY = "rongin-current-user-email";
    protected static final String PASSWORD_KEY = "rongin-current-user-password";

    private SharedPreferences mSharedPreferences;

    private EditText metvEmail;
    private EditText metvReEmail;
    private EditText metvPassword;
    private EditText metvRePassword;

    private TextView mtvConfirm;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_email_address);

        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        metvEmail = findViewById(R.id.edit_email_address);
        metvReEmail = findViewById(R.id.re_edit_email_address);
        metvPassword = findViewById(R.id.edit_password);
        metvRePassword = findViewById(R.id.re_edit_password);

        mtvConfirm = findViewById(R.id.button_confirm);
        mtvConfirm.setOnClickListener(this);
    }

    private void checkAndRegisterEmail(){
        boolean isAllOk = true;
        if(metvEmail.getText().toString().equals("") || metvEmail.getText().toString().isEmpty()){
            isAllOk = false;
            metvEmail.setError("Can't be empty.");
        }
        if(metvReEmail.getText().toString().equals("") || metvReEmail.getText().toString().isEmpty()){
            isAllOk = false;
            metvReEmail.setError("Can't be empty.");
        }if(metvPassword.getText().toString().equals("") || metvPassword.getText().toString().isEmpty()){
            isAllOk = false;
            metvPassword.setError("Can't be empty.");
        }
        if(metvRePassword.getText().toString().equals("") || metvRePassword.getText().toString().isEmpty()){
            isAllOk = false;
            metvRePassword.setError("Can't be empty.");
        }
        if(isAllOk){
            checkPhaseTwo();
        }
    }

    private void checkPhaseTwo(){
        boolean isAllOk = true;
        if(!metvEmail.getText().toString().equals(metvReEmail.getText().toString())){
            isAllOk = false;
            metvEmail.setError("Email address didn't match.");
            metvReEmail.setError("Email address didn't match.");
        }
        if(!metvPassword.getText().toString().equals(metvRePassword.getText().toString())){
            isAllOk = false;
            metvPassword.setError("Password didn't match.");
            metvRePassword.setError("Password didn't match.");
        }
        if(isAllOk){
            checkPhaseThree();
        }
    }

    private void checkPhaseThree(){
        boolean isAllOk = true;
        CharSequence email = metvEmail.getText();
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            isAllOk = false;
            metvEmail.setError("Invalid email address.");
        }
        String password = metvPassword.getText().toString();
        if(password.length()<8){
            isAllOk = false;
            metvPassword.setError("At least 8 characters.");
        } else {
            for(char c: password.toCharArray()){
                if(!(c>='a' && c<='z') && !(c>='A' && c<='Z') && !(c>='0' && c<='9')){
                    isAllOk = false;
                    metvPassword.setError("Should only contain alphabet and number.");
                }
            }
        }
        if(isAllOk){
            checkPhaseFour();
        }
    }

    private void checkPhaseFour(){
        String email = metvEmail.getText().toString();
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RANDOM_EMAIL_INFO);
        databaseReference.orderByChild(DATABASE_DIR_RANDOM_EMAIL_INFO_CHILD_EMAIL)
                .equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    metvEmail.setError("Already in use.");
                } else {
                    showConfirmationDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showConfirmationDialog(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Are you sure?")
                .setMessage("You may not be able to change your email address on in this beta version of the app. If this is not your email address, then you may not be able to reset your password.")
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        linkEmailAddress();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void linkEmailAddress(){
        String email = metvEmail.getText().toString().trim();
        String password = metvPassword.getText().toString();

        mSharedPreferences.edit().putBoolean(IS_LOGGED_IN_KEY, true);
        mSharedPreferences.edit().putString(EMAIL_KEY, email).apply();
        mSharedPreferences.edit().putString(PASSWORD_KEY, password).apply();

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mFirebaseUser.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    updateEmailInDatabase();
                }
            }
        });
    }

    private void updateEmailInDatabase(){
        DatabaseReference dbReference = mDatabaseReference.child(DATABASE_DIR_RANDOM_EMAIL_INFO)
                .child(mFirebaseUser.getPhoneNumber()).child(DATABASE_DIR_RANDOM_EMAIL_INFO_CHILD_EMAIL);
        dbReference.setValue(metvEmail.getText().toString().trim());

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mFirebaseUser.getUid()).child(DATABASE_DIR_USER_BASIC_INFO_CHILD_EMAIL);
        databaseReference.setValue(mFirebaseUser.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_confirm:
                checkAndRegisterEmail();
                break;
        }
    }
}
