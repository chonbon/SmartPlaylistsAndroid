package com.chonbonstudios.smartplaylists.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chonbonstudios.smartplaylists.ModelData.DataHandler;
import com.chonbonstudios.smartplaylists.ModelData.Playlist;
import com.chonbonstudios.smartplaylists.ModelData.Song;
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
    private ArrayList<Playlist> transferPlaylists;
    private int playlistsFinished = 0;
    private int songsToTransfer = 0;
    private int songsFound = 0;

    private OkHttpClient client;
    private Message msg;


    public TransferService() {
        super("TransferService");
        client = new OkHttpClient();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Messenger handler = intent.getParcelableExtra("handler");
        dh = new DataHandler(this);

        playlists = dh.getPlaylistToTransfer();


        if(playlists == null || playlists.size() < 1){
            msg = new Message();
            msg.obj = "Playlists object is null or less than 1";
            msg.what = STATUS_ERROR;
            try {
                handler.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        //First Step is to fill the song arrays of each playlist chosen
        //Spotify returns a track array of 100 at a time, so a For array is necessary to get all
        //the tracks
        //Apple music is the same as spotify, returns only 100 at a time
        switch(playlists.get(0).getSource()){
            case "APPLE_MUSIC": break;
            case "SPOTIFY": apiSpotifyGetTracks(handler); break;
            default: msg = new Message();
                msg.obj = "Playlists source not supported at this time";
                msg.what = STATUS_ERROR;
                try {
                    handler.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
        //Second step is to search the destination service for the tracks and get an id of each track
        //this is called from apiGetTrackHelper

        //Third step is to create the playlist and then submit all the track ids found

    }

    public void findSongsAtDest(Messenger handler){
        switch(dh.getDestinationService()){
            case DataHandler.DEST_SPOTIFY: //call to search music
                                        break;
            case DataHandler.DEST_APPLEMUSIC: apiFindSongsOnAppleMusic(handler);//call to search music
                                        break;

            default: msg = new Message();
                msg.obj = "Destination is not specified or supported!";
                msg.what = STATUS_ERROR;
                try {
                    handler.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } break;

        }
    }

    //APi Calls

    //Apple Music
    //Get Tracks

    //Search music on apple
    public void apiFindSongsOnAppleMusic(Messenger handler){
        transferPlaylists = new ArrayList<>();

        for(int i = 0; i < playlists.size(); i++){
            transferPlaylists.add(new Playlist(playlists.get(i).getName(),"APPLE_MUSIC"));
            for(int j = 0; j < playlists.get(i).getTracks().size(); j++){
                Song song = playlists.get(i).getTracks().get(j);
                String termSearch = (song.getTrack() + song.getArtist()).replace(' ', '+');

                Request request = new Request.Builder()
                        .url(getString(R.string.api_apple_search_track) + "?term="+termSearch+"&type=songs")
                        .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                        .header("Music-User-Token", dh.getAppleMusicUserToken())
                        .build();

                try(Response response = client.newCall(request).execute()){
                    if(response.isSuccessful()){
                        Log.v(TAG,"Apple Music Find Songs Response: " + response.body().string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //Spotify
    //Get Tracks this will iterate through each playlist selected and make the first call to get tracks
    //if paging is required then it will offload paging to a helper method
    public void apiSpotifyGetTracks(Messenger handler){
        for(int i = 0; i < playlists.size(); i++){
            Request request = new Request.Builder()
                    .url(getString(R.string.api_spotify_gettracks, playlists.get(i).getId()))
                    .header("Authorization", "Bearer "+ dh.getSpotifyUserToken())
                    .build();


            // ensure the response (and underlying response body) is closed
            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    songsToTransfer += jsonObject.getInt("total");

                    msg = new Message();
                    msg.obj = songsToTransfer + " songs to transfer";
                    msg.what = STATUS_RUNNING;
                    handler.send(msg);

                    apiSpotifyGetTracksHelper(i,jsonObject,handler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    //Get tracks helper method, this will iterate through a single playlist and get all tracks
    // deals with paging
    public void apiSpotifyGetTracksHelper(final int playlistPos, JSONObject playlistItems, Messenger handler){
        int trackTotal = 0;
        int offset = 0;
        try {
            trackTotal = playlistItems.getInt("total");
            final int offsetTotal = (int) Math.ceil((double)trackTotal/100);
            //Round up to find the amount of times we need to iterate through the paging
            for(int i = 0; i < offsetTotal; i++){
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

            // determine if the last parse just happened to move on to finding the songs
            if(playlistPos == playlists.size() -1){
                findSongsAtDest(handler);
            }
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();

        }

    }

    //parses song arrays from response and adds them to the playlist song arraylist
    public void parseSpotifyTracks(JSONArray res, int playlistPos){

        Song song;
        JSONObject jsonObject;

        for(int i = 0; i < res.length(); i++){
            try {
                jsonObject = res.getJSONObject(i);

                song = new Song(jsonObject.getJSONObject("track").getJSONArray("artists")
                        .getJSONObject(0).getString("name"),
                        jsonObject.getJSONObject("track").getString("name"),
                        jsonObject.getJSONObject("track").getJSONObject("album")
                        .getString("name"));

                playlists.get(playlistPos).addTrack(song);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
