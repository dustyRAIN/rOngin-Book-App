package com.creation.daguru.ronginbookapp.data;

public class AllBooksLibBookDetails {
    public String bookName;
    public String authorName;
    public String bookUId;
    public String ownerFirstName;
    public String ownerUId;
    public String bookNameLower;
    public int ownerCount;
    public double ownerLatitude;
    public double ownerLongitude;


    public AllBooksLibBookDetails() {
    }

    public AllBooksLibBookDetails(String bookName, String authorName, String bookUId, String ownerFirstName, String ownerUId,
                                  String bookNameLower, int ownerCount, double ownerLatitude, double ownerLongitude) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.bookUId = bookUId;
        this.ownerFirstName = ownerFirstName;
        this.ownerUId = ownerUId;
        this.bookNameLower = bookNameLower;
        this.ownerCount = ownerCount;
        this.ownerLatitude = ownerLatitude;
        this.ownerLongitude = ownerLongitude;
    }
}
