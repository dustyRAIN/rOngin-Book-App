package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.ListBookFullDetails;

import java.util.ArrayList;
import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;

public class BooksViewAdapter extends RecyclerView.Adapter<BooksViewAdapter.BooksViewHolder> {

    private Context mContext;
    private List<ListBookFullDetails> mBookList;

    private BooksAdapterOnClickHandler mOnClickHandler;

    public interface BooksAdapterOnClickHandler{
        void bookOnclick(String bookUId, String ownerUId, String bookName, String authorName, String ownerName, String distance);
    }

    BooksViewAdapter(Context context, BooksAdapterOnClickHandler booksAdapterOnClickHandler, List<ListBookFullDetails> list) {
        mContext = context;
        mOnClickHandler = booksAdapterOnClickHandler;
        mBookList = list;
    }

    @NonNull
    @Override
    public BooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.books_list_item, parent, false);
        return new BooksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BooksViewHolder holder, int position) {
        ListBookFullDetails fullDetails = mBookList.get(position);

        if(fullDetails.ownerCount == 1){
            if(fullDetails.ownerUid.equals(RONGIN_UID)){
                holder.ronginLogo.setVisibility(View.VISIBLE);
                holder.userName.setVisibility(View.GONE);
            } else {
                holder.ronginLogo.setVisibility(View.GONE);
                holder.userName.setVisibility(View.VISIBLE);
                holder.userName.setText(fullDetails.ownerFirstName);
            }
            holder.distance.setVisibility(View.VISIBLE);
            holder.distance.setText(fullDetails.distance);
        } else {
            holder.ronginLogo.setVisibility(View.GONE);
            holder.userName.setVisibility(View.VISIBLE);
            holder.userName.setText(String.valueOf(fullDetails.ownerCount) + " owner");
            holder.distance.setVisibility(View.GONE);
        }

        holder.bookName.setText(fullDetails.bookName);
        holder.authorName.setText(fullDetails.authorName);
    }

    @Override
    public int getItemCount() {
        if(mBookList == null){
            //Toast.makeText(mContext, "cry", Toast.LENGTH_LONG).show();
            return 0;
        }

        //Toast.makeText(mContext, String.valueOf(mBookList.size()), Toast.LENGTH_LONG).show();
        return mBookList.size();
    }


    public class BooksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView bookName;
        final TextView authorName;
        final TextView distance;
        final TextView userName;
        final ImageView ronginLogo;

        public BooksViewHolder(View itemView) {
            super(itemView);

            bookName = itemView.findViewById(R.id.list_book_name);
            authorName = itemView.findViewById(R.id.list_author_name);
            distance = itemView.findViewById(R.id.list_distance);
            userName = itemView.findViewById(R.id.list_user_name);
            ronginLogo = itemView.findViewById(R.id.list_rongin_logo);

            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            ListBookFullDetails bookFullDetails = mBookList.get(position);
            if(bookFullDetails.ownerCount == 1) {
                mOnClickHandler.bookOnclick(bookFullDetails.bookUId, bookFullDetails.ownerUid,
                        bookFullDetails.bookName, bookFullDetails.authorName,
                        bookFullDetails.ownerFirstName, bookFullDetails.distance);
            } else {
                mOnClickHandler.bookOnclick(bookFullDetails.bookUId, null,
                        bookFullDetails.bookName, bookFullDetails.authorName, null, null);
            }
        }
    }
}
