# TeamPlugin
### Commands
* `/team [optional:team]` This command lets players change teams. But a player only has 3 minutes to change teams! *Important: a player needs to be alive to change teams.*

### Admin Only commands
* `/forceteam [team/off]` New players will be assigned to this team (resets after a game over)
* `/spectate` Enter/leave spectate mode.

### Important
All commands are chatcommands.
This plugin isn't compatible with hexed!

### Feedback
Open an issue if you have a suggestion.

### Building a Jar 

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


### Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins by running the `plugins` command.
