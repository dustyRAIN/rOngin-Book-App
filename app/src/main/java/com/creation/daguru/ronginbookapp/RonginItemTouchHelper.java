package com.creation.daguru.ronginbookapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

public class RonginItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    protected static final String SLIDE_KEY_CANT_BE_DELETED = "cantDelete";

    private DatabaseReference mDatabaseReference;
    private Context mContext;

    public RonginItemTouchHelper(int dragDirs, int swipeDirs, Context context, DatabaseReference databaseReference) {
        super(dragDirs, swipeDirs);
        mDatabaseReference = databaseReference;
        mContext = context;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        String key = (String) viewHolder.itemView.getTag();
        if(!key.equals(SLIDE_KEY_CANT_BE_DELETED)){
            mDatabaseReference.child(key).setValue(null);
        } else {
            Toast.makeText(mContext, "Can't be removed.", Toast.LENGTH_LONG).show();
        }
    }
}
