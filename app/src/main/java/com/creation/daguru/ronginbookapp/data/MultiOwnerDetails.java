package com.creation.daguru.ronginbookapp.data;

import android.support.annotation.NonNull;

public class MultiOwnerDetails implements Comparable<MultiOwnerDetails>{
    public String ownerUId;
    public String ownerName;
    public double distance;

    public MultiOwnerDetails() {
    }

    public MultiOwnerDetails(String ownerUId, String ownerName, double distance) {
        this.ownerUId = ownerUId;
        this.ownerName = ownerName;
        this.distance = distance;
    }

    @Override
    public int compareTo(@NonNull MultiOwnerDetails ownerDetails) {
        if(this.distance < ownerDetails.distance) return -1;
        if(this.distance > ownerDetails.distance) return 1;
        return 0;
    }
}
