package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

import com.chonbonstudios.smartplaylists.Adapters.ListOfServicesAdapter;
import com.chonbonstudios.smartplaylists.Adapters.PlaylistAdapter;
import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.ModelData.StreamingServices;



public class TransferActivity extends AppCompatActivity implements ListOfServicesAdapter.OnServiceClick {

    private RecyclerView listOfServices, listOfPlaylists;
    private RecyclerView.Adapter servicesAdapter, playlistsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StreamingServices> servicesList;
    private ArrayList<Playlist> playlistsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        listOfServices = findViewById(R.id.listStreamingServices);
        listOfPlaylists = findViewById(R.id.listPlaylistChooser);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listOfServices.setHasFixedSize(true);
        listOfPlaylists.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);

        listOfServices.setLayoutManager(layoutManager);
        //listOfPlaylists.setLayoutManager(layoutManager);

        //add horizontal divider decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        listOfServices.addItemDecoration(itemDecoration);
        listOfPlaylists.addItemDecoration(itemDecoration);

        //fill out arraylist
        createServiceList();
        createPlaylists();

        // specify an adapter and set the adapter to the list
        servicesAdapter = new ListOfServicesAdapter(servicesList, this);
        playlistsAdapter = new PlaylistAdapter(playlistsList);
        listOfServices.setAdapter(servicesAdapter);
    }


    // creates a default services list with temp data
    public void createServiceList(){
        servicesList = new ArrayList<>();
        StreamingServices temp = new StreamingServices("Spotify", true);
        servicesList.add(temp);
        temp = new StreamingServices("Apple Music", false);
        servicesList.add(temp);
    }

    //creates a default playlist list with temp data
    public void createPlaylists(){
        playlistsList = new ArrayList<>();
        playlistsList.add(new Playlist("release radar"));
        playlistsList.add(new Playlist("discover weekly"));
        playlistsList.add(new Playlist("austins mega"));
    }

    //Apple Music api

    //Search for existing playlists and populate
    public void appleSearchPlaylists(){}

    //Create or update playlists
    public void appleCreatePlaylists(ArrayList<Playlist> playlists){}


    //Spotify api

    //Search for existing playlists and populate
    public void spotifySearchPlaylists(){}

    //Create or update playlists
    public void spotifyCreatePlaylists(ArrayList<Playlist> playlists){}

    @Override
    public void onServiceClick(int position) {
        Toast.makeText(this, "POS Clicked " + position,Toast.LENGTH_SHORT).show();
    }
}
