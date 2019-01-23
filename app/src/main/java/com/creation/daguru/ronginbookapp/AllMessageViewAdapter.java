package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.creation.daguru.ronginbookapp.data.MessageQuickDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;

public class AllMessageViewAdapter extends RecyclerView.Adapter<AllMessageViewAdapter.MessageViewHolder> {



    private Context mContext;
    private List<MessageQuickDetails> mMessageList;
    private MessageInfoClickHandler mOnClickHandler;

    private MessageViewHolder mMessageViewHolder;

    private int holderCounter;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private interface FirebaseCallback{
        void dataReady(String photoUrl, MessageViewHolder holder);
    }

    public interface MessageInfoClickHandler{
        void onMessageInfoClicked(MessageQuickDetails messageQuickDetails);
    }

    public AllMessageViewAdapter(Context context, List<MessageQuickDetails> messageQuickDetails,
                                 MessageInfoClickHandler messageInfoClickHandler) {
        mContext = context;
        mMessageList = messageQuickDetails;
        mOnClickHandler = messageInfoClickHandler;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        holderCounter = 0;

        View view = LayoutInflater.from(mContext).inflate(R.layout.all_message_list_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageQuickDetails messageQuickDetails = mMessageList.get(position);
        mMessageViewHolder = holder;

        readData(new FirebaseCallback() {
            @Override
            public void dataReady(String photoUrl, MessageViewHolder hlder) {
                try {
                    Glide.with(mContext).load(photoUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(hlder.userPic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, messageQuickDetails, holder);



        if(messageQuickDetails.isRead == 0){
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorRecViewBG));
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

    private void readData(final FirebaseCallback firebaseCallback, MessageQuickDetails messageQuickDetails,
                          final MessageViewHolder holder) {
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BASIC_INFO)
                .child(messageQuickDetails.otherUserUId).child(DATABASE_DIR_USER_BASIC_INFO_CHILD_PHOTO_URL);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String photoUrl = dataSnapshot.getValue(String.class);
                    if(photoUrl != null && !photoUrl.equals("") && !photoUrl.isEmpty()) {

                        Log.d("PhotoUrl", photoUrl);

                        firebaseCallback.dataReady(photoUrl, holder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if(mMessageList == null) return 0;
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView userName;
        final TextView boldUserName;
        final TextView messageTime;
        final TextView boldMessageTime;
        final TextView lastMessage;
        final TextView boldLastMessage;
        final ImageView userPic;

        public MessageViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.all_msg_name);
            boldUserName = itemView.findViewById(R.id.all_msg_name_bold);
            messageTime = itemView.findViewById(R.id.msg_time);
            boldMessageTime = itemView.findViewById(R.id.msg_time_bold);
            lastMessage = itemView.findViewById(R.id.all_msg_last_msg);
            boldLastMessage = itemView.findViewById(R.id.all_msg_last_msg_bold);
            userPic = itemView.findViewById(R.id.all_msg_pro_pic);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            MessageQuickDetails messageQuickDetails = mMessageList.get(position);
            mOnClickHandler.onMessageInfoClicked(messageQuickDetails);
        }
    }
}
