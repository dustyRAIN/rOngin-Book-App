package com.creation.daguru.ronginbookapp.data;

public class ListUserBasicInfo {
    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;
    public String photoUrl;
    public String userUId;
    public double latitude;
    public double longitude;

    public ListUserBasicInfo() {
    }

    public ListUserBasicInfo(String firstName, String lastName, String email, String phoneNumber, String photoUrl,
                             String userUId, double latitude, double longitude) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.userUId = userUId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
