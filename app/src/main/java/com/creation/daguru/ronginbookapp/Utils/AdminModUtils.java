package com.creation.daguru.ronginbookapp.Utils;

import com.creation.daguru.ronginbookapp.data.ModWorkDetails;

public class AdminModUtils {
    public static final String EXTRA_KEY_ADMIN_NAME = "adminName";
    public static final String EXTRA_KEY_ADMIN_PHOTO_URL = "adminPhotoUrl";
    public static final String EXTRA_KEY_ADMIN_UID = "adminUId";
    public static final String EXTRA_KEY_ADMIN_POST = "adminPost";

    public static final String EXTRA_KEY_ADMIN_WORK_TYPE = "workType";
    public static final String EXTRA_KEY_ADMIN_WORK_UID = "workUId";
    public static final String EXTRA_KEY_ADMIN_BOOK_LATEST = "bookName1";
    public static final String EXTRA_KEY_ADMIN_AUTHOR_LATEST = "authorName1";
    public static final String EXTRA_KEY_ADMIN_BOOK_OLD = "bookName2";
    public static final String EXTRA_KEY_ADMIN_AUTHOR_OLD = "authorName2";
    public static final String EXTRA_KEY_ADMIN_BOOK_UID = "bookUId";
    public static final String EXTRA_KEY_ADMIN_OTHER_USER_UID = "otherUserUId";
    public static final String EXTRA_KEY_ADMIN_OTHER_USER_NAME = "otherUserName";
    public static final String EXTRA_KEY_ADMIN_REPLY_TYPE = "replyType";
    public static final String EXTRA_KEY_ADMIN_WORK_TIME = "workTime";


    public static final int ADMIN_POST_MASTER_ADMIN = 1;
    public static final int ADMIN_POST_PRIMARY_ADMIN = 2;
    public static final int ADMIN_POST_SECONDARY_ADMIN = 3;
    public static final int ADMIN_POST_MODERATOR = 4;

    public static final int ADMIN_WORK_TYPE_HANDLE_BOOK_REQUEST = 201;
    public static final int ADMIN_WORK_TYPE_ADD_TO_UNIQUE_LIBRRY = 202;
    public static final int ADMIN_WORK_TYPE_ADD_TO_RONGIN_LIBRARY = 203;
    public static final int ADMIN_WORK_TYPE_LENT_A_BOOK = 204;
    public static final int ADMIN_WORK_TYPE_RECIEVE_A_BOOK = 205;
    public static final int ADMIN_WORK_TYPE_CALL_A_USER = 206;


    public static String getWorkShortDescription(ModWorkDetails modWorkDetails){
        switch (modWorkDetails.workType){
            case ADMIN_WORK_TYPE_HANDLE_BOOK_REQUEST:
                return String.format("Handled the request for \"%s\".", modWorkDetails.bookName1);

            case ADMIN_WORK_TYPE_ADD_TO_UNIQUE_LIBRRY:
                return String.format("\"%s\" to Unique Library.", modWorkDetails.bookName1);

            case ADMIN_WORK_TYPE_ADD_TO_RONGIN_LIBRARY:
                return String.format("\"%s\" to rOngin Library.", modWorkDetails.bookName1);

            case ADMIN_WORK_TYPE_LENT_A_BOOK:
                return String.format("Lent \"%s\" to %s.",modWorkDetails.bookName1, modWorkDetails.otherUserName);

            case ADMIN_WORK_TYPE_RECIEVE_A_BOOK:
                return String.format("Received \"%s\" from %s.",modWorkDetails.bookName1, modWorkDetails.otherUserName);

            case ADMIN_WORK_TYPE_CALL_A_USER:
                return String.format("Called %s.", modWorkDetails.otherUserName);

            default:
                return "Dont know what he did.";
        }
    }
}
