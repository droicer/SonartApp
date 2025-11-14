package com.music.sonart.model.Song;

import java.util.List;

public class SongStatsResponse {
    private boolean success;
    private int artist_id;
    private String artist_name;
    private List<SongStats> songs;
    private int total_songs;
    private int total_likes;
    private int total_plays;

    // Getters
    public boolean isSuccess() { return success; }
    public int getArtistId() { return artist_id; }
    public String getArtistName() { return artist_name; }
    public List<SongStats> getSongs() { return songs; }
    public int getTotalSongs() { return total_songs; }
    public int getTotalLikes() { return total_likes; }
    public int getTotalPlays() { return total_plays; }

    // Clase interna para cada canción
    public static class SongStats {
        private int id;
        private String title;
        private String artist;
        private String cover_url;
        private int likes_count;
        private int plays_count;

        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public String getCoverUrl() { return cover_url; }
        public int getLikesCount() { return likes_count; }
        public int getPlaysCount() { return plays_count; }
    }
}