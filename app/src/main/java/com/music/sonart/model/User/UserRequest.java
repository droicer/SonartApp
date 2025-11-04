package com.music.sonart.model.User;

public class UserRequest {
    public String firebase_uid;
    public String email;
    public String name;
    public String role;

    public UserRequest(String firebase_uid, String email, String name, String role) {
        this.firebase_uid = firebase_uid;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}

