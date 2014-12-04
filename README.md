Twitch Tools
====================================

### What is it?

Twitch Tools is a set of Java applicaions which I use personally when streaming on Twitch. The include the following programs:

- Followers.java writes the username of your latest follower and the total number of followers to separate .txt files for reading in with OBS (Pass in 2 arguments, Username and the wait time in between checks in seconds)
- MicrophoneStatus.java gets the status of your Microphone and shows a icon in your taskbar depending on if your microphone is muted or not (Pass in 2 arguments, the wait time in between checks in milliseconds and if the gui should be displayed showing colours depending on the status of the microphone with 1 being to show it and 0 being to not show it). You can mute the Microphone on Windows only using the Ctrl+Alt+B Global HotKey that's created

### Coding Standards

+ Please keep all line lengths to 120 characters and use 4 spaces rather than tab characters
+ Please keep all variables at the top of the class
+ Please keep all inner classes at the bottom
+ Please don't use star imports
+ Please use the IntelliJ-Coding-Style.xml for the project (if using IntelliJ) in order to keep all formatting consistent
+ Please update the CHANGELOG.md file when fixing/adding something so it's easier to keep track of than git commits. Feel free to add in a 'by MyUsername' to the end of the changes you've made.
+ Please don't do large commits. My preference is a single commit for a single fix/addition rather than bundled up commits.

### Usage

To use these tools simply create a runnable JAR and point it to the TwitchTools.java file as the Main Class and pass in the name of the tool to run and any command line arguments for that tool

### License

This work is licensed under the GNU General Public License v3.0. To view a copy of this license, visit http://www.gnu.org/licenses/gpl-3.0.txt.
