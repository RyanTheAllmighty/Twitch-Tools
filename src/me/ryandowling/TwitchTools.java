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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Point;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TwitchTools {
    public static Settings settings;
    public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        loadSettings();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                saveSettings();
            }
        });
        if (args.length == 0) {
            System.err.println("Invalid number of arguments specified!");
            System.exit(0);
        } else if (args.length >= 1 && args.length <= 4) {
            if (args[0].equalsIgnoreCase("Followers") || args[0].equalsIgnoreCase("MicrophoneStatus")) {
                if (args[0].equalsIgnoreCase("Followers")) {
                    if (args.length == 4) {
                        new Followers(args[1], Integer.parseInt(args[2]), Boolean.parseBoolean(args[3])).run();
                    } else {
                        System.err.println("Invalid number of arguments specified!");
                        System.exit(0);
                    }
                } else if (args[0].equalsIgnoreCase("MicrophoneStatus")) {
                    if (args.length == 3) {
                        final int delay = Integer.parseInt(args[1]);
                        final boolean guiDisplay = Boolean.parseBoolean(args[2]);

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                new MicrophoneStatus(delay, guiDisplay);
                            }
                        });
                    } else {
                        System.err.println("Invalid number of arguments specified!");
                        System.err.println("Arguments are: [delay in ms for updates] [if the gui should show]!");
                        System.err.println("For example: [100] [true]!");
                        System.exit(0);
                    }
                }
            } else {
                System.err.println("Invalid tool name specified!");
                System.err.println("Options are: Followers or MicrophoneStatus!");
                System.exit(0);
            }
        }
    }

    private static void loadSettings() {
        if (!Utils.getSettingsFile().exists()) {
            createDefaultSettingsFile();
        }

        int tries = 1;

        while (settings == null && tries <= 10) {
            try {
                FileReader reader = new FileReader(Utils.getSettingsFile());
                settings = GSON.fromJson(reader, Settings.class);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                createDefaultSettingsFile();
            }

            tries++;
        }

        if (settings == null) {
            System.err.println("Error loading settings from " + Utils.getSettingsFile().getAbsolutePath());
            System.exit(1);
        }
    }

    private static void createDefaultSettingsFile() {
        Utils.getDataDir().mkdir();
        settings = new Settings();
        settings.setMicrophoneStatus(new WindowDetails(new Dimension(200, 200), new Point(100, 100)));
        saveSettings();
    }

    private static void saveSettings() {
        try {
            FileWriter writer = new FileWriter(Utils.getSettingsFile());
            writer.write(GSON.toJson(settings));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
