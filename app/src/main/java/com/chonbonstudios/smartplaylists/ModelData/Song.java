package com.chonbonstudios.smartplaylists.ModelData;

public class Song {
    private String artist, track, album;

    public Song(String artist, String track, String album){
        this.artist = artist;
        this.track = track;
        this.album = album;
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
