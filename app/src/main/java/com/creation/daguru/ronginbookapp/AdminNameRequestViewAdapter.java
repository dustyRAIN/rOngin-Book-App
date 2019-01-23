package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.AdminNameRequestDetails;

import java.util.List;

import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;

public class AdminNameRequestViewAdapter extends RecyclerView.Adapter<AdminNameRequestViewAdapter.NameViewHolder> {

    private Context mContext;
    private List<AdminNameRequestDetails> mNameList;
    private OnNameClickHandler mClickHandler;

    public interface OnNameClickHandler{
        void onRequestClicked(AdminNameRequestDetails nameRequestDetails);
    }

    public AdminNameRequestViewAdapter(Context mContext, List<AdminNameRequestDetails> mNameList,
                                       OnNameClickHandler clickHandler) {
        this.mContext = mContext;
        this.mNameList = mNameList;
        this.mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public NameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.admin_name_request_list_item, parent, false);
        return new NameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NameViewHolder holder, int position) {
        AdminNameRequestDetails nameRequestDetails = mNameList.get(position);

        holder.itemView.setTag(nameRequestDetails.requestUId);

        holder.userName.setText(nameRequestDetails.otherUserName);
        holder.createTime.setText(getFriendlyDateString(mContext, getLocalDateFromUTC(nameRequestDetails.createTime), true));
    }

    @Override
    public int getItemCount() {
        if(mNameList == null) return 0;
        return mNameList.size();
    }

    public class NameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView userName;
        final TextView createTime;

        public NameViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.admin_name_list_user_name);
            createTime = itemView.findViewById(R.id.admin_name_list_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            mClickHandler.onRequestClicked(mNameList.get(pos));
        }
    }
}
