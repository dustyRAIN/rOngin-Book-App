package com.creation.daguru.ronginbookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.ChatMessagesAdapterDetails;
import com.creation.daguru.ronginbookapp.data.ChatMessagesDetails;
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
import java.util.Locale;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_IS_BLOCKED;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_LAST_MESSAGE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_SHOW_NUMBER;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_MESSAGE_INFORMATION_IS_READ;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_CHAT_MESSAGES;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_CHAT_MESSAGES_CHILD_CREATE_TIME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_MESSAGE;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_NAME;
import static com.creation.daguru.ronginbookapp.AllExtaKeys.EXTRA_KEY_CHAT_OTHER_USER_UID;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class AdminChatMessagesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChaterBal";

    private static final int MESSAGE_LENGTH_LIMIT = 1000;

    private static final int REQUEST_PHONE_CALL = 78;

    private String mOtherUserUId;
    private String mOtherUserName;

    private int mOtherUserRead;

    private Toast mToast;

    private ImageView mivSenderProPic;
    private TextView mtvSenderName;
    private EditText metvTypeMessage;
    private TextView mtvbSendButton;
    private TextView mtvSeen;

    private ConstraintLayout mDropDownArea;
    private TextView mtvPhoneNumber;
    private TextView mtvbShowNumber;
    private ImageView mivOpenMap;
    private TextView mtvbBlockButton;
    private ImageView mivDropDown;
    private ImageView mivDropUp;

    private RecyclerView mRecyclerView;
    private AdminChatMessagesViewAdapter mAdapter;

    private List<ChatMessagesAdapterDetails> mMessagesList;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mOtherUserReadReference;
    private DatabaseReference mThisUserReadReference;
    private DatabaseReference mThisUserBlockReference;
    private DatabaseReference mOtherUserBlockReference;
    private DatabaseReference mThisUserShowNumberReference;
    private DatabaseReference mOtherUserShowNumberReference;
    private FirebaseUser mFirebaseUser;
    private Query mQuery;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mOtherUserReadListener;
    private ValueEventListener mThisUserReadListener;
    private ValueEventListener mThisUserBlockListener;
    private ValueEventListener mOtherUserBlockListener;
    private ValueEventListener mThisUserShowNumberListener;
    private ValueEventListener mOtherUserShowNumberListener;


    private String mCurrentTypedMessage;
    private boolean mIsThisUserBlocked;
    private boolean mIsOtherUserBlocked;
    private boolean mThisUserShowNumber;
    private boolean mOtherUserShowNumber;

    private UserBasicInfo mOtherUserBasicInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_messages);

        checkForIntentExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getOtherUserBasicInfo();

        mivSenderProPic = findViewById(R.id.chat_sender_pro_pic);
        mtvSenderName = findViewById(R.id.chat_sender_name);
        metvTypeMessage = findViewById(R.id.chat_type_message_edit);
        mtvbSendButton = findViewById(R.id.chat_send_button);
        mtvSeen = findViewById(R.id.chat_seen);

        mDropDownArea = findViewById(R.id.chat_drop_down);
        mtvPhoneNumber = findViewById(R.id.chat_phone_number);
        mtvbShowNumber = findViewById(R.id.chat_show_number);
        mivOpenMap = findViewById(R.id.iv_ic_open_map);
        mtvbBlockButton = findViewById(R.id.chat_block_button);
        mivDropDown = findViewById(R.id.chat_arrow_down);
        mivDropUp = findViewById(R.id.chat_arrow_up);

        mtvPhoneNumber.setOnClickListener(this);
        mtvbShowNumber.setOnClickListener(this);
        mivOpenMap.setOnClickListener(this);
        mtvbBlockButton.setOnClickListener(this);
        mivDropDown.setOnClickListener(this);
        mivDropUp.setOnClickListener(this);

        mIsThisUserBlocked = true;
        mIsOtherUserBlocked = true;
        mThisUserShowNumber = false;
        mOtherUserShowNumber = false;

        mtvbSendButton.setEnabled(false);

        addTextConstraints();

        mOtherUserReadReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mOtherUserUId).child(RONGIN_UID)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_IS_READ);

        mThisUserReadReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID).child(mOtherUserUId)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_IS_READ);

        mThisUserBlockReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID).child(mOtherUserUId)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_IS_BLOCKED);

        mOtherUserBlockReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mOtherUserUId).child(RONGIN_UID)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_IS_BLOCKED);

        mThisUserShowNumberReference = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID).child(mOtherUserUId)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_SHOW_NUMBER);


        mRecyclerView = findViewById(R.id.chat_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mtvbSendButton.setOnClickListener(this);

        setUpUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMessagesList = new ArrayList<>();
        mAdapter = new AdminChatMessagesViewAdapter(this, mMessagesList);
        mRecyclerView.setAdapter(mAdapter);

        attatchChatChangeListener();
        attatchOtherUserReadListener();
        attatchThisUserReadListener();
        attatchBlockAndNumberListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detatchAllListeners();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mOtherUserBasicInfo.phoneNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Permission denied to make call.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void getOtherUserBasicInfo(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(mOtherUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOtherUserBasicInfo = dataSnapshot.getValue(UserBasicInfo.class);
                mOtherUserName = mOtherUserBasicInfo.firstName + " " + mOtherUserBasicInfo.lastName;
                mtvSenderName.setText(mOtherUserName);
                setUpOtherUserPic();
                mOtherUserShowNumber = true;
                mtvPhoneNumber.setText(mOtherUserBasicInfo.phoneNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpOtherUserPic(){
        String photoUrl = mOtherUserBasicInfo.photoUrl;
        if(photoUrl != null && !photoUrl.equals("") && !photoUrl.isEmpty()) {

            Log.d("PhotoUrl", photoUrl);

            try {
                Glide.with(AdminChatMessagesActivity.this).load(photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mivSenderProPic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpUI(){
        mtvSenderName.setText(mOtherUserName);
        arrowUpClicked();
    }

    private void addTextConstraints(){
        metvTypeMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().length() > 0) {
                    mtvbSendButton.setEnabled(true);
                } else {
                    mtvbSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        metvTypeMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MESSAGE_LENGTH_LIMIT)});
    }



    private void attatchChatChangeListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_CHAT_MESSAGES)
                .child(RONGIN_UID).child(mOtherUserUId);

        mQuery = databaseReference.orderByChild(DATABASE_DIR_CHAT_MESSAGES_CHILD_CREATE_TIME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    ChatMessagesDetails chatMessagesDetails = dataSnapshot.getValue(ChatMessagesDetails.class);
                    ChatMessagesAdapterDetails messagesAdapterDetails = new ChatMessagesAdapterDetails(
                            chatMessagesDetails.chatMessage,
                            chatMessagesDetails.senderUId,
                            chatMessagesDetails.createTime,
                            1
                    );

                    if(chatMessagesDetails.senderUId.equals(RONGIN_UID)){
                        messagesAdapterDetails.messageOrigin = 1;
                    } else {
                        messagesAdapterDetails.messageOrigin = 0;
                        mtvSeen.setVisibility(View.GONE);
                    }

                    mMessagesList.add(messagesAdapterDetails);
                    mAdapter.notifyDataSetChanged();

                    int position = mMessagesList.size()-1;
                    mRecyclerView.scrollToPosition(position);
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

    private void attatchOtherUserReadListener(){

        if(mOtherUserReadListener == null){
            mOtherUserReadListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mOtherUserRead = dataSnapshot.getValue(Integer.class);
                    if(mOtherUserRead == 1){
                        if(mMessagesList != null && mMessagesList.size()>0) {
                            int size = mMessagesList.size();
                            ChatMessagesAdapterDetails chatMessagesDetails = mMessagesList.get(size-1);
                            Log.d(TAG, String.valueOf(size) + "  " + chatMessagesDetails.chatMessage);
                            if (chatMessagesDetails != null) {
                                if (chatMessagesDetails.messageOrigin == 1) {
                                    mtvSeen.setVisibility(View.VISIBLE);
                                } else {
                                    mtvSeen.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            mtvSeen.setVisibility(View.GONE);
                        }
                    } else {
                        mtvSeen.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mOtherUserReadReference.addValueEventListener(mOtherUserReadListener);
        }
    }

    private void attatchThisUserReadListener(){

        if(mThisUserReadListener == null){
            mThisUserReadListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int isRead = dataSnapshot.getValue(Integer.class);
                    if(isRead == 0){
                        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                                .child(RONGIN_UID).child(mOtherUserUId)
                                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_IS_READ);

                        dbRef.setValue(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mThisUserReadReference.addValueEventListener(mThisUserReadListener);
        }
    }

    private void attatchBlockAndNumberListener(){
        if(mThisUserBlockListener == null){
            mThisUserBlockListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int isBlocked = dataSnapshot.getValue(Integer.class);
                    mIsThisUserBlocked = isBlocked == 1;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mThisUserBlockReference.addValueEventListener(mThisUserBlockListener);
        }

        if(mOtherUserBlockListener == null){
            mOtherUserBlockListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int isBlocked = dataSnapshot.getValue(Integer.class);
                    if(isBlocked == 1){
                        mIsOtherUserBlocked = true;
                        mtvbBlockButton.setText("Unblock");
                        Log.d("huhu", "damn");
                    } else {
                        mIsOtherUserBlocked = false;
                        mtvbBlockButton.setText("Block");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mOtherUserBlockReference.addValueEventListener(mOtherUserBlockListener);
        }

        if(mThisUserShowNumberListener == null){
            mThisUserShowNumberListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int showNumber = dataSnapshot.getValue(Integer.class);
                    if(showNumber == 1){
                        mThisUserShowNumber = true;
                        mtvbShowNumber.setText("Hide Your Number");
                    } else {
                        mThisUserShowNumber = false;
                        mtvbShowNumber.setText("Show Your Number");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mThisUserShowNumberReference.addValueEventListener(mThisUserShowNumberListener);
        }
    }


    private void detatchAllListeners(){
        if(mChildEventListener != null){
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

        if(mOtherUserReadListener != null){
            mOtherUserReadReference.removeEventListener(mOtherUserReadListener);
            mOtherUserReadListener = null;
        }

        if(mThisUserReadListener != null){
            mThisUserReadReference.removeEventListener(mThisUserReadListener);
            mThisUserReadListener = null;
        }

        if(mThisUserBlockListener != null){
            mThisUserBlockReference.removeEventListener(mThisUserBlockListener);
        }

        if(mOtherUserBlockListener != null){
            mOtherUserBlockReference.removeEventListener(mOtherUserBlockListener);
        }

        if(mThisUserShowNumberListener != null){
            mThisUserShowNumberReference.removeEventListener(mThisUserShowNumberListener);
        }
    }



    private void checkForIntentExtras(){
        if(getIntent().hasExtra(EXTRA_KEY_CHAT_OTHER_USER_UID)){
            mOtherUserUId = getIntent().getStringExtra(EXTRA_KEY_CHAT_OTHER_USER_UID);
        } else {
            finish();
        }

        if(getIntent().hasExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME)){
            mOtherUserName = getIntent().getStringExtra(EXTRA_KEY_CHAT_OTHER_USER_NAME);
        } else {
            finish();
        }
    }


    private void callTheUser(){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mOtherUserBasicInfo.phoneNumber));
        if (ContextCompat.checkSelfPermission(AdminChatMessagesActivity.this,
                android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AdminChatMessagesActivity.this,
                    new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        }
        else
        {
            startActivity(intent);
        }
    }

    private void preProcessMessage(){
        mCurrentTypedMessage = metvTypeMessage.getText().toString();
        metvTypeMessage.setText("");
    }

    private void sendMessage() {

        DatabaseReference otherChatReference = mDatabaseReference.child(DATABASE_DIR_CHAT_MESSAGES)
                .child(mOtherUserUId).child(RONGIN_UID);

        DatabaseReference thisChatReference = mDatabaseReference.child(DATABASE_DIR_CHAT_MESSAGES)
                .child(RONGIN_UID).child(mOtherUserUId);

        long currentTime = getUTCDateFromLocal(System.currentTimeMillis());

        ChatMessagesDetails chatMessagesDetails = new ChatMessagesDetails(
                mCurrentTypedMessage,
                RONGIN_UID,
                currentTime
        );

        otherChatReference.push().setValue(chatMessagesDetails);
        thisChatReference.push().setValue(chatMessagesDetails);
        mOtherUserReadReference.setValue(0);

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_NEW_MESSAGE)
                .child(mOtherUserUId);
        databaseReference.setValue(1);

        mtvSeen.setVisibility(View.GONE);

        updateLastMessage();
    }

    private void updateLastMessage(){
        DatabaseReference dbRefThis = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(RONGIN_UID).child(mOtherUserUId)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_LAST_MESSAGE);

        DatabaseReference dbRefOther = mDatabaseReference.child(DATABASE_DIR_ALL_MESSAGE_INFORMATION)
                .child(mOtherUserUId).child(RONGIN_UID)
                .child(DATABASE_DIR_ALL_MESSAGE_INFORMATION_CHILD_LAST_MESSAGE);

        dbRefThis.setValue(mCurrentTypedMessage);
        dbRefOther.setValue(mCurrentTypedMessage);
    }

    private void showOrHideYourNumber(){
        if(mThisUserShowNumber){
            mThisUserShowNumberReference.setValue(0);
        } else {
            mThisUserShowNumberReference.setValue(1);
        }
    }

    private void blockOrUnblockOtherUser(){
        if(mIsOtherUserBlocked){
            mOtherUserBlockReference.setValue(0);
        } else {
            mOtherUserBlockReference.setValue(1);
        }
    }

    private void arrowDownClicked(){
        mivDropDown.setVisibility(View.GONE);
        mivDropUp.setVisibility(View.VISIBLE);
        mDropDownArea.setVisibility(View.VISIBLE);
    }

    private void arrowUpClicked(){
        mivDropDown.setVisibility(View.VISIBLE);
        mivDropUp.setVisibility(View.GONE);
        mDropDownArea.setVisibility(View.GONE);
    }

    private void showLoacationOnMap(){
        if(mOtherUserBasicInfo != null){
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mOtherUserBasicInfo.latitude,
                    mOtherUserBasicInfo.longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
    }

    private void decideToCallTheNumber(){
        callTheUser();
    }

    public void onMessageClicked(ChatMessagesAdapterDetails chatMessagesAdapterDetails) {
        if(mToast != null){
            mToast.cancel();
        }
        String time = getFriendlyDateString(this,
                getLocalDateFromUTC(chatMessagesAdapterDetails.createTime), true);

        mToast = Toast.makeText(this, time, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chat_send_button:
                preProcessMessage();
                sendMessage();
                break;

            case R.id.chat_phone_number:
                decideToCallTheNumber();
                break;

            case R.id.chat_show_number:
                showOrHideYourNumber();
                break;

            case R.id.iv_ic_open_map:
                showLoacationOnMap();
                break;

            case R.id.chat_block_button:
                blockOrUnblockOtherUser();
                break;

            case R.id.chat_arrow_down:
                arrowDownClicked();
                break;

            case R.id.chat_arrow_up:
                arrowUpClicked();
                break;
        }
    }

















    public class AdminChatMessagesViewAdapter extends RecyclerView.Adapter<AdminChatMessagesViewAdapter.MessagesViewHolder> {

        private Context mContext;
        private List<ChatMessagesAdapterDetails> mMessagesList;

        public AdminChatMessagesViewAdapter(Context mContext, List<ChatMessagesAdapterDetails> mMessagesList) {
            this.mContext = mContext;
            this.mMessagesList = mMessagesList;
        }

        @NonNull
        @Override
        public AdminChatMessagesViewAdapter.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.admin_chat_message_list_item, parent,
                    false);
            return new MessagesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdminChatMessagesViewAdapter.MessagesViewHolder holder, int position) {
            ChatMessagesAdapterDetails messagesDetails = mMessagesList.get(position);

            if (messagesDetails.messageOrigin == 1) {
                holder.chatInMessage.setVisibility(View.GONE);
                holder.chatOutMessage.setVisibility(View.VISIBLE);

                holder.chatOutMessage.setText(messagesDetails.chatMessage);
            } else {
                holder.chatOutMessage.setVisibility(View.GONE);
                holder.chatInMessage.setVisibility(View.VISIBLE);

                holder.chatInMessage.setText(messagesDetails.chatMessage);
            }
        }

        @Override
        public int getItemCount() {
            if (mMessagesList == null) return 0;
            return mMessagesList.size();
        }

        public class MessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final TextView chatInMessage;
            final TextView chatOutMessage;

            public MessagesViewHolder(View itemView) {
                super(itemView);

                chatInMessage = itemView.findViewById(R.id.chat_in_message);
                chatOutMessage = itemView.findViewById(R.id.chat_out_message);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = getAdapterPosition();
                onMessageClicked(mMessagesList.get(id));
            }
        }
    }
}
