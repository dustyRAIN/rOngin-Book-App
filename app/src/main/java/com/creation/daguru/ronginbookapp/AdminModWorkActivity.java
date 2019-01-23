package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.AdminModDetails;
import com.creation.daguru.ronginbookapp.data.ModWorkDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_MOD_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_MOD_LIST_CHILD_ADMIN_POST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_WORK_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_WORK_LIST_WORK_TIME;
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
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.getWorkShortDescription;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.isTimeValid;

public class AdminModWorkActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private AdminModWorkViewAdapter mAdapter;

    private TextView mtvbRemove;
    private TextView mtvbDisable;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private AdminModDetails mAdminModDetails;

    private List<ModWorkDetails> mList;

    private boolean mIsButtonEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mod_work);

        mAdminModDetails = new AdminModDetails();

        checkForIntentExtras();

        mRecyclerView = findViewById(R.id.admin_mod_work_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mtvbRemove = findViewById(R.id.admin_mod_remove);
        mtvbDisable = findViewById(R.id.admin_mod_able);

        mtvbDisable.setOnClickListener(this);
        mtvbRemove.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mIsButtonEnabled = false;

        setUpUI();
    }

    @Override
    protected void onResume() {
        mList = new ArrayList<>();
        mAdapter = new AdminModWorkViewAdapter(this, mList);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachDatabaseListener();
        super.onPause();
    }

    private void setUpUI(){
        if(mAdminModDetails.adminPost % 10 == 0){
            activeEnableButton();
        }
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_WORK_LIST)
                .child(mFirebaseUser.getUid());

        mQuery = databaseReference.orderByChild(DATABASE_DIR_ADMIN_WORK_LIST_WORK_TIME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    ModWorkDetails modWorkDetails = dataSnapshot.getValue(ModWorkDetails.class);
                    mList.add(modWorkDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mQuery.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseListener(){
        if(mChildEventListener != null){
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_NAME)){
            mAdminModDetails.adminName = getIntent().getStringExtra(EXTRA_KEY_ADMIN_NAME);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_PHOTO_URL)){
            mAdminModDetails.adminPhotoUrl = getIntent().getStringExtra(EXTRA_KEY_ADMIN_PHOTO_URL);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_UID)){
            mAdminModDetails.adminUId = getIntent().getStringExtra(EXTRA_KEY_ADMIN_UID);
        }
        if(getIntent().hasExtra(EXTRA_KEY_ADMIN_POST)){
            mAdminModDetails.adminPost = getIntent().getIntExtra(EXTRA_KEY_ADMIN_POST, 0);
        }
    }

    private void activeEnableButton(){
        mtvbDisable.setBackground(getResources().getDrawable(R.color.colorPositive));
        mtvbDisable.setText("Enable");
        mIsButtonEnabled = true;
    }

    private void activeDisableButton(){
        mtvbDisable.setBackground(getResources().getDrawable(R.color.colorPerfectRonginRed));
        mtvbDisable.setText("Disable");
        mIsButtonEnabled = false;
    }

    private void enableModerator(boolean choice){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_MOD_LIST)
                .child(mAdminModDetails.adminUId).child(DATABASE_DIR_ADMIN_MOD_LIST_CHILD_ADMIN_POST);

        if(choice){
            if(mAdminModDetails.adminPost % 10 == 0){
                mAdminModDetails.adminPost /= 10;
                databaseReference.setValue(mAdminModDetails.adminPost);
            }
        } else {
            if(mAdminModDetails.adminPost % 10 != 0){
                mAdminModDetails.adminPost *= 10;
                databaseReference.setValue(mAdminModDetails.adminPost);
            }
        }
    }


    private Intent addExtrasToIntent(Intent intent, ModWorkDetails modWorkDetails){
        intent.putExtra(EXTRA_KEY_ADMIN_WORK_TYPE, modWorkDetails.workType);
        intent.putExtra(EXTRA_KEY_ADMIN_WORK_UID, modWorkDetails.workUId);
        intent.putExtra(EXTRA_KEY_ADMIN_BOOK_LATEST, modWorkDetails.bookName1);
        intent.putExtra(EXTRA_KEY_ADMIN_AUTHOR_LATEST, modWorkDetails.authorName1);
        intent.putExtra(EXTRA_KEY_ADMIN_BOOK_OLD, modWorkDetails.bookName2);
        intent.putExtra(EXTRA_KEY_ADMIN_AUTHOR_OLD, modWorkDetails.authorName2);
        intent.putExtra(EXTRA_KEY_ADMIN_BOOK_UID, modWorkDetails.bookUId);
        intent.putExtra(EXTRA_KEY_ADMIN_OTHER_USER_UID, modWorkDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_ADMIN_OTHER_USER_NAME, modWorkDetails.otherUserName);
        intent.putExtra(EXTRA_KEY_ADMIN_REPLY_TYPE, modWorkDetails.replyType);
        intent.putExtra(EXTRA_KEY_ADMIN_WORK_TIME, modWorkDetails.workTime);

        return intent;
    }

    private void onClickWork(ModWorkDetails modWorkDetails){
        Intent intent = new Intent(this, AdminGeneralWorkDetailsActivity.class);
        intent = addExtrasToIntent(intent, modWorkDetails);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.admin_mod_remove:
                break;

            case R.id.admin_mod_able:
                if(mIsButtonEnabled){
                    activeDisableButton();
                    enableModerator(true);
                } else {
                    activeEnableButton();
                    enableModerator(false);
                }
                break;
        }
    }








    private class AdminModWorkViewAdapter extends RecyclerView.Adapter<AdminModWorkViewAdapter.ModWorkViewHolder> {

        private Context mContext;
        private List<ModWorkDetails> mList;

        public AdminModWorkViewAdapter(Context mContext, List<ModWorkDetails> mList) {
            this.mContext = mContext;
            this.mList = mList;
        }

        @NonNull
        @Override
        public ModWorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_mod_work_list_item, parent, false);
            return new ModWorkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ModWorkViewHolder holder, int position) {
            ModWorkDetails modWorkDetails = mList.get(position);
            holder.workTime.setText(getFriendlyDateString(mContext,
                    getLocalDateFromUTC(modWorkDetails.workTime),
                    true));
            holder.workDescripption.setText(getWorkShortDescription(modWorkDetails));
        }

        @Override
        public int getItemCount() {
            if(mList == null) return 0;
            return mList.size();
        }

        public class ModWorkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView workTime;
            final TextView workDescripption;

            public ModWorkViewHolder(View itemView) {
                super(itemView);

                workTime = itemView.findViewById(R.id.admin_mod_work_time);
                workDescripption = itemView.findViewById(R.id.admin_mod_work_description);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                onClickWork(mList.get(id));
            }
        }
    }
}
