package com.music.sonart.model.Artist;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ArtistResponse {

    private int id;


    @SerializedName("firebase_uid")
    private String firebaseUid;

    @SerializedName("stage_name")
    private String stageName;

    private String genre;
    private String bio;

    @SerializedName("social_links")
    private Map<String, String> socialLinks;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // === Getters ===
    public int getId() { return id; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getStageName() { return stageName; }
    public String getGenre() { return genre; }
    public String getBio() { return bio; }
    public Map<String, String> getSocialLinks() { return socialLinks; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // === Setters (opcionales) ===
    public void setId(int id) { this.id = id; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setBio(String bio) { this.bio = bio; }
    public void setSocialLinks(Map<String, String> socialLinks) { this.socialLinks = socialLinks; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
