package com.creation.daguru.ronginbookapp.data;

public class UserExchangeBookDetails {

    public String bookName;
    public String authorName;
    public String bookUId;
    public String otherUserUId;
    public String otherUserName;
    public long exchangeTime;
    public int dayLimit;

    public UserExchangeBookDetails() {
    }

    public UserExchangeBookDetails(String bookName, String authorName, String bookUId, String otherUserUId,
                                   String otherUserFirstNanme, long exchangeTime, int dayLimit) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.bookUId = bookUId;
        this.otherUserUId = otherUserUId;
        this.otherUserName = otherUserFirstNanme;
        this.exchangeTime = exchangeTime;
        this.dayLimit = dayLimit;
    }
}
