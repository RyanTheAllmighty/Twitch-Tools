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

import java.util.Arrays;

public class TwitchTools {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Invalid number of arguments specified!");
            System.exit(0);
        } else if (args.length >= 1 && args.length <= 3) {
            if (args[0].equalsIgnoreCase("Followers") || args[0].equalsIgnoreCase("MicrophoneStatus")) {
                if (args[0].equalsIgnoreCase("Followers")) {
                    if (args.length == 3) {
                        Followers.main(Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        System.out.println("Invalid number of arguments specified!");
                        System.exit(0);
                    }
                } else if (args[0].equalsIgnoreCase("MicrophoneStatus")) {
                    if (args.length == 3) {
                        MicrophoneStatus.main(Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        System.out.println("Invalid number of arguments specified!");
                        System.exit(0);
                    }
                }
            } else {
                System.out.println("Invalid tool name specified!");
                System.exit(0);
            }
        }
    }
}
