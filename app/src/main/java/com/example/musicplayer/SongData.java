package com.example.musicplayer;

public class SongData {

    private String path;
    private final String title;
    private final String artist;
    private final String album;
    private final String duration;
    private String id;


    public SongData(String album, String title, String duration, String path, String artist, String id) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }


    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }


    public String getDuration() {
        return duration;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
