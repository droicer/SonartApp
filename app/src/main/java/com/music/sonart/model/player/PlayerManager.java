package com.music.sonart.model.player;

import com.music.sonart.model.Song;

public class PlayerManager {
    private static PlayerManager instance;
    private Song currentSong;
    private boolean isPlaying = false;

    public static PlayerManager getInstance() {
        if (instance == null) instance = new PlayerManager();
        return instance;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song song) {
        this.currentSong = song;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void clear() {
        currentSong = null;
        isPlaying = false;
    }
}
