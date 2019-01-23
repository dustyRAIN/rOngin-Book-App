package com.creation.daguru.ronginbookapp.data;

public class ListBookFullDetails {
    public String bookUId;
    public String ownerUid;

    public String bookName;
    public String authorName;
    public String distance;
    public String ownerFirstName;

    public int ownerCount;

    public ListBookFullDetails(String bookUId, String ownerUid, String bookName, String authorName, String distance, String ownerFirstName, int ownerCount) {
        this.bookUId = bookUId;
        this.ownerUid = ownerUid;
        this.bookName = bookName;
        this.authorName = authorName;
        this.distance = distance;
        this.ownerFirstName = ownerFirstName;
        this.ownerCount = ownerCount;
    }

    public ListBookFullDetails(){

    }
}
