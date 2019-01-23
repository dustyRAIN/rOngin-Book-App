package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.DailyQuoteDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_QUOTE_DETAILS;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class DailyQuoteActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtvLastUpdate;
    private EditText metvQuote;
    private EditText metvSource;
    private EditText metvQuoteDetails;
    private TextView mtvbPublshButton;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    private DailyQuoteDetails mDailyQuoteDetails;

    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quote);

        mtvLastUpdate = findViewById(R.id.last_update);
        metvQuote = findViewById(R.id.actual_quote);
        metvSource = findViewById(R.id.quote_source);
        metvQuoteDetails = findViewById(R.id.quote_details);
        mtvbPublshButton = findViewById(R.id.update_quote_button);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();

        mtvbPublshButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDailyQuoteDetails = new DailyQuoteDetails();
        attatchDatabaseListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detatchDatabaseListener();
    }

    private void attatchDatabaseListener(){
        if(mValueEventListener == null){
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        mDailyQuoteDetails = dataSnapshot.getValue(DailyQuoteDetails.class);
                        setUpUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_QUOTE_DETAILS);
        databaseReference.addValueEventListener(mValueEventListener);
    }

    private void detatchDatabaseListener(){
        if(mValueEventListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_QUOTE_DETAILS);
            databaseReference.removeEventListener(mValueEventListener);
        }
    }

    private void setUpUI(){
        mtvLastUpdate.setText(getFriendlyDateString(this,
                getLocalDateFromUTC(mDailyQuoteDetails.createTime),
                true));

        metvQuote.setText(mDailyQuoteDetails.dailyQuote);
        metvSource.setText(mDailyQuoteDetails.quoteSource);
        metvQuoteDetails.setText(mDailyQuoteDetails.quoteDescription);
    }

    private void publishQuote(){
        mDailyQuoteDetails.createTime = getUTCDateFromLocal(System.currentTimeMillis());
        mDailyQuoteDetails.dailyQuote = metvQuote.getText().toString();
        mDailyQuoteDetails.quoteSource = metvSource.getText().toString();
        mDailyQuoteDetails.quoteDescription = metvQuoteDetails.getText().toString();

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_QUOTE_DETAILS);
        databaseReference.setValue(mDailyQuoteDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(DailyQuoteActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DailyQuoteActivity.this, "Couldn't be updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.update_quote_button:
                publishQuote();
                break;
        }
    }
}
