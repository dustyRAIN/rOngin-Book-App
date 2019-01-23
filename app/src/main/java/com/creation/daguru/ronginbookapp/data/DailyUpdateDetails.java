package com.creation.daguru.ronginbookapp.data;

public class DailyUpdateDetails {
    public String updateMessage;
    public long createTime;

    public DailyUpdateDetails() {
    }

    public DailyUpdateDetails(String updateMessage, long createTime) {
        this.updateMessage = updateMessage;
        this.createTime = createTime;
    }
}
