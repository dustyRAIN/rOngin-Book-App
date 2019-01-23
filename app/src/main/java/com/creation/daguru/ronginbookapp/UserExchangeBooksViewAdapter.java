package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.ListUserBookExchangeDetails;

import java.util.List;
import java.util.zip.Inflater;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;

public class UserExchangeBooksViewAdapter extends RecyclerView.Adapter<UserExchangeBooksViewAdapter.UserBooksHolder> {

    private Context mContext;
    private int mExchangeType;
    private ExchangeBookOnClickHandler mOnClickHandler;

    private List<ListUserBookExchangeDetails> mBookList;

    public static final int LENT_EXCHANGE_TYPE = 1;
    public static final int BORROWED_EXCHANGE_TYPE = 2;

    public interface ExchangeBookOnClickHandler{
        void bookOnClicked(ListUserBookExchangeDetails bookExchangeDetails);
    }

    public UserExchangeBooksViewAdapter(Context mContext, int mExchangeType, List<ListUserBookExchangeDetails> list,
                                        ExchangeBookOnClickHandler onClickHandler) {
        this.mContext = mContext;
        this.mExchangeType = mExchangeType;
        this.mBookList = list;
        this.mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public UserExchangeBooksViewAdapter.UserBooksHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_exchange_books_list_item, parent, false);
        return new UserBooksHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserExchangeBooksViewAdapter.UserBooksHolder holder, int position) {
        ListUserBookExchangeDetails bookExchangeDetails = mBookList.get(position);
        if(bookExchangeDetails.daysLeft <= 3){
            if(mExchangeType == LENT_EXCHANGE_TYPE){
                holder.daysLeft.setVisibility(View.GONE);
                holder.daysLeftGreen.setVisibility(View.VISIBLE);
                holder.daysLeftRed.setVisibility(View.GONE);


                if(bookExchangeDetails.daysLeft == 1 || bookExchangeDetails.daysLeft == 0){
                    holder.daysLeftGreen.setText(bookExchangeDetails.daysLeft + " day left");
                } else {
                    holder.daysLeftGreen.setText(bookExchangeDetails.daysLeft + " days left");
                }
            } else if(mExchangeType == BORROWED_EXCHANGE_TYPE){
                holder.daysLeft.setVisibility(View.GONE);
                holder.daysLeftGreen.setVisibility(View.GONE);
                holder.daysLeftRed.setVisibility(View.VISIBLE);

                if(bookExchangeDetails.daysLeft == 0){
                    holder.daysLeftRed.setText("Return");
                } else if(bookExchangeDetails.daysLeft == 1){
                    holder.daysLeftRed.setText(bookExchangeDetails.daysLeft + " day left");
                } else {
                    holder.daysLeftRed.setText(bookExchangeDetails.daysLeft + " days left");
                }
            }
        } else {
            holder.daysLeft.setVisibility(View.VISIBLE);
            holder.daysLeftGreen.setVisibility(View.GONE);
            holder.daysLeftRed.setVisibility(View.GONE);

            holder.daysLeft.setText(bookExchangeDetails.daysLeft + " days left");
        }

        holder.bookName.setText(bookExchangeDetails.bookName);
        holder.authorName.setText(bookExchangeDetails.authorName);

        if(bookExchangeDetails.otherUserUId.equals(RONGIN_UID)){
            holder.ronginLogo.setVisibility(View.VISIBLE);
            holder.userName.setVisibility(View.GONE);
        } else {
            holder.ronginLogo.setVisibility(View.GONE);
            holder.userName.setVisibility(View.VISIBLE);

            holder.userName.setText(bookExchangeDetails.otherUserName);
        }
    }

    @Override
    public int getItemCount() {
        if(mBookList == null){
            return 0;
        }
        return mBookList.size();
    }


    public class UserBooksHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView bookName;
        final TextView authorName;
        final TextView userName;
        final TextView daysLeft;
        final TextView daysLeftRed;
        final TextView daysLeftGreen;
        final ImageView ronginLogo;

        public UserBooksHolder(View itemView) {
            super(itemView);

            bookName = itemView.findViewById(R.id.list_ex_book_name);
            authorName = itemView.findViewById(R.id.list_ex_author_name);
            userName = itemView.findViewById(R.id.list_ex_user_name);
            ronginLogo = itemView.findViewById(R.id.list_ex_rongin_logo);
            daysLeft = itemView.findViewById(R.id.list_ex_days_left);
            daysLeftRed = itemView.findViewById(R.id.list_ex_days_left_red);
            daysLeftGreen = itemView.findViewById(R.id.list_ex_days_left_green);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnClickHandler.bookOnClicked(mBookList.get(position));
        }
    }
}
