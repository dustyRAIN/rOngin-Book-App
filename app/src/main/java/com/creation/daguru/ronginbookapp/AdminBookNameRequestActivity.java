package com.creation.daguru.ronginbookapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.AdminNameRequestDetails;
import com.creation.daguru.ronginbookapp.data.AllBooksLibBookDetails;
import com.creation.daguru.ronginbookapp.data.DailyUpdateDetails;
import com.creation.daguru.ronginbookapp.data.NotificationDetails;
import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;
import com.creation.daguru.ronginbookapp.data.UserExchangeBookDetails;
import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS_CHILD_CREATE_TIME;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ADMIN_NEW_NAME_REQUEST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_LIB;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_ALL_BOOKS_OWNERS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_DAILY_UPDATE_LIST;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_NEW_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_UNIQUE_BOOK_DETAILS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_BORROWED_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_LIB_BOOKS;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.DATABASE_DIR_USER_NOTIFICATION;
import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_ADDED;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_BOOK_CONFIRMATION;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getUTCDateFromLocal;

public class AdminBookNameRequestActivity extends AppCompatActivity implements AdminNameRequestViewAdapter.OnNameClickHandler,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "NameRequestList";

    private RecyclerView mRecyclerView;
    private AdminNameRequestViewAdapter mAdapter;

    private ScrollView mScrollView;

    private AutoCompleteTextView mactvSearchField;
    private EditText metvAuthorName;
    private EditText metvKeyWord1;
    private EditText metvKeyWord2;
    private EditText metvKeyWord3;

    private TextView mtvSearchedBook;
    private TextView mtvAuthorName;
    private TextView mtvKeyWord1;
    private TextView mtvKeyWord2;
    private TextView mtvKeyWord3;

    private TextView mtvRequestedBookName;
    private TextView mtvRequestedAuthorName;
    private TextView mtvRequestedBookLanguage;

    private TextView mtvbSearchedBookCancel;
    private TextView mtvbAddNow;
    private TextView mtvbSendNotification;

    private RonginItemTouchHelper mItemTouchHelper;

    private AutoSearchBookAdapter mAutoAdapter;
    private List<UniqueBookDetails> mBookList;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ChildEventListener mChildEventListener;
    private ChildEventListener mBookChildEventListener;
    private ValueEventListener mLightListener;
    private Query mQuery;

    private boolean mIsListShown;
    private boolean mIsBookFound;

    private UniqueBookDetails mFoundBook;

    private AdminNameRequestDetails mCurrentRequestDetails;
    private UserLibraryBookDetails mCurrentUserBook;

    private List<AdminNameRequestDetails> mNameRequestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_book_name_request);

        mRecyclerView = findViewById(R.id.admin_name_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                true);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mScrollView = findViewById(R.id.admin_name_request_respond_view);

        mactvSearchField = findViewById(R.id.admin_name_book_search);
        metvAuthorName = findViewById(R.id.admin_name_book_author);
        metvKeyWord1 = findViewById(R.id.admin_name_keyword_1);
        metvKeyWord2 = findViewById(R.id.admin_name_keyword_2);
        metvKeyWord3 = findViewById(R.id.admin_name_keyword_3);
        mtvSearchedBook = findViewById(R.id.admin_name_searched_book);
        mtvAuthorName = findViewById(R.id.admin_name_tv_book_author);
        mtvKeyWord1 = findViewById(R.id.admin_name_tv_keyword_1);
        mtvKeyWord2 = findViewById(R.id.admin_name_tv_keyword_2);
        mtvKeyWord3 = findViewById(R.id.admin_name_tv_keyword_3);

        mtvRequestedBookName = findViewById(R.id.admin_name_requested_book_name);
        mtvRequestedAuthorName = findViewById(R.id.admin_name_requested_book_author);
        mtvRequestedBookLanguage = findViewById(R.id.admin_name_requested_book_language);

        mtvbSearchedBookCancel = findViewById(R.id.admin_name_searched_book_cancel);
        mtvbAddNow = findViewById(R.id.admin_name_add_book);
        mtvbSendNotification = findViewById(R.id.admin_name_send_notification);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mactvSearchField.setOnItemClickListener(this);
        mtvbSearchedBookCancel.setOnClickListener(this);
        mtvbAddNow.setOnClickListener(this);
        mtvbSendNotification.setOnClickListener(this);

        setItemTouchListener();
        setUpUI();
    }

    private void setUpUI(){
        showRecyclerView();
    }

    @Override
    protected void onResume() {
        attachLightListener();

        mBookList = new ArrayList<>();
        mAutoAdapter = new AutoSearchBookAdapter(this, mBookList);
        mactvSearchField.setAdapter(mAutoAdapter);
        attatchBookListener();

        mNameRequestList = new ArrayList<>();
        mAdapter = new AdminNameRequestViewAdapter(this, mNameRequestList, this);
        mRecyclerView.setAdapter(mAdapter);
        attachDatabaseListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        detachLightListener();
        detachDatabaseReadListener();
        detatchBookListener();
        super.onPause();
    }

    private void setItemTouchListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS);

        mItemTouchHelper = new RonginItemTouchHelper(0, ItemTouchHelper.LEFT,
                this, databaseReference);
        new ItemTouchHelper(mItemTouchHelper).attachToRecyclerView(mRecyclerView);
    }

    private void attachLightListener(){
        if(mLightListener == null){
            mLightListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int state = dataSnapshot.getValue(Integer.class);
                        if(state != 0){
                            DatabaseReference databaseReference = mDatabaseReference
                                    .child(DATABASE_DIR_ADMIN_NEW_NAME_REQUEST);

                            databaseReference.setValue(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_NEW_NAME_REQUEST);
            databaseReference.addValueEventListener(mLightListener);
        }
    }

    private void detachLightListener(){
        if(mLightListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_NEW_NAME_REQUEST);
            databaseReference.removeEventListener(mLightListener);
        }
    }

    private void attatchBookListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);

        if(mBookChildEventListener == null){
            mBookChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UniqueBookDetails bookDetails = dataSnapshot.getValue(UniqueBookDetails.class);

                    mBookList.add(bookDetails);
                    mAutoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            databaseReference.addChildEventListener(mBookChildEventListener);
        }
    }

    private void attachDatabaseListener(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS);

        Log.d(TAG, databaseReference.getKey());

        mQuery = databaseReference.orderByChild(DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS_CHILD_CREATE_TIME);

        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    AdminNameRequestDetails nameRequestDetails = dataSnapshot.getValue(AdminNameRequestDetails.class);


                    mNameRequestList.add(nameRequestDetails);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //Log.d(TAG, s);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    int pos = getRequestId(dataSnapshot.getValue(AdminNameRequestDetails.class));
                    mNameRequestList.remove(pos);
                    mAdapter.notifyItemRemoved(pos);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mQuery.addChildEventListener(mChildEventListener);
        }
    }

    private int getRequestId(AdminNameRequestDetails requestDetails){
        int i = 0;
        for(AdminNameRequestDetails details: mNameRequestList){
            if(requestDetails.requestUId.equals(details.requestUId)){
                return i;
            }
            i++;
        }

        return -1;
    }

    private void detatchBookListener(){
        if(mBookChildEventListener != null){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);
            databaseReference.removeEventListener(mBookChildEventListener);
            mBookChildEventListener = null;
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mQuery.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void initializeDetailsView(){
        beforeBookFound();
        mtvRequestedBookName.setText(mCurrentRequestDetails.bookName);
        mtvRequestedAuthorName.setText(mCurrentRequestDetails.authorName);
        mtvRequestedBookLanguage.setText(mCurrentRequestDetails.language);
        enableButtons();
        showDetailsView();
    }

    private void beforeBookFound(){
        mIsBookFound = false;

        metvAuthorName.setText("");
        metvKeyWord1.setText("");
        metvKeyWord2.setText("");
        metvKeyWord3.setText("");

        showEmptyBookDetails();
    }

    private void afterBookFound(UniqueBookDetails bookDetails){
        mIsBookFound = true;
        mFoundBook = bookDetails;

        mtvSearchedBook.setText(mFoundBook.bookName);
        mtvAuthorName.setText(mFoundBook.authorName);
        mtvKeyWord1.setText(mFoundBook.keyword1);
        mtvKeyWord2.setText(mFoundBook.keyword2);
        mtvKeyWord3.setText(mFoundBook.keyword3);

        showFoundBookDetails();
    }

    private void showEmptyBookDetails(){
        mactvSearchField.setVisibility(View.VISIBLE);
        metvAuthorName.setVisibility(View.VISIBLE);
        metvKeyWord1.setVisibility(View.VISIBLE);
        metvKeyWord2.setVisibility(View.VISIBLE);
        metvKeyWord3.setVisibility(View.VISIBLE);

        mtvSearchedBook.setVisibility(View.GONE);
        mtvbSearchedBookCancel.setVisibility(View.GONE);
        mtvAuthorName.setVisibility(View.GONE);
        mtvKeyWord1.setVisibility(View.GONE);
        mtvKeyWord2.setVisibility(View.GONE);
        mtvKeyWord3.setVisibility(View.GONE);
    }

    private void showFoundBookDetails(){
        mactvSearchField.setVisibility(View.GONE);
        metvAuthorName.setVisibility(View.GONE);
        metvKeyWord1.setVisibility(View.GONE);
        metvKeyWord2.setVisibility(View.GONE);
        metvKeyWord3.setVisibility(View.GONE);

        mtvSearchedBook.setVisibility(View.VISIBLE);
        mtvbSearchedBookCancel.setVisibility(View.VISIBLE);
        mtvAuthorName.setVisibility(View.VISIBLE);
        mtvKeyWord1.setVisibility(View.VISIBLE);
        mtvKeyWord2.setVisibility(View.VISIBLE);
        mtvKeyWord3.setVisibility(View.VISIBLE);
    }




    private void initializeRecyclerView(){
        showRecyclerView();
    }

    private void showRecyclerView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.GONE);
        mIsListShown = true;
    }

    private void showDetailsView(){
        mRecyclerView.setVisibility(View.GONE);
        mScrollView.setVisibility(View.VISIBLE);
        mIsListShown = false;
    }



    private void checkAndPreAddBook(){
        if(mIsBookFound){
            DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_BORROWED_BOOKS)
                    .child(mCurrentRequestDetails.otherUserUId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int i = 0, n = 0;
                    boolean isOk = true;

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        n++;
                    }

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        i++;
                        UserExchangeBookDetails bookDetails = snapshot.getValue(UserExchangeBookDetails.class);

                        if(bookDetails.bookUId.equals(mFoundBook.bookUId)){
                            isOk = false;
                        }

                        if(i == n){
                            if(isOk){
                                preAddBook();
                            } else {
                                Toast.makeText(AdminBookNameRequestActivity.this,
                                        "Can't be added, the user has this book in his/her borrowed book section.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    if(n == 0){
                        preAddBook();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            addToUniqueLibraryFirst(true);
        }
    }

    private void preAddBook(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                .child(mCurrentRequestDetails.otherUserUId).child(mFoundBook.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    UserLibraryBookDetails libraryBookDetails = dataSnapshot.getValue(UserLibraryBookDetails.class);
                    libraryBookDetails.bookName = mFoundBook.bookName;
                    libraryBookDetails.authorName = mFoundBook.authorName;
                    libraryBookDetails.copyCount++;

                    mCurrentUserBook = libraryBookDetails;
                    addToUserLib();
                } else {
                    UserLibraryBookDetails libraryBookDetails = new UserLibraryBookDetails(
                            mFoundBook.bookName,
                            mFoundBook.authorName,
                            mFoundBook.bookUId,
                            mCurrentRequestDetails.isVisible,
                            1
                    );

                    mCurrentUserBook = libraryBookDetails;
                    addToUserLib();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToUniqueLibraryFirst(boolean toAdd){
        String book = mactvSearchField.getText().toString();
        String author = metvAuthorName.getText().toString();
        String key1 = metvKeyWord1.getText().toString();
        String key2 = metvKeyWord2.getText().toString();
        String key3 = metvKeyWord3.getText().toString();

        UniqueBookDetails bookDetails = new UniqueBookDetails(
                book,
                null,
                author,
                key1,
                key2,
                key3
        );

        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_UNIQUE_BOOK_DETAILS);
        String key = databaseReference.push().getKey();

        bookDetails.bookUId = key;
        databaseReference.child(key).setValue(bookDetails);

        mFoundBook = bookDetails;

        if(toAdd){
            UserLibraryBookDetails libraryBookDetails = new UserLibraryBookDetails(
                    bookDetails.bookName,
                    bookDetails.authorName,
                    bookDetails.bookUId,
                    mCurrentRequestDetails.isVisible,
                    1
            );

            mCurrentUserBook = libraryBookDetails;
            addToUserLib();
        }
    }

    private void addToUserLib(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_USER_LIB_BOOKS)
                .child(mCurrentRequestDetails.otherUserUId).child(mCurrentUserBook.bookUId);

        databaseReference.setValue(mCurrentUserBook);

        if(mCurrentUserBook.isVisible == 1){
            addToAllLib();
            addToUpdateList();
        }

        NotificationDetails notificationDetails = new NotificationDetails(
                TYPE_NOTIFICATION_BOOK_ADDED,
                null,
                mCurrentUserBook.bookUId,
                mCurrentUserBook.bookName,
                RONGIN_UID,
                null,
                getUTCDateFromLocal(System.currentTimeMillis()),
                0,
                1,
                -1,
                -1,
                RONGIN_UID + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_ADDED)
        );

        DatabaseReference notiReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mCurrentRequestDetails.otherUserUId);

        String key = notiReference.push().getKey();
        notificationDetails.notificationUId = key;

        notiReference.child(key).setValue(notificationDetails);

        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                .child(mCurrentRequestDetails.otherUserUId);
        dbRef.setValue(1);

        deleteCurrentRequest();
    }

    private void addToAllLib(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                .child(mCurrentUserBook.bookUId).child(mCurrentRequestDetails.otherUserUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int bookCopy = -1;

                if(dataSnapshot.exists()){
                    bookCopy = dataSnapshot.getValue(Integer.class);
                    bookCopy++;

                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mCurrentUserBook.bookUId).child(mCurrentRequestDetails.otherUserUId);

                    databaseReference.setValue(bookCopy);

                } else {
                    DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_OWNERS)
                            .child(mCurrentUserBook.bookUId).child(mCurrentRequestDetails.otherUserUId);

                    databaseReference.setValue(1);
                    inreaseOwnerCountInLibrary();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToUpdateList(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_DAILY_UPDATE_LIST);
        DailyUpdateDetails updateDetails = new DailyUpdateDetails(
                String.format(getResources().getString(R.string.update_message_book_added),
                        mCurrentRequestDetails.otherUserName),
                getUTCDateFromLocal(System.currentTimeMillis())
        );

        databaseReference.push().setValue(updateDetails);
    }

    private void inreaseOwnerCountInLibrary(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                .child(mCurrentUserBook.bookUId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AllBooksLibBookDetails booksLibBookDetails = dataSnapshot.getValue(AllBooksLibBookDetails.class);
                if(booksLibBookDetails != null){
                    booksLibBookDetails.ownerCount++;
                } else {
                    booksLibBookDetails = new AllBooksLibBookDetails(
                            mCurrentUserBook.bookName,
                            mCurrentUserBook.authorName,
                            mCurrentUserBook.bookUId,
                            mCurrentRequestDetails.otherUserName,
                            mCurrentRequestDetails.otherUserUId,
                            mCurrentUserBook.bookName.toLowerCase(),
                            1,
                            mCurrentRequestDetails.otherLatitude,
                            mCurrentRequestDetails.otherLongitude
                    );
                }

                DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ALL_BOOKS_LIB)
                        .child(mCurrentUserBook.bookUId);

                databaseReference.setValue(booksLibBookDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AdminBookNameRequestActivity.this,
                                    "Book added to 'All books'", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AdminBookNameRequestActivity.this,
                                    "Book adding failed. Check internet connection.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void notifyToConfirm(){

        if(!mIsBookFound){
            addToUniqueLibraryFirst(false);
        }

        deleteCurrentRequest();

        NotificationDetails notificationDetails = new NotificationDetails(
                TYPE_NOTIFICATION_BOOK_CONFIRMATION,
                null,
                mFoundBook.bookUId,
                mFoundBook.bookName,
                RONGIN_UID,
                null,
                getUTCDateFromLocal(System.currentTimeMillis()),
                0,
                1,
                mCurrentRequestDetails.isVisible,
                -1,
                RONGIN_UID + "_" + String.valueOf(TYPE_NOTIFICATION_BOOK_CONFIRMATION)
        );

        DatabaseReference notiReference = mDatabaseReference.child(DATABASE_DIR_USER_NOTIFICATION)
                .child(mCurrentRequestDetails.otherUserUId);

        String key = notiReference.push().getKey();
        notificationDetails.notificationUId = key;

        notiReference.child(key).setValue(notificationDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AdminBookNameRequestActivity.this, "Notification Sent", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        DatabaseReference dbRef = mDatabaseReference.child(DATABASE_DIR_NEW_NOTIFICATION)
                .child(mCurrentRequestDetails.otherUserUId);
        dbRef.setValue(1);
    }


    private void deleteCurrentRequest(){
        DatabaseReference databaseReference = mDatabaseReference.child(DATABASE_DIR_ADMIN_BOOK_NAME_REQUESTS);
        databaseReference.child(mCurrentRequestDetails.requestUId).setValue(null);
    }

    private void disableButtons(){
        mtvbSendNotification.setEnabled(false);
        mtvbAddNow.setEnabled(false);
    }

    private void enableButtons(){
        mtvbSendNotification.setEnabled(true);
        mtvbAddNow.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(mIsListShown){
            super.onBackPressed();
        } else {
            initializeRecyclerView();
        }
    }

    @Override
    public void onRequestClicked(AdminNameRequestDetails nameRequestDetails) {
        mCurrentRequestDetails = nameRequestDetails;
        initializeDetailsView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UniqueBookDetails bookDetails = (UniqueBookDetails) parent.getItemAtPosition(position);
        afterBookFound(bookDetails);
        //Log.d("itemclicked", bookDetails.bookName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.admin_name_searched_book_cancel:
                beforeBookFound();
                break;

            case R.id.admin_name_add_book:
                disableButtons();
                checkAndPreAddBook();
                break;

            case R.id.admin_name_send_notification:
                disableButtons();
                notifyToConfirm();
                break;
        }
    }
}
