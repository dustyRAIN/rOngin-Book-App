package com.creation.daguru.ronginbookapp.data;

public class ListUserBookExchangeDetails {
    public String bookName;
    public String authorName;
    public String bookUId;
    public String otherUserUId;
    public String otherUserName;
    public int daysLeft;

    public ListUserBookExchangeDetails(String bookName, String authorName, String bookUId,
                                       String otherUserUId, String userName, int daysLeft) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.bookUId = bookUId;
        this.otherUserUId = otherUserUId;
        this.otherUserName = userName;
        this.daysLeft = daysLeft;
    }

    public ListUserBookExchangeDetails() {
    }
}
