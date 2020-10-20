package me.tomski.utils;

import org.bukkit.entity.*;
import org.bukkit.*;
import me.tomski.prophunt.*;
import java.util.*;
import me.tomski.arenas.*;
import me.tomski.language.*;

public class PropHuntMessaging
{
    private static String prefix;
    private static String banner;
    
    public static void sendMessage(final Player p, final String msg) {
        final String finalmsg = parseChatColors(PropHuntMessaging.prefix + msg);
        p.sendMessage(finalmsg);
    }
    
    public static void broadcastMessage(final String msg) {
        final String finalmsg = parseChatColors(PropHuntMessaging.prefix + msg);
        Bukkit.broadcastMessage(finalmsg);
    }
    
    public static void broadcastLobby(final String msg) {
        final String finalmsg = parseChatColors(PropHuntMessaging.prefix + msg);
        for (final String s : GameManager.playersWaiting) {
            if (Bukkit.getServer().getPlayer(s) != null) {
                Bukkit.getServer().getPlayer(s).sendMessage(finalmsg);
            }
        }
    }
    
    public static void sendGameStatus(final Player p) {
        if (!GameManager.gameStatus && GameManager.isHosting) {
            p.sendMessage(parseChatColors(PropHuntMessaging.banner));
            p.sendMessage(parseChatColors("&4[&fGameStatus&4]: &6Pre-Game"));
            p.sendMessage(parseChatColors("&c[&bArena&c]: &b" + GameManager.currentGameArena.getArenaName()));
            p.sendMessage(parseChatColors("&c[&bPlayers&c]: &b" + GameManager.playersWaiting.size()));
            p.sendMessage(parseChatColors("&c[&bPlayers&c]: &f" + GameManager.playersWaiting));
            return;
        }
        if (!GameManager.gameStatus) {
            p.sendMessage(parseChatColors(PropHuntMessaging.banner));
            p.sendMessage(parseChatColors("&4[&fGameStatus&4]: &6Not-Live"));
        }
        else {
            p.sendMessage(parseChatColors(PropHuntMessaging.banner));
            p.sendMessage(parseChatColors("&4[&fGameStatus&4]: &6Live"));
            p.sendMessage(parseChatColors("&c[&bArena&c]: &b" + GameManager.currentGameArena.getArenaName()));
            p.sendMessage(parseChatColors("&c[&bSeekers&c]: &b(&4" + GameManager.seekers.size() + "&b) &a" + GameManager.seekers));
            p.sendMessage(parseChatColors("&c[&bHiders&c]: &b(&4" + GameManager.hiders.size() + "&b) &a" + GameManager.hiders));
            p.sendMessage(parseChatColors("&c[&bSpectators&c]: &b(&4" + GameManager.spectators.size() + "&b) &a" + GameManager.spectators));
            p.sendMessage(parseChatColors("&d[&fTimeLeft&d]: &f" + GameManager.timeleft));
        }
    }
    
    public static void sendEconomyHelp(final Player p) {
        p.sendMessage(parseChatColors(PropHuntMessaging.banner));
        p.sendMessage(parseChatColors("&b/ph currency <player> give <amount> &0- &6Grant credits"));
        p.sendMessage(parseChatColors("&b/ph currency <player> remove <amount> &0- &6Remove credits"));
        p.sendMessage(parseChatColors("&b/ph currency <player> set <amount> &0- &6Set credits"));
    }
    
    public static void sendPlayerHelp(final Player p) {
        p.sendMessage(parseChatColors(PropHuntMessaging.banner));
        p.sendMessage(parseChatColors("&b/ph join &0- &6Join the current PropHunt game"));
        p.sendMessage(parseChatColors("&b/ph leave &0- &6Leave the current PropHunt game"));
        p.sendMessage(parseChatColors("&b/ph spectate &0- &6Spectate the current PropHunt game"));
        p.sendMessage(parseChatColors("&b/ph status &0- &6Check the status of PropHunt"));
        p.sendMessage(parseChatColors("&b/ph shop &0- &6Open the PropHunt shop"));
        p.sendMessage(parseChatColors("&b/ph chooser &0- &6Open the disguise chooser"));
        p.sendMessage(parseChatColors("&b/ph loadout &0- &6Open the loadout chooser"));
        p.sendMessage(parseChatColors("&b/ph balance &0- &6Check your PropHunt balance"));
    }
    
    public static void sendHostHelp(final Player p) {
        sendPlayerHelp(p);
        p.sendMessage(parseChatColors("&b/ph host <arena> &0- &6Host a game of PropHunt"));
        p.sendMessage(parseChatColors("&b/ph start &0- &6Start the current PropHunt game"));
        p.sendMessage(parseChatColors("&b/ph stop &0- &6Stop the current PropHunt game"));
        p.sendMessage(parseChatColors("&b/ph kick <player> &0- &6Kick a player from the game"));
        p.sendMessage(parseChatColors("&b/ph setup <ArenaName>&0- &4Admin Only command- setup!"));
        p.sendMessage(parseChatColors("&b/ph delete <arena> &0- &4Admin Only command- delete!!"));
        p.sendMessage(parseChatColors("&b/ph currency &0- &4Currency commands!!"));
    }
    
    public static void broadcastMessageToPlayers(final List<String> hiders, final List<String> seekers, final String msg) {
        for (final String hider : hiders) {
            if (Bukkit.getServer().getPlayer(hider) != null) {
                sendMessage(Bukkit.getServer().getPlayer(hider), msg);
            }
        }
        for (final String seeker : seekers) {
            if (Bukkit.getServer().getPlayer(seeker) != null) {
                sendMessage(Bukkit.getServer().getPlayer(seeker), msg);
            }
        }
    }
    
    private static String parseChatColors(final String m) {
        return m.replaceAll("&", "\u00A7");
    }
    
    public static void sendAvailableArenas(final Player p, final Map<String, Arena> playableArenas) {
        p.sendMessage(parseChatColors(PropHuntMessaging.banner));
        p.sendMessage(parseChatColors("&fPlayable arenas:"));
        if (playableArenas == null || playableArenas.size() == 0) {
            p.sendMessage(parseChatColors("No arenas setup"));
            return;
        }
        for (final String arenaName : playableArenas.keySet()) {
            p.sendMessage(parseChatColors("&6" + arenaName));
        }
    }
    
    static {
        PropHuntMessaging.prefix = MessageBank.BRACKET_COLOUR.getMsg() + "[" + MessageBank.PROP_COLOUR.getMsg() + "Prop" + MessageBank.HUNT_COLOUR.getMsg() + "Hunt" + MessageBank.BRACKET_COLOUR.getMsg() + "]: &a";
        PropHuntMessaging.banner = "&60o0&0_______ &bPropHunt &0_______&60o0";
    }
}
