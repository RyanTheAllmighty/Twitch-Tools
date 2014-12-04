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
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

public class MicrophoneStatus implements HotKeyListener {

    private JFrame frame;
    private Image unknownIcon;
    private Image normalIcon;
    private Image mutedIcon;
    private SystemTray sysTray;
    private PopupMenu menu;
    private MenuItem item1;
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
        this.provider = Provider.getCurrentProvider(true);
        this.provider.register(KeyStroke.getKeyStroke("ctrl alt B"), this);
        this.delay = delay;
        this.guiDisplay = guiDisplay;
        initComponents();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void initComponents() {
        frame = new JFrame("Microphone Status");

        if (this.guiDisplay) {
            this.guiFrame = new JFrame();
            this.guiFrame.setLayout(new BorderLayout());
            this.guiPanel = new JPanel();
            this.guiFrame.setContentPane(this.guiPanel);
            this.guiPanel.setBackground(this.unknownColour);
            this.guiFrame.setSize(this.guiSize);
            this.guiFrame.setVisible(true);
        }

        if (SystemTray.isSupported()) {
            sysTray = SystemTray.getSystemTray();
            unknownIcon = getImage("/sound_unknown.png");
            normalIcon = getImage("/sound_on.png");
            mutedIcon = getImage("/sound_mute.png");
            menu = new PopupMenu("Menu");
            item1 = new MenuItem("Exit");
            menu.add(item1);

            item1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            trayIcon = new TrayIcon(unknownIcon, "Microphone Status", menu);

            try {
                sysTray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
                System.exit(0);
            }

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
            }).start();
        } else {
            System.err.println("System Tray is not supported! Not showing the icon.");
            if (!this.guiDisplay) {
                System.exit(1);
            }
        }
    }

    public Image getImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            System.err.println("Unable to load image: " + path);
        }

        return new ImageIcon(url).getImage();
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
                } catch (Exception ex) {
                    continue;
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
                } catch (Exception ex) {
                    continue;
                }
            }
        }
        return;
    }


    @Override
    public void onHotKey(HotKey hotKey) {
        muteMicrophone();
    }
}