package com.chonbonstudios.smartplaylists.Services;

import android.app.IntentService;
import android.content.Intent;
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
    private int songsNotFound = 0;

    private OkHttpClient client;
    private Message msg;


    JSONObject messageContent = new JSONObject();

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
                stopSelf();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        //First Step is to fill the song arrays of each playlist chosen
        switch(playlists.get(0).getSource()){
            case "APPLE_MUSIC": apiAppleGetTracks(handler);break;
            case "SPOTIFY": apiSpotifyGetTracks(handler); break;
            default: msg = new Message();
                msg.obj = "Playlists source not supported at this time";
                msg.what = STATUS_ERROR;
                try {
                    handler.send(msg);
                    stopSelf();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    //method used to determine where the songs are supposed to go
    public void findSongsAtDest(Messenger handler){
        switch(dh.getDestinationService()){
            case DataHandler.DEST_SPOTIFY: apiFindSongsOnSpotify(handler);//call to search music
                                        break;
            case DataHandler.DEST_APPLEMUSIC: apiFindSongsOnAppleMusic(handler);//call to search music
                                        break;

            default: msg = new Message();
                msg.obj = "Destination is not specified or supported!";
                msg.what = STATUS_ERROR;
                try {
                    handler.send(msg);
                    stopSelf();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } break;

        }
    }

    //gets rid of accented characters for good!
    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    //gets rid of the Feat. Tag in songs
    private String removeFeat(String songName) {
        if(songName.contains("feat.")){
            int index = songName.indexOf("feat.");
            songName = songName.substring(0,index -1);
        }
        return songName;
    }

    //APi Calls

    //Apple Music


    //Source Calls

    //Get tracks, this will iterate through each playlist selected and make the first call to get tracks
    //if paging is require then it will offload paging to a helper method
    public void apiAppleGetTracks(Messenger handler){
        updateMessage(handler, 1);
        for(int i = 0; i < playlists.size(); i++){
            Request request = new Request.Builder()
                    .url(getString(R.string.api_apple_get_tracks_from_playlist)+playlists.get(i).getId()+"?include=tracks")
                    .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                    .header("Music-User-Token", dh.getAppleMusicUserToken())
                    .build();


            // ensure the response (and underlying response body) is closed
            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Log.v(TAG, jsonObject.toString());
                    //updateMessage(handler,1);
                    apiAppleGetTracksHelper(i,jsonObject.getJSONArray("data")
                            .getJSONObject(0).getJSONObject("relationships").getJSONObject("tracks"),handler);
                }
                Log.v(TAG, "apple get tracks failed: " + response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //Get tracks helper method, this will iterate though a single playlist and get all tracks
    //deals with paging
    public void apiAppleGetTracksHelper(final int playlistPos, JSONObject playlistItems, final Messenger handler){
        int offset = 0;
        boolean isLarger;
        boolean hasMore;

        try {
            if(playlistItems.getString("next") != null){
                isLarger = true;
            } else {
                isLarger = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            isLarger = false;
        }

        if(isLarger){
            hasMore = true;

            while(hasMore){

                Request request = new Request.Builder()
                        .url(getString(R.string.api_apple_get_tracks_from_playlist)+playlists.get(playlistPos).getId()+"/tracks?offset=" + offset)
                        .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                        .header("Music-User-Token", dh.getAppleMusicUserToken())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        Log.v(TAG, res);
                        try {
                            JSONObject object = new JSONObject(res);
                            JSONArray array = object.getJSONArray("data");
                            parseAppleTracks(handler, array, playlistPos);
                            // Iterate the offset by 100
                            offset += 100;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Log.e(TAG, "Not Successful: " + response.body().string());
                        hasMore = false;
                    }
                } catch (IOException e){
                    Log.e(TAG, "IO Exception caught: ", e);
                }

            }

        } else {
            try {
                parseAppleTracks(handler,playlistItems.getJSONArray("data"),playlistPos);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(playlistPos == playlists.size() -1){
            findSongsAtDest(handler);
        }

    }

    //parses tracks from apples response
    public void parseAppleTracks(Messenger handler, JSONArray res, int playlistPos){
        songsToTransfer += res.length();
        updateMessage(handler, 1);
        Song song;
        for(int i =0; i < res.length(); i++){
            try {
                JSONObject currentSong = res.getJSONObject(i).getJSONObject("attributes");
                song = new Song(currentSong.getString("artistName"),
                        currentSong.getString("name"),
                        currentSong.getString("albumName"));

                playlists.get(playlistPos).addTrack(song);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    //Destination Calls

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
                        appleMusicMatchSong(handler,res,i,song,false);
                    } else {
                        Log.v(TAG,"Failed " + response.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            updateMessage(handler,3);
            apiCreatePlaylistsAppleMusic(handler);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Takes in a response from the search query and parses the songs, finds the best match and adds
    // the song to the array
    public void appleMusicMatchSong(Messenger handler, String res, int playlistPos, Song currentTrack, boolean lastSearch){
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
                            songsFound+=1;
                            updateMessage(handler, 2);
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
                songsFound+=1;
                updateMessage(handler, 2);
            } else if(matched){
                //do nothing, we matched
            } else {
                //retest with another search query
                if(!lastSearch) {
                    apiFindSongReverseSearchApple(handler,currentTrack, playlistPos);
                } else {
                    transferPlaylists.get(playlistPos).addUnMatchedTracks(currentTrack);
                    songsNotFound+=1;
                    updateMessage(handler, 2);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //array or response body error, retry call artist only
            if(!lastSearch) {
                apiFindSongReverseSearchApple(handler,currentTrack, playlistPos);
            } else {
                transferPlaylists.get(playlistPos).addUnMatchedTracks(currentTrack);
                songsNotFound+=1;
                updateMessage(handler, 2);
            }
        }
    }

    //swaps artist and song name for search, helps with some querys if nothing is found originally
    public void apiFindSongReverseSearchApple(Messenger handler, Song song, int playlistPos){
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
                appleMusicMatchSong(handler,res,playlistPos,song,true);
            } else {
                Log.v(TAG,"Failed " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //this will create all playlists and add all tracks from said playlists
    public void apiCreatePlaylistsAppleMusic(Messenger handler) throws JSONException, IOException {
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

            String playlistID = "";
            for(int j = 0; j < transferPlaylists.get(i).getTracks().size();j++){
                JSONObject dataObject = new JSONObject();
                dataObject.put("id", transferPlaylists.get(i).getTracks().get(j).getId());
                dataObject.put("type", "songs");
                dataArray.put(dataObject);

                if(j == 150){
                    //add data array to tracks object
                    JSONObject tracksObject = new JSONObject();
                    tracksObject.put("data", dataArray);

                    JSONObject relationshipsObject = new JSONObject();
                    relationshipsObject.put("tracks", tracksObject);

                    //finally add relationships object to playlistObject
                    playlistObject.put("relationships",relationshipsObject);

                    //clear array
                    dataArray = new JSONArray();

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
                            String res = response.body().string();
                            Log.v(TAG, "create playlist success " + res);
                            Log.v(TAG, "Couldnt match " +
                                    transferPlaylists.get(i).getUnMatchedTracks().size() + " Songs");

                            JSONObject jObject = new JSONObject(res);
                            playlistID = jObject.getJSONArray("data").getJSONObject(0).getString("id");


                        }

                        Log.e(TAG, "creating playlist failed " + response.toString());
                    }


                } else if ((j%150) == 0){
                    //add data array to tracks object
                    JSONObject tracksObject = new JSONObject();
                    tracksObject.put("data", dataArray);

                    //clear array
                    dataArray = new JSONArray();

                    Log.v(TAG, playlistObject.toString());

                    RequestBody body = RequestBody.create(tracksObject.toString(), JSON); // new
                    // RequestBody body = RequestBody.create(JSON, json); // old
                    Request request = new Request.Builder()
                            .url(getString(R.string.api_apple_add_tracks_to_playlists,playlistID))
                            .header("Authorization", "Bearer "+ getString(R.string.apple_dev_token))
                            .header("Music-User-Token", dh.getAppleMusicUserToken())
                            .post(body)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if(response.isSuccessful()){
                            String res = response.body().string();
                            Log.v(TAG, "add tracks success " + res);
                            //Log.v(TAG, "Couldnt match " +
                             //       transferPlaylists.get(i).getUnMatchedTracks().size() + " Songs");

                            //JSONObject jObject = new JSONObject(res);
                            //playlistID = jObject.getJSONArray("data").getJSONObject(0).getString("id");


                        }

                        Log.e(TAG, "creating playlist failed " + response.toString());
                    }

                }

            }

            if(transferPlaylists.get(i).getTracks().size() > 150) {
                //add data array to tracks object
                JSONObject tracksObject = new JSONObject();
                tracksObject.put("data", dataArray);

                //clear array
                dataArray = new JSONArray();

                Log.v(TAG, playlistObject.toString());

                RequestBody body = RequestBody.create(tracksObject.toString(), JSON); // new
                // RequestBody body = RequestBody.create(JSON, json); // old
                Request request = new Request.Builder()
                        .url(getString(R.string.api_apple_add_tracks_to_playlists, playlistID))
                        .header("Authorization", "Bearer " + getString(R.string.apple_dev_token))
                        .header("Music-User-Token", dh.getAppleMusicUserToken())
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String res = response.body().string();
                        Log.v(TAG, "add tracks success " + res);
                        //Log.v(TAG, "Couldnt match " +
                        //       transferPlaylists.get(i).getUnMatchedTracks().size() + " Songs");

                        //JSONObject jObject = new JSONObject(res);
                        //playlistID = jObject.getJSONArray("data").getJSONObject(0).getString("id");


                    }

                    Log.e(TAG, "creating playlist failed " + response.toString());
                }
            } else {
                //add data array to tracks object
                JSONObject tracksObject = new JSONObject();
                tracksObject.put("data", dataArray);

                JSONObject relationshipsObject = new JSONObject();
                relationshipsObject.put("tracks", tracksObject);

                //finally add relationships object to playlistObject
                playlistObject.put("relationships",relationshipsObject);

                //clear array
                dataArray = new JSONArray();

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
                        String res = response.body().string();
                        Log.v(TAG, "create playlist success " + res);
                        Log.v(TAG, "Couldnt match " +
                                transferPlaylists.get(i).getUnMatchedTracks().size() + " Songs");

                        JSONObject jObject = new JSONObject(res);
                        playlistID = jObject.getJSONArray("data").getJSONObject(0).getString("id");


                    }

                    Log.e(TAG, "creating playlist failed " + response.toString());
                }
            }


            playlistsFinished++;
            updateMessage(handler,3);

        }

        msg = new Message();
        msg.obj = "Done";
        msg.what = STATUS_FINISHED;
        try {
            handler.send(msg);
            stopSelf();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    //Spotify

    //Source Calls

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
                    updateMessage(handler,1);

                    apiSpotifyGetTracksHelper(i,jsonObject,handler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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

    //Destination Calls

    //Search music on spotify
    public void apiFindSongsOnSpotify(Messenger handler){
        transferPlaylists = new ArrayList<>();
        updateMessage(handler,2);

        for(int i = 0; i < playlists.size(); i++){
            transferPlaylists.add(new Playlist(playlists.get(i).getName(),"SPOTIFY"));
            for(int j = 0; j < playlists.get(i).getTracks().size(); j++){

                Song song = playlists.get(i).getTracks().get(j);
                String termSearch = (removeFeat(song.getTrack()) + " " + song.getArtist());
                termSearch = termSearch.replace("&", "");
                termSearch = termSearch.replace("?", "");
                termSearch = termSearch.replace("#", "");
                termSearch.replace(' ', '+');
                Log.v(TAG, "Term Search: " + termSearch);

                Request request = new Request.Builder()
                        .url(getString(R.string.api_spotify_search) + "?q="+termSearch+"&type=track"+"&limit=50")
                        .header("Authorization", "Bearer "+ dh.getSpotifyUserToken())
                        .build();

                try(Response response = client.newCall(request).execute()){
                    if(response.isSuccessful()){
                        String res = response.body().string();
                        //Log.v(TAG,"Spotify Find Songs Response: " + res);
                        spotifyMatchSong(handler,res,i,song,false);
                    } else {
                        Log.v(TAG,"Failed " + response.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }



        try {
            updateMessage(handler,3);
            apiCreatePlaylistsSpotify(handler);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Takes in a response from the search query and parses the songs, find the best match and add it
    public void spotifyMatchSong(Messenger handler,String res, int playlistPos, Song currentTrack, boolean lastSearch){
        try {
            JSONArray songArray = new JSONObject(res).getJSONObject("tracks").getJSONArray("items");
            int songPos = -1;
            int diffName = 900;
            boolean matched = false;

            for(int i = 0; i < songArray.length(); i++){
                //Spotify current track details
                JSONObject songObject = songArray.getJSONObject(i);
                String artistName = "";
                for(int j = 0; j < songObject.getJSONArray("artists").length(); j++){
                    artistName = artistName + songObject.getJSONArray("artists").getJSONObject(j).getString("name");

                    if(j + 1 >= songObject.getJSONArray("artists").length()){

                    } else {
                        artistName = artistName + ", ";
                    }
                }
                artistName = deAccent(artistName);
                String songName = songObject.getString("name").replace("(", "")
                        .replace(")", "").replace("-", "");
                songName = deAccent(songName);
                songName = removeFeat(songName);
                String albumName = songObject.getJSONObject("album")
                        .getString("name");


                //source current track details
                String sourceArtist = currentTrack.getArtist();
                sourceArtist = deAccent(sourceArtist);
                sourceArtist = sourceArtist.replace("&", "");
                String sourceName = currentTrack.getTrack().replace("(", "")
                        .replace(")", "").replace("-", "");
                sourceName = deAccent(sourceName);
                sourceName = removeFeat(sourceName);
                String sourceAlbum = currentTrack.getAlbum();

                int diff = sourceName.compareToIgnoreCase(songName);

                Log.v(TAG,"Comparison:Source " + sourceArtist + " : " + sourceName +
                        " vs Spotify " + artistName + " : " + songName);

                Log.v(TAG, "Compare Value = Artist Name: " +
                        artistName.compareToIgnoreCase(sourceArtist) + " song name: " +
                        diff);

                if(artistName.toLowerCase().contains(sourceArtist.toLowerCase()) ||
                        sourceArtist.toLowerCase().contains(artistName.toLowerCase()) ||
                        artistName.compareToIgnoreCase(sourceArtist) >= 0){

                    if(sourceName.toLowerCase().contains(songName.toLowerCase()) ||
                            diff >= 0){

                        //exact match
                        if(sourceName.compareToIgnoreCase(songName) == 0) {
                            Log.v(TAG, "Song Matched! " + songName);
                            Song song = new Song(artistName, songName, albumName);
                            song.setId(songObject.getString("id"));
                            transferPlaylists.get(playlistPos).addTrack(song);
                            matched = true;
                            songsFound+=1;
                            updateMessage(handler, 2);
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

                //Spotify track details
                JSONObject songObject = songArray.getJSONObject(songPos);
                String artistName = "";
                for(int j = 0; j < songObject.getJSONArray("artists").length(); j++){
                    artistName = artistName + songObject.getJSONArray("artists").getJSONObject(j).getString("name");

                    if(j + 1 >= songObject.getJSONArray("artists").length()){

                    } else {
                        artistName = artistName + ", ";
                    }
                }
                artistName = deAccent(artistName);
                String songName = songObject.getString("name").replace("(", "")
                        .replace(")", "").replace("-", "");
                songName = deAccent(songName);
                songName = removeFeat(songName);
                String albumName = songObject.getJSONObject("album")
                        .getString("name");

                Log.v(TAG, "Song Matched! " + songName);
                Song song = new Song(artistName, songName, albumName);
                song.setId(songObject.getString("id"));
                transferPlaylists.get(playlistPos).addTrack(song);
                songsFound+=1;
                updateMessage(handler, 2);

            } else if(matched){
                //do nothing, we matched
            } else {
                //retest with another search query
                if(!lastSearch) {
                    apiFindSongReverseSearchSpotify(handler,currentTrack, playlistPos);
                } else {
                    transferPlaylists.get(playlistPos).addUnMatchedTracks(currentTrack);
                    songsNotFound+=1;
                    Log.e(TAG, "NOT FOUND " + currentTrack.getTrack());
                    updateMessage(handler, 2);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //array or response body error, retry call artist only
            if(!lastSearch) {
                apiFindSongReverseSearchSpotify(handler,currentTrack, playlistPos);
            } else {
                transferPlaylists.get(playlistPos).addUnMatchedTracks(currentTrack);
                songsNotFound+=1;
                updateMessage(handler, 2);
            }
        }
    }


    //swaps artist and song name for search, helps with some
    public void apiFindSongReverseSearchSpotify(Messenger handler,Song song, int playlistPos){

        String termSearch = (song.getArtist() + " " + song.getTrack());
        termSearch = termSearch.replace("&", "");
        termSearch = termSearch.replace("?", "");
        termSearch = termSearch.replace("#", "");
        termSearch.replace(' ', '+');
        Log.v(TAG, "Term Search: " + termSearch);

        Request request = new Request.Builder()
                .url(getString(R.string.api_spotify_search) + "?q="+termSearch+"&type=track"+"&limit=50")
                .header("Authorization", "Bearer "+ dh.getSpotifyUserToken())
                .build();

        try(Response response = client.newCall(request).execute()){
            if(response.isSuccessful()){
                String res = response.body().string();
                //Log.v(TAG,"Spotify Find Songs Response: " + res);
                spotifyMatchSong(handler,res,playlistPos,song,true);
            } else {
                Log.v(TAG,"Failed " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //this will create all playlists and add all tracks from said playlists
    public void apiCreatePlaylistsSpotify(Messenger handler) throws JSONException {
        for(int i = 0; i < transferPlaylists.size();i++) {
            // Create playlistObject that will be sent to spotify
            // Create attributes object to add
            JSONObject attributesObject = new JSONObject();
            attributesObject.put("name", transferPlaylists.get(i).getName());
            attributesObject.put("description", "Playlist transfered from " +
                    playlists.get(i).getSource() +
                    " by Smart Playlists! Get on Google Play or App Store now!");
            attributesObject.put("public", false);

        }
    }


    //This sends back progress to the results activity
    public void updateMessage(Messenger handler, int stage){
        try {
            messageContent.put("stage", stage);
            messageContent.put("totalSongs", songsToTransfer);
            messageContent.put("songsMatched", songsFound);
            messageContent.put("songsNotMatched", songsNotFound);
            messageContent.put("playlistsCreated", playlistsFinished);

            msg = new Message();
            msg.obj = messageContent;
            msg.what = STATUS_RUNNING;
            handler.send(msg);
        } catch (JSONException | RemoteException e) {
            e.printStackTrace();
        }
    }
}
