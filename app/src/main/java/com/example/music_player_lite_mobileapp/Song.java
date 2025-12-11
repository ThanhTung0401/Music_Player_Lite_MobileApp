package com.example.music_player_lite_mobileapp;
import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private String artist;
    private String path;
    private Long totalTime;

    public Song(String title, String artist, String path,Long totalTime) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.totalTime= totalTime;
    }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPath() { return path; }
    public Long getTotalTime() { return totalTime; }

    public String getDuration() {
        int minutes = (int) (totalTime / 1000) / 60;
        int seconds = (int) (totalTime / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getCurrentTime(int currentPos) {
        int minutes = (int) (currentPos / 1000) / 60;
        int seconds = (int) (currentPos / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}