package com.creation.daguru.ronginbookapp.Utils;

import com.creation.daguru.ronginbookapp.data.UserBasicInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ExtraWorks {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    private static final String DATABASE_DIR_USER_BASIC_INFO = "user-basic-info";

    public static String getDistanceBetweenUsersInString(String UId_1, String UId_2){
        String distance = "8 km";
        return distance;
    }

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
