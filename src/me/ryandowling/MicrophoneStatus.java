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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

public class MicrophoneStatus {

    private WindowDetails windowDetails;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Image unknownIcon;
    private Image normalIcon;
    private Image mutedIcon;
    private SystemTray sysTray;
    private PopupMenu systemTrayMenu;
    private MenuItem exitMenuItem;
    private TrayIcon trayIcon;
    private int delay;
    private boolean guiDisplay;
    private Provider provider;

    private MicStatus status;

    private JFrame guiFrame;
    private JPanel guiPanel;
    private Color unknownColour = Color.YELLOW;
    private Color normalColour = Color.GREEN;
    private Color mutedColour = Color.RED;
    private Dimension guiSize = new Dimension(300, 300);

    public MicrophoneStatus(int delay, boolean guiDisplay) {
        if (!SystemTray.isSupported()) {
            System.err.println("System Tray is not supported!");
            System.exit(1);
        }

        this.delay = delay;
        this.guiDisplay = guiDisplay;

        this.provider = Provider.getCurrentProvider(true);
        this.provider.register(KeyStroke.getKeyStroke("ctrl alt B"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                muteMicrophone();
            }
        });

        initComponents();
    }

    private void initComponents() {
        if (this.guiDisplay) {
            this.guiFrame = new JFrame();
            this.guiFrame.setLayout(new BorderLayout());

            this.guiPanel = new JPanel();
            this.guiFrame.setContentPane(this.guiPanel);

            loadWindowDetails();

            this.guiFrame.setVisible(true);

            this.guiFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    saveWindowDetails();
                    System.exit(0);
                }
            });

            this.guiFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    updateWindowDetails();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    super.componentMoved(e);
                    updateWindowDetails();
                }
            });
        }

        unknownIcon = Utils.getImage("/sound_unknown.png");
        normalIcon = Utils.getImage("/sound_on.png");
        mutedIcon = Utils.getImage("/sound_mute.png");

        setupSystemTray();

        switch (isMuted()) {
            case 1:
                this.status = MicStatus.MUTED;
                trayIcon.setImage(mutedIcon);
                if (guiDisplay) {
                    guiPanel.setBackground(mutedColour);
                }
                break;
            case 0:
                this.status = MicStatus.UNMUTED;
                trayIcon.setImage(normalIcon);
                if (guiDisplay) {
                    guiPanel.setBackground(normalColour);
                }
                break;
            default:
                this.status = MicStatus.UNKNOWN;
                trayIcon.setImage(unknownIcon);
                if (guiDisplay) {
                    guiPanel.setBackground(unknownColour);
                }
                break;
        }

        new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkStatus();
            }
        }).start();
    }

    private void checkStatus() {
        switch (isMuted()) {
            case 1:
                if (status != MicStatus.MUTED) {
                    status = MicStatus.MUTED;
                    trayIcon.setImage(mutedIcon);
                    if (guiDisplay) {
                        guiPanel.setBackground(mutedColour);
                    }
                }
                break;
            case 0:
                if (status != MicStatus.UNMUTED) {
                    status = MicStatus.UNMUTED;
                    trayIcon.setImage(normalIcon);
                    if (guiDisplay) {
                        guiPanel.setBackground(normalColour);
                    }
                }
                break;
            default:
                if (status != MicStatus.UNKNOWN) {
                    status = MicStatus.UNKNOWN;
                    trayIcon.setImage(unknownIcon);
                    if (guiDisplay) {
                        guiPanel.setBackground(unknownColour);
                    }
                }
                break;
        }
    }

    private void setupSystemTray() {
        this.sysTray = SystemTray.getSystemTray();
        this.systemTrayMenu = new PopupMenu("Menu");

        this.exitMenuItem = new MenuItem("Exit");
        this.systemTrayMenu.add(this.exitMenuItem);

        this.exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveWindowDetails();
                System.exit(0);
            }
        });

        this.trayIcon = new TrayIcon(this.unknownIcon, "Microphone Status", this.systemTrayMenu);

        try {
            this.sysTray.add(this.trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public int isMuted() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            int maxLines = mixer.getMaxLines(Port.Info.MICROPHONE);
            Port lineIn = null;
            if (maxLines > 0) {
                try {
                    lineIn = (Port) mixer.getLine(Port.Info.MICROPHONE);
                    lineIn.open();
                    CompoundControl cc = (CompoundControl) lineIn.getControls()[0];
                    Control[] controls = cc.getMemberControls();
                    for (Control c : controls) {
                        if (c.getType() == BooleanControl.Type.MUTE) {
                            return (((BooleanControl) c).getValue()) ? 1 : 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;

    }

    public void muteMicrophone() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            int maxLines = mixer.getMaxLines(Port.Info.MICROPHONE);
            Port lineIn = null;
            if (maxLines > 0) {
                try {
                    lineIn = (Port) mixer.getLine(Port.Info.MICROPHONE);
                    lineIn.open();
                    CompoundControl cc = (CompoundControl) lineIn.getControls()[0];
                    Control[] controls = cc.getMemberControls();
                    for (Control c : controls) {
                        if (c.getType() == BooleanControl.Type.MUTE) {
                            if (((BooleanControl) c).getValue()) {
                                ((BooleanControl) c).setValue(false); // Mute it
                            } else {
                                ((BooleanControl) c).setValue(true); // Mute it
                            }
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadWindowDetails() {
        if (!Utils.getSettingsFile().exists()) {
            createDefaultSettingsFile();
        }

        int tries = 1;

        while (this.windowDetails == null && tries <= 10) {
            try {
                FileReader reader = new FileReader(Utils.getSettingsFile());
                this.windowDetails = this.gson.fromJson(reader, WindowDetails.class);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                createDefaultSettingsFile();
            }

            tries++;
        }

        if (this.windowDetails == null) {
            System.err.println("Error loading settings from " + Utils.getSettingsFile().getAbsolutePath());
            System.exit(1);
        }

        this.guiFrame.setSize(this.windowDetails.getSize());
        this.guiFrame.setLocation(this.windowDetails.getPosition());
    }

    private void createDefaultSettingsFile() {
        this.windowDetails = new WindowDetails(new Dimension(200, 200), new Point(100, 100));
        saveWindowDetails();
        this.windowDetails = null;
    }

    private void updateWindowDetails() {
        this.windowDetails.setSize(this.guiFrame.getSize());
        this.windowDetails.setPosition(this.guiFrame.getLocation());

        checkStatus();
    }

    private void saveWindowDetails() {
        try {
            FileWriter writer = new FileWriter(Utils.getSettingsFile());
            writer.write(gson.toJson(this.windowDetails));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}