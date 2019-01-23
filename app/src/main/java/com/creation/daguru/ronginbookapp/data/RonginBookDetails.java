package com.creation.daguru.ronginbookapp.data;

public class RonginBookDetails {

    public String bookName;
    public String authorName;
    public String bookUId;
    public String ownerUId;
    public int copyCount;
    public double ownerLatitude;
    public double ownerLongitude;

    public RonginBookDetails(String bookName, String authorName, String bookUId, String ownerUId, int copyCount,
                             double ownerLatitude, double ownerLongitude) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.bookUId = bookUId;
        this.ownerUId = ownerUId;
        this.copyCount = copyCount;
        this.ownerLatitude = ownerLatitude;
        this.ownerLongitude = ownerLongitude;
    }

    public  RonginBookDetails(){

    }
}
