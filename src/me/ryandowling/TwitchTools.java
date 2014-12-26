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
import org.apache.commons.io.FileUtils;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;

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
            switch(args[0]) {
                case "Followers":
                    if (args.length == 4) {
                        new Followers(args[1], Integer.parseInt(args[2]), Boolean.parseBoolean(args[3])).run();
                    } else {
                        System.err.println("Invalid number of arguments specified!");
                        System.exit(1);
                    }
                case "MicrophoneStatus":
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
                        System.exit(1);
                    }
                case "NowPlayingConverter":
                    if (args.length == 2) {
                        final int delay = Integer.parseInt(args[1]);

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                new NowPlayingConverter(delay).run();
                            }
                        });
                    } else {
                        System.err.println("Invalid number of arguments specified!");
                        System.err.println("Arguments are: [delay in ms for updates]!");
                        System.err.println("For example: [100]!");
                        System.exit(1);
                    }
                case "FoobarControls":
                    if (args.length == 1) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                new FoobarControls().run();
                            }
                        });
                    } else {
                        System.err.println("Invalid number of arguments specified!");
                        System.err.println("There are no arguments to provide!");
                        System.exit(1);
                    }
                default:
                    System.err.println("Invalid tool name specified!");
                    System.err.println("Options are: Followers, MicrophoneStatus or NowPlayingConverter!");
                    System.exit(1);
            }

            System.exit(0);
        }
    }

    private static void loadSettings() {
        if (!Files.exists(Utils.getSettingsFile())) {
            createDefaultSettingsFile();
        }

        int tries = 1;

        while (settings == null && tries <= 10) {
            try {
                settings = GSON.fromJson(FileUtils.readFileToString(Utils.getSettingsFile().toFile()), Settings.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            tries++;
        }

        if (settings == null) {
            System.err.println("Error loading settings from " + Utils.getSettingsFile().toAbsolutePath());
            System.exit(1);
        }
    }

    private static void createDefaultSettingsFile() {
        try {
            Files.createDirectories(Utils.getDataDir());
        } catch (IOException e) {
            e.printStackTrace();
        }

        settings = new Settings();
        settings.setMicrophoneStatus(new WindowDetails(new Dimension(200, 200), new Point(100, 100)));
        saveSettings();
    }

    private static void saveSettings() {
        try {
            FileUtils.write(Utils.getSettingsFile().toFile(), GSON.toJson(settings));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
