// com.music.sonart.network.ApiService.java
package com.music.sonart.network;

import com.music.sonart.model.Artist.Artist;
import com.music.sonart.model.Artist.ArtistRequest;
import com.music.sonart.model.Artist.ArtistResponse;
import com.music.sonart.model.Search.SearchResponse;
import com.music.sonart.model.Song.Song;
import com.music.sonart.model.Song.SongResponse;
import com.music.sonart.model.Song.SongStatsResponse;
import com.music.sonart.model.User.UpdateProfileResponse;
import com.music.sonart.model.User.UserRequest;
import com.music.sonart.model.User.UserResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ===== Menu =====
    @GET("songs/random")
    Call<List<Song>> getRandomSongs();

    @GET("songs/liked/{user_id}")
    Call<List<Song>> getLikedSongs(@Path("user_id") int userId);

    @GET("artists/followed/{user_id}")
    Call<List<Artist>> getFollowedArtists(@Path("user_id") int userId);


    // ===== Usuarios =====

    @GET("users/email/{email}")
    Call<Map<String, Object>> getUserByEmail(@Path("email") String email);


    @POST("users/sync")
    Call<UserResponse> syncUser(@Body UserRequest userRequest);

    @GET("user/profile")
    Call<UserResponse> getProfile();

    @Multipart
    @POST("user/profile/update")
    Call<UpdateProfileResponse> updateProfile(
            @Part("firebase_uid") RequestBody firebaseUid,
            @Part("name") RequestBody name,
            @Part MultipartBody.Part profile_photo
    );

    // ===== Artistas =====
    @GET("artists/firebase/{firebase_uid}")
    Call<ArtistResponse> getArtistByFirebase(@Path("firebase_uid") String firebaseUid);

    @POST("artists")
    Call<ArtistResponse> createArtist(@Body ArtistRequest artistRequest);

    @PUT("artists/firebase/{firebase_uid}")
    Call<ArtistResponse> updateArtistByFirebase(@Path("firebase_uid") String firebaseUid, @Body ArtistRequest artistRequest);

    @DELETE("artists/firebase/{firebase_uid}")
    Call<Void> deleteArtistByFirebase(@Path("firebase_uid") String firebaseUid);

    // ===== Canciones =====

    @GET("search")
    Call<SearchResponse> search(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("page") int page
    );

    // Obtener estadísticas de canciones
    @GET("songs/stats")
    Call<SongStatsResponse> getSongStats(@Query("firebase_uid") String firebaseUid);

    @GET("songs")
    Call<List<Song>> getSongs();

    @GET("songs/artist/{artistId}")
    Call<List<Song>> getSongsByArtist(@Path("artistId") int artistId);

    @GET("songs")
    Call<List<Song>> getSongsByFirebase(@Query("firebase_uid") String firebaseUid);

    @Multipart
    @POST("songs")
    Call<SongResponse> createSong(
            @Part("firebase_uid") RequestBody firebaseUid,
            @Part("title") RequestBody title,
            @Part("genre") RequestBody genre,
            @Part MultipartBody.Part file,
            @Part MultipartBody.Part cover
    );

    @DELETE("songs/{id}")
    Call<Void> deleteSong(@Path("id") int songId);

    @Multipart
    @POST("songs/{id}")
    Call<SongResponse> updateSong(
            @Path("id") int songId,
            @Part("title") RequestBody title,
            @Part("genre") RequestBody genre,
            @Part MultipartBody.Part file,
            @Part MultipartBody.Part cover
    );

    // ===== Likes =====
    @POST("likes")
    Call<Map<String, Object>> likeSong(@Body Map<String, Integer> body); // body: {"user_id": 1, "song_id": 42}

    @HTTP(method = "DELETE", path = "likes", hasBody = true)
    Call<Map<String, Object>> unlikeSong(@Body Map<String, Integer> body);

    @GET("likes/{song_id}")
    Call<Map<String, Object>> getLikesCount(@Path("song_id") int songId);

    @GET("likes/check")
    Call<Map<String, Boolean>> checkLike(
            @Query("user_id") int userId,
            @Query("song_id") int songId
    );

    // ===== Follows =====
    @POST("follows")
    Call<Map<String, Object>> followUser(@Body Map<String, Integer> body); // body: {"follower_id": 1, "followed_id": 5}

    @HTTP(method = "DELETE", path = "follows", hasBody = true)
    Call<Map<String, Object>> unfollowUser(@Body Map<String, Integer> body);

    @GET("follows/{user_id}/followers")
    Call<Map<String, Object>> getFollowers(@Path("user_id") int userId);

    @GET("follows/{user_id}/following")
    Call<Map<String, Object>> getFollowing(@Path("user_id") int userId);

    @GET("follows/check")
    Call<Map<String, Boolean>> checkFollow(@Query("follower_id") int userId, @Query("followed_artist_id") int artistId);

    // ===== Plays =====
    @POST("plays")
    Call<Map<String, Object>> playSong(@Body Map<String, Integer> body);

    @GET("plays/{song_id}")
    Call<Map<String, Object>> getPlaysCount(@Path("song_id") int songId);

}
