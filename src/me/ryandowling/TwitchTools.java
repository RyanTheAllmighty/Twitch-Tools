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

import java.util.Arrays;

public class TwitchTools {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Invalid number of arguments specified!");
            System.exit(0);
        } else if (args.length >= 1 && args.length <= 3) {
            if (args[0].equalsIgnoreCase("VLCNowPlaying") || args[0].equalsIgnoreCase("Followers")
                    || args[0].equalsIgnoreCase("MicrophoneStatus")) {
                if (args[0].equalsIgnoreCase("VLCNowPlaying")) {
                    if (args.length == 2 || args.length == 3) {
                        VLCNowPlaying.main(Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        System.out.println("Invalid number of arguments specified!");
                        System.exit(0);
                    }
                } else if (args[0].equalsIgnoreCase("Followers")) {
                    if (args.length == 3) {
                        Followers.main(Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        System.out.println("Invalid number of arguments specified!");
                        System.exit(0);
                    }
                } else if (args[0].equalsIgnoreCase("MicrophoneStatus")) {
                    if (args.length == 2) {
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
