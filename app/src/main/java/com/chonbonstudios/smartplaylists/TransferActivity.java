package com.chonbonstudios.smartplaylists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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
    private Button btnTransfer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListOfServicesAdapter servicesAdapter;
    private PlaylistAdapter playlistsAdapter;
    private RecyclerView.LayoutManager layoutManager,layoutManager2;
    private ArrayList<StreamingServices> servicesList;
    private ArrayList<Playlist> showList;
    private ArrayList<Playlist> transferList = new ArrayList<>();

    private boolean serviceListActive = false;
    private boolean selectingDestination = false;
    private OkHttpClient client;
    private DataHandler dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        //init client and datahandler
        dh = new DataHandler(this);
        client = new OkHttpClient();

        //fill out arraylist
        createServiceList();

        //find views
        listOfServices = findViewById(R.id.listStreamingServices);
        listOfPlaylists = findViewById(R.id.listPlaylistChooser);
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        btnTransfer = findViewById(R.id.btnTransfer);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listOfServices.setHasFixedSize(true);
        listOfPlaylists.setHasFixedSize(false);

        //add horizontal divider decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        listOfServices.addItemDecoration(itemDecoration);
        listOfPlaylists.addItemDecoration(itemDecoration);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        layoutManager2 = new LinearLayoutManager(this);

        listOfServices.setLayoutManager(layoutManager);
        listOfPlaylists.setLayoutManager(layoutManager2);


        // specify an adapter and set the adapter to the list
        servicesAdapter = new ListOfServicesAdapter(servicesList, this);
        listOfServices.setAdapter(servicesAdapter);
        serviceListActive = true;

        showList = new ArrayList<>();
        playlistsAdapter = new PlaylistAdapter(showList, this);
        listOfPlaylists.setAdapter(playlistsAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                if(showList.get(0).getSource().equals("SPOTIFY")){
                    spotifySearchPlaylists();
                } else {
                    appleSearchPlaylists();
                }

            }
        });
    }

    // creates a default services list with temp data
    public void createServiceList(){
        servicesList = new ArrayList<>();
        StreamingServices temp;

        if(!dh.getSpotifyUserToken().equals("")){
            temp = new StreamingServices("Spotify", true);
            servicesList.add(temp);
        } else {
            temp = new StreamingServices("Spotify", false);
            servicesList.add(temp);
        }

        if(!dh.getAppleMusicUserToken().equals("")){
            temp = new StreamingServices("Apple Music", true);
            servicesList.add(temp);
        } else {
            temp = new StreamingServices("Apple Music", false);
            servicesList.add(temp);
        }

    }

    // Handle what service to go in to select playlists
    @Override
    public void onServiceClick(int position) {
        playlistsAdapter.clear();
        if(position == 0){
            if(!dh.getSpotifyUserToken().equals("")) {
                if(selectingDestination){
                    dh.writeIntData(dh.DEST, dh.DEST_SPOTIFY);
                    for(int i = 0; i < showList.size(); i++){
                        if(showList.get(i).isSelected()){
                            transferList.add(showList.get(i));
                        }
                    }
                    dh.writePlaylistTransferList(transferList);
                    startActivity(new Intent(TransferActivity.this, ResultActivity.class));
                    finish();
                } else {
                    spotifySearchPlaylists();
                    listOfServices.setVisibility(View.INVISIBLE);
                    listOfPlaylists.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    serviceListActive = false;
                    btnTransfer.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if(!dh.getAppleMusicUserToken().equals("")) {
                if(selectingDestination){
                    dh.writeIntData(dh.DEST, dh.DEST_APPLEMUSIC);
                    for(int i = 0; i < showList.size(); i++){
                        if(showList.get(i).isSelected()){
                            transferList.add(showList.get(i));
                        }
                    }
                    dh.writePlaylistTransferList(transferList);
                    startActivity(new Intent(TransferActivity.this, ResultActivity.class));
                    finish();
                } else {
                    appleSearchPlaylists();
                    listOfServices.setVisibility(View.INVISIBLE);
                    listOfPlaylists.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    serviceListActive = false;
                    btnTransfer.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            startActivity(new Intent(TransferActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    //On back button pressed, revert to last state first before going back in stack
    @Override
    public void onBackPressed() {
        if(serviceListActive){
            super.onBackPressed();
        } else {
            serviceListActive = true;
            listOfPlaylists.setVisibility(View.INVISIBLE);
            listOfServices.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.INVISIBLE);
            btnTransfer.setVisibility(View.INVISIBLE);
        }
    }

    //Transfer Button Clicked
    public void onClickTransfer(View view){
        //go back to services list
        serviceListActive = true;
        selectingDestination = true;
        listOfPlaylists.setVisibility(View.INVISIBLE);
        listOfServices.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        btnTransfer.setVisibility(View.INVISIBLE);
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
            public void onResponse(@NotNull Call call, @NotNull Response response){
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

    //Parse playlists into objects
    public void appleLoadPlaylists(String response) {
        showList = new ArrayList<>();
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
                showList.add(new Playlist(temp.getJSONObject("attributes").getString("name"),
                        temp.getString("id"),"APPLE_MUSIC"));
                showList.get(i).setImageUrl(temp.getJSONObject("attributes")
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
                    playlistsAdapter.addAll(showList);
                    swipeRefreshLayout.setRefreshing(false);

                }
            });

        }

    }

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
            public void onResponse(@NotNull Call call, @NotNull Response response){
                try {
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        Log.v(TAG, res);

                        spotifyLoadPlaylists(res);
                    }else {
                        spotifyGetRefreshToken();
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
        showList = new ArrayList<>();
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
                showList.add(new Playlist(temp.getString("name"),
                        temp.getString("id"),"SPOTIFY"));
                showList.get(i).setImageUrl(temp.getJSONArray("images")
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
                    playlistsAdapter.addAll(showList);
                    swipeRefreshLayout.setRefreshing(false);

                }
            });
        }
    }

    //Refresh Token if expired
    public void spotifyGetRefreshToken(){
        String base = getString(R.string.spotify_client_id) + ":" + getString(R.string.spotify_client_secret);
        String encoded = Base64.encodeToString(base.getBytes(),Base64.NO_WRAP);

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token",dh.getSpotifyRefreshToken()).build();
        Request request = new Request.Builder()
                .url(getString(R.string.api_spotify_accesss))
                .post(requestBody)
                .header("Authorization", "Basic "+ encoded)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response){
                try {
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        Log.v(TAG, res);
                        JSONObject object = new JSONObject(res);
                        dh.writeStringData(dh.SPOTIFY, object.getString("access_token"));
                        //dh.writeStringData(dh.SPOTIFY_REFRESH, object.getString("refresh_token"));
                        spotifySearchPlaylists();
                    }
                    Log.e(TAG, response.toString());
                } catch (IOException | JSONException e){
                    Log.e(TAG, "IO Exception caught: ", e);
                }

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "IO Exception caught: ", e);
            }
        });
    }

}
