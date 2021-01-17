package team;

import arc.*;
import arc.struct.ObjectMap;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.world.Tile;

// use java.util for now
import java.text.MessageFormat;
import java.util.Arrays;

import static mindustry.Vars.tilesize;


public class TeamPlugin extends Plugin {
    //private boolean DEBUG = false;
    private long TEAM_CD = 5L;

    private ObjectMap<Player, Long> teamTimers = new ObjectMap<>();

    private Team spectateTeam = Team.all[8];
    private ObjectMap<Player, Team> rememberSpectate = new ObjectMap<>();

    //register event handlers and create variables in the constructor
    public TeamPlugin(){
        Events.on(PlayerLeave.class, event -> {
            if(rememberSpectate.containsKey(event.player)){
                rememberSpectate.remove(event.player);
            }
            if(teamTimers.containsKey(event.player)){
                teamTimers.remove(event.player);
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
        handler.<Player>register("team", "change team - cooldown", (args, player) ->{
            if(rememberSpectate.containsKey(player)){
                player.sendMessage(">[orange] transferring back to last team");
                player.team(rememberSpectate.get(player));
                Call.setPlayerTeamEditor(player, rememberSpectate.get(player));
                rememberSpectate.remove(player);
                return;
            }
            if(System.currentTimeMillis() < teamTimers.get(player,0L)){
                player.sendMessage(">[orange] command is on a 5 second cooldown...");
                return;
            }
            Team newTeam = getPosTeam(player);
            coreTeamReturn ret = getPosTeamLoc(player);
            if(ret != null) {
                Call.setPlayerTeamEditor(player, newTeam);
                player.team(newTeam);
                //maybe not needed
                Call.setPosition(player.con, ret.x, ret.y);
                player.unit().set(ret.x, ret.y);
                player.snapSync();

                teamTimers.put(player, System.currentTimeMillis()+TEAM_CD);
                Call.sendChatMessage(String.format("> %s []changed to team [sky]%s", player.name, newTeam));
            }else{
                player.sendMessage("[scarlet]You can't change teams ...");
            }
        });

        handler.<Player>register("spectate", "[scarlet]Admin only[]", (args, player) -> {
            if(!player.admin()){
               player.sendMessage("[scarlet]This command is only for admins.");
               return;
            }
            if(rememberSpectate.containsKey(player)){
                player.team(rememberSpectate.get(player));
                Call.setPlayerTeamEditor(player, rememberSpectate.get(player));
                rememberSpectate.remove(player);
                player.sendMessage("[gold]PLAYER MODE[]");
            }else{
                rememberSpectate.put(player, player.unit().team);
                player.team(spectateTeam);
                Call.setPlayerTeamEditor(player, spectateTeam);
                player.unit().kill();
                player.sendMessage("[green]SPECTATE MODE[]");
                player.sendMessage("use /team or /spectate to go back to player mode");
            }
        });
        /*
        //change teams
        handler.<Player>register("team", "[optional:Team]", "You have max 3 minute to change teams after joining.", (args, player) -> {
            if (!Vars.state.rules.pvp) return;
            long current = System.currentTimeMillis();
            // change teams
            if(player.dead() && Groups.player.count(p -> p.team() == player.team()) != Groups.player.size() && player.team().cores().size > 0 ){
                player.sendMessage("\n[scarlet]You need to be [accent]alive[] to change teams!");
                return;
            }else if(rememberSpectate.containsKey(player)){
                player.sendMessage("\nFirst use [accent]/spectate[] to leave spectate mode...");
                return;
            }
            if(player.team().cores().size == 0){
                timers.put(player, System.currentTimeMillis());
            }
            if(timers.get(player) > current - 180000L || player.admin()) {
                if(args.length == 0){
                    player.team(getPosTeam(player));
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
                            player.team(teamMap.get(args[0]));
                        }else{
                            player.sendMessage("[scarlet]This team has no cores!");
                            return;
                        }
                    }
                }
                //player.spawner = player.lastSpawner = null;
                Call.unitDeath(player.unit().id);
                Call.sendMessage(player.name + "[sky] changed teams.");
            }else{
                player.sendMessage("[scarlet] you can't change teams anymore");
            }
        });

         */
        /*
        if(DEBUG){
            handler.<Player>register("loc", "change team", (args, player) -> {
                StringBuilder sb = new StringBuilder();
                sb.append(player.x).append(";").append(player.y);
                player.sendMessage(sb.toString());
            });

            handler.<Player>register("setloc","<x> <y>", "setloc", (args, player) ->{
                float x = Float.valueOf(args[0]) * tilesize;
                float y = Float.valueOf(args[1]) * tilesize;
                player.sendMessage(MessageFormat.format("{0};{1}", x, y));
                player.set(x, y);

                Call.setPosition(player.con(), x, y);
            });

            handler.<Player>register("setloc_kd", "[x] [y]", "hacky way", (args, player) ->{
                //float x = Float.valueOf(args[0]) * tilesize;
                //float y = Float.valueOf(args[1]) * tilesize;
                //int x = Integer.valueOf(args[0]);
                //int y = Integer.valueOf(args[1]);
                //Call.playerSpawn(Vars.world.rawTile(x, y), player);
                //Call.unitDeath(player.id);
                //player.unit().kill();
                //player.x = x*8;
                //player.y = y*8;
                //player.unit().lastX = x*8;
                //player.unit().lastY = y*8;
                //player.update();
                Tile t = Vars.world.tiles.get(100,100);
                player.unit().set(t.drawx(), t.drawy());
                player.snapSync();
            });
        }*/
    }
    //search a possible team
    private Team getPosTeam(Player p){
        Team currentTeam = p.team();
        int c_index = Arrays.asList(Team.baseTeams).indexOf(currentTeam);
        int i = (c_index+1)%6;
        while (i != c_index){
            if (Team.baseTeams[i].cores().size > 0){
                return Team.baseTeams[i];
            }
            i = (i + 1) % Team.baseTeams.length;
        }
        return currentTeam;
    }

    private coreTeamReturn getPosTeamLoc(Player p){
        Team currentTeam = p.team();
        Team newTeam = getPosTeam(p);
        if (newTeam == currentTeam){
            return null;
        }else{
            Tile coreTile = newTeam.core().tileOn();
            return new coreTeamReturn(newTeam, coreTile.drawx(), coreTile.drawy());
        }
    }

    class coreTeamReturn{
        Team t;
        float x,y;
        public coreTeamReturn(Team _t, float _x, float _y){
            t = _t;
            x = _x;
            y = _y;
        }
    }
}
