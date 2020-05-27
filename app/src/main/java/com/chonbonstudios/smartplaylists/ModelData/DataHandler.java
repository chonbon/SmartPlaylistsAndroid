package com.chonbonstudios.smartplaylists.ModelData;

import android.content.Context;
import android.content.SharedPreferences;

public class DataHandler {
    private static final String SMART = "SMART_PLAYLISTS";
    public static final String SPOTIFY = "SPOTIFY_USER_TOKEN";
    public static final String APPLE_MUSIC = "APPLE_MUSIC_USER_TOKEN";

    private Context c;

    private SharedPreferences sharedPreferences;

    String spotifyUserToken, appleMusicUserToken;

    public DataHandler(Context c){
        this.c = c;
        init();
    }

    public void init(){
        //Setup sharedprefs
        sharedPreferences = c.getSharedPreferences(SMART,0);

        //get values
        refreshData();
    }

    public void writeStringData(String name, String data){
        sharedPreferences.edit().putString(name, data).apply();
        refreshData();
    }

    public void refreshData(){
        spotifyUserToken = sharedPreferences.getString(SPOTIFY, "");
        appleMusicUserToken = sharedPreferences.getString(APPLE_MUSIC, "");
    }

    public String getSpotifyUserToken() {
        return spotifyUserToken;
    }

    public String getAppleMusicUserToken() {
        return appleMusicUserToken;
    }
}
