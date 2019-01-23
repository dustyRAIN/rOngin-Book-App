package com.creation.daguru.ronginbookapp.data;

public class UserFeedbackDetails {
    public String feedback;
    public String userUId;
    public String userName;
    public String feedbackUId;
    public long feedbackTime;
    public int isRead;

    public UserFeedbackDetails(String feedback, String userUId, String userName, String feedbackUId,
                               long feedbackTime, int isRead) {
        this.feedback = feedback;
        this.userUId = userUId;
        this.userName = userName;
        this.feedbackUId = feedbackUId;
        this.feedbackTime = feedbackTime;
        this.isRead = isRead;
    }

    public UserFeedbackDetails() {
    }
}
