package team;

import arc.*;
import arc.struct.ObjectMap;
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
    private ObjectMap<Player, Long> timers = new ObjectMap<>();
    private ObjectMap<String, Team> teamMap = new ObjectMap<>();

    private Team forceTeam = null;
    private Team spectateTeam = Team.all()[6];
    private ObjectMap<Player, Team> rememberSpectate = new ObjectMap<>();

    //register event handlers and create variables in the constructor
    public TeamPlugin(){
        for(Team t: Team.base()){
            teamMap.put(t.toString(), t);
        }
        teamMap.put("off", null);

        Events.on(PlayerJoin.class, event -> {
            if (Vars.state.rules.pvp) {
                timers.put(event.player, System.currentTimeMillis());
                if(forceTeam == null){
                    event.player.sendMessage("You have 1 minute if you want to change teams.");
                }else{
                    event.player.setTeam(forceTeam);
                    event.player.spawner = event.player.lastSpawner = null;
                    Call.onPlayerDeath(event.player);
                    event.player.sendMessage("[sky]ForceTeam[] is activated");
                    //Call.sendMessage(event.player.name + "[sky] changed teams.");
                }
            }
        });

        Events.on(GameOverEvent.class, event -> {
            if(this.forceTeam != null){
                this.forceTeam = null;
                Call.sendMessage("[accent]All players are allowed to change teams.");
            }
            if(Vars.state.rules.pvp){
                Call.sendMessage("You have 1 minute if you want to change teams.");
                for(Player p: timers.keys()){
                    timers.put(p, System.currentTimeMillis());
                }
            }
        });

        Events.on(PlayerLeave.class, event -> {
            //maybe this could cause an error
            if(timers.containsKey(event.player)){
                timers.remove(event.player);
            }

            if(rememberSpectate.containsKey(event.player)){
                rememberSpectate.remove(event.player);
            }
        });
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("spectate", "[scarlet]Admin only[]", (args, player) -> {
            if(!player.isAdmin){
                player.sendMessage("[scarlet]This command is only for admins!");
                return;
            }
            if(player.getTeam() == spectateTeam){
                player.setTeam(rememberSpectate.get(player));
                player.dead = false;
                player.sendMessage("[gold]PLAYER MODE[]");
            }else{
                rememberSpectate.put(player, player.getTeam());
                player.setTeam(spectateTeam);
                player.dead = true;
                player.sendMessage("[green]SPECTATE MODE[]");
            }
        });

        //change teams
        handler.<Player>register("team", "[optional:Team]", "You have max 1 minute to change teams after joining.", (args, player) -> {
            if (!Vars.state.rules.pvp) return;
            long current = System.currentTimeMillis();
            // change teams
            if(player.dead && Vars.playerGroup.count(p -> p.getTeam() == player.getTeam()) != Vars.playerGroup.size()){
                player.sendMessage("\n[scarlet]You need to be [accent]alive[] to change teams!");
                return;
            }

            if(timers.get(player) > current - 60000L || player.isAdmin) {
                if(args.length == 0){
                    player.setTeam(getPosTeam(player));
                }else{
                    if(!teamMap.containsKey(args[0]) || args[0].equals("off")){
                        player.sendMessage("[scarlet]Invalid team!");
                        StringBuilder sb = new StringBuilder();
                        sb.append("[accent]\nValid teams:[]");
                        for(String name: teamMap.keys()){
                            if(name.equals("off")) continue;
                            if(teamMap.get(name).cores().size == 0){
                                sb.append("[scarlet]");
                            }
                            sb.append("\n").append(name).append("[]");
                        }
                        player.sendMessage(sb.toString());
                        return;
                    }else{
                        if(teamMap.get(args[0]).cores().size > 0) {
                            player.setTeam(teamMap.get(args[0]));
                        }else{
                            player.sendMessage("This team has no cores!");
                            return;
                        }
                    }
                }
                player.spawner = player.lastSpawner = null;
                Call.onPlayerDeath(player);
                Call.sendMessage(player.name + "[sky] changed teams.");
            }else{
                player.sendMessage("[scarlet] you can't change teams anymore");
            }
        });


        handler.<Player>register("forceteam", "<team>", "[scarlet]Admin only[] force new players to join <team>. 'off' to disable", (args, player) -> {
           if(!Vars.state.rules.pvp) return;
           if(!player.isAdmin){
               player.sendMessage("[scarlet]This command is only for admins!");
               return;
           }
           if(!teamMap.containsKey(args[0])){
               player.sendMessage("[scarlet]Invalid team!");
               return;
           }else if(teamMap.get(args[0]).cores().size < 1){
               player.sendMessage("[scarlet]This team has no []cores!");
               return;
           }
           this.forceTeam = teamMap.get(args[0]);
           if(this.forceTeam != null) {
               Call.sendMessage("All [accent]new players[] will join team: [sky]" + args[0]);
           }else{
               Call.sendMessage("All [accent]new players[] will join [sky]a random[] team.");
           }
        });

    }

    //search a possible team
    private Team getPosTeam(Player p){
        Team currentTeam = p.getTeam();
        int c_index = Arrays.asList(Team.base()).indexOf(currentTeam);
        int i = (c_index+1)%6;
        while (i != c_index){
            if (Team.base()[i].cores().size > 0){
                return Team.base()[i];
            }
            i = (i + 1) % Team.base().length;
        }
        return currentTeam;
    }
}
