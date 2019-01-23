package com.creation.daguru.ronginbookapp.data;

public class AdminModDetails {
    public String adminName;
    public String adminPhotoUrl;
    public String adminUId;
    public int adminPost;

    public AdminModDetails() {
    }

    public AdminModDetails(String adminName, String adminPhotoUrl, String adminUId, int adminPost) {
        this.adminName = adminName;
        this.adminPhotoUrl = adminPhotoUrl;
        this.adminUId = adminUId;
        this.adminPost = adminPost;
    }
}
