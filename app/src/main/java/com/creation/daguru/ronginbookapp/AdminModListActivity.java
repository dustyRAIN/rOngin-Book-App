package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.AdminModDetails;
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
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_MOD_LIST_CHILD_ADMIN_NAME;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.ADMIN_POST_MASTER_ADMIN;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_NAME;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_POST;
import static com.creation.daguru.ronginbookapp.Utils.AdminModUtils.EXTRA_KEY_ADMIN_UID;

public class AdminModListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private AdminModViewAdapter mAdapter;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private Query mQuery;

    private List<AdminModDetails> mModList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mod_list);

        mRecyclerView = findViewById(R.id.admin_mod_recycler_view);

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
        mModList = new ArrayList<>();

        mAdapter = new AdminModViewAdapter(this, mModList);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_MOD_LIST);

        mQuery = databaseReference.orderByChild(DATABASE_DIR_ADMIN_MOD_LIST_CHILD_ADMIN_NAME);

        if(mChildEventListener == null){

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    AdminModDetails modDetails = dataSnapshot.getValue(AdminModDetails.class);
                    if(modDetails.adminPost != ADMIN_POST_MASTER_ADMIN){
                        mModList.add(modDetails);
                        mAdapter.notifyDataSetChanged();
                        Log.d("fofo", "i am numb");
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

    private Intent addExtrasToIntent(Intent intent, AdminModDetails modDetails){
        intent.putExtra(EXTRA_KEY_ADMIN_NAME, modDetails.adminName);
        intent.putExtra(EXTRA_KEY_ADMIN_PHOTO_URL, modDetails.adminPhotoUrl);
        intent.putExtra(EXTRA_KEY_ADMIN_UID, modDetails.adminUId);
        intent.putExtra(EXTRA_KEY_ADMIN_POST, modDetails.adminPost);

        return intent;
    }

    private void onClickModerator(AdminModDetails modDetails){
        Intent intent = new Intent(this, AdminModWorkActivity.class);
        intent = addExtrasToIntent(intent, modDetails);
        startActivity(intent);
    }











    private class AdminModViewAdapter extends RecyclerView.Adapter<AdminModViewAdapter.ModViewHolder> {

        private Context mContext;
        private List<AdminModDetails> mModList;

        public AdminModViewAdapter(Context mContext, List<AdminModDetails> mModList) {
            this.mContext = mContext;
            this.mModList = mModList;
        }

        @NonNull
        @Override
        public ModViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_user_list_item, parent, false);
            return new ModViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ModViewHolder holder, int position) {
            AdminModDetails adminModDetails = mModList.get(position);
            holder.userName.setText(adminModDetails.adminName);
        }

        @Override
        public int getItemCount() {
            if(mModList == null) return 0;
            return mModList.size();
        }

        public class ModViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView userPic;
            final TextView userName;

            public ModViewHolder(View itemView) {
                super(itemView);

                userPic = itemView.findViewById(R.id.admin_user_pic);
                userName = itemView.findViewById(R.id.admin_user_name);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                onClickModerator(mModList.get(id));
            }
        }
    }
}
