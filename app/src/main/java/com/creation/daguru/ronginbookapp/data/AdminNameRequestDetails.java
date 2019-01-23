package com.creation.daguru.ronginbookapp.data;

public class AdminNameRequestDetails {
    public String bookName;
    public String authorName;
    public String otherUserUId;
    public String otherUserName;
    public String requestUId;
    public String language;
    public int isVisible;
    public long createTime;
    public double otherLatitude;
    public double otherLongitude;

    public AdminNameRequestDetails() {
    }

    public AdminNameRequestDetails(String bookName, String authorName, String otherUserUId, String otherUserName,
                                   String requestUId, String language, int isVisible, long createTime, double latitude,
                                   double longitude) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.otherUserUId = otherUserUId;
        this.otherUserName = otherUserName;
        this.requestUId = requestUId;
        this.language = language;
        this.isVisible = isVisible;
        this.createTime = createTime;
        this.otherLatitude = latitude;
        this.otherLongitude = longitude;
    }
}
