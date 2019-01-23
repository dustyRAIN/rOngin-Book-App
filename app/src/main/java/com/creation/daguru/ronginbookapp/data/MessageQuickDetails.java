package com.creation.daguru.ronginbookapp.data;

public class MessageQuickDetails {

    public String lastMessage;
    public String otherUserName;
    public String otherUserUId;
    public long lastUpdateTime;
    public int isRead;
    public int isBlocked;
    public int showNumber;

    public MessageQuickDetails(String lastMessage, String otherUserName, String otherUserUId, long lastUpdateTime,
                               int isRead, int isBlocked, int showNumber) {
        this.lastMessage = lastMessage;
        this.otherUserName = otherUserName;
        this.otherUserUId = otherUserUId;
        this.lastUpdateTime = lastUpdateTime;
        this.isRead = isRead;
        this.isBlocked = isBlocked;
        this.showNumber = showNumber;
    }

    public MessageQuickDetails() {
    }
}
