package me.ryandowling;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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
            }

            String info = "";

            switch (this.type) {
                case "html":
                    info = "<a href=\"" + website + "\">" + artist + "</a><br/>\n";
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
