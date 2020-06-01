package com.chonbonstudios.smartplaylists.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class TransferService extends IntentService {
    private static final String TAG = TransferService.class.getSimpleName();
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private DataHandler dh;
    private ArrayList<Playlist> playlists;

    private OkHttpClient client;

    public TransferService() {
        super("TransferService");
        dh = new DataHandler(this);
        client = new OkHttpClient();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Handler handler = intent.getParcelableExtra("handler");

        playlists = dh.getPlaylistToTransfer();
        Message msg;

        if(playlists == null || playlists.size() < 1){
            msg = new Message();
            msg.obj = "Playlists object is null or less than 1";
            msg.what = STATUS_ERROR;
            handler.sendMessage(msg);
        }

        //First Step is to fill the song arrays of each playlist chosen
        //Spotify returns a track array of 100 at a time, so a For array is necessary to get all
        //the tracks
        //Apple music is the same as spotify, returns only 100 at a time
        switch(playlists.get(0).getSource()){
            case "APPLE_MUSIC": break;
            case "SPOTIFY": apiSpotifyGetTracks(); break;
            default: msg = new Message();
                msg.obj = "Playlists source not supported at this time";
                msg.what = STATUS_ERROR;
                handler.sendMessage(msg); break;
        }
        //Second step is to search the destination service for the tracks and get an id of each track

        //Third step is to create the playlist and then submit all the track ids found

    }

    //APi Calls

    //Apple Music
    //Get Tracks


    //Spotify
    //Get Tracks this will iterate through each playlist selected and make the first call to get tracks
    //if paging is required then it will offload paging to a helper method
    public void apiSpotifyGetTracks(){
        for(int i = 0; i < playlists.size(); i++){
            Request request = new Request.Builder()
                    .url(getString(R.string.api_spotify_gettracks, playlists.get(i).getId()))
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
                            try {
                                JSONObject object = new JSONObject(res);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }else {
                            Log.e(TAG, "Not Successful: " + response.body().string());
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
    }

    //Get tracks helper method, this will iterate through a single playlist and get all tracks
    // deals with paging
    public void apiSpotifyGetTracksHelper(final int playlistPos, JSONObject playlistItems){
        int trackTotal = 0;
        int offset = 0;
        try {
            trackTotal = playlistItems.getInt("total");
            //Round up to find the amount of times we need to iterate through the paging
            for(int i = 0; i < (int) Math.ceil((double)trackTotal/100); i++){
                Request request = new Request.Builder()
                        .url(getString(R.string.api_spotify_gettracks, playlists.get(playlistPos).getId())+ "?offset=" + offset)
                        .header("Authorization", "Bearer "+ dh.getSpotifyUserToken())
                        .build();

                Call call = client.newCall(request);

                final CountDownLatch countDownLatch = new CountDownLatch(1);

                call.enqueue(new Callback(){

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response){
                        try {
                            if(response.isSuccessful()){
                                String res = response.body().string();
                                Log.v(TAG, res);
                                try {
                                    JSONObject object = new JSONObject(res);
                                    parseSpotifyTracks(object.getJSONArray("items"), playlistPos);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }else {
                                Log.e(TAG, "Not Successful: " + response.body().string());
                            }
                            countDownLatch.countDown();
                        } catch (IOException e){
                            Log.e(TAG, "IO Exception caught: ", e);
                            countDownLatch.countDown();
                        }

                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
                // Iterate the offset by 100
                offset += 100;
            }
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();

        }

    }

    public void parseSpotifyTracks(JSONArray res, int playlistPos){

    }

}
