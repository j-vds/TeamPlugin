package team;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.plugin.Plugin;

// use java.util for now
import java.util.Arrays;
import java.util.HashMap;


public class TeamPlugin extends Plugin{
    private HashMap<Player, Long> timers = new HashMap<>();

    //register event handlers and create variables in the constructor
    public TeamPlugin(){
        Events.on(PlayerJoin.class, event -> {
            if (Vars.state.rules.pvp){
                event.player.sendMessage("You have 1 minute if you want to change teams.");
                timers.put(event.player, System.currentTimeMillis());
            }
        });

        Events.on(PlayerLeave.class, event -> {
            //maybe this could cause an error
           timers.remove(event.player);
        });
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        /*
        handler.register("reactors", "List all thorium reactors in the map.", args -> {
        });
         */
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        //change teams
        handler.<Player>register("team", "You have max 1 minute to change teams after joining.", (args, player) -> {
            if (!Vars.state.rules.pvp) return;
            long current = System.currentTimeMillis();
            // change teams
            if (timers.get(player) > current - 60000L) {
                player.setTeam(getPosTeam(player));
                Call.onPlayerDeath(player);
                Call.sendMessage(player.name + "[sky] changed teams.");
            } else {
                player.sendMessage("[scarlet] you can't change teams anymore");
            }
        });

    }

    //search a possible team
    private Team getPosTeam(Player p){
        Team currentTeam = p.getTeam();
        int c_index = Arrays.asList(Team.base()).indexOf(currentTeam);
        int i = c_index+1;
        while (i != c_index){
            if (Team.base()[i].cores().size > 0){
                return Team.base()[i];
            }
            i = (i + 1) % Team.base().length;
        }
        return currentTeam;
    }
}
