package com.creation.daguru.ronginbookapp.data;

public class DailyQuoteDetails {
    public String dailyQuote;
    public String quoteSource;
    public String quoteDescription;
    public long createTime;

    public DailyQuoteDetails() {
    }

    public DailyQuoteDetails(String dailyQuote, String quoteSource, String quoteDescription, long createTime) {
        this.dailyQuote = dailyQuote;
        this.quoteSource = quoteSource;
        this.quoteDescription = quoteDescription;
        this.createTime = createTime;
    }
}
