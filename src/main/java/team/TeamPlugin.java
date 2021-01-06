package team;

import arc.*;
import arc.math.geom.Position;
import arc.scene.Group;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap;
import arc.util.*;
import mindustry.*;
import mindustry.core.World;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

// use java.util for now
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;


public class TeamPlugin extends Plugin {
    private ObjectMap<Player, Long> timers = new ObjectMap<>();
    private ObjectMap<String, Team> teamMap = new ObjectMap<>();

    private Team forceTeam = null;
    private Team spectateTeam = Team.all[6];
    private ObjectMap<Player, Team> rememberSpectate = new ObjectMap<>();

    //register event handlers and create variables in the constructor
    public TeamPlugin(){
        /*
        for(Team t: Team.baseTeams){
            teamMap.put(t.toString(), t);
        }
        teamMap.put("off", null);

        Events.on(PlayerJoin.class, event -> {
            if (Vars.state.rules.pvp) {
                timers.put(event.player, System.currentTimeMillis());
                if(forceTeam == null){
                    event.player.sendMessage("You have 3 minutes if you want to change teams.");
                }else{
                    event.player.team(forceTeam);
                    //event.player.spawner = event.player.lastSpawner = null;
                    //event.player.unit().kill();
                    Call.unitDeath(event.player.unit().id);
                    //Call.OnPlayerDeath(event.player);
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
        */
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("team", "change team", (args, player) ->{
            Team newTeam = getPosTeam(player);
            coreTeamReturn ret = getPosTeamLoc(player);
            if(ret != null) {
                Call.setPlayerTeamEditor(player, newTeam);
                player.team(newTeam);
                Call.setPosition(player.con, ret.x, ret.y);
                player.set(ret.x, ret.y);
            }
        });

        handler.<Player>register("loc", "change team", (args, player) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(player.x).append(";").append(player.y);
            player.sendMessage(sb.toString());
        });

        handler.<Player>register("setloc","<x> <y>", "setloc", (args, player) ->{
            float x = Float.valueOf(args[0]) * 8;
            float y = Float.valueOf(args[1]) * 8;
            player.sendMessage(MessageFormat.format("{0};{1}", x, y));
            player.set(x, y);

            Call.setPosition(player.con(), x, y);
        });


        handler.<Player>register("spectate", "[scarlet]Admin only[]", (args, player) -> {


            /*
            if(!player.admin()){
                player.sendMessage("[scarlet]This command is only for admins!");
                return;
            }
            if(player.team() == spectateTeam){
                player.team(rememberSpectate.get(player));
                rememberSpectate.remove(player);
                player.unit().dead = false;
                player.sendMessage("[gold]PLAYER MODE[]");
            }else{
                rememberSpectate.put(player, player.team());
                player.team(spectateTeam);
                player.unit().kill();
                //player.spawner = player.lastSpawner = null;
                //Call.onPlayerDeath(player);
                player.sendMessage("[green]SPECTATE MODE[]");
            }
            */
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

        handler.<Player>register("forceteam", "<team> [change:1]", "[scarlet]Admin only[] force new players to join <team>. 'off' to disable", (args, player) -> {
            if(!Vars.state.rules.pvp) return;
            if(!player.admin()){
                player.sendMessage("[scarlet]This command is only for admins!");
                return;
            }
            if(!teamMap.containsKey(args[0])) {
                player.sendMessage("[scarlet]Invalid team!");
                return;
            }else if(args[0].equals("off")){
                Log.info("forceTeam: off");
            }else if(teamMap.get(args[0]).cores().size < 1){
                player.sendMessage("[scarlet]This team has no cores!");
                return;
            }
            this.forceTeam = teamMap.get(args[0]);
            if(this.forceTeam != null) {
                Log.info("forceTeam: " + args[0]);
                Call.sendMessage("All [accent]new players[] will join team: [sky]" + args[0]);
            }else{
                Call.sendMessage("All [accent]new players[] will join [sky]a random[] team.");
            }

            if(this.forceTeam != null && args.length > 1){
                if(args[1].equals("1")){
                    Call.sendMessage("All players will change team immediately...");
                    for(Player p: Groups.player){
                        if(p != player){
                            p.team(this.forceTeam);
                            Call.unitDeath(p.unit().id);
                        }
                    }
                }
            }
        });

         */
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
            return new coreTeamReturn(newTeam, coreTile.getX(), coreTile.getY());
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
