package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.ChatMessagesAdapterDetails;
import com.creation.daguru.ronginbookapp.data.ChatMessagesDetails;

import java.util.List;

public class ChatMessagesViewAdapter extends RecyclerView.Adapter<ChatMessagesViewAdapter.MessagesViewHolder> {

    private Context mContext;
    private List<ChatMessagesAdapterDetails> mMessagesList;
    private OnMessageClicked mMessageClicked;

    public interface OnMessageClicked{
        void onMessageClicked(ChatMessagesAdapterDetails chatMessagesAdapterDetails);
    }

    public ChatMessagesViewAdapter(Context mContext, List<ChatMessagesAdapterDetails> mMessagesList,
                                   OnMessageClicked onMessageClicked) {
        this.mContext = mContext;
        this.mMessagesList = mMessagesList;
        this.mMessageClicked = onMessageClicked;
    }

    @NonNull
    @Override
    public ChatMessagesViewAdapter.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_messages_list_item, parent, false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessagesViewAdapter.MessagesViewHolder holder, int position) {
        ChatMessagesAdapterDetails messagesDetails = mMessagesList.get(position);

        if(messagesDetails.messageOrigin == 1){
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
        if(mMessagesList == null) return 0;
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
            mMessageClicked.onMessageClicked(mMessagesList.get(id));
        }
    }
}
