package com.music.sonart.model.Artist;

import java.io.Serializable;
import java.util.Map;

public class Artist implements Serializable {
    private int id;
    private int user_id;
    private String stage_name;
    private String genre;
    private String bio;
    private Map<String, String> social_links;
    private boolean verified;
    private String created_at;
    private String updated_at;

    public Artist(int id, int user_id, String stage_name, String genre, String bio,
                  Map<String, String> social_links, boolean verified,
                  String created_at, String updated_at) {
        this.id = id;
        this.user_id = user_id;
        this.stage_name = stage_name;
        this.genre = genre;
        this.bio = bio;
        this.social_links = social_links;
        this.verified = verified;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getStageName() {
        return stage_name;
    }

    public Map<String, String> getSocialLinks() {
        return social_links;
    }
}
