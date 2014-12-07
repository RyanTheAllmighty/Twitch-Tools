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

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

public class Followers {
    private String username;
    private int secondsToWait;

    private Path followersStartTodayTxrFile = Utils.getDataDir().resolve("followers/start-today.txt");
    private Path followersTodayTxtFile = Utils.getDataDir().resolve("followers/total-today.txt");
    private Path numberOfFollowersFile = Utils.getDataDir().resolve("followers/total.txt");
    private Path latestFollowerFile = Utils.getDataDir().resolve("followers/latest.txt");

    private String followerInformation = null;

    private boolean newStream;
    private boolean newStreamRun = false;

    private String firstFollower;
    private String latestFollower;
    private long numberOfFollowers;
    private long startNumberOfFollowers;

    private String tempLatestFollower;
    private long tempNumberOfFollowers;

    public Followers(String username, int secondsToWait, boolean newStream) {
        this.username = username;
        this.secondsToWait = secondsToWait;
        this.newStream = newStream;

        if (newStream) {
            try {
                // Clear the followers today file if we are on a new stream
                FileUtils.write(this.followersTodayTxtFile.toFile(), "0");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            if (Files.exists(this.followersStartTodayTxrFile)) {
                try {
                    this.startNumberOfFollowers = TwitchTools.GSON.fromJson(FileUtils.readFileToString(this
                            .followersStartTodayTxrFile.toFile()), Long.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
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

            try {
                Object obj = parser.parse(followerInformation);
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray msg = (JSONArray) jsonObject.get("follows");
                this.latestFollower = (String) ((JSONObject) ((JSONObject) msg.get(0)).get("user")).get("display_name");
                this.tempNumberOfFollowers = (Long) jsonObject.get("_total");
            } catch (Exception e) {
                e.printStackTrace();
                sleep();
                continue;
            }


            if (this.newStream && !this.newStreamRun) {
                try {
                    // Save the number of followers at the start of the stream
                    FileUtils.write(this.followersStartTodayTxrFile.toFile(), this.numberOfFollowers + "");
                    this.startNumberOfFollowers = this.numberOfFollowers;
                    this.newStreamRun = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (this.firstFollower == null && this.latestFollower != null) {
                this.firstFollower = this.latestFollower;
            }

            if (!this.tempLatestFollower.equalsIgnoreCase(this.latestFollower) && this.tempNumberOfFollowers > this
                    .numberOfFollowers) {
                newFollower();
            }

            if (this.tempNumberOfFollowers > this.numberOfFollowers) {
                this.numberOfFollowers = this.tempNumberOfFollowers;
                moreFollowers();
            }

            System.out.println("Latest follower is " + this.latestFollower);
            System.out.println("There are " + this.numberOfFollowers + " followers");
            System.out.println("There have been " + (this.numberOfFollowers - this.startNumberOfFollowers) +
                    " followers today");
            sleep();
        }
    }

    private void newFollower() {
        try {
            FileUtils.write(this.latestFollowerFile.toFile(), this.latestFollower);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void moreFollowers() {
        try {
            DecimalFormat formatter = new DecimalFormat("#,###");
            FileUtils.write(this.followersTodayTxtFile.toFile(), formatter.format(this.numberOfFollowers - this
                    .startNumberOfFollowers));
            FileUtils.write(this.numberOfFollowersFile.toFile(), formatter.format(this.numberOfFollowers));
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
