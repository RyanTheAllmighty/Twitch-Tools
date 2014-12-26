package me.ryandowling;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MusicCreditsGenerator {
    private String type;

    public MusicCreditsGenerator(String type) {
        this.type = type;
    }

    public void run() {
        StringBuilder builder = new StringBuilder();

        List<String> artistsDone = new ArrayList<>();

        for (File file : FileUtils.listFiles(Utils.getMusicPath().toFile(), new String[]{"mp3"}, true)) {
            String artist = file.getParentFile().getName();

            if (artistsDone.contains(artist)) {
                continue;
            }

            String website = "";

            try {
                website = FileUtils.readFileToString(new File(file.getParentFile(), "website.txt"));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            String info = "";

            switch (this.type) {
                case "html":
                    info = "<a href=\"" + website + "\">" + artist + "</a><br/>\n";
                    break;
                case "october":
                    try {
                        String response = Utils.urlToString("http://api.soundcloud.com/resolve" + ".json?url=" +
                                URLEncoder.encode(website, "UTF-8") + "&client_id=" + TwitchTools.settings
                                .getSoundCloudClientID());

                        SoundCloudResolveAPIResponse resolve = TwitchTools.GSON.fromJson(response,
                                SoundCloudResolveAPIResponse.class);

                        if (resolve.getLocation() == null) {
                            System.err.println("Error finding SoundCloud profile API link for " + website);
                            System.exit(1);
                        }

                        response = Utils.urlToString(resolve.getLocation());

                        SoundCloudProfileAPIResponse profile = TwitchTools.GSON.fromJson(response,
                                SoundCloudProfileAPIResponse.class);

                        info = "\t{% partial \"artist-panel\" artist=\"" + profile.getUsername() + "\" image=\"" +
                                profile.getAvatarUrl() + "\" page=\"" + profile.getPermalinkUrl() + "\" %}\n";

                        if ((artistsDone.size() + 1) == 1) {
                            info = "<div class=\"row\">\n" + info;
                        } else if (((artistsDone.size() + 1) % 3) == 0) {
                            info = info + "</div>\n\n<div class=\"row\">\n";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "markdown":
                    info = "[" + artist + "](" + website + ")  \n";
                    break;
            }

            artistsDone.add(artist);

            builder.append(info);
        }

        System.out.println(builder.toString());
    }
}
