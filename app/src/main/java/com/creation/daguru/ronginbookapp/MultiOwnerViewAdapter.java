package com.creation.daguru.ronginbookapp;

import android.app.LoaderManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.creation.daguru.ronginbookapp.data.MultiOwnerDetails;

import java.util.List;

import static com.creation.daguru.ronginbookapp.AllConstantsDatabase.RONGIN_UID;

public class MultiOwnerViewAdapter extends RecyclerView.Adapter<MultiOwnerViewAdapter.OwnerViewHolder> {

    private Context mContext;
    private List<MultiOwnerDetails> mOwnerList;
    private MultiOwnerAdapterOnClickHandler mOnClickHandler;

    public interface MultiOwnerAdapterOnClickHandler{
        void ownerOnClick(String ownerUId, String ownerName, double distance);
    }

    public MultiOwnerViewAdapter(Context context, List<MultiOwnerDetails> list, MultiOwnerAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mOwnerList = list;
        mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public OwnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.multi_owner_list_item, parent, false);
        return new OwnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerViewHolder holder, int position) {
        MultiOwnerDetails ownerDetails = mOwnerList.get(position);
        if(ownerDetails.ownerUId.equals(RONGIN_UID)){
            holder.ronginLogo.setVisibility(View.VISIBLE);
            holder.ownerName.setVisibility(View.INVISIBLE);
        } else {
            holder.ronginLogo.setVisibility(View.INVISIBLE);
            holder.ownerName.setVisibility(View.VISIBLE);
            holder.ownerName.setText(ownerDetails.ownerName);
        }
        holder.distance.setText(String.format("%.1f km", ownerDetails.distance));
    }

    @Override
    public int getItemCount() {
        if(mOwnerList == null) return 0;
        return mOwnerList.size();
    }

    public void updateList(List<MultiOwnerDetails> data){
        mOwnerList = data;
        notifyDataSetChanged();
    }

    public class OwnerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView ownerName;
        final TextView distance;
        final ImageView ronginLogo;

        public OwnerViewHolder(View itemView) {
            super(itemView);
            ownerName = itemView.findViewById(R.id.multi_list_owner_name);
            distance = itemView.findViewById(R.id.multi_list_distance);
            ronginLogo = itemView.findViewById(R.id.multi_list_rongin_logo);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            MultiOwnerDetails ownerDetails = mOwnerList.get(position);

            mOnClickHandler.ownerOnClick(ownerDetails.ownerUId, ownerDetails.ownerName, ownerDetails.distance);
        }
    }
}
