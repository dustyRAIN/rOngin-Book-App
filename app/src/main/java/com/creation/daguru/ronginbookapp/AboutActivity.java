package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MANAGER_EMAIL_1 = "mhmmrm@gmail.com";
    private static final String MANAGER_EMAIL_2 = "plabonkumar112@gmail.com";
    private static final String MANAGER_EMAIL_3 = "rajpartho007@gmail.com";
    private static final String MANAGER_EMAIL_4 = "guru.ananda.gayen@gmail.com";
    private static final String MANAGER_FB_1 = "mhmmrm";
    private static final String MANAGER_FB_2 = "plabonmeans.plabon.9";
    private static final String MANAGER_FB_3 = "Rajpartho";
    private static final String RONGIN_FB = "dontwastehappiness";
    private static final String DEVELOPER_EMAIL_1 = "guru.ananda.gayen@gmail.com";

    private ImageView mivFbRongin;
    private ImageView mivEmail1;
    private ImageView mivEmail2;
    private ImageView mivEmail3;
    private ImageView mivEmail4;
    private ImageView mivFbId1;
    private ImageView mivFbId2;
    private ImageView mivFbId3;
    private ImageView mivEmailDeveloper;

    private TextView mtvFbRongin;
    private TextView mtvEmailDeveloper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mivFbRongin = findViewById(R.id.img_ic_facebook);
        mivEmail1 = findViewById(R.id.manage_email_1);
        mivEmail2 = findViewById(R.id.manage_email_2);
        mivEmail3 = findViewById(R.id.manage_email_3);
        mivEmail4 = findViewById(R.id.manage_email_4);
        mivFbId1 = findViewById(R.id.manage_fb_1);
        mivFbId2 = findViewById(R.id.manage_fb_2);
        mivFbId3 = findViewById(R.id.manage_fb_3);
        mivEmailDeveloper = findViewById(R.id.programmer_email_1);

        mtvFbRongin = findViewById(R.id.fb_link);
        mtvEmailDeveloper = findViewById(R.id.programmer_email_name_1);

        mivFbRongin.setOnClickListener(this);
        mivEmail1.setOnClickListener(this);
        mivEmail2.setOnClickListener(this);
        mivEmail3.setOnClickListener(this);
        mivEmail4.setOnClickListener(this);
        mivFbId1.setOnClickListener(this);
        mivFbId2.setOnClickListener(this);
        mivFbId3.setOnClickListener(this);
        mivEmailDeveloper.setOnClickListener(this);
        mtvFbRongin.setOnClickListener(this);
        mtvEmailDeveloper.setOnClickListener(this);
    }

    private Intent getOpenFacebookIntent(String userName) {

        try {
            getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + userName + "/"));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + userName + "/"));
        }
    }

    private void openFBPage(String userName){
        Intent intent = getOpenFacebookIntent(userName);
        startActivity(intent);
    }

    private void sendEmail(String email){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
        startActivity(Intent.createChooser(emailIntent, "Send mail"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_ic_facebook:
            case R.id.fb_link:
                openFBPage(RONGIN_FB);
                break;

            case R.id.manage_email_1:
                sendEmail(MANAGER_EMAIL_1);
                break;

            case R.id.manage_email_2:
                sendEmail(MANAGER_EMAIL_2);
                break;

            case R.id.manage_email_3:
                sendEmail(MANAGER_EMAIL_3);
                break;

            case R.id.manage_email_4:
                sendEmail(MANAGER_EMAIL_4);
                break;

            case R.id.manage_fb_1:
                openFBPage(MANAGER_FB_1);
                break;

            case R.id.manage_fb_2:
                openFBPage(MANAGER_FB_2);
                break;

            case R.id.manage_fb_3:
                openFBPage(MANAGER_FB_3);
                break;

            case R.id.programmer_email_1:
            case R.id.programmer_email_name_1:
                sendEmail(DEVELOPER_EMAIL_1);
                break;
        }
    }
}
