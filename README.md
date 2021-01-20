# TeamPlugin
### Commands
* `/team ` This command lets players change teams. It cycles through a list of possible teams and teleports the players to on of their cores. *This command has a 10 second cooldown.*

### Admin Only commands
* `/spectate` Enter/leave spectate mode.

### Previous version v5
The command `/forceteam`is implemented in a new [plugin](https://github.com/J-VdS/ForceTeamPlugin). Both plugins are compatible. <br/>
* ~~`/forceteam [team/off]` New players will be assigned to this team (resets after a game over)~~ 


### Important
All commands are chatcommands.
This plugin isn't compatible with hexed!

### Feedback
Open an issue if you have a suggestion.

### Releases
Prebuild relases can be found [here](https://github.com/J-VdS/TeamPlugin/releases)

### Building a Jar 

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


### Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins by running the `plugins` command.
