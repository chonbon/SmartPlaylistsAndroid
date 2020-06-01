package com.chonbonstudios.smartplaylists.ModelData;

public class Song {

    private String artist, track, album;
    private String id;

    public Song(String artist, String track, String album){
        this.artist = artist;
        this.track = track;
        this.album = album;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTrack() {
        return track;
    }

    public String getAlbum() {
        return album;
    }
}
