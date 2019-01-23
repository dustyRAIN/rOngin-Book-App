package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.creation.daguru.ronginbookapp.data.NotificationDetails;

import java.util.List;

import static com.creation.daguru.ronginbookapp.RonginItemTouchHelper.SLIDE_KEY_CANT_BE_DELETED;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.TYPE_NOTIFICATION_ACCOUNT_FREEZE;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.getNotificationDescription;
import static com.creation.daguru.ronginbookapp.Utils.NotificationExtraUtils.getNotificationTitle;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getFriendlyDateString;
import static com.creation.daguru.ronginbookapp.Utils.RonginDateUtils.getLocalDateFromUTC;

public class NotificationViewAdapter extends RecyclerView.Adapter<NotificationViewAdapter.NotificationViewHolder> {

    private Context mContext;
    private List<NotificationDetails> mNotificationList;
    private NotificationClickHandler mClickHandler;

    public interface NotificationClickHandler{
        void onClickedNotification(NotificationDetails notificationDetails);
    }

    public NotificationViewAdapter(Context context, List<NotificationDetails> notificationDetailsList,
                                   NotificationClickHandler clickHandler) {
        mContext = context;
        mNotificationList = notificationDetailsList;
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_list_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationDetails notificationDetails = mNotificationList.get(position);

        if(notificationDetails.notificationType == TYPE_NOTIFICATION_ACCOUNT_FREEZE){
            holder.itemView.setTag(SLIDE_KEY_CANT_BE_DELETED);
        } else {
            holder.itemView.setTag(notificationDetails.notificationUId);
        }

        if(notificationDetails.isRead == 0){
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorRecViewBG));
            holder.notificationTitle.setVisibility(View.GONE);
            holder.notificationTime.setVisibility(View.GONE);
            holder.notificationDescription.setVisibility(View.GONE);

            holder.boldNotificationTitle.setVisibility(View.VISIBLE);
            holder.boldNotificationTime.setVisibility(View.VISIBLE);
            holder.boldNotificationDescription.setVisibility(View.VISIBLE);

            holder.boldNotificationTitle.setText(getNotificationTitle(mContext,notificationDetails.notificationType));
            holder.boldNotificationTime.setText(getFriendlyDateString(mContext,
                    getLocalDateFromUTC(notificationDetails.createTime),
                    true));
            holder.boldNotificationDescription.setText(getNotificationDescription(mContext, notificationDetails));

        } else {
            holder.notificationTitle.setVisibility(View.VISIBLE);
            holder.notificationTime.setVisibility(View.VISIBLE);
            holder.notificationDescription.setVisibility(View.VISIBLE);

            holder.boldNotificationTitle.setVisibility(View.GONE);
            holder.boldNotificationTime.setVisibility(View.GONE);
            holder.boldNotificationDescription.setVisibility(View.GONE);

            holder.notificationTitle.setText(getNotificationTitle(mContext,notificationDetails.notificationType));
            holder.notificationTime.setText(getFriendlyDateString(mContext,
                    getLocalDateFromUTC(notificationDetails.createTime),
                    true));
            holder.notificationDescription.setText(getNotificationDescription(mContext, notificationDetails));
        }
    }

    @Override
    public int getItemCount() {
        if(mNotificationList == null) return 0;
        return mNotificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView notificationTitle;
        final TextView boldNotificationTitle;
        final TextView notificationTime;
        final TextView boldNotificationTime;
        final TextView notificationDescription;
        final TextView boldNotificationDescription;

        public NotificationViewHolder(View itemView) {
            super(itemView);

            notificationTitle = itemView.findViewById(R.id.noti_title);
            boldNotificationTitle = itemView.findViewById(R.id.noti_title_bold);
            notificationTime = itemView.findViewById(R.id.noti_time);
            boldNotificationTime = itemView.findViewById(R.id.noti_time_bold);
            notificationDescription = itemView.findViewById(R.id.noti_description);
            boldNotificationDescription = itemView.findViewById(R.id.noti_description_bold);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mClickHandler.onClickedNotification(mNotificationList.get(position));
        }
    }
}
