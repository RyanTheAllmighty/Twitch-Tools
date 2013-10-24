Twitch Tools
====================================

### What is it?

Twitch Tools is a set of Java applicaions which I use personally when streaming on Twitch. The include the following programs:

- Followers.java writes the username of your latest follower and the total number of followers to seperate .txt files for reading in with OBS (Pass in 2 arguments, Username and the wait time in between checks in seconds)
- VLCNowPlaying.java writes the details of the currently playing song in VLC to a .txt file for reading in with OBS (Pass in 1 argument, the wait time in between checks in seconds)
- MicrophoneStatus.java gets the status of your Microphone and shows a icon in your taskbar depending on if your microphone is muted or not (No Arguments)

### Coding Standards

Please keep all line lengths to 100 characters and use 4 spaces rather than tab characters

### Usage

To use these tools simply create a runnable JAR and point it to the TwitchTools.java file as the Main Class and pass in the name of the tool to run and any command line arguments for that tool

### License

Your free to use this however you please as long as you follow the license

This work is licensed under the GPLv2 License. To view a copy of this license, visit http://www.gnu.org/licenses/gpl-2.0.html.