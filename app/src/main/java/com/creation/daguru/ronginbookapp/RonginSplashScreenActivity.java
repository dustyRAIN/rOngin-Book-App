package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.net.InetAddress;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.EMAIL_KEY;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.IS_LOGGED_IN_KEY;
import static com.creation.daguru.ronginbookapp.GetEmailAddressActivity.PASSWORD_KEY;
import static com.creation.daguru.ronginbookapp.MainActivity.ATTACH_AUTH_LISTENER;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RonginSplashScreenActivity extends AppCompatActivity {

    protected static final String IS_ACCOUNT_FREEZED_KEY = "is-account-freezed";
    protected static final String SHOULD_WARN_TO_RETURN_KEY = "should-warn-to-return";

    private SharedPreferences mSharedPreferences;

    private String mEmail;
    private String mPassword;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private TextView mtvOperationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rongin_splash_screen);

        mtvOperationStatus = findViewById(R.id.splash_wait);
        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        checkInternetAndProceed();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("https://www.google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    private void checkInternetAndProceed(){
        if(isNetworkConnected()){
            new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() { checkForSignInInfo();
                    }
                }, 1000);
        } else {
            showDialogForNoInternet();
        }
    }

    private void checkForSignInInfo(){
        if(mSharedPreferences.contains(IS_LOGGED_IN_KEY)){
            if(mSharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false)){
                mEmail = mSharedPreferences.getString(EMAIL_KEY, "");
                mPassword = mSharedPreferences.getString(PASSWORD_KEY, "");

                logInWithInfo();
            } else {
                mSharedPreferences.edit().putBoolean(ATTACH_AUTH_LISTENER, true).apply();
                setResult(RESULT_OK);
                finish();
            }
        } else {
            mSharedPreferences.edit().putBoolean(ATTACH_AUTH_LISTENER, true).apply();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void logInWithInfo(){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mtvOperationStatus.setText("Setting up profile, please wait");
                            checkForValidity();
                        } else {
                            showDialog();
                        }
                    }
                });
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please wait")
                .setMessage("Something went wrong. Please check your internet connection and try again.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logInWithInfo();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialogForNoInternet(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet")
                .setMessage("Please connect to the internet.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkInternetAndProceed();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
                        mSharedPreferences.edit().putBoolean(ATTACH_AUTH_LISTENER, true).apply();
                        setResult(RESULT_OK);
                        finish();
                    }
                }

                if(i==n){
                    mSharedPreferences.edit().putBoolean(ATTACH_AUTH_LISTENER, true).apply();
                    setResult(RESULT_OK);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDialog();
            }
        });
    }
}
