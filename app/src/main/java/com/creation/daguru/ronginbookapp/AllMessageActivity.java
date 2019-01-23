package com.creation.daguru.ronginbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.creation.daguru.ronginbookapp.data.MessageQuickDetails;
import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_LAST_UPDATE_TIME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_MESSAGE;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_UID;

public class AllMessageActivity extends AppCompatActivity implements AllMessageViewAdapter.MessageInfoClickHandler {


    private RecyclerView mRecyclerView;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;

    private ValueEventListener mNewMessageListener;

    private Query mQuery;

    private AllMessageViewAdapter mAdapter;

    private List<MessageQuickDetails> mMessageList;

    private  MessageQuickDetails mQuickDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_message);

        mRecyclerView = findViewById(R.id.all_msg_recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onPause() {
        detachDatabaseReadListener();
        detachLightListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        attachLightListener();
        mMessageList = new ArrayList<>();

        mAdapter = new AllMessageViewAdapter(this, mMessageList, this);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mFirebaseUser.getUid());

        mQuery = databaseReference.orderByChild(DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_LAST_UPDATE_TIME);

        if(mChildEventListener == null){

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mQuickDetails = dataSnapshot.getValue(MessageQuickDetails.class);
                    mQuickDetails.otherUserUId = dataSnapshot.getKey();

                    mMessageList.add(mQuickDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    MessageQuickDetails messageQuickDetails = dataSnapshot.getValue(MessageQuickDetails.class);
                    messageQuickDetails.otherUserUId = dataSnapshot.getKey();
                    int id = getListItemId(messageQuickDetails);

                    mMessageList.set(id, messageQuickDetails);
                    mAdapter.notifyItemChanged(id);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    MessageQuickDetails messageQuickDetails = dataSnapshot.getValue(MessageQuickDetails.class);
                    messageQuickDetails.otherUserUId = dataSnapshot.getKey();
                    int id = getListItemId(messageQuickDetails);

                    mMessageList.remove(id);
                    mAdapter.notifyItemRemoved(id);
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

    private int getListItemId(MessageQuickDetails messageQuickDetails){
        int i = 0;
        for(MessageQuickDetails quickDetails: mMessageList){
            if(quickDetails.otherUserUId.equals(messageQuickDetails.otherUserUId)){
                return i;
            }
            i++;
        }

        return -1;
    }

    private void detachDatabaseReadListener(){
        if(mChildEventListener != null){
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attachLightListener(){
        if(mNewMessageListener == null){
            mNewMessageListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state != 0){
                            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                                    .child(mFirebaseUser.getUid());

                            databaseReference.setValue(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(mFirebaseUser.getUid());

            databaseReference.addValueEventListener(mNewMessageListener);
        }
    }

    private void detachLightListener(){
        if(mNewMessageListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(mFirebaseUser.getUid());
            databaseReference.removeEventListener(mNewMessageListener);

            mNewMessageListener = null;
        }
    }

    private Intent addExtrasToIntent(Intent intent, MessageQuickDetails messageQuickDetails){
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_UID, messageQuickDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME, messageQuickDetails.otherUserName);

        return intent;
    }

    @Override
    public void onMessageInfoClicked(MessageQuickDetails messageQuickDetails) {
        Intent intent = new Intent(this, ChatMessagesActivity.class);
        intent = addExtrasToIntent(intent, messageQuickDetails);
        startActivity(intent);
    }
}
