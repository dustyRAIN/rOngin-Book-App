package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.DailyUpdateDetails;

import java.util.List;

import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getOnlyTime;

public class UpdateListViewAdapter extends RecyclerView.Adapter<UpdateListViewAdapter.UpdateListViewHolder>{

    private Context mContext;
    private List<DailyUpdateDetails> mUpdateList;

    public UpdateListViewAdapter(Context context, List<DailyUpdateDetails> updateList) {
        mContext = context;
        mUpdateList = updateList;
    }

    @NonNull
    @Override
    public UpdateListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_update_list_item, parent, false);
        return new UpdateListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateListViewHolder holder, int position) {
        Log.d("haha", "adapter working");
        DailyUpdateDetails updateDetails = mUpdateList.get(position);
        String time = getOnlyTime(mContext, getLocalDateFromUTC(updateDetails.createTime));
        String fullMessage = "# " + time + "  -  " + updateDetails.updateMessage;
        holder.updateMessage.setText(fullMessage);
    }

    @Override
    public int getItemCount() {
        if(mUpdateList == null) return 0;
        Log.d("haha", String.valueOf(mUpdateList.size()));
        return mUpdateList.size();
    }

    public class UpdateListViewHolder extends RecyclerView.ViewHolder {
        final TextView updateMessage;

        public UpdateListViewHolder(View itemView) {
            super(itemView);
            updateMessage = itemView.findViewById(R.id.update_details);
        }
    }
}
