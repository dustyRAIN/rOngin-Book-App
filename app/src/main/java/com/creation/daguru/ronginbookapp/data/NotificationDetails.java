package com.creation.daguru.ronginbookapp.data;

public class NotificationDetails {
    public int notificationType;
    public String notificationUId;
    public String bookUId;
    public String bookName;
    public String otherUserUId;
    public String otherUserName;
    public long createTime;
    public int isRead;
    public int isValid;
    public int notificationReplyType;
    public int requestedDay;
    public String otherUId_notiType;

    public NotificationDetails() {
    }

    public NotificationDetails(int notificationType, String notificationUId, String bookUId, String bookName,
                               String otherUserUId, String otherUserName, long createTime,
                               int isRead, int isValid, int notificationReplyType, int requestedDay, String otherUId_notiType) {

        this.notificationType = notificationType;
        this.notificationUId = notificationUId;
        this.bookUId = bookUId;
        this.bookName = bookName;
        this.otherUserUId = otherUserUId;
        this.otherUserName = otherUserName;
        this.createTime = createTime;
        this.isRead = isRead;
        this.isValid = isValid;
        this.notificationReplyType = notificationReplyType;
        this.requestedDay = requestedDay;
        this.otherUId_notiType = otherUId_notiType;
    }

}
