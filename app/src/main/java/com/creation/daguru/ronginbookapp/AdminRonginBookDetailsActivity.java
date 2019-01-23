package com.creation.daguru.ronginbookapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.RonginBookDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_OWNERS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_COPY_COUNT;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_AUTHOR_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_BOOK_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_COPY_COUNT;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_LATITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_LONGITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_OWNER_UID;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;

public class AdminRonginBookDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtvBookName;
    private TextView mtvAuthorName;
    private TextView mtvbIncrease;
    private TextView mtvbDecrease;
    private TextView mtvbRemove;
    private TextView mtvbSave;

    private EditText metvCopyInput;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private RonginBookDetails mBookDetails;

    private AllBooksLibBookDetails tLibBookDetails;
    private String tUserUId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_rongin_book_details);

        mBookDetails = new RonginBookDetails();
        checkForIntentExtras();

        tLibBookDetails = new AllBooksLibBookDetails();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mtvBookName = findViewById(R.id.book_name);
        mtvAuthorName = findViewById(R.id.author_name);
        mtvbIncrease = findViewById(R.id.increase_button);
        mtvbDecrease = findViewById(R.id.decrease_button);
        mtvbRemove = findViewById(R.id.remove_book);
        mtvbSave = findViewById(R.id.save_button);
        metvCopyInput = findViewById(R.id.book_copy_input);

        mtvbIncrease.setOnClickListener(this);
        mtvbDecrease.setOnClickListener(this);
        mtvbRemove.setOnClickListener(this);
        mtvbSave.setOnClickListener(this);

        setUpUI();
    }

    private void setUpUI(){
        mtvBookName.setText(mBookDetails.bookName);
        mtvAuthorName.setText(mBookDetails.authorName);
        metvCopyInput.setText(String.valueOf(mBookDetails.copyCount));
    }

    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_BOOK_NAME)){
            mBookDetails.bookName = getIntent().getStringExtra(EXTRA_KEY_BOOK_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_AUTHOR_NAME)){
            mBookDetails.authorName = getIntent().getStringExtra(EXTRA_KEY_AUTHOR_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_BOOK_UID)){
            mBookDetails.bookUId = getIntent().getStringExtra(EXTRA_KEY_BOOK_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_OWNER_UID)){
            mBookDetails.ownerUId = getIntent().getStringExtra(EXTRA_KEY_OWNER_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_COPY_COUNT)){
            mBookDetails.copyCount = getIntent().getIntExtra(EXTRA_KEY_COPY_COUNT, 1);
        }
        if(getIntent().hasExtra(EXTRA_KEY_LATITUDE)){
            mBookDetails.ownerLatitude = getIntent().getDoubleExtra(EXTRA_KEY_LATITUDE, 1);
        }
        if(getIntent().hasExtra(EXTRA_KEY_LONGITUDE)){
            mBookDetails.ownerLongitude = getIntent().getDoubleExtra(EXTRA_KEY_LONGITUDE, 1);
        }
    }




    private void checkAndSaveDetails(){
        if(metvCopyInput.getText() != null && !metvCopyInput.getText().toString().trim().equals("")){
            int copy = Integer.valueOf(metvCopyInput.getText().toString().trim());
            if((copy != 0 && copy != mBookDetails.copyCount)){
                finallySave();
            } else {
                Toast.makeText(this, "Nothing is changed.", Toast.LENGTH_LONG).show();
            }
        } else {
            finish();
        }
    }

    private void finallySave(){
        if(Integer.valueOf(metvCopyInput.getText().toString()) != mBookDetails.copyCount){
            mBookDetails.copyCount = Integer.valueOf(metvCopyInput.getText().toString());
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                    .child(mBookDetails.bookUId)
                    .child(DATABASE_DIR_RONGIN_LIB_BOOKS_CHILD_COPY_COUNT);

            databaseReference.setValue(mBookDetails.copyCount);

            DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                    .child(mBookDetails.bookUId).child(RONGIN_UID);

            dbRef.setValue(mBookDetails.copyCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(AdminRonginBookDetailsActivity.this,
                                "Successfully Done.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }















    private void removeBook(){
        removeBookFromOwnerLibrary();
        removeBookFromAllLibrary();
        finish();
    }

    private void removeBookFromOwnerLibrary(){

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_RONGIN_LIB_BOOKS)
                .child(mBookDetails.bookUId);

        databaseReference.setValue(null);
    }

    private void removeBookFromAllLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mBookDetails.bookUId).child(RONGIN_UID);

        databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    decreaseOwnerCountFromLibrary();
                }
            }
        });
    }

    private void decreaseOwnerCountFromLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mBookDetails.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllBooksLibBookDetails libBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);
                if(dataSnapshot.exists()){
                    if(libBookDetails.ownerCount <= 1){
                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                                .child(mBookDetails.bookUId);

                        databaseReference.setValue(null);
                    } else if(libBookDetails.ownerCount == 2){
                        setDataToAddMoreDetailsToLibBook(libBookDetails);

                        libBookDetails.ownerCount--;
                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                                .child(mBookDetails.bookUId);

                        databaseReference.setValue(libBookDetails);
                    } else {
                        libBookDetails.ownerCount--;
                        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                                .child(mBookDetails.bookUId);

                        databaseReference.setValue(libBookDetails);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDataToAddMoreDetailsToLibBook(AllBooksLibBookDetails libBookDetails){
        tLibBookDetails.bookName = libBookDetails.bookName;
        tLibBookDetails.authorName = libBookDetails.authorName;
        tLibBookDetails.bookUId = libBookDetails.bookUId;
        tLibBookDetails.bookNameLower = libBookDetails.bookNameLower;
        tLibBookDetails.ownerCount = libBookDetails.ownerCount;
        tLibBookDetails.ownerUId = libBookDetails.ownerUId;
        tLibBookDetails.ownerLongitude = libBookDetails.ownerLongitude;
        tLibBookDetails.ownerLatitude = libBookDetails.ownerLatitude;
        tLibBookDetails.ownerFirstName = libBookDetails.ownerFirstName;

        mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(tLibBookDetails.bookUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    tUserUId = snapshot.getKey();
                    addMoreDetailsToLibBook();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMoreDetailsToLibBook(){

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(tUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserBasicInfo userBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                tLibBookDetails.ownerFirstName = userBasicInfo.firstName + " " + userBasicInfo.lastName;
                tLibBookDetails.ownerCount = 1;
                tLibBookDetails.ownerLatitude = userBasicInfo.latitude;
                tLibBookDetails.ownerLongitude = userBasicInfo.longitude;
                tLibBookDetails.ownerUId = tUserUId;

                finallyAddDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void finallyAddDetails(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mBookDetails.bookUId);

        databaseReference.setValue(tLibBookDetails);
    }

    private void showAlertDialog(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Remove Book")
                .setMessage("Are you sure you want to remove this book?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeBook();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.increase_button:
                int copyToIncrease = Integer.valueOf(metvCopyInput.getText().toString());
                copyToIncrease++;
                metvCopyInput.setText(String.valueOf(copyToIncrease));
                break;

            case R.id.decrease_button:
                int copyToDecrease = Integer.valueOf(metvCopyInput.getText().toString());
                copyToDecrease--;
                if(copyToDecrease<1) copyToDecrease = 1;
                metvCopyInput.setText(String.valueOf(copyToDecrease));
                break;

            case R.id.remove_book:
                showAlertDialog();
                break;

            case R.id.save_button:
                checkAndSaveDetails();
                break;
        }
    }
}
