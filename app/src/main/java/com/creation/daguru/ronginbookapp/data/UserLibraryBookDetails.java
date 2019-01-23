package com.creation.daguru.ronginbookapp.data;

public class UserLibraryBookDetails {
    public String bookName;
    public String authorName;
    public String bookUId;
    public int isVisible;
    public int copyCount;

    public UserLibraryBookDetails(String bookName, String authorName, String bookUId, int isVisible, int copyCount) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.bookUId = bookUId;
        this.isVisible = isVisible;
        this.copyCount = copyCount;
    }

    public UserLibraryBookDetails() {
    }
}
