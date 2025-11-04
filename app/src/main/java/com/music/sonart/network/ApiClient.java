// com.music.sonart.network.ApiClient.java
package com.music.sonart.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    //private static final String BASE_URL = "http://tu-dominio.com/"; // Reemplaza con la URL de tu API
    private static Retrofit retrofit = null;
    //private static final String BASE_URL = "http://10.0.2.2:8000/api/";

    private static final String BASE_URL = "http://10.211.149.170:8000/api/";


    public static ApiService getApiService(String token) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}