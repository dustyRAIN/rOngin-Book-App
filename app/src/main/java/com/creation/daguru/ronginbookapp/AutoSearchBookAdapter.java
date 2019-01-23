package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.creation.daguru.ronginbookapp.data.UniqueBookDetails;

import java.util.ArrayList;
import java.util.List;

public class AutoSearchBookAdapter extends ArrayAdapter<UniqueBookDetails>{

    private Context mContext;
    private List<UniqueBookDetails> mBookList;
    private List<UniqueBookDetails> mTempBookList;


    public AutoSearchBookAdapter(@NonNull Context context, @NonNull List<UniqueBookDetails> list) {
        super(context, 0, new ArrayList<UniqueBookDetails>(list));

        mContext = context;
        mBookList = list;
        mTempBookList = new ArrayList<>(list);
        Log.d("bookdrop", "Initial List size " + String.valueOf(list.size()));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.auto_search_book_list_item, parent, false);
        }

        TextView bookName = convertView.findViewById(R.id.auto_search_book_name);
        TextView authorName = convertView.findViewById(R.id.auto_search_author_name);

        UniqueBookDetails bookDetails = mTempBookList.get(position);
        if(bookDetails != null){
            bookName.setText(bookDetails.bookName);
            authorName.setText(bookDetails.authorName);
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return bookFilter;
    }

    private Filter bookFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<UniqueBookDetails> searchResults = new ArrayList<>();

            if(constraint != null && constraint.length()>=1){
                String toSearch = constraint.toString().toLowerCase().trim();
                int i = 0;

                for(UniqueBookDetails bookDetails: mBookList){
                    String bookName = bookDetails.bookName;

                    if(bookName.toLowerCase().contains(toSearch)){
                        searchResults.add(bookDetails);
                        i++;
                    }
                    if(i>=50){
                        break;
                    }
                }
            }

            filterResults.values = searchResults;
            filterResults.count = searchResults.size();

            Log.d("bookdrop", "cons " + String.valueOf(constraint) + " Now size " +
                    String.valueOf(mBookList.size()));

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<UniqueBookDetails> detailsList = (List) results.values;

            clear();
            addAll(detailsList);
            mTempBookList.clear();
            mTempBookList.addAll(detailsList);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            UniqueBookDetails bookDetails = (UniqueBookDetails) resultValue;
            //mOnClickHandler.onItemClicked(bookDetails);
            return bookDetails.bookName;
        }
    };
}
