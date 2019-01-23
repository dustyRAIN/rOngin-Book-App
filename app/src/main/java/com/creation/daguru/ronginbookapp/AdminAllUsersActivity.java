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
import android.widget.ImageView;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.ListUserBasicInfo;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
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

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_NAME_FIRST_NAME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_EMAIL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_LATITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_LONGITUDE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_PHONE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_USER_UID;

public class AdminAllUsersActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private AdminUserViewAdapter mAdapter;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private List<ListUserBasicInfo> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_users);

        mRecyclerView = findViewById(R.id.admin_user_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onPause() {
        detachDatabaseReadListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mUserList = new ArrayList<>();

        mAdapter = new AdminUserViewAdapter(this, mUserList);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO);

        mQuery = databaseReference.orderByChild(DATABASE_DIR_USER_BASIC_INFO_NAME_FIRST_NAME);

        if(mChildEventListener == null){

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserBasicInfo userBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                    if(!dataSnapshot.getKey().equals(RONGIN_UID)){
                        mUserList.add(new ListUserBasicInfo(
                                userBasicInfo.firstName,
                                userBasicInfo.lastName,
                                userBasicInfo.email,
                                userBasicInfo.phoneNumber,
                                userBasicInfo.photoUrl,
                                dataSnapshot.getKey(),
                                userBasicInfo.latitude,
                                userBasicInfo.longitude
                        ));
                        mAdapter.notifyDataSetChanged();
                    }
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

    private void detachDatabaseReadListener(){
        if(mChildEventListener != null){
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private Intent addExtrasToIntent(Intent intent, ListUserBasicInfo userBasicInfo){
        intent.putExtra(EXTRA_KEY_USER_NAME, userBasicInfo.firstName + " " + userBasicInfo.lastName);
        intent.putExtra(EXTRA_KEY_USER_EMAIL, userBasicInfo.email);
        intent.putExtra(EXTRA_KEY_USER_PHONE, userBasicInfo.phoneNumber);
        intent.putExtra(EXTRA_KEY_USER_PHOTO_URL, userBasicInfo.photoUrl);
        intent.putExtra(EXTRA_KEY_USER_UID, userBasicInfo.userUId);
        intent.putExtra(EXTRA_KEY_USER_LATITUDE, userBasicInfo.latitude);
        intent.putExtra(EXTRA_KEY_USER_LONGITUDE, userBasicInfo.longitude);

        return intent;
    }

    private void onUserClicked(ListUserBasicInfo userBasicInfo){
        Intent intent = new Intent(this, AdminUserDetailsActivity.class);
        intent = addExtrasToIntent(intent, userBasicInfo);
        startActivity(intent);
    }









    private class AdminUserViewAdapter extends RecyclerView.Adapter<AdminUserViewAdapter.UserViewHolder> {

        private Context mContext;
        private List<ListUserBasicInfo> mUserList;

        public AdminUserViewAdapter(Context mContext, List<ListUserBasicInfo> mUserList) {
            this.mContext = mContext;
            this.mUserList = mUserList;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_user_list_item, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            ListUserBasicInfo userBasicInfo = mUserList.get(position);
            holder.userName.setText(userBasicInfo.firstName + " " + userBasicInfo.lastName);
        }

        @Override
        public int getItemCount() {
            if(mUserList == null) return 0;
            return mUserList.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView userPic;
            final TextView userName;

            public UserViewHolder(View itemView) {
                super(itemView);

                userPic = itemView.findViewById(R.id.admin_user_pic);
                userName = itemView.findViewById(R.id.admin_user_name);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                onUserClicked(mUserList.get(id));
            }
        }
    }
}
