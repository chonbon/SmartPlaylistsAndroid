package com.chonbonstudios.smartplaylists.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chonbonstudios.smartplaylists.ModelData.StreamingServices;
import com.chonbonstudios.smartplaylists.R;

public class ListOfServicesAdapter extends RecyclerView.Adapter<ListOfServicesAdapter.MyViewHolder> {
    private ArrayList<StreamingServices> mDataset;
    private OnServiceClick mOnServiceClick;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListOfServicesAdapter(ArrayList<StreamingServices> myDataset, OnServiceClick onServiceClick) {
        mDataset = myDataset;
        mOnServiceClick = onServiceClick;
    }


    //Direct ref to each item in the view
    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView serviceName;
        ConstraintLayout layout;
        OnServiceClick onServiceClick2;
        MyViewHolder(LinearLayout v,OnServiceClick onServiceClick) {
            super(v);
            serviceName = v.findViewById(R.id.txtServiceListName);
            layout = v.findViewById(R.id.containerList);
            onServiceClick2 = onServiceClick;
            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onServiceClick2.onServiceClick(getAdapterPosition());
        }
    }



    //Inflate the view that is the list design
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_services, parent, false);

        MyViewHolder vh = new MyViewHolder(v,mOnServiceClick);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.serviceName.setText(mDataset.get(position).getName());

        if(!mDataset.get(position).isSignedIn()){
            holder.layout.setBackgroundResource(R.color.gray);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface OnServiceClick {
        void onServiceClick(int position);
    }
}
