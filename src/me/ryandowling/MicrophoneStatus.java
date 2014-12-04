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
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

public class MicrophoneStatus {

    private WindowDetails windowDetails;
    private BooleanControl muteControl;

    private Image unknownIcon;
    private Image normalIcon;
    private Image mutedIcon;
    private SystemTray sysTray;
    private PopupMenu systemTrayMenu;
    private MenuItem exitMenuItem;
    private MenuItem resetMenuItem;
    private TrayIcon trayIcon;

    private int delay;
    private boolean guiDisplay;
    private Provider provider;

    private MicStatus status;
    private String[] microphones = getMicrophones();
    private String microphoneToUse = null;

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
                toggleMuteStatus();
            }
        });

        if (microphones.length == 1) {
            microphoneToUse = microphones[0];
        } else {
            microphoneToUse = (String) JOptionPane.showInputDialog(new JFrame(), "Select a microphone to use",
                    "Select a microphone to use", JOptionPane.QUESTION_MESSAGE, null, microphones, microphones[0]);
        }

        if (microphoneToUse == null) {
            System.err.println("Couldn't find a microphone to use!");
            System.exit(1);
        }

        setupMicrophone(microphoneToUse);

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
                    System.exit(1);
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

        if (isMuted()) {
            this.status = MicStatus.MUTED;
            trayIcon.setImage(mutedIcon);
            if (guiDisplay) {
                guiPanel.setBackground(mutedColour);
            }
        } else {
            this.status = MicStatus.UNMUTED;
            trayIcon.setImage(normalIcon);
            if (guiDisplay) {
                guiPanel.setBackground(normalColour);
            }
        }

        new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkStatus(false);
            }
        }).start();
    }

    private void checkStatus(boolean force) {
        if (isMuted()) {
            if (status != MicStatus.MUTED || force) {
                status = MicStatus.MUTED;
                trayIcon.setImage(mutedIcon);
                if (guiDisplay) {
                    guiPanel.setBackground(mutedColour);
                }
            }
        } else {
            if (status != MicStatus.UNMUTED || force) {
                status = MicStatus.UNMUTED;
                trayIcon.setImage(normalIcon);
                if (guiDisplay) {
                    guiPanel.setBackground(normalColour);
                }
            }
        }
    }

    private void setupSystemTray() {
        this.sysTray = SystemTray.getSystemTray();
        this.systemTrayMenu = new PopupMenu("Menu");

        this.resetMenuItem = new MenuItem("Reset Size & Position");

        this.resetMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                guiFrame.setSize(new Dimension(100, 100));
                guiFrame.setLocation(0, 0);
            }
        });

        this.systemTrayMenu.add(this.resetMenuItem);

        this.exitMenuItem = new MenuItem("Exit");

        this.exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        this.systemTrayMenu.add(this.exitMenuItem);

        this.trayIcon = new TrayIcon(this.unknownIcon, "Microphone Status", this.systemTrayMenu);

        try {
            this.sysTray.add(this.trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean isMuted() {
        return this.muteControl.getValue();
    }

    public String[] getMicrophones() {
        List<String> microphones = new ArrayList<String>();

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            int maxLines = mixer.getMaxLines(Port.Info.MICROPHONE);
            Port lineIn = null;
            if (maxLines > 0) {
                microphones.add(info.getName());
            }
        }

        return microphones.toArray(new String[microphones.size()]);
    }

    private void setupMicrophone(String name) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixerInfos) {
            if (!info.getName().equals(name)) {
                continue;
            }

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
                            this.muteControl = (BooleanControl) c;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.muteControl == null) {
            System.err.println("Couldn't setup microphone!");
            System.exit(1);
        }
    }

    public void toggleMuteStatus() {
        this.muteControl.setValue(!isMuted());
    }

    private void loadWindowDetails() {
        this.windowDetails = TwitchTools.settings.getMicrophoneStatus();

        if (this.windowDetails == null || this.windowDetails.getSize() == null || this.windowDetails.getPosition() ==
                null) {
            System.err.println("Settings file invalid!");
            System.exit(1);
        }

        this.guiFrame.setSize(this.windowDetails.getSize());
        this.guiFrame.setLocation(this.windowDetails.getPosition());
    }

    private void updateWindowDetails() {
        this.windowDetails.setSize(this.guiFrame.getSize());
        this.windowDetails.setPosition(this.guiFrame.getLocation());
        TwitchTools.settings.setMicrophoneStatus(this.windowDetails);

        checkStatus(true);
    }
}