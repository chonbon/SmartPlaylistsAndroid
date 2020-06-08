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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TransferService extends IntentService {
    private static final String TAG = TransferService.class.getSimpleName();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
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

    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
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
                String termSearch = (song.getTrack() + "+" + song.getArtist());
                termSearch = termSearch.replace("&", "");
                termSearch = termSearch.replace("?", "");
                termSearch = termSearch.replace("#", "");
                termSearch.replace(' ', '+');
                Log.v(TAG, "Term Search: " + termSearch);

                Request request = new Request.Builder()
                        .url(getString(R.string.api_apple_search_track) + "?term="+termSearch+"&limit=20"+"&types=songs")
                        .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                        .build();

                try(Response response = client.newCall(request).execute()){
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        //Log.v(TAG,"Apple Music Find Songs Response: " + res);
                        appleMusicMatchSong(res,i,song,false);
                    } else {
                        Log.v(TAG,"Failed " + response.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            apiCreatePlaylistsAppleMusic(handler);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Takes in a response from the search query and parses the songs, finds the best match and adds
    // the song to the array
    public void appleMusicMatchSong(String res, int playlistPos, Song currentTrack, boolean lastSearch){
        try {
            JSONArray songArray = new JSONObject(res).getJSONObject("results")
                    .getJSONObject("songs").getJSONArray("data");
            int songPos = -1;
            int diffName = 900;
            boolean matched = false;

            for(int i = 0; i < songArray.length(); i++){
                //Apple current track details
                JSONObject songObject = songArray.getJSONObject(i);
                String artistName = songObject.getJSONObject("attributes")
                        .getString("artistName");
                artistName = deAccent(artistName);
                String songName = songObject.getJSONObject("attributes")
                        .getString("name").replace("(", "")
                        .replace(")", "").replace("-", "");
                songName = deAccent(songName);
                String albumName = songObject.getJSONObject("attributes")
                        .getString("albumName");

                //source current track details
                String sourceArtist = currentTrack.getArtist();
                sourceArtist = deAccent(sourceArtist);
                String sourceName = currentTrack.getTrack().replace("(", "")
                        .replace(")", "").replace("-", "");
                sourceName = deAccent(sourceName);
                String sourceAlbum = currentTrack.getAlbum();

                int diff = songName.compareToIgnoreCase(sourceName);

                Log.v(TAG,"Comparison:Source " + sourceArtist + " : " + sourceName +
                        " vs apple " + artistName + " : " + songName);

                Log.v(TAG, "Compare Value = Artist Name: " +
                        artistName.compareToIgnoreCase(sourceArtist) + " song name: " +
                        diff);

                if(artistName.toLowerCase().contains(sourceArtist.toLowerCase()) ||
                        sourceArtist.toLowerCase().contains(artistName.toLowerCase())){

                    if(songName.toLowerCase().contains(sourceName.toLowerCase()) ||
                            diff >= 0){

                        //exact match
                        if(songName.compareToIgnoreCase(sourceName) == 0) {
                            Log.v(TAG, "Song Matched! " + songName);
                            Song song = new Song(artistName, songName, albumName);
                            song.setId(songObject.getString("id"));
                            transferPlaylists.get(playlistPos).addTrack(song);
                            matched = true;
                            break;
                        }

                        //Not an exact match, search and find the closest to zero.
                        else {

                            //test against last one
                            if(diff < diffName) {
                                songPos = i;
                                diffName = diff;
                            }

                        }

                    }
                }
            }

            //testing results
            if(diffName != 900 && !matched){
                JSONObject songObject = songArray.getJSONObject(songPos);
                String artistName = songObject.getJSONObject("attributes")
                        .getString("artistName");
                artistName = deAccent(artistName);
                String songName = songObject.getJSONObject("attributes")
                        .getString("name").replace("(", "")
                        .replace(")", "").replace("-", "");
                songName = deAccent(songName);
                String albumName = songObject.getJSONObject("attributes")
                        .getString("albumName");
                Log.v(TAG, "Song Matched! " + songName);
                Song song = new Song(artistName, songName, albumName);
                song.setId(songObject.getString("id"));
                transferPlaylists.get(playlistPos).addTrack(song);
            } else if(matched){
                //do nothing, we matched
            } else {
                //retest with another search query
                if(!lastSearch) {
                    apiFindSongReverseSearchApple(currentTrack, playlistPos);
                } else {
                    transferPlaylists.get(playlistPos).addUnMatchedTracks(currentTrack);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //array or response body error, retry call artist only
            if(!lastSearch) {
                apiFindSongReverseSearchApple(currentTrack, playlistPos);
            } else {
                transferPlaylists.get(playlistPos).addUnMatchedTracks(currentTrack);
            }
        }
    }

    public void apiFindSongReverseSearchApple(Song song, int playlistPos){
        String termSearch = (song.getArtist() + "+" + song.getTrack());
        termSearch = termSearch.replace("&", "");
        termSearch = termSearch.replace("?", "");
        termSearch = termSearch.replace("#", "");
        termSearch.replace(' ', '+');
        Log.v(TAG, "Term Search: " + termSearch);

        Request request = new Request.Builder()
                .url(getString(R.string.api_apple_search_track) + "?term="+termSearch+"&limit=20"+"&types=songs")
                .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                .build();

        try(Response response = client.newCall(request).execute()){
            if(response.isSuccessful()){
                String res = response.body().string();
                //Log.v(TAG,"Apple Music Find Songs Response: " + res);
                appleMusicMatchSong(res,playlistPos,song,true);
            } else {
                Log.v(TAG,"Failed " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //this will create all playlists and add all tracks from said playlists
    public void apiCreatePlaylistsAppleMusic(Messenger Handler) throws JSONException, IOException {
        for(int i = 0; i < transferPlaylists.size();i++){
            // Create playlistObject that will be sent to apple music
            JSONObject playlistObject = new JSONObject();
            // Create attributes object to add
            JSONObject attributesObject = new JSONObject();
            attributesObject.put("name", transferPlaylists.get(i).getName());
            attributesObject.put("description", "Playlist transfered from " +
                    playlists.get(i).getSource() +
                    " by Smart Playlists! Get on Google Play or App Store now!");

            playlistObject.put("attributes",attributesObject);

            //Create data array of songs
            JSONArray dataArray = new JSONArray();
            for(int j = 0; j < transferPlaylists.get(i).getTracks().size();j++){
                JSONObject dataObject = new JSONObject();
                dataObject.put("id", transferPlaylists.get(i).getTracks().get(j).getId());
                dataObject.put("type", "songs");
                dataArray.put(dataObject);
            }

            //add data array to tracks object
            JSONObject tracksObject = new JSONObject();
            tracksObject.put("data", dataArray);

            JSONObject relationshipsObject = new JSONObject();
            relationshipsObject.put("tracks", tracksObject);

            //finally add relationships object to playlistObject
            playlistObject.put("relationships",relationshipsObject);

            Log.v(TAG, playlistObject.toString());
            // send object via post

            RequestBody body = RequestBody.create(playlistObject.toString(), JSON); // new
            // RequestBody body = RequestBody.create(JSON, json); // old
            Request request = new Request.Builder()
                    .url(getString(R.string.api_apple_create_playlists))
                    .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                    .header("Music-User-Token", dh.getAppleMusicUserToken())
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    Log.v(TAG, "create playlist success " + response.body().string());
                    Log.v(TAG, "Couldnt match " +
                            transferPlaylists.get(i).getUnMatchedTracks().size() + " Songs");
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
