package com.chonbonstudios.smartplaylists.ModelData;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DataHandler {
    private static final String SMART = "SMART_PLAYLISTS";
    public static final String SPOTIFY = "SPOTIFY_USER_TOKEN";
    public static final String SPOTIFY_REFRESH = "SPOTIFY_REFRESH_TOKEN";
    public static final String APPLE_MUSIC = "APPLE_MUSIC_USER_TOKEN";
    public static final String USER = "USER_STATUS";
    public static final String PLAYLIST_TRANSFER = "PLAYLIST_TO_TRANSFER";
    public static final String DEST = "DEST";
    public static final int DEST_SPOTIFY = 0;
    public static final int DEST_APPLEMUSIC = 1;

    private Context c;

    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Type type;

    public DataHandler(Context c){
        this.c = c;
        init();
    }

    public void init(){
        //Setup sharedprefs
        sharedPreferences = c.getSharedPreferences(SMART,0);
        gson = new Gson();
        type = new TypeToken<ArrayList<Playlist>>(){}.getType();
    }

    public void writeStringData(String name, String data){
        sharedPreferences.edit().putString(name, data).apply();
    }

    public void writeIntData(String name, int data){
        sharedPreferences.edit().putInt(name, data).apply();
    }

    public void writePlaylistTransferList(ArrayList<Playlist> data){
        sharedPreferences.edit().putString(PLAYLIST_TRANSFER, gson.toJson(data)).apply();
    }

    public String getSpotifyUserToken() {
        return sharedPreferences.getString(SPOTIFY, "");
    }

    public String getAppleMusicUserToken() {
        return sharedPreferences.getString(APPLE_MUSIC, "");
    }

    public String getSpotifyRefreshToken(){
        return sharedPreferences.getString(SPOTIFY_REFRESH, "");
    }

    public int getDestinationService(){
        return sharedPreferences.getInt(DEST,-1);
    }

    public String getUserStatus(){
        return sharedPreferences.getString(USER, "Free");
    }

    public ArrayList<Playlist> getPlaylistToTransfer(){
        ArrayList<Playlist> playlists;
        playlists = gson.fromJson(sharedPreferences.getString(PLAYLIST_TRANSFER, ""), type);
        return playlists;
    }

}
