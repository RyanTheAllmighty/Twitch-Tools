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
    private String username;
    private int secondsToWait;
    private File numberOfFollowersFile = new File("followers.txt");
    private File latestFollowerFile = new File("latestfollower.txt");
    private String followerInformation = null;

    private String latestFollower;
    private long numberOfFollowers;

    private String tempLatestFollower;
    private long tempNumberOfFollowers;

    public Followers(String username, int secondsToWait) {
        this.username = username;
        this.secondsToWait = secondsToWait;
    }

    public void run() {
        this.latestFollower = this.username;
        this.numberOfFollowers = 0;
        this.tempLatestFollower = this.username;
        this.tempNumberOfFollowers = 0;

        while (true) {
            System.out.println("Getting Information From Twitch API");
            try {
                followerInformation = Utils.urlToString("https://api.twitch.tv/kraken/channels/" + username +
                        "/follows?direction=DESC&limit=1&offset=0");
            } catch (ConnectException e) {
                System.err.println("Couldn't Connect To Twitch API!");
                sleep();
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                sleep();
                continue;
            }

            JSONParser parser = new JSONParser();

            this.tempLatestFollower = this.latestFollower;
            this.tempNumberOfFollowers = this.numberOfFollowers;

            try {
                Object obj = parser.parse(followerInformation);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray msg = (JSONArray) jsonObject.get("follows");
                this.latestFollower = (String) ((JSONObject) ((JSONObject) msg.get(0)).get("user")).get("display_name");
                System.out.println("Latest follower is " + this.latestFollower);
                this.numberOfFollowers = (Long) jsonObject.get("_total");
                System.out.println("There are " + this.numberOfFollowers + " followers");
            } catch (Exception e) {
                e.printStackTrace();
                sleep();
                continue;
            }

            if (!this.tempLatestFollower.equalsIgnoreCase(this.latestFollower)) {
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(this.latestFollowerFile, "UTF-8");
                    writer.println(this.latestFollower);
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    sleep();
                    continue;
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }

            if (this.tempNumberOfFollowers != this.numberOfFollowers) {
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(this.numberOfFollowersFile, "UTF-8");
                    writer.println(this.numberOfFollowers);
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    System.exit(0);
                    sleep();
                    continue;
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }

            sleep();
        }
    }

    public void sleep() {
        try {
            System.out.println("Sleeping For " + secondsToWait + " seconds");
            Thread.sleep(secondsToWait * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("--------------------------------------");
    }
}
