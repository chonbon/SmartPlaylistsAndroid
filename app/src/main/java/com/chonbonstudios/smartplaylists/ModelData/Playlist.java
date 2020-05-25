package com.chonbonstudios.smartplaylists.ModelData;

import java.util.ArrayList;

public class Playlist {
    private String name;
    private ArrayList<Song> tracks;
    private ArrayList<Song> unMatchedTracks;

    public Playlist(String name){
        this.name = name;
        this.tracks = new ArrayList<>();
        this.unMatchedTracks = new ArrayList<>();
    }

    public void addTrack(Song temp){
        tracks.add(temp);
    }

    public void addUnMatchedTracks(Song temp){
        unMatchedTracks.add(temp);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Song> getTracks() {
        return tracks;
    }

    public ArrayList<Song> getUnMatchedTracks() {
        return unMatchedTracks;
    }
}
