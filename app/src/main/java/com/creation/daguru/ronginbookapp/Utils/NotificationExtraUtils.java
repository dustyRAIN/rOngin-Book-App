package com.creation.daguru.ronginbookapp.Utils;

import android.content.Context;

import com.creation.daguru.ronginbookapp.R;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;

public class NotificationExtraUtils {
    public static final int TYPE_NOTIFICATION_BOOK_ADDED = 101;
    public static final int TYPE_NOTIFICATION_BOOK_CONFIRMATION = 102;
    public static final int TYPE_NOTIFICATION_BOOK_REQUEST = 111;
    public static final int TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT = 112;
    public static final int TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY = 113;
    public static final int TYPE_NOTIFICATION_BOOK_GIVING_REQUEST = 114;
    public static final int TYPE_NOTIFICATION_BOOK_RECEIVED = 115;
    public static final int TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE = 116;
    public static final int TYPE_NOTIFICATION_MORE_DAY_REQUEST = 121;
    public static final int TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT = 122;
    public static final int TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY = 123;
    public static final int TYPE_NOTIFICATION_BOOK_RETURN = 131;
    public static final int TYPE_NOTIFICATION_ACCOUNT_FREEZE = 141;

    public static String getNotificationTitle(Context context, int notificationType){
        switch (notificationType){
            case TYPE_NOTIFICATION_BOOK_ADDED:
                return context.getString(R.string.noti_title_book_added);
            case TYPE_NOTIFICATION_BOOK_CONFIRMATION:
                return context.getString(R.string.noti_title_book_confirm);
            case TYPE_NOTIFICATION_BOOK_REQUEST:
                return context.getString(R.string.noti_title_book_request);
            case TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT:
                return context.getString(R.string.noti_title_book_request_reply_accept);
            case TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY:
                return context.getString(R.string.noti_title_book_request_reply_deny);
            case TYPE_NOTIFICATION_BOOK_GIVING_REQUEST:
                return context.getString(R.string.noti_title_book_request_book_give);
            case TYPE_NOTIFICATION_BOOK_RECEIVED:
                return context.getString(R.string.noti_title_book_request_book_take);
            case TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE:
                return context.getString(R.string.noti_title_book_receive_returned);
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST:
                return context.getString(R.string.noti_title_more_day_request);
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT:
                return context.getString(R.string.noti_title_more_day_request_accept);
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY:
                return context.getString(R.string.noti_title_more_day_request_deny);
            case TYPE_NOTIFICATION_BOOK_RETURN:
                return context.getString(R.string.noti_title_return_book);
            case TYPE_NOTIFICATION_ACCOUNT_FREEZE:
                return context.getString(R.string.noti_title_account_freezed);
            default:
                return context.getString(R.string.noti_title_in_general);
        }
    }

    public static String getNotificationDescription(Context context, NotificationDetails notificationDetails){
        switch (notificationDetails.notificationType){
            case TYPE_NOTIFICATION_BOOK_ADDED:
                return String.format(context.getString(R.string.noti_desc_book_added), notificationDetails.bookName);
            case TYPE_NOTIFICATION_BOOK_CONFIRMATION:
                return String.format(context.getString(R.string.noti_desc_book_confirm), notificationDetails.bookName);
            case TYPE_NOTIFICATION_BOOK_REQUEST:
                return String.format(context.getString(R.string.noti_desc_book_request), notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_ACCEPT:
                return String.format(context.getString(R.string.noti_desc_book_request_reply_accept),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_BOOK_REQUEST_REPLY_DENY:
                return String.format(context.getString(R.string.noti_desc_book_request_reply_deny),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_BOOK_GIVING_REQUEST:
                return String.format(context.getString(R.string.noti_desc_book_request_book_give),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_BOOK_RECEIVED:
                return String.format(context.getString(R.string.noti_desc_book_request_book_take),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_RETURNED_BOOK_RECEIVE:
                return String.format(context.getString(R.string.noti_desc_book_receive_returned),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST:
                return String.format(context.getString(R.string.noti_desc_more_day_request), notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_ACCEPT:
                return String.format(context.getString(R.string.noti_desc_more_day_request_accept),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_MORE_DAY_REQUEST_REPLY_DENY:
                return String.format(context.getString(R.string.noti_desc_more_day_request_deny),
                        notificationDetails.otherUserName);
            case TYPE_NOTIFICATION_BOOK_RETURN:
                return String.format(context.getString(R.string.noti_desc_return_book));
            case TYPE_NOTIFICATION_ACCOUNT_FREEZE:
                return String.format(context.getString(R.string.noti_desc_account_freezed));
            default:
                return String.format(context.getString(R.string.noti_desc_in_general));
        }
    }
}
