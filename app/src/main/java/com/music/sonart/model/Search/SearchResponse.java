package com.music.sonart.model.Search;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class SearchResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("query")
    private String query;

    @SerializedName("results")
    private Results results;

    public boolean isSuccess() { return success; }
    public Results getResults() { return results; }

    public static class Results {
        @SerializedName("songs")
        public SongResult songs;
        @SerializedName("artists")
        public ArtistResult artists;
    }

    public static class SongResult {
        @SerializedName("data")
        public List<Song> data;
        @SerializedName("pagination")
        public Pagination pagination;
    }

    public static class ArtistResult {
        @SerializedName("data")
        public List<Artist> data;
        @SerializedName("pagination")
        public Pagination pagination;
    }

    public static class Pagination {
        @SerializedName("current_page")
        public int currentPage;
        @SerializedName("total_pages")
        public int totalPages;
        @SerializedName("total")
        public int total;
        @SerializedName("per_page")
        public int perPage;
    }

    // ===============================
    // SONG
    // ===============================
    public static class Song implements Parcelable {
        @SerializedName("id")
        public int id;
        @SerializedName("title")
        public String title;
        @SerializedName("cover_image")
        public String coverImage;
        @SerializedName("file_path")
        public String filePath;
        @SerializedName("file_url")
        public String fileUrl;
        @SerializedName("cover_url")
        public String coverUrl;
        @SerializedName("likes_count")
        public int likesCount;
        @SerializedName("plays_count")
        public int playsCount;
        @SerializedName("artist")
        public ArtistInfo artist;

        public Song() {}

        protected Song(Parcel in) {
            id = in.readInt();
            title = in.readString();
            coverImage = in.readString();
            filePath = in.readString();
            fileUrl = in.readString();
            coverUrl = in.readString();
            likesCount = in.readInt();
            playsCount = in.readInt();
            artist = in.readParcelable(ArtistInfo.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeString(coverImage);
            dest.writeString(filePath);
            dest.writeString(fileUrl);
            dest.writeString(coverUrl);
            dest.writeInt(likesCount);
            dest.writeInt(playsCount);
            dest.writeParcelable(artist, flags);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<Song> CREATOR = new Creator<Song>() {
            @Override
            public Song createFromParcel(Parcel in) { return new Song(in); }
            @Override
            public Song[] newArray(int size) { return new Song[size]; }
        };
    }

    // ===============================
    // ARTIST INFO (para canciones)
    // ===============================
    public static class ArtistInfo implements Parcelable {
        @SerializedName("id")
        public int id;
        @SerializedName("user_id")
        public int userId;
        @SerializedName("stage_name")
        public String stageName;
        @SerializedName("genre")
        public String genre;
        @SerializedName("bio")
        public String bio;
        @SerializedName("social_links")
        public Map<String, String> socialLinks;
        @SerializedName("verified")
        public boolean verified;
        @SerializedName("profile_image")
        public String profileImage; // ← del backend
        @SerializedName("name")
        public String name; // ← nombre real del usuario
        @SerializedName("user")
        public User user;

        public ArtistInfo() {}

        protected ArtistInfo(Parcel in) {
            id = in.readInt();
            userId = in.readInt();
            stageName = in.readString();
            genre = in.readString();
            bio = in.readString();
            socialLinks = in.readHashMap(String.class.getClassLoader());
            verified = in.readByte() != 0;
            profileImage = in.readString();
            name = in.readString();
            user = in.readParcelable(User.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(userId);
            dest.writeString(stageName);
            dest.writeString(genre);
            dest.writeString(bio);
            dest.writeMap(socialLinks);
            dest.writeByte((byte) (verified ? 1 : 0));
            dest.writeString(profileImage);
            dest.writeString(name);
            dest.writeParcelable(user, flags);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<ArtistInfo> CREATOR = new Creator<ArtistInfo>() {
            @Override
            public ArtistInfo createFromParcel(Parcel in) { return new ArtistInfo(in); }
            @Override
            public ArtistInfo[] newArray(int size) { return new ArtistInfo[size]; }
        };
    }

    // ===============================
    // USER (dentro del artista)
    // ===============================
    public static class User implements Parcelable {
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
        @SerializedName("profile_photo")
        public String profilePhoto;

        public User() {}

        protected User(Parcel in) {
            id = in.readInt();
            name = in.readString();
            profilePhoto = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(name);
            dest.writeString(profilePhoto);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<User> CREATOR = new Creator<User>() {
            @Override
            public User createFromParcel(Parcel in) { return new User(in); }
            @Override
            public User[] newArray(int size) { return new User[size]; }
        };
    }

    // ===============================
    // ARTIST (para pestaña de artistas)
    // ===============================
    public static class Artist implements Parcelable {
        @SerializedName("id")
        public int id;
        @SerializedName("user_id")
        public int userId;
        @SerializedName("stage_name")
        public String stageName;
        @SerializedName("genre")
        public String genre;
        @SerializedName("bio")
        public String bio;
        @SerializedName("social_links")
        public Map<String, String> socialLinks;
        @SerializedName("verified")
        public boolean verified;
        @SerializedName("profile_image")
        public String profileImage;
        @SerializedName("name")
        public String name;
        @SerializedName("user")
        public User user;

        public Artist() {}

        protected Artist(Parcel in) {
            id = in.readInt();
            userId = in.readInt();
            stageName = in.readString();
            genre = in.readString();
            bio = in.readString();
            socialLinks = in.readHashMap(String.class.getClassLoader());
            verified = in.readByte() != 0;
            profileImage = in.readString();
            name = in.readString();
            user = in.readParcelable(User.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(userId);
            dest.writeString(stageName);
            dest.writeString(genre);
            dest.writeString(bio);
            dest.writeMap(socialLinks);
            dest.writeByte((byte) (verified ? 1 : 0));
            dest.writeString(profileImage);
            dest.writeString(name);
            dest.writeParcelable(user, flags);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<Artist> CREATOR = new Creator<Artist>() {
            @Override
            public Artist createFromParcel(Parcel in) { return new Artist(in); }
            @Override
            public Artist[] newArray(int size) { return new Artist[size]; }
        };
    }
}