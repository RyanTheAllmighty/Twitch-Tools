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
    public static void run(String username, int secondsToWait) {
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
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                if (writer1 != null) {
                    writer1.close();
                }
                if (writer2 != null) {
                    writer2.close();
                }
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
