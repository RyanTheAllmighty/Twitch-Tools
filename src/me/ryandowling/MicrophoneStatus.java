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

import java.awt.AWTException;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class MicrophoneStatus {

    private JFrame frame;
    private Image unknownIcon;
    private Image normalIcon;
    private Image mutedIcon;
    private SystemTray sysTray;
    private PopupMenu menu;
    private MenuItem item1;
    private TrayIcon trayIcon;
    private int delay;

    public MicrophoneStatus(int delay) {
        this.delay = delay;
        initComponents();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void initComponents() {
        frame = new JFrame("Microphone Status");

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
            new Timer(delay, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isMuted() == 1) {
                        trayIcon.setImage(mutedIcon);
                    } else if (isMuted() == 0) {
                        trayIcon.setImage(normalIcon);
                    } else {
                        trayIcon.setImage(unknownIcon);
                    }
                }
            }).start();
            ;
        } else {
            System.err.println("System Tray is not supported!");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("1 Argument Is Expected. Time Delay");
            System.exit(0);
        }
        final int delay = Integer.parseInt(args[0]);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MicrophoneStatus(delay);
            }
        });
    }

    public Image getImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            System.err.println("Unable to load image: " + path);
        }

        ImageIcon icon = new ImageIcon(url);

        return icon.getImage();
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
                        if (c instanceof BooleanControl) {
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
}