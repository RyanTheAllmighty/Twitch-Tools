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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Followers {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("2 Arguments Are Expected. Twitch Name and Time Delay");
            System.exit(0);
        }
        String username = args[0];
        int secondsToWait = Integer.parseInt(args[1]);
        File numberOfFollowersFile = new File("followers.txt");
        File latestFollowerFile = new File("latestfollower.txt");
        String followerInformation = null;
        String latestFollower = username;
        long numberOfFollowers = 0;

        while (true) {
            System.err.println("Getting Information From Twitch API");
            try {
                followerInformation = Utils.urlToString("https://api.twitch.tv/kraken/channels/"
                        + username + "/follows?direction=DESC&limit=1&offset=0");
            } catch (ConnectException e) {
                System.err.println("Couldn't Connect To Twitch API!");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(followerInformation);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray msg = (JSONArray) jsonObject.get("follows");
                latestFollower = (String) ((JSONObject) ((JSONObject) msg.get(0)).get("user"))
                        .get("display_name");
                System.out.println("Latest follower is " + latestFollower);
                numberOfFollowers = (Long) jsonObject.get("_total");
                System.out.println("There are " + numberOfFollowers + " followers");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            PrintWriter writer1 = null;
            PrintWriter writer2 = null;
            try {
                writer1 = new PrintWriter(numberOfFollowersFile, "UTF-8");
                writer1.println(numberOfFollowers);
                writer2 = new PrintWriter(latestFollowerFile, "UTF-8");
                writer2.println(latestFollower);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                System.exit(0);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                System.exit(0);
            } finally {
                writer1.close();
                writer2.close();
            }

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
