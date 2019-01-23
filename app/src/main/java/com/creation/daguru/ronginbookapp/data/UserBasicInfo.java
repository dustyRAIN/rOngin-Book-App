package com.creation.daguru.ronginbookapp.data;

public class UserBasicInfo {

    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;
    public String photoUrl;
    public double latitude;
    public double longitude;

    public UserBasicInfo(){

    }

    public UserBasicInfo(String firstName, String lastName, String email, String phoneNumber,
                         String photoUrl, double latitude, double longitude) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
