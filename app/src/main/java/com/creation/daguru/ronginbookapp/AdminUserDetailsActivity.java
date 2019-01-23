package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.ListUserBasicInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_EMAIL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_LATITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_LONGITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_PHONE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_UID;

public class AdminUserDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mivUserPic;
    private TextView mtvUserName;
    private TextView mtvUserPhone;
    private TextView mtvUserEmail;
    private TextView mtvUserUid;
    private TextView mtvbOpenLocation;

    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private ListUserBasicInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_details);

        mUserInfo = new ListUserBasicInfo();

        checkForIntentExtras();

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        mivUserPic = findViewById(R.id.admin_user_pic);
        mtvUserName = findViewById(R.id.admin_user_name);
        mtvUserPhone = findViewById(R.id.admin_user_number);
        mtvUserEmail = findViewById(R.id.admin_user_email);
        mtvUserUid = findViewById(R.id.admin_user_uid);
        mtvbOpenLocation = findViewById(R.id.admin_user_address);

        mtvbOpenLocation.setOnClickListener(this);

        setUpUI();
    }

    private void setUpUI(){
        mtvUserName.setText(mUserInfo.firstName);
        mtvUserEmail.setText(mUserInfo.email);
        mtvUserPhone.setText(mUserInfo.phoneNumber);
        mtvUserUid.setText(mUserInfo.userUId);

        if(mUserInfo.photoUrl != null
                && !mUserInfo.photoUrl.toString().equals("")
                && !mUserInfo.photoUrl.toString().isEmpty()) {

            Log.d("PhotoUrl", mUserInfo.photoUrl.toString());

            try {
                Glide.with(AdminUserDetailsActivity.this).load(mUserInfo.photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mivUserPic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_USER_NAME)){
            mUserInfo.firstName = getIntent().getStringExtra(EXTRA_KEY_USER_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_USER_EMAIL)){
            mUserInfo.email = getIntent().getStringExtra(EXTRA_KEY_USER_EMAIL);
        }
        if(getIntent().hasExtra(EXTRA_KEY_USER_PHONE)){
            mUserInfo.phoneNumber = getIntent().getStringExtra(EXTRA_KEY_USER_PHONE);
        }
        if(getIntent().hasExtra(EXTRA_KEY_USER_PHOTO_URL)){
            mUserInfo.photoUrl = getIntent().getStringExtra(EXTRA_KEY_USER_PHOTO_URL);
        }
        if(getIntent().hasExtra(EXTRA_KEY_USER_UID)){
            mUserInfo.userUId = getIntent().getStringExtra(EXTRA_KEY_USER_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_USER_LATITUDE)){
            mUserInfo.latitude = getIntent().getDoubleExtra(EXTRA_KEY_USER_LATITUDE, 0);
        }
        if(getIntent().hasExtra(EXTRA_KEY_USER_LONGITUDE)){
            mUserInfo.longitude = getIntent().getDoubleExtra(EXTRA_KEY_USER_LONGITUDE, 0);
        }
    }

    private void showLocationInMap(){
        if(mUserInfo != null){
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mUserInfo.latitude,
                    mUserInfo.longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        showLocationInMap();
    }
}
