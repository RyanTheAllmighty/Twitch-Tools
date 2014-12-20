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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NowPlayingConverter {
    private int delay;

    public NowPlayingConverter(int delay) {
        this.delay = delay;
        updateFiles();
    }

    public void run() {
        TimerTask task = new FileWatcher(Utils.getNowPlayingRawPath().toFile()) {
            @Override
            protected void onChange(File file) {
                updateFiles();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, new Date(), this.delay);
    }

    private void updateFiles() {
        try {
            String nowPlaying = FileUtils.readFileToString(Utils.getNowPlayingRawPath().toFile());
            String[] parts = nowPlaying.split("\\|\\|\\|");

            if (parts.length == 2) {
                FileUtils.write(Utils.getNowPlayingPath().toFile(), parts[0]);
                FileUtils.write(Utils.getNowPlayingFilePath().toFile(), parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}