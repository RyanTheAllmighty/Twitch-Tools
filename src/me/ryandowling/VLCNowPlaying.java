/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package me.ryandowling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class VLCNowPlaying {

    public static void main(String[] args) throws FileNotFoundException,
            UnsupportedEncodingException {
        if (args.length != 1) {
            System.out.println("1 Argument Is Expected. Time Delay");
            System.exit(0);
        }
        int secondsToWait = Integer.parseInt(args[0]);
        File nowPlayingFile = new File("nowplaying.txt");
        String vlcInfo = null;
        String nowPlayingSong = "";
        String nowPlayingArtist = "";

        while (true) {
            System.out.println("Getting Information From VLC Web Interface");
            try {
                vlcInfo = Utils.urlToString("http://localhost:8080/requests/status.json");
            } catch (ConnectException e) {
                System.out
                        .println("Couldn't Connect To Web Interface! Make Sure It's Enabled And VLC Is Open!");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(vlcInfo);
                JSONObject jsonObject = (JSONObject) obj;
                JSONObject information = (JSONObject) jsonObject.get("information");
                JSONObject category = (JSONObject) information.get("category");
                JSONObject meta = (JSONObject) category.get("meta");
                nowPlayingSong = (String) meta.get("title");
                nowPlayingArtist = (String) meta.get("artist");
                System.out.println("Playing " + nowPlayingSong + " by " + nowPlayingArtist);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            PrintWriter writer1 = new PrintWriter(nowPlayingFile, "UTF-8");
            writer1.println("'" + nowPlayingSong + "' by " + nowPlayingArtist);
            writer1.close();

            try {
                System.out.println("Sleeping For " + secondsToWait + " seconds");
                Thread.sleep(secondsToWait * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("--------------------------------------");
        }
    }

}
