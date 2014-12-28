package me.ryandowling;

import java.io.IOException;

public class SteamGamesGenerator {
    private String type;

    public SteamGamesGenerator(String type) {
        this.type = type;
    }

    public void run() {
        StringBuilder builder = new StringBuilder();

        try {
            SteamOwnedGamesResponse response = TwitchTools.GSON.fromJson(Utils.urlToString("http://api.steampowered" +
                            ".com/IPlayerService/GetOwnedGames/v0001/?key=" + TwitchTools.settings.getSteamApiKey() +
                            "&steamid=" + TwitchTools.settings.getSteamPlayerID() + "&format=json&include_appinfo=1")
                    , SteamOwnedGamesResponse.class);

            int done = 0;

            for (SteamOwnedGame game : response.getGames()) {
                done++;
                String info = "";
                switch (this.type) {
                    case "october":
                        info = "\t{% partial \"game-panel\" game=\"" + game.getName() + "\" image=\"" +
                                game.getImageLogoURL() + "\" page=\"" + game.getLink() + "\" %}\n";

                        if (done == 1) {
                            info = "<div class=\"row\">\n" + info;
                        } else if ((done % 3) == 0) {
                            info = info + "</div>\n\n<div class=\"row\">\n";
                        }
                        break;
                }

                builder.append(info);
            }

            System.out.println(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}