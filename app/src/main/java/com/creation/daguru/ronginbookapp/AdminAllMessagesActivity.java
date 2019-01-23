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
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.MessageQuickDetails;
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
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;

public class AdminAllMessagesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mLightListener;

    private Query mQuery;

    private AdminMessageViewAdapter mAdapter;

    private List<MessageQuickDetails> mMessageList;

    private  MessageQuickDetails mQuickDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_messages);

        mRecyclerView = findViewById(R.id.admin_message_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onPause() {
        detachLightListener();
        detachDatabaseReadListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        attachLightListener();
        mMessageList = new ArrayList<>();

        mAdapter = new AdminMessageViewAdapter(this, mMessageList);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    private void attachLightListener(){
        if(mLightListener == null){
            mLightListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state != 0){
                            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                                    .child(RONGIN_UID);

                            databaseReference.setValue(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(RONGIN_UID);
            databaseReference.addValueEventListener(mLightListener);
        }
    }

    private void detachLightListener(){
        if(mLightListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                    .child(RONGIN_UID);
            databaseReference.removeEventListener(mLightListener);
        }
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID);

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

    private Intent addExtrasToIntent(Intent intent, MessageQuickDetails messageQuickDetails){
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_UID, messageQuickDetails.otherUserUId);
        intent.putExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME, messageQuickDetails.otherUserName);

        return intent;
    }

    private void onMessageInfoClicked(MessageQuickDetails messageQuickDetails) {
        Intent intent = new Intent(this, AdminChatMessagesActivity.class);
        intent = addExtrasToIntent(intent, messageQuickDetails);
        startActivity(intent);
    }







    private class AdminMessageViewAdapter extends RecyclerView.Adapter<AdminMessageViewAdapter.MessageViewHolder> {

        private Context mContext;
        private List<MessageQuickDetails> mMessageList;

        public AdminMessageViewAdapter(Context mContext, List<MessageQuickDetails> mMessageList) {
            this.mContext = mContext;
            this.mMessageList = mMessageList;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_all_message_list_item, parent,
                    false);
            return new AdminMessageViewAdapter.MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            MessageQuickDetails messageQuickDetails = mMessageList.get(position);

            if(messageQuickDetails.isRead == 0){
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
                holder.userName.setVisibility(View.GONE);
                holder.messageTime.setVisibility(View.GONE);
                holder.lastMessage.setVisibility(View.GONE);

                holder.boldUserName.setVisibility(View.VISIBLE);
                holder.boldMessageTime.setVisibility(View.VISIBLE);
                holder.boldLastMessage.setVisibility(View.VISIBLE);

                holder.boldUserName.setText(messageQuickDetails.otherUserName);
                holder.boldMessageTime.setText(getFriendlyDateString(mContext,
                        getLocalDateFromUTC(messageQuickDetails.lastUpdateTime),
                        true));
                holder.boldLastMessage.setText(messageQuickDetails.lastMessage);

            } else {
                holder.userName.setVisibility(View.VISIBLE);
                holder.messageTime.setVisibility(View.VISIBLE);
                holder.lastMessage.setVisibility(View.VISIBLE);

                holder.boldUserName.setVisibility(View.GONE);
                holder.boldMessageTime.setVisibility(View.GONE);
                holder.boldLastMessage.setVisibility(View.GONE);

                holder.userName.setText(messageQuickDetails.otherUserName);
                holder.messageTime.setText(getFriendlyDateString(mContext,
                        getLocalDateFromUTC(messageQuickDetails.lastUpdateTime),
                        true));
                holder.lastMessage.setText(messageQuickDetails.lastMessage);
            }
        }

        @Override
        public int getItemCount() {
            if(mMessageList == null) return 0;
            return mMessageList.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView userName;
            final TextView boldUserName;
            final TextView messageTime;
            final TextView boldMessageTime;
            final TextView lastMessage;
            final TextView boldLastMessage;

            public MessageViewHolder(View itemView) {
                super(itemView);

                userName = itemView.findViewById(R.id.all_msg_name);
                boldUserName = itemView.findViewById(R.id.all_msg_name_bold);
                messageTime = itemView.findViewById(R.id.msg_time);
                boldMessageTime = itemView.findViewById(R.id.msg_time_bold);
                lastMessage = itemView.findViewById(R.id.all_msg_last_msg);
                boldLastMessage = itemView.findViewById(R.id.all_msg_last_msg_bold);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                MessageQuickDetails messageQuickDetails = mMessageList.get(position);
                onMessageInfoClicked(messageQuickDetails);
            }
        }
    }
}
