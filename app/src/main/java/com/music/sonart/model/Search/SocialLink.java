package com.music.sonart.model.Search;

public class SocialLink {
    public String platform;
    public String url;
    public int iconRes;

    public SocialLink(String platform, String url, int iconRes) {
        this.platform = platform;
        this.url = url;
        this.iconRes = iconRes;
    }
}