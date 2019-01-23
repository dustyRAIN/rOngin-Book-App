package com.creation.daguru.ronginbookapp.data;

public class BookWithOwnerDetails {
    private String bookName;
    private String authorName;
    private String ownerName;
    private String distance;
    private double latitude;
    private double longitude;

    public BookWithOwnerDetails(String bookName, String authorName, String ownerName,
                                String distance, double latitude, double longitude) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.ownerName = ownerName;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
