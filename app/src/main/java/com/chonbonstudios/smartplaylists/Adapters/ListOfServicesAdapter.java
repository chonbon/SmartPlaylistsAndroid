package com.chonbonstudios.smartplaylists.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chonbonstudios.smartplaylists.ModelData.StreamingServices;
import com.chonbonstudios.smartplaylists.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListOfServicesAdapter extends RecyclerView.Adapter<ListOfServicesAdapter.MyViewHolder> {
    private ArrayList<StreamingServices> mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListOfServicesAdapter(ArrayList<StreamingServices> myDataset) {
        mDataset = myDataset;
    }


    //Direct ref to each item in the view
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        MyViewHolder(LinearLayout v) {
            super(v);
            serviceName = v.findViewById(R.id.txtServiceListName);
        }
    }



    //Inflate the view that is the list design
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_services, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.serviceName.setText(mDataset.get(position).getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
