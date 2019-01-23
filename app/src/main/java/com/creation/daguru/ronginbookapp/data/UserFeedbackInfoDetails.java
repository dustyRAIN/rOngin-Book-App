package com.creation.daguru.ronginbookapp.data;

public class UserFeedbackInfoDetails {
    public int ability;
    public long lastFeedbackTime;

    public UserFeedbackInfoDetails(int ability, long lastFeedbackTime) {
        this.ability = ability;
        this.lastFeedbackTime = lastFeedbackTime;
    }

    public UserFeedbackInfoDetails() {
    }
}
