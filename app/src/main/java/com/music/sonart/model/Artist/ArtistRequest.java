package com.music.sonart.model.Artist;

import java.util.Map;

public class ArtistRequest {
    private String firebase_uid;
    private String stage_name;
    private String genre;
    private String bio;
    private Map<String, String> social_links;

    public ArtistRequest(String firebase_uid, String stage_name, String genre, String bio, Map<String, String> social_links) {
        this.firebase_uid = firebase_uid;
        this.stage_name = stage_name;
        this.genre = genre;
        this.bio = bio;
        this.social_links = social_links;
    }

    // Getters y setters (opcional si usas Gson)
}
