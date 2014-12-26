package me.ryandowling;

public class SoundCloudProfileAPIResponse {
    private String username;
    private String avatar_url;
    private String permalink_url;

    public String getUsername() {
        return this.username;
    }

    public String getAvatarUrl() {
        return this.avatar_url;
    }

    public String getPermalinkUrl() {
        return this.permalink_url;
    }
}
