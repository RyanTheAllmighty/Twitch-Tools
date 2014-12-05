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

import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Followers {
    private String username;
    private int secondsToWait;

    private List<String> followers = new ArrayList<String>();

    private Path followersTodayJsonFile = Utils.getDataDir().resolve("followers/list-today.json");
    private Path followersTodayTxtFile = Utils.getDataDir().resolve("followers/total-today.txt");
    private Path numberOfFollowersFile = Utils.getDataDir().resolve("followers/total.txt");
    private Path latestFollowerFile = Utils.getDataDir().resolve("followers/latest.txt");

    private String followerInformation = null;

    private String firstFollower;
    private String latestFollower;
    private long numberOfFollowers;

    private String tempLatestFollower;
    private long tempNumberOfFollowers;

    public Followers(String username, int secondsToWait, boolean newStream) {
        this.username = username;
        this.secondsToWait = secondsToWait;

        if (newStream) {
            try {
                // Clear the followers today file if we are on a new stream
                FileUtils.write(this.followersTodayJsonFile.toFile(), "");
                FileUtils.write(this.followersTodayTxtFile.toFile(), "0");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            if (Files.exists(this.followersTodayJsonFile)) {
                try {
                    this.followers = TwitchTools.GSON.fromJson(FileUtils.readFileToString(this.followersTodayJsonFile
                            .toFile()), new TypeToken<List<String>>() {

                    }.getType());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            try {
                DecimalFormat formatter = new DecimalFormat("#,###");
                FileUtils.write(this.followersTodayTxtFile.toFile(), formatter.format(this.followers.size()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                this.numberOfFollowers = (Long) jsonObject.get("_total");
            } catch (Exception e) {
                e.printStackTrace();
                sleep();
                continue;
            }


            if (this.firstFollower == null && this.latestFollower != null) {
                this.firstFollower = this.latestFollower;
            }

            if (!this.tempLatestFollower.equalsIgnoreCase(this.latestFollower)) {
                newFollower();
            }

            if (this.tempNumberOfFollowers != this.numberOfFollowers) {
                moreFollowers();
            }

            System.out.println("Latest follower is " + this.latestFollower);
            System.out.println("There are " + this.numberOfFollowers + " followers");
            System.out.println("There are " + this.followers.size() + " followers today");

            sleep();
        }
    }

    private void newFollower() {
        if (!this.firstFollower.equals(this.latestFollower)) {
            this.followers.add(this.latestFollower);
        }

        try {
            FileUtils.write(this.followersTodayJsonFile.toFile(), TwitchTools.GSON.toJson(this.followers));
            FileUtils.write(this.latestFollowerFile.toFile(), this.latestFollower);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void moreFollowers() {
        try {
            DecimalFormat formatter = new DecimalFormat("#,###");
            FileUtils.write(this.followersTodayTxtFile.toFile(), formatter.format(this.followers.size()));
            FileUtils.write(this.numberOfFollowersFile.toFile(), this.numberOfFollowers + "");
        } catch (IOException e) {
            e.printStackTrace();
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
