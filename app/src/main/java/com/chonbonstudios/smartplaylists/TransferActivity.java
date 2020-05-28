package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

import com.chonbonstudios.smartplaylists.Adapters.ListOfServicesAdapter;
import com.chonbonstudios.smartplaylists.Adapters.PlaylistAdapter;
import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.ModelData.StreamingServices;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TransferActivity extends AppCompatActivity implements ListOfServicesAdapter.OnServiceClick {
    public static final String TAG = TransferActivity.class.getSimpleName();

    private RecyclerView listOfServices, listOfPlaylists;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.Adapter servicesAdapter;
    private PlaylistAdapter playlistsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StreamingServices> servicesList;
    private ArrayList<Playlist> spotifyList;
    private ArrayList<Playlist> appleList;

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
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listOfServices.setHasFixedSize(true);
        listOfPlaylists.setHasFixedSize(false);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);

        listOfServices.setLayoutManager(layoutManager);


        //add horizontal divider decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        listOfServices.addItemDecoration(itemDecoration);
        listOfPlaylists.addItemDecoration(itemDecoration);

        //fill out arraylist
        createServiceList();

        // specify an adapter and set the adapter to the list
        servicesAdapter = new ListOfServicesAdapter(servicesList, this);

        listOfServices.setAdapter(servicesAdapter);

        token = getIntent().getStringExtra("TOKEN");
        if(token != null){
            dh.writeStringData(dh.SPOTIFY, token);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                if(dh.getSpotifyUserToken() != ""){
                    spotifySearchPlaylists();
                }

                if(dh.getAppleMusicUserToken() != ""){
                    appleSearchPlaylists();
                }
            }
        });

    }

    // creates a default services list with temp data
    public void createServiceList(){
        servicesList = new ArrayList<>();
        StreamingServices temp;

        if(dh.getSpotifyUserToken() != ""){
            temp = new StreamingServices("Spotify", true);
            spotifySearchPlaylists();
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
        if(position == 0){
            if(dh.getSpotifyUserToken() != "") {
                layoutManager = new LinearLayoutManager(this);
                listOfPlaylists.setLayoutManager(layoutManager);
                playlistsAdapter = new PlaylistAdapter(spotifyList, this);
                listOfPlaylists.setAdapter(playlistsAdapter);
                listOfServices.setVisibility(View.INVISIBLE);
                listOfPlaylists.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if(dh.getAppleMusicUserToken() != "") {
                layoutManager = new LinearLayoutManager(this);
                listOfPlaylists.setLayoutManager(layoutManager);
                playlistsAdapter = new PlaylistAdapter(appleList, this);
                listOfPlaylists.setAdapter(playlistsAdapter);
                listOfServices.setVisibility(View.INVISIBLE);
                listOfPlaylists.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
            }
        }
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
                        String res = response.body().string();
                        Log.v(TAG, res);

                        appleLoadPlaylists(res);
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

    public void appleLoadPlaylists(String response) {
        appleList = new ArrayList<>();
        JSONArray jArray = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject(response);
            jArray = jsonObject.getJSONArray("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jArray.length(); i++) {
            try {
                JSONObject temp = jArray.getJSONObject(i);
                appleList.add(new Playlist(temp.getJSONObject("attributes").getString("name"),
                        temp.getString("id")));
                appleList.get(i).setImageUrl(temp.getJSONObject("attributes")
                        .getJSONObject("artwork").getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (playlistsAdapter != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI
                    playlistsAdapter.clear();
                    playlistsAdapter.addAll(appleList);
                    swipeRefreshLayout.setRefreshing(false);

                }
            });

        }

    }

    //Create or update playlists
    public void appleCreatePlaylists(ArrayList<Playlist> playlists){}


    //Spotify api

    //Search for existing playlists and populate
    public void spotifySearchPlaylists(){
        Request request = new Request.Builder()
                .url(getString(R.string.api_spotify_getplaylists))
                .header("Authorization", "Bearer "+ dh.getSpotifyUserToken())
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        Log.v(TAG, res);

                        spotifyLoadPlaylists(res);
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
    private void spotifyLoadPlaylists(String response) {
        spotifyList = new ArrayList<>();
        JSONArray jArray = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject(response);
            jArray = jsonObject.getJSONArray("items");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < jArray.length(); i++){
            try {
                JSONObject temp = jArray.getJSONObject(i);
                spotifyList.add(new Playlist(temp.getString("name"),
                        temp.getString("id")));
                spotifyList.get(i).setImageUrl(temp.getJSONArray("images")
                        .getJSONObject(0).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (playlistsAdapter != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI
                    playlistsAdapter.clear();
                    playlistsAdapter.addAll(spotifyList);
                    swipeRefreshLayout.setRefreshing(false);

                }
            });
        }
    }

    //Create or update playlists
    public void spotifyCreatePlaylists(ArrayList<Playlist> playlists){

    }

}
