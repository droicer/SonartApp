package com.music.sonart.model.Song;

import com.music.sonart.model.Artist.Artist;
import java.io.Serializable;

public class Song implements Serializable {
    private Integer id;
    private Integer artist_id;
    private String artist_name;
    private String title;
    private String genre;
    private String file_path;
    private String cover_image;
    private boolean approved;
    private String created_at;
    private String updated_at;
    private String file_url;
    private String cover_url;
    private Artist artist;

    // 🔹 Constructor vacío requerido por Gson
    public Song() {}

    // 🔹 Constructor completo (por si lo usas manualmente)
    public Song(int id, Integer artist_id, String title, String genre, String file_path, String cover_image,
                boolean approved, String created_at, String updated_at,
                String file_url, String cover_url, Artist artist) {
        this.id = id;
        this.artist_id = artist_id;
        this.title = title;
        this.genre = genre;
        this.file_path = file_path;
        this.cover_image = cover_image;
        this.approved = approved;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.file_url = file_url;
        this.cover_url = cover_url;
        this.artist = artist;
    }

    // ===============================
    // 🔹 GETTERS
    // ===============================
    public Integer getId() { return id; }
    public Integer getArtistId() { return artist_id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getFilePath() { return file_path; }
    public String getCoverImage() { return cover_image; }
    public boolean isApproved() { return approved; }
    public String getCreatedAt() { return created_at; }
    public String getUpdatedAt() { return updated_at; }
    public String getFileUrl() { return file_url; }
    public String getCoverUrl() { return cover_url; }
    public Artist getArtist() { return artist; }

    // 🔹 Nombre del artista (prioriza lo que venga del objeto Artist)
    public String getArtistName() {
        if (artist != null && artist.getStageName() != null && !artist.getStageName().isEmpty()) {
            return artist.getStageName();
        } else if (artist_name != null && !artist_name.isEmpty()) {
            return artist_name;
        } else {
            return "Artista desconocido";
        }
    }

    // 🔹 Imagen del artista (si existe en el objeto Artist)
    public String getArtistImageUrl() {
        if (artist != null && artist.getProfileImage() != null && !artist.getProfileImage().isEmpty()) {
            return artist.getProfileImage();
        } else {
            // imagen por defecto si el artista no tiene
            return "https://tuapp.com/storage/default-artist.jpg";
        }
    }

    // ===============================
    // 🔹 SETTERS
    // ===============================
    public void setId(int id) { this.id = id; }
    public void setArtistId(Integer artist_id) { this.artist_id = artist_id; }
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setFilePath(String file_path) { this.file_path = file_path; }
    public void setCoverImage(String cover_image) { this.cover_image = cover_image; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
    public void setUpdatedAt(String updated_at) { this.updated_at = updated_at; }
    public void setFileUrl(String file_url) { this.file_url = file_url; }
    public void setCoverUrl(String cover_url) { this.cover_url = cover_url; }
    public void setArtist(Artist artist) { this.artist = artist; }
    public void setArtistName(String artist_name) { this.artist_name = artist_name; }
}
