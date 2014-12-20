/*
 * Twitch Tools - https://github.com/RyanTheAllmighty/Twitch-Tools
 * Copyright (C) 2014 Ryan Dowling
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.ryandowling;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static String urlToString(String url) throws IOException {
        StringBuilder response = null;
        URL urll = new URL(url);
        URLConnection connection = urll.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64)");
        connection.setConnectTimeout(5000);
        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    public static Image getImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            System.err.println("Unable to load image: " + path);
        }

        return new ImageIcon(url).getImage();
    }

    public static Path getDataDir() {
        return Paths.get(System.getProperty("user.dir"), "Data");
    }

    public static Path getNowPlayingRawPath() {
        return Paths.get(System.getProperty("user.dir"), "nowplayingraw.txt");
    }

    public static Path getNowPlayingPath() {
        return Paths.get(System.getProperty("user.dir"), "nowplaying.txt");
    }

    public static Path getNowPlayingFilePath() {
        return Paths.get(System.getProperty("user.dir"), "nowplayingfile.txt");
    }

    public static Path getSettingsFile() {
        return getDataDir().resolve("settings.json");
    }

}
