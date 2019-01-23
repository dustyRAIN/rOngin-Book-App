package com.creation.daguru.ronginbookapp.data;

public class ChatMessagesDetails {
    public String chatMessage;
    public String senderUId;
    public long createTime;

    public ChatMessagesDetails(String chatMessage, String senderUId, long createTime) {
        this.chatMessage = chatMessage;
        this.senderUId = senderUId;
        this.createTime = createTime;
    }

    public ChatMessagesDetails() {
    }
}
