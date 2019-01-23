package com.creation.daguru.ronginbookapp;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.UserLibraryBookDetails;

import java.util.List;

public class UserBooksViewAdapter extends RecyclerView.Adapter<UserBooksViewAdapter.UserBooksViewHolder> {

    private Context mContext;
    private List<UserLibraryBookDetails> mBookDetails;
    private BookClickHandler mClickHandler;

    public interface BookClickHandler{
        void onClickedBook(UserLibraryBookDetails bookDetails);
    }

    public UserBooksViewAdapter(Context context, List<UserLibraryBookDetails> list, BookClickHandler clickHandler) {
        mContext = context;
        mBookDetails = list;
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public UserBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_books_list_item, parent, false);
        return new UserBooksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserBooksViewHolder holder, int position) {
        UserLibraryBookDetails bookDetails = mBookDetails.get(position);
        holder.bookName.setText(bookDetails.bookName);
        holder.authorName.setText(bookDetails.authorName);

        if(bookDetails.isVisible == 1){
            holder.tickIcon.setVisibility(View.VISIBLE);
            holder.visibilityCheck.setVisibility(View.VISIBLE);
        } else {
            holder.tickIcon.setVisibility(View.GONE);
            holder.visibilityCheck.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(mBookDetails == null){
            return 0;
        }
        return mBookDetails.size();
    }

    public void updateList(List<UserLibraryBookDetails> detailsList){
        mBookDetails = detailsList;
        notifyDataSetChanged();
    }


    public class UserBooksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView bookName;
        final TextView authorName;
        final TextView visibilityCheck;
        final ImageView tickIcon;

        public UserBooksViewHolder(View itemView) {
            super(itemView);

            bookName = itemView.findViewById(R.id.user_list_book_name);
            authorName = itemView.findViewById(R.id.user_list_author_name);
            visibilityCheck = itemView.findViewById(R.id.user_list_visible);
            tickIcon = itemView.findViewById(R.id.ic_list_tick);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = getAdapterPosition();
            mClickHandler.onClickedBook(mBookDetails.get(id));
        }
    }
}
