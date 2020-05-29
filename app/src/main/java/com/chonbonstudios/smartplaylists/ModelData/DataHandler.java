package com.chonbonstudios.smartplaylists.ModelData;

import android.content.Context;
import android.content.SharedPreferences;

public class DataHandler {
    private static final String SMART = "SMART_PLAYLISTS";
    public static final String SPOTIFY = "SPOTIFY_USER_TOKEN";
    public static final String SPOTIFY_REFRESH = "SPOTIFY_REFRESH_TOKEN";
    public static final String APPLE_MUSIC = "APPLE_MUSIC_USER_TOKEN";
    public static final String USER = "USER_STATUS";

    private Context c;

    private SharedPreferences sharedPreferences;

    public DataHandler(Context c){
        this.c = c;
        init();
    }

    public void init(){
        //Setup sharedprefs
        sharedPreferences = c.getSharedPreferences(SMART,0);

    }

    public void writeStringData(String name, String data){
        sharedPreferences.edit().putString(name, data).apply();
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

    public String getUserStatus(){
        return sharedPreferences.getString(USER, "Free");
    }

}
