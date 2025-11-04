package com.music.sonart.model.User;

import com.music.sonart.model.Artist.ArtistResponse;

public class UserResponse {
    private long id;
    private String firebase_uid;
    private String name;
    private String email;
    private String role;
    private String profile_photo;

    // 👇 Nuevo campo: el perfil de artista (puede ser null si no tiene)
    private ArtistResponse artist;

    // --- Getters ---
    public long getId() { return id; }
    public String getFirebaseUid() { return firebase_uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getProfilePhoto() { return profile_photo; }
    public ArtistResponse getArtist() { return artist; }

    // --- Setters (opcional si no los usas directamente) ---
    public void setId(long id) { this.id = id; }
    public void setFirebaseUid(String firebase_uid) { this.firebase_uid = firebase_uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setProfilePhoto(String profile_photo) { this.profile_photo = profile_photo; }
    public void setArtist(ArtistResponse artist) { this.artist = artist; }
}
