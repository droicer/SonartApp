package com.music.sonart.model;

public class SongResponse {
    private String message;
    private Song song;

    public String getMessage() { return message; }
    public Song getSong() { return song; }

    public void setMessage(String message) { this.message = message; }
    public void setSong(Song song) { this.song = song; }
}