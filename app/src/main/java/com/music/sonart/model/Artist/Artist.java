package com.music.sonart.model.Artist;

import java.io.Serializable;
import java.util.Map;

public class Artist implements Serializable {
    private int id;
    private int user_id;
    private String stage_name;
    private String name; // alias del stage_name (Laravel lo devuelve)
    private String genre;
    private String bio;
    private Map<String, String> social_links;
    private boolean verified;
    public String profile_image; // 🔹 imagen del usuario artista
    private String created_at;
    private String updated_at;

    // 🔸 Constructor vacío (requerido por Gson)
    public Artist() {}

    // 🔸 Constructor completo (opcional, si lo usas manualmente)
    public Artist(int id, int user_id, String stage_name, String name, String genre, String bio,
                  Map<String, String> social_links, boolean verified, String profile_image,
                  String created_at, String updated_at) {
        this.id = id;
        this.user_id = user_id;
        this.stage_name = stage_name;
        this.name = name;
        this.genre = genre;
        this.bio = bio;
        this.social_links = social_links;
        this.verified = verified;
        this.profile_image = profile_image;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // 🔹 Getters (lo que usará la app)
    public int getId() { return id; }
    public int getUserId() { return user_id; }
    public String getStageName() { return stage_name; }
    public String getName() { return name != null ? name : stage_name; } // por compatibilidad
    public String getGenre() { return genre; }
    public String getBio() { return bio; }
    public Map<String, String> getSocialLinks() { return social_links; }
    public boolean isVerified() { return verified; }
    public String getProfileImage() { return profile_image; }
    public String getCreatedAt() { return created_at; }
    public String getUpdatedAt() { return updated_at; }

    // 🔹 Setters (por si Retrofit necesita deserializar o modificar)
    public void setId(int id) { this.id = id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
    public void setStageName(String stage_name) { this.stage_name = stage_name; }
    public void setName(String name) { this.name = name; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setBio(String bio) { this.bio = bio; }
    public void setSocialLinks(Map<String, String> social_links) { this.social_links = social_links; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setProfileImage(String profile_image) { this.profile_image = profile_image; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
    public void setUpdatedAt(String updated_at) { this.updated_at = updated_at; }
}
