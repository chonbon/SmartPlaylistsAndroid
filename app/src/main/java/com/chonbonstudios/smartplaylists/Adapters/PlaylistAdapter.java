package com.chonbonstudios.smartplaylists.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.squareup.picasso.Picasso;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder>  {
    private ArrayList<Playlist> playlists;
    private Context c;

    //constructor
    public PlaylistAdapter(ArrayList<Playlist> playlists, Context c){
        this.playlists = playlists;
        this.c = c;
    }

    //direct ref to view
    //Direct ref to each item in the view
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView playlistName;
        ImageView playlistArt;
        CheckBox selected;
        MyViewHolder(LinearLayout v) {
            super(v);
            playlistName = v.findViewById(R.id.txtPlaylistName);
            playlistArt = v.findViewById(R.id.imgPlaylistCover);
            selected = v.findViewById(R.id.checkPlaylist);
        }
    }

    //Inflate the view that is the list design
    @NonNull
    @Override
    public PlaylistAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_playlist, parent, false);

        PlaylistAdapter.MyViewHolder vh = new PlaylistAdapter.MyViewHolder(v);
        return vh;
    }
    //replace contents of the view
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Playlist playlist = playlists.get(position);
        holder.playlistName.setText(playlist.getName());
        holder.selected.setOnCheckedChangeListener(null);

        holder.selected.setChecked(playlist.isSelected());
        holder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                playlist.setSelected(isChecked);
            }
        });

        if(playlist.getImageUrl() != ""){
            Picasso.get().load(playlist.getImageUrl()).into(holder.playlistArt);
        } else {
            holder.playlistArt.setImageResource(R.drawable.stock_playlist_art);
        }

        if(position%10 == 0){
            //Ad load
            AdLoader adLoader = new AdLoader.Builder(c, c.getString(R.string.native_ad_list_v1))
                    .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            // Show the ad.
                            
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // Handle the failure by logging, altering the UI, and so on.
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder()
                            // Methods in the NativeAdOptions.Builder class can be
                            // used here to specify individual options settings.
                            .build())
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }


    }

    //return count of playlists
    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void clear() {
        playlists.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(ArrayList<Playlist> list) {
        playlists.addAll(list);
        notifyDataSetChanged();
    }
}
