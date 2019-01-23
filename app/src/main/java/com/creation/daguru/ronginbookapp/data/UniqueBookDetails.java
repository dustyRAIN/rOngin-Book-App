package com.creation.daguru.ronginbookapp.data;

public class UniqueBookDetails {

    public String bookName;
    public String bookUId;
    public String authorName;
    public String keyword1;
    public String keyword2;
    public String keyword3;

    public UniqueBookDetails(String bookName, String bookUId, String authorName, String keyword1,
                             String keyword2, String keyword3) {
        this.bookName = bookName;
        this.bookUId = bookUId;
        this.authorName = authorName;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.keyword3 = keyword3;
    }

    public UniqueBookDetails() {
    }
}
