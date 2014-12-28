package me.ryandowling;

public class SteamOwnedGame {
    private int appid;
    private String name;
    private int playtime_forever;
    private String img_icon_url;
    private String img_logo_url;
    private boolean has_community_visible_stats;

    public int getAppID() {
        return this.appid;
    }

    public String getName() {
        return this.name;
    }

    public int getPlaytimeForever() {
        return this.playtime_forever;
    }

    public String getImageIconURL() {
        return "http://media.steampowered.com/steamcommunity/public/images/apps/" + this.appid + "/" + this
                .img_icon_url + ".jpg";
    }

    public String getImageLogoURL() {
        return "http://media.steampowered.com/steamcommunity/public/images/apps/" + this.appid + "/" + this
                .img_logo_url + ".jpg";
    }

    public boolean hasCommunityVisibleStats() {
        return this.has_community_visible_stats;
    }

    public String getLink() {
        return "http://store.steampowered.com/app/" + this.appid + "/";
    }
}
