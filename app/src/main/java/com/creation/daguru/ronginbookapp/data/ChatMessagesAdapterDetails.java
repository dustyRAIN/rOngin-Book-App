package com.creation.daguru.ronginbookapp.data;

public class ChatMessagesAdapterDetails {
    public String chatMessage;
    public String senderUId;
    public long createTime;
    public int messageOrigin;

    public ChatMessagesAdapterDetails() {
    }

    public ChatMessagesAdapterDetails(String chatMessage, String senderUId, long createTime, int messageOrigin) {
        this.chatMessage = chatMessage;
        this.senderUId = senderUId;
        this.createTime = createTime;
        this.messageOrigin = messageOrigin;
    }
}
