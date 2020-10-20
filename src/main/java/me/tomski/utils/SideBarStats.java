package me.tomski.utils;

import org.bukkit.entity.*;
import me.tomski.prophunt.*;
import org.bukkit.*;
import org.bukkit.scoreboard.*;
import org.bukkit.plugin.*;
import java.util.*;

public class SideBarStats
{
    Scoreboard board;
    PropHunt plugin;
    public static Map<Player, Scoreboard> playerBoards;
    
    public SideBarStats(final PropHunt plugin) {
        this.plugin = plugin;
    }
    
    public void updateBoard() {
        for (final Player p : SideBarStats.playerBoards.keySet()) {
            if (p == null || !p.isOnline()) {
                return;
            }
            if (GameManager.seekers.contains(p.getName())) {
                Objective ob = SideBarStats.playerBoards.get(p).getObjective("seekerboard");
                if (ob == null) {
                    ob = SideBarStats.playerBoards.get(p).registerNewObjective("seekerboard", "dummy");
                }
                final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + this.plugin.ST.seeker_Translate + ":"));
                score.setScore(GameManager.seekers.size());
                final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + this.plugin.ST.hider_Translate + ":"));
                score2.setScore(GameManager.hiders.size());
                final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + this.plugin.ST.time_Left_Translate + ":"));
                score3.setScore(GameManager.timeleft);
            }
            if (GameManager.hiders.contains(p.getName())) {
                Objective ob = SideBarStats.playerBoards.get(p).getObjective("hiderboard");
                if (ob == null) {
                    ob = SideBarStats.playerBoards.get(p).registerNewObjective("hiderboard", "dummy");
                }
                if (this.plugin.dm.isDisguised(p)) {
                    ob.setDisplayName(ChatColor.AQUA + this.plugin.ST.getDisguiseTranslate(this.plugin.dm.getDisguiseName(p)));
                }
                final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + this.plugin.ST.seeker_Translate + ":"));
                score.setScore(GameManager.seekers.size());
                final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + this.plugin.ST.hider_Translate + ":"));
                score2.setScore(GameManager.hiders.size());
                final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + this.plugin.ST.time_Left_Translate + ":"));
                score3.setScore(GameManager.timeleft);
                if (GameManager.usingSolidBlock) {
                    final Score score4 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "" + ChatColor.ITALIC + this.plugin.ST.solid_Time_Translate + ":"));
                    if (SolidBlockTracker.solidBlocks.containsKey(p.getName())) {
                        p.getScoreboard().resetScores(Bukkit.getOfflinePlayer(ChatColor.GREEN + "" + ChatColor.ITALIC + this.plugin.ST.solid_Time_Translate + ":"));
                        final Score newscore = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "" + ChatColor.BOLD + this.plugin.ST.solid_Translate + ""));
                        newscore.setScore(1);
                    }
                    else {
                        p.getScoreboard().resetScores(Bukkit.getOfflinePlayer(ChatColor.GOLD + "" + ChatColor.BOLD + this.plugin.ST.solid_Translate + ""));
                        score4.setScore(GameManager.solidBlockTime - SolidBlockTracker.movementTracker.get(p.getName()));
                    }
                }
            }
            if (GameManager.playersWaiting.contains(p.getName())) {
                Objective ob = SideBarStats.playerBoards.get(p).getObjective("lobbyboard");
                if (ob == null) {
                    ob = SideBarStats.playerBoards.get(p).registerNewObjective("lobbyboard", "dummy");
                }
                if (!GameManager.gameStatus) {
                    final Score scorey = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + this.plugin.ST.starting_In_Translate + ""));
                    scorey.setScore(GameManager.currentLobbyTime);
                }
                else {
                    p.getScoreboard().resetScores(Bukkit.getOfflinePlayer(ChatColor.GREEN + this.plugin.ST.starting_In_Translate + ""));
                }
                final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + this.plugin.ST.seeker_Translate + ":"));
                score.setScore(GameManager.seekers.size());
                final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + this.plugin.ST.hider_Translate + ":"));
                score2.setScore(GameManager.hiders.size());
                final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + this.plugin.ST.time_Left_Translate + ":"));
                score3.setScore(GameManager.timeleft);
                final Score score4 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + this.plugin.ST.player_Translate + ":"));
                score4.setScore(GameManager.playersWaiting.size());
                final Score score5 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + this.plugin.ST.spectator_Translate + ":"));
                score5.setScore(GameManager.spectators.size());
            }
            if (!GameManager.spectators.contains(p.getName())) {
                continue;
            }
            Objective ob = SideBarStats.playerBoards.get(p).getObjective("lobbyboard");
            if (ob == null) {
                ob = SideBarStats.playerBoards.get(p).registerNewObjective("lobbyboard", "dummy");
            }
            final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + this.plugin.ST.seeker_Translate + ":"));
            score.setScore(GameManager.seekers.size());
            final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + this.plugin.ST.hider_Translate + ":"));
            score2.setScore(GameManager.hiders.size());
            final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + this.plugin.ST.time_Left_Translate + ":"));
            score3.setScore(GameManager.timeleft);
            final Score score4 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + this.plugin.ST.player_Translate + ":"));
            score4.setScore(GameManager.playersWaiting.size());
            final Score score5 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + this.plugin.ST.spectator_Translate + ":"));
            score5.setScore(GameManager.spectators.size());
        }
    }
    
    public void removeScoreboard(final PropHunt plugin, final Player p) {
        if (p == null) {
            return;
        }
        if (!p.isOnline()) {
            return;
        }
        if (SideBarStats.playerBoards.containsKey(p)) {
            SideBarStats.playerBoards.remove(p);
            p.setScoreboard(plugin.getServer().getScoreboardManager().getNewScoreboard());
        }
    }
    
    public void addPlayerToGame(final PropHunt plugin, final Player p) {
        if (p == null || !p.isOnline()) {
            return;
        }
        if (GameManager.seekers.contains(p.getName())) {
            final Scoreboard sb = plugin.getServer().getScoreboardManager().getNewScoreboard();
            final Objective ob = sb.registerNewObjective("seekerboard", "dummy");
            ob.setDisplaySlot(DisplaySlot.SIDEBAR);
            ob.setDisplayName(ChatColor.GOLD + "PropHunt Stats");
            final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + plugin.ST.seeker_Translate + ":"));
            score.setScore(GameManager.seekers.size());
            final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + plugin.ST.hider_Translate + ":"));
            score2.setScore(GameManager.hiders.size());
            final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + plugin.ST.time_Left_Translate + ":"));
            score3.setScore(GameManager.timeleft);
            p.setScoreboard(sb);
            SideBarStats.playerBoards.put(p, sb);
        }
        if (GameManager.hiders.contains(p.getName())) {
            final Scoreboard sb = plugin.getServer().getScoreboardManager().getNewScoreboard();
            final Objective ob = sb.registerNewObjective("hiderboard", "dummy");
            ob.setDisplaySlot(DisplaySlot.SIDEBAR);
            ob.setDisplayName(ChatColor.AQUA + plugin.ST.getDisguiseTranslate(plugin.dm.getDisguiseName(p)));
            final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + plugin.ST.seeker_Translate + ":"));
            score.setScore(GameManager.seekers.size());
            final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + plugin.ST.hider_Translate + ":"));
            score2.setScore(GameManager.hiders.size());
            final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + plugin.ST.time_Left_Translate + ":"));
            score3.setScore(GameManager.timeleft);
            p.setScoreboard(sb);
            SideBarStats.playerBoards.put(p, sb);
        }
    }
    
    public void addPlayerToLobby(final PropHunt plugin, final Player p) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                if (p == null || !p.isOnline()) {
                    return;
                }
                final Scoreboard sb = plugin.getServer().getScoreboardManager().getNewScoreboard();
                final Objective ob = sb.registerNewObjective("lobbyboard", "dummy");
                ob.setDisplaySlot(DisplaySlot.SIDEBAR);
                ob.setDisplayName(ChatColor.GOLD + "PropHunt Stats");
                if (GameManager.gameStatus) {
                    final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + plugin.ST.seeker_Translate + ":"));
                    score.setScore(GameManager.seekers.size());
                    final Score score2 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.AQUA + plugin.ST.hider_Translate + ":"));
                    score2.setScore(GameManager.hiders.size());
                    final Score score3 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + plugin.ST.time_Left_Translate + ":"));
                    score3.setScore(GameManager.timeleft);
                    final Score score4 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + plugin.ST.player_Translate + ":"));
                    score4.setScore(GameManager.playersWaiting.size());
                    final Score score5 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + plugin.ST.spectator_Translate + ":"));
                    score5.setScore(GameManager.spectators.size());
                    p.setScoreboard(sb);
                    SideBarStats.playerBoards.put(p, sb);
                }
                else {
                    ob.setDisplayName(ChatColor.GOLD + "Arena: " + ChatColor.AQUA + GameManager.currentGameArena.getArenaName());
                    final Score score = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + plugin.ST.starting_In_Translate + ""));
                    score.setScore(GameManager.currentLobbyTime);
                    final Score score6 = ob.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + plugin.ST.player_Translate + ":"));
                    score6.setScore(GameManager.playersWaiting.size());
                    p.setScoreboard(sb);
                    SideBarStats.playerBoards.put(p, sb);
                }
            }
        }, 40L);
    }
    
    static {
        SideBarStats.playerBoards = new HashMap<Player, Scoreboard>();
    }
}
