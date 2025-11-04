// com.music.sonart.network.ApiService.java
package com.music.sonart.network;

import com.music.sonart.model.Artist.ArtistRequest;
import com.music.sonart.model.Artist.ArtistResponse;
import com.music.sonart.model.Song;
import com.music.sonart.model.SongResponse;
import com.music.sonart.model.User.UpdateProfileResponse;
import com.music.sonart.model.User.UserRequest;
import com.music.sonart.model.User.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // POST /users/sync
    @POST("users/sync")
    Call<UserResponse> syncUser(@Body UserRequest userRequest);

    // GET /songs
    @GET("songs")
    Call<List<Song>> getSongs();

    // ✅ GET /user/profile
    @GET("user/profile")
    Call<UserResponse> getProfile();

    // ✅ POST /user/profile/update (multipart)
    @Multipart
    @POST("user/profile/update")
    Call<UpdateProfileResponse> updateProfile(
            @Part("firebase_uid") RequestBody firebaseUid,
            @Part("name") RequestBody name,
            @Part MultipartBody.Part profile_photo
    );

    // Get artist by Firebase UID
    @GET("artists/firebase/{firebase_uid}")
    Call<ArtistResponse> getArtistByFirebase(@Path("firebase_uid") String firebaseUid);

    // Create new artist
    @POST("artists")
    Call<ArtistResponse> createArtist(@Body ArtistRequest artistRequest);

    // Update artist by Firebase UID
    @PUT("artists/firebase/{firebase_uid}")
    Call<ArtistResponse> updateArtistByFirebase(@Path("firebase_uid") String firebaseUid, @Body ArtistRequest artistRequest);

    // Delete artist by Firebase UID
    @DELETE("artists/firebase/{firebase_uid}")
    Call<Void> deleteArtistByFirebase(@Path("firebase_uid") String firebaseUid);

    // Get songs by Firebase UID
    @GET("songs")
    Call<List<Song>> getSongsByFirebase(@Query("firebase_uid") String firebaseUid);

    // Create new song (multipart)
    @Multipart
    @POST("songs")
    Call<SongResponse> createSong(
            @Part("firebase_uid") RequestBody firebaseUid,
            @Part("title") RequestBody title,
            @Part("genre") RequestBody genre,
            @Part MultipartBody.Part file,
            @Part MultipartBody.Part cover
    );

    // Delete song by ID
    @DELETE("songs/{id}")
    Call<Void> deleteSong(@Path("id") int songId);

    // Update song by ID
    @Multipart
    @POST("songs/{id}")
    Call<SongResponse> updateSong(
            @Path("id") int songId,
            @Part("title") RequestBody title,
            @Part("genre") RequestBody genre,
            @Part MultipartBody.Part file,
            @Part MultipartBody.Part cover
    );

    // Canciones de un artista en especifico
    @GET("songs/artist/{artistId}")
    Call<List<Song>> getSongsByArtist(@Path("artistId") int artistId);

}
