package com.chonbonstudios.smartplaylists.ModelData;

import java.util.ArrayList;

public class Playlist {
    private String name;
    private String id;
    private ArrayList<Song> tracks;
    private ArrayList<Song> unMatchedTracks;
    private boolean isSelected;
    private String imageUrl = "";
    private String source;

    public Playlist(String name, String id, String source){
        this.name = name;
        this.id = id;
        this.source = source;
        this.tracks = new ArrayList<>();
        this.unMatchedTracks = new ArrayList<>();
    }

    public Playlist(String name,  String source){
        this.name = name;
        this.source = source;
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

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public ArrayList<Song> getTracks() {
        return tracks;
    }

    public ArrayList<Song> getUnMatchedTracks() {
        return unMatchedTracks;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSource() {
        return source;
    }

}
