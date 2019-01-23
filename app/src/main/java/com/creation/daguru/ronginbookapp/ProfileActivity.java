package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.AdminModDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Locale;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_MOD_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.STORAGE_DIR_PRO_PIC;
import static com.creation.daguru.ronginbookapp.MainActivity.SHARED_PREFERENCES_NAME;
import static com.creation.daguru.ronginbookapp.RonginSplashScreenActivity.IS_ACCOUNT_FREEZED_KEY;
import static com.creation.daguru.ronginbookapp.RonginSplashScreenActivity.SHOULD_WARN_TO_RETURN_KEY;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.ADMIN_POST_MASTER_ADMIN;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_POST;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int RC_BROWSE_IMAGE = 1;

    private ImageView mivProPic;
    private TextView mtvFirstName;
    private TextView mtvLastName;
    private EditText metvFirstName;
    private EditText metvLastName;
    private ImageView micEdit;
    private TextView mtvEmailAddress;
    private TextView mtvPhoneNumber;
    private TextView mtvLatitude;
    private TextView mtvLongitude;
    private TextView mtvbContinue;

    private View mAdminGate;

    private SharedPreferences mSharedPreferences;

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseReference;
    private StorageReference mImageStorageReference;
    private FirebaseUser mFirebaseUser;

    private Uri mSelectedImageUri;
    private Uri mUploadedPhotoUrl;

    private Bitmap mBitmap;

    private UserBasicInfo mUserBasicInfo;
    private UserProfileChangeRequest mProfileChangeRequest;
    private AdminModDetails mAdminModDetails;

    private boolean isEditFieldVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        mivProPic = findViewById(R.id.pro_pro_pic);
        mtvFirstName = findViewById(R.id.pro_tv_first_name);
        mtvLastName = findViewById(R.id.pro_tv_last_name);
        metvFirstName = findViewById(R.id.pro_etv_first_name);
        metvLastName = findViewById(R.id.pro_etv_last_name);
        micEdit = findViewById(R.id.pro_ic_edit);
        mtvEmailAddress = findViewById(R.id.pro_tv_email);
        mtvPhoneNumber = findViewById(R.id.pro_tv_phone);
        mtvLatitude = findViewById(R.id.pro_tv_latitude);
        mtvLongitude = findViewById(R.id.pro_tv_longitude);
        mtvbContinue = findViewById(R.id.pro_tvb_save);

        mAdminGate = findViewById(R.id.pro_admin_gate);

        micEdit.setVisibility(View.INVISIBLE);

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mImageStorageReference = mStorage.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        hideEditFields();
        getBasicInfo();

        micEdit.setOnClickListener(this);
        mivProPic.setOnClickListener(this);
        mtvbContinue.setOnClickListener(this);
        mAdminGate.setOnClickListener(this);
    }

    private void getBasicInfo(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mFirebaseUser.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);

                setUpUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpUI(){

        hideEditFields();

        mtvFirstName.setText(mUserBasicInfo.firstName);
        mtvLastName.setText(mUserBasicInfo.lastName);

        metvFirstName.setText(mUserBasicInfo.firstName);
        metvLastName.setText(mUserBasicInfo.lastName);

        mtvEmailAddress.setText(mUserBasicInfo.email);
        mtvPhoneNumber.setText(mUserBasicInfo.phoneNumber);
        mtvLatitude.setText(String.format("%.4f", mUserBasicInfo.latitude));
        mtvLongitude.setText(String.format("%.4f", mUserBasicInfo.longitude));

        micEdit.setVisibility(View.VISIBLE);

        if(mFirebaseUser.getPhotoUrl() != null
                && !mFirebaseUser.getPhotoUrl().toString().equals("")
                && !mFirebaseUser.getPhotoUrl().toString().isEmpty()) {

            Log.d("PhotoUrl", mFirebaseUser.getPhotoUrl().toString());

            try {
                Glide.with(ProfileActivity.this).load(mFirebaseUser.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(mivProPic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void hideEditFields(){
        metvFirstName.setVisibility(View.INVISIBLE);
        metvLastName.setVisibility(View.INVISIBLE);

        mtvFirstName.setVisibility(View.VISIBLE);
        mtvLastName.setVisibility(View.VISIBLE);

        isEditFieldVisible = false;
    }

    private void showEditFields(){
        metvFirstName.setVisibility(View.VISIBLE);
        metvLastName.setVisibility(View.VISIBLE);

        mtvFirstName.setVisibility(View.INVISIBLE);
        mtvLastName.setVisibility(View.INVISIBLE);

        isEditFieldVisible = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == RC_BROWSE_IMAGE){
                if(data != null){
                    Uri uri = data.getData();
                    cropImage(uri);
                }
            }   else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                if(data != null) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    mSelectedImageUri = result.getUri();
                    setCircularImage(mSelectedImageUri);
                }
            }
        } else if(resultCode == RESULT_CANCELED){

        }
    }

    private void cropImage(Uri uri){
        CropImage.activity(uri).setAspectRatio(1, 1).start(this);
    }

    private void setCircularImage(Uri uri){
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), mBitmap);
            roundedBitmapDrawable.setCircular(true);

            mivProPic.setImageDrawable(roundedBitmapDrawable);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void saveDataAndContinue(){

        mProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(metvFirstName.getText().toString() + ' ' + metvLastName.getText().toString())
                .build();

        if(mSelectedImageUri != null){

            StorageReference storageReference = mImageStorageReference.child(STORAGE_DIR_PRO_PIC).child(mFirebaseUser.getUid());
            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        UploadNewPhoto();
                    }
                }
            });

        } else {
            mUploadedPhotoUrl = mFirebaseUser.getPhotoUrl();
            updateProfileInfo();
        }
    }

    private void UploadNewPhoto(){
        final StorageReference reference = mImageStorageReference.child(STORAGE_DIR_PRO_PIC).child(mFirebaseUser.getUid());

        UploadTask uploadTask = reference.putFile(mSelectedImageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    mUploadedPhotoUrl = task.getResult();
                    mProfileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(metvFirstName.getText().toString() + ' ' + metvLastName.getText().toString())
                            .setPhotoUri(mUploadedPhotoUrl)
                            .build();

                    updateProfileInfo();
                    //Toast.makeText(GetUserInfoActivity.this, mUploadedPhotoUrl.toString(), Toast.LENGTH_LONG).show();
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    private void updateProfileInfo(){
        UserBasicInfo userBasicInfo = new UserBasicInfo(metvFirstName.getText().toString(), metvLastName.getText().toString(),
                mUserBasicInfo.email,  mFirebaseUser.getPhoneNumber(), mUploadedPhotoUrl.toString(), mUserBasicInfo.latitude,
                mUserBasicInfo.longitude);

        mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO).child(mFirebaseUser.getUid()).setValue(userBasicInfo);

        mFirebaseUser.updateProfile(mProfileChangeRequest).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("GetUserInfo", "Name and propicurl done");
                        if(task.isSuccessful()) {
                            //Toast.makeText(GetUserInfoActivity.this, "dekhi ki hoy" + mFirebaseUser.getPhotoUrl().toString(), Toast.LENGTH_LONG).show();
                            Toast.makeText(ProfileActivity.this, "Saved Successfully.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {

                        }
                    }
                });
    }



    private void checkAllFieldAndContnue(){
        if(metvFirstName.getText().toString().trim().equals("") || metvFirstName.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "First name can not be empty.", Toast.LENGTH_LONG).show();
        } else if(isNameValid()){
            saveDataAndContinue();
        }
    }

    private boolean isNameValid(){
        String firstName = metvFirstName.getText().toString().trim();
        String lastName = metvLastName.getText().toString().trim();

        boolean isOk = true;

        if(firstName.length()>18){
            Toast.makeText(this, "The first name is too large.", Toast.LENGTH_LONG).show();
            isOk = false;
        } else if(isThereAnyOtherChar(firstName)){
            Toast.makeText(this, "The first name should contain only alphabets", Toast.LENGTH_LONG).show();
            isOk = false;
        }
        if(lastName.length()>18){
            Toast.makeText(this, "The last name is too large.", Toast.LENGTH_LONG).show();
            isOk = false;
        } else if(isThereAnyOtherChar(lastName)){
            Toast.makeText(this, "The last name should contain only alphabets", Toast.LENGTH_LONG).show();
            isOk = false;
        }

        return isOk;
    }

    private boolean isThereAnyOtherChar(String name){
        for(char c: name.toCharArray()){
            if(!((c>='a' && c<='z') || (c>='A' && c<='Z') || c == ' ')){
                return true;
            }
        }
        return false;
    }

    private void checkIfTheUserIsALegitAdmin(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_MOD_LIST)
                .child(mFirebaseUser.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mAdminModDetails = dataSnapshot.getValue(AdminModDetails.class);
                    passThisAdminThroughAllFilters();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void passThisAdminThroughAllFilters(){
        if(mAdminModDetails.adminPost == ADMIN_POST_MASTER_ADMIN){
            goToAdminPanel();
        } else {
            if(isAccountFreezed()){
                Toast.makeText(this, "Restricted. Your Account is Frozen.", Toast.LENGTH_LONG).show();
            } else if(mAdminModDetails.adminPost % 10 == 0){
                Toast.makeText(this,
                        "Due to on going server maintenence, your adminship is temporarily disabled.",
                        Toast.LENGTH_LONG).show();
            } else {
                goToAdminPanel();
            }
        }
    }

    private boolean isAccountFreezed(){
        if(mSharedPreferences.contains(IS_ACCOUNT_FREEZED_KEY)){
            return mSharedPreferences.getBoolean(IS_ACCOUNT_FREEZED_KEY, false);
        }
        return false;
    }

    private void goToAdminPanel(){
        Intent adminIntent = new Intent(this, AdminPanelActivity.class);
        adminIntent.putExtra(EXTRA_KEY_ADMIN_POST, mAdminModDetails.adminPost);
        startActivity(adminIntent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pro_ic_edit:
                if(isEditFieldVisible){
                    hideEditFields();
                    mtvFirstName.setText(metvFirstName.getText());
                    mtvLastName.setText(metvLastName.getText());
                } else {
                    showEditFields();
                    metvFirstName.setText(mtvFirstName.getText());
                    metvLastName.setText(mtvLastName.getText());
                }

                break;

            case R.id.pro_pro_pic:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select an Image"), RC_BROWSE_IMAGE);
                break;

            case R.id.pro_tvb_save:
                checkAllFieldAndContnue();
                break;

            case R.id.pro_admin_gate:
                checkIfTheUserIsALegitAdmin();
                break;
        }
    }
}
