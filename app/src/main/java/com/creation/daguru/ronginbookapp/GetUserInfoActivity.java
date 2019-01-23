package com.creation.daguru.ronginbookapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.STORAGE_DIR_PRO_PIC;

public class GetUserInfoActivity extends AppCompatActivity implements
        View.OnClickListener{

    protected static final int RC_BROWSE_IMAGE = 1;
    protected static final int RC_PLACE_PICK = 2;


    private String mFirstName;
    private String mLastName;

    private ConstraintLayout mLoadingLayout;

    private TextView mtvbSelectPhoto;
    private TextView mtvLatitude;
    private TextView mtvLongitude;
    private TextView mtvbContinue;

    private EditText metvFirstName;
    private EditText metvLastName;

    private ImageView mivUserPic;
    private ImageView mivPickLocation;

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseReference;
    private StorageReference mImageStorageReference;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private Uri mSelectedImageUri;
    private Uri mUploadedPhotoUrl;

    private Place mPlace;

    private Bitmap mBitmap;

    private UserProfileChangeRequest mProfileChangeRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_info);

        mLoadingLayout = findViewById(R.id.gi_layout_progress_bar);

        mtvbSelectPhoto = findViewById(R.id.gi_tv_up_button);
        mtvLatitude = findViewById(R.id.gi_tv_latitude);
        mtvLongitude = findViewById(R.id.gi_tv_longitude);
        mtvbContinue = findViewById(R.id.gi_tvb_continue);

        metvFirstName = findViewById(R.id.gi_etv_first_name);
        metvLastName = findViewById(R.id.gi_etv_last_name);

        mivUserPic = findViewById(R.id.gi_iv_pic);
        mivPickLocation = findViewById(R.id.gi_iv_ic_pick_location);

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabaseReference = mDatabase.getReference().child(DATABASE_DIR_USER_BASIC_INFO);
        mImageStorageReference = mStorage.getReference().child(STORAGE_DIR_PRO_PIC);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mtvbSelectPhoto.setOnClickListener(this);
        mtvbContinue.setOnClickListener(this);
        mivPickLocation.setOnClickListener(this);

        mLoadingLayout.setVisibility(View.GONE);
        mtvbContinue.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == RC_BROWSE_IMAGE){
                if(data != null){
                    Uri uri = data.getData();
                    cropImage(uri);
                }
            } else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                if(data != null) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    mSelectedImageUri = result.getUri();
                    setCircularImage(mSelectedImageUri);
                }
            } else if(requestCode == RC_PLACE_PICK){
                mPlace = PlacePicker.getPlace(this, data);
                mtvLatitude.setText(String.valueOf(mPlace.getLatLng().latitude));
                mtvLongitude.setText(String.valueOf(mPlace.getLatLng().longitude));
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

            mivUserPic.setImageDrawable(roundedBitmapDrawable);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkAllFields(){
        if(metvFirstName.getText().toString().trim().equals("") || metvFirstName.getText().toString().trim().isEmpty()){
            return false;
        } else if(mPlace == null){
            return false;
        } else {
            return isNameValid();
        }
    }

    private boolean isNameValid(){
        String firstName = metvFirstName.getText().toString().trim();
        String lastName = metvLastName.getText().toString().trim();

        boolean isOk = true;

        if(firstName.length()>18){
            metvFirstName.setError("Too large");
            isOk = false;
        } else if(isThereAnyOtherChar(firstName)){
            metvFirstName.setError("Only alphabets allowed.");
            isOk = false;
        }
        if(lastName.length()>18){
            metvLastName.setError("Too large");
            isOk = false;
        } else if(isThereAnyOtherChar(lastName)){
            metvLastName.setError("Only alphabets allowed.");
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

    private void setErrorToFields(){
        if(metvFirstName.getText().toString().equals("") || metvFirstName.getText().toString().isEmpty()) {
            metvFirstName.setError("Can not be Empty");
        } else if(mPlace == null){
            Toast.makeText(this, "Please Select a Location. Tap the Map Icon.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveDataAndContinue(){

        mFirstName = metvFirstName.getText().toString().trim();
        mLastName = metvLastName.getText().toString().trim();

        mProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(mFirstName + ' ' + mLastName)
                .build();

        if(mSelectedImageUri != null){
            final StorageReference reference = mImageStorageReference.child(mFirebaseUser.getUid());

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
                                .setDisplayName(mFirstName + ' ' + mLastName)
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
        } else {
            Toast.makeText(this, "Please upload a photo.", Toast.LENGTH_SHORT).show();
            mtvbContinue.setEnabled(true);
        }
    }

    private void updateProfileInfo(){
        mLoadingLayout.setVisibility(View.VISIBLE);

        UserBasicInfo userBasicInfo = new UserBasicInfo(mFirstName, mLastName, mFirebaseUser.getEmail(),
                mFirebaseUser.getPhoneNumber(), mUploadedPhotoUrl.toString(), mPlace.getLatLng().latitude,
                mPlace.getLatLng().longitude);

        mDatabaseReference.child(mFirebaseUser.getUid()).setValue(userBasicInfo);

        mFirebaseUser.updateProfile(mProfileChangeRequest).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("GetUserInfo", "Name and propicurl done");
                        if(task.isSuccessful()) {
                            //Toast.makeText(GetUserInfoActivity.this, "dekhi ki hoy" + mFirebaseUser.getPhotoUrl().toString(), Toast.LENGTH_LONG).show();
                            returnSuccessAndQuit();
                        } else {

                        }
                    }
                });
    }

    private void returnSuccessAndQuit(){
        mLoadingLayout.setVisibility(View.GONE);
        setResult(RESULT_OK);
        finish();
    }









    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.gi_tv_up_button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select an Image"), RC_BROWSE_IMAGE);
                break;

            case R.id.gi_tvb_continue:
                if(checkAllFields()){
                    mtvbContinue.setEnabled(false);
                    saveDataAndContinue();
                } else {
                    setErrorToFields();
                    mtvbContinue.setEnabled(true);
                }
                break;

            case R.id.gi_iv_ic_pick_location:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(this), RC_PLACE_PICK);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
