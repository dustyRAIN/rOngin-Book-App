package com.creation.daguru.ronginbookapp.data;

public class ModWorkDetails {
    public int workType;
    public String workUId;
    public String bookName1;
    public String bookName2;
    public String authorName1;
    public String authorName2;
    public String bookUId;
    public String otherUserUId;
    public String otherUserName;
    public int replyType;
    public long workTime;

    public ModWorkDetails() {
    }

    public ModWorkDetails(int workType, String workUId, String bookName1, String bookName2, String authorName1,
                          String authorName2, String bookUId, String otherUserUId, String otherUserName,
                          int replyType, long workTime) {

        this.workType = workType;
        this.workUId = workUId;
        this.bookName1 = bookName1;
        this.bookName2 = bookName2;
        this.authorName1 = authorName1;
        this.authorName2 = authorName2;
        this.bookUId = bookUId;
        this.otherUserUId = otherUserUId;
        this.otherUserName = otherUserName;
        this.replyType = replyType;
        this.workTime = workTime;
    }
}
