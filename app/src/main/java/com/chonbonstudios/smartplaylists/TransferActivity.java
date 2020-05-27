package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.chonbonstudios.smartplaylists.Adapters.ListOfServicesAdapter;
import com.chonbonstudios.smartplaylists.Adapters.PlaylistAdapter;
import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.ModelData.StreamingServices;

import org.jetbrains.annotations.NotNull;


public class TransferActivity extends AppCompatActivity implements ListOfServicesAdapter.OnServiceClick {
    public static final String TAG = TransferActivity.class.getSimpleName();

    private RecyclerView listOfServices, listOfPlaylists;
    private RecyclerView.Adapter servicesAdapter, playlistsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StreamingServices> servicesList;
    private ArrayList<Playlist> playlistsList;

    OkHttpClient client;
    String token;
    DataHandler dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        dh = new DataHandler(this);
        client = new OkHttpClient();

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

        // specify an adapter and set the adapter to the list
        servicesAdapter = new ListOfServicesAdapter(servicesList, this);
        playlistsAdapter = new PlaylistAdapter(playlistsList);
        listOfServices.setAdapter(servicesAdapter);

        token = getIntent().getStringExtra("TOKEN");
        if(token != null){
            dh.writeStringData(dh.SPOTIFY, token);
            spotifySearchPlaylists(token);
        }

    }

    // creates a default services list with temp data
    public void createServiceList(){
        servicesList = new ArrayList<>();
        StreamingServices temp;

        if(dh.getSpotifyUserToken() != ""){
            temp = new StreamingServices("Spotify", true);
            spotifySearchPlaylists(token);
            servicesList.add(temp);
        } else {
            temp = new StreamingServices("Spotify", false);
            servicesList.add(temp);
        }

        if(dh.getAppleMusicUserToken() != ""){
            temp = new StreamingServices("Apple Music", true);
            appleSearchPlaylists();
            servicesList.add(temp);
        } else {
            temp = new StreamingServices("Apple Music", false);
            servicesList.add(temp);
        }

    }

    // Handle what service to go in to select playlists
    @Override
    public void onServiceClick(int position) {

    }

    //Apple Music api

    //Search for existing playlists and populate
    public void appleSearchPlaylists(){
        Request request = new Request.Builder()
                .url(getString(R.string.api_apple_get_all_playlists))
                .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                .header("Music-User-Token", dh.getAppleMusicUserToken())
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        Log.v(TAG, response.body().string());

                        appleLoadPlaylists(response);
                    }
                } catch (IOException e){
                    Log.e(TAG, "IO Exception caught: ", e);
                }

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "IO Exception caught: ", e);
            }
        });
    }

    public void appleLoadPlaylists(Response response){

    }

    //Create or update playlists
    public void appleCreatePlaylists(ArrayList<Playlist> playlists){}


    //Spotify api

    //Search for existing playlists and populate
    public void spotifySearchPlaylists(String token){
        Request request = new Request.Builder()
                .url(getString(R.string.api_spotify_getplaylists))
                .header("Authorization", "Bearer "+ token)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        Log.v(TAG, response.body().string());

                        spotifyLoadPlaylists(response);
                    }
                } catch (IOException e){
                    Log.e(TAG, "IO Exception caught: ", e);
                }

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "IO Exception caught: ", e);
            }
        });
    }

    // Loads playlists from response
    private void spotifyLoadPlaylists(Response response) {

    }

    //Create or update playlists
    public void spotifyCreatePlaylists(ArrayList<Playlist> playlists){

    }

}
