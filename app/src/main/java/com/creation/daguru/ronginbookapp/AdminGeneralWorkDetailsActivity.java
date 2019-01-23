package com.creation.daguru.ronginbookapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.ModWorkDetails;

import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_AUTHOR_LATEST;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_AUTHOR_OLD;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_BOOK_LATEST;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_BOOK_OLD;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_BOOK_UID;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_NAME;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_POST;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_REPLY_TYPE;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_UID;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_WORK_TIME;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_WORK_TYPE;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_WORK_UID;

public class AdminGeneralWorkDetailsActivity extends AppCompatActivity {

    private TextView mtvWorkDesc;
    private TextView mtvBook1;
    private TextView mtvAuthor1;
    private TextView mtvBook2;
    private TextView mtvAuthor2;
    private TextView mtvUserName;


    private ModWorkDetails mWorkDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_general_work_details);

        mWorkDetails = new ModWorkDetails();
        checkForIntentExtras();

        mtvWorkDesc = findViewById(R.id.admin_work_desc);
        mtvBook1 = findViewById(R.id.admin_book_latest);
        mtvAuthor1 = findViewById(R.id.admin_author_latest);
        mtvBook2 = findViewById(R.id.admin_book_old);
        mtvAuthor2 = findViewById(R.id.admin_author_old);
        mtvUserName = findViewById(R.id.admin_other_user);

        setUpUI();
    }

    private void setUpUI(){
        if(mWorkDetails.bookName1 != null){
            mtvBook1.setText(mWorkDetails.bookName1);
        }
        if(mWorkDetails.bookName2 != null){
            mtvBook2.setText(mWorkDetails.bookName2);
        }
        if(mWorkDetails.authorName1 != null){
            mtvAuthor1.setText(mWorkDetails.authorName1);
        }
        if(mWorkDetails.authorName2 != null){
            mtvAuthor2.setText(mWorkDetails.authorName2);
        }
        if(mWorkDetails.otherUserName != null){
            mtvUserName.setText(mWorkDetails.otherUserName);
        }
    }





    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_WORK_TYPE)){
            mWorkDetails.workType = getIntent().getIntExtra(EXTRA_KEY_ADMIN_WORK_TYPE, 1);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_WORK_UID)){
            mWorkDetails.workUId = getIntent().getStringExtra(EXTRA_KEY_ADMIN_WORK_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_BOOK_LATEST)){
            mWorkDetails.bookName1 = getIntent().getStringExtra(EXTRA_KEY_ADMIN_BOOK_LATEST);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_AUTHOR_LATEST)){
            mWorkDetails.authorName1 = getIntent().getStringExtra(EXTRA_KEY_ADMIN_AUTHOR_LATEST);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_BOOK_OLD)){
            mWorkDetails.bookName2 = getIntent().getStringExtra(EXTRA_KEY_ADMIN_BOOK_OLD);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_AUTHOR_OLD)){
            mWorkDetails.authorName2 = getIntent().getStringExtra(EXTRA_KEY_ADMIN_AUTHOR_OLD);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_BOOK_UID)){
            mWorkDetails.bookUId = getIntent().getStringExtra(EXTRA_KEY_ADMIN_BOOK_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_OTHER_USER_UID)){
            mWorkDetails.otherUserUId = getIntent().getStringExtra(EXTRA_KEY_ADMIN_OTHER_USER_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_OTHER_USER_NAME)){
            mWorkDetails.otherUserName = getIntent().getStringExtra(EXTRA_KEY_ADMIN_OTHER_USER_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_REPLY_TYPE)){
            mWorkDetails.replyType = getIntent().getIntExtra(EXTRA_KEY_ADMIN_REPLY_TYPE, 1);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_WORK_TIME)){
            mWorkDetails.workTime = getIntent().getLongExtra(EXTRA_KEY_ADMIN_WORK_TIME, 1);
        }
    }
}
