package com.chonbonstudios.smartplaylists.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.R;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder>  {
    private ArrayList<Playlist> playlists;

    //constructor
    public PlaylistAdapter(ArrayList<Playlist> playlists){
        this.playlists = playlists;
    }

    //direct ref to view
    //Direct ref to each item in the view
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        ConstraintLayout layout;
        MyViewHolder(LinearLayout v) {
            super(v);
            serviceName = v.findViewById(R.id.txtServiceListName);
            layout = v.findViewById(R.id.containerList);
        }
    }

    //Inflate the view that is the list design
    @NonNull
    @Override
    public PlaylistAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_services, parent, false);

        PlaylistAdapter.MyViewHolder vh = new PlaylistAdapter.MyViewHolder(v);
        return vh;
    }
    //replace contents of the view
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

    }

    //return count of playlists
    @Override
    public int getItemCount() {
        return playlists.size();
    }
}
