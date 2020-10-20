package me.tomski.utils;

import org.bukkit.entity.*;
import org.mcsg.double0negative.tabapi.*;
import org.bukkit.plugin.*;
import me.tomski.prophunt.*;
import me.tomski.blocks.*;
import org.bukkit.*;
import java.util.*;

public class PHScoreboard
{
    private PropHunt plugin;
    public static boolean disguisesBlown;
    
    public PHScoreboard(final PropHunt plugin) {
        this.plugin = plugin;
    }
    
    public void updateTab(final Player p) {
        if (!p.isOnline()) {
            return;
        }
        TabAPI.setPriority((Plugin)this.plugin, p, 2);
        TabAPI.updatePlayer(p);
        TabAPI.setTabString((Plugin)this.plugin, p, 1, 0, ChatColor.GOLD + "" + ChatColor.BOLD + "Prop");
        TabAPI.setTabString((Plugin)this.plugin, p, 1, 1, ChatColor.GOLD + "" + ChatColor.BOLD + "Hunt");
        TabAPI.setTabString((Plugin)this.plugin, p, 1, 2, ChatColor.GOLD + "" + ChatColor.BOLD + "Status");
        TabAPI.setTabString((Plugin)this.plugin, p, 0, 0, ChatColor.GREEN + "----------" + TabAPI.nextNull());
        TabAPI.setTabString((Plugin)this.plugin, p, 0, 1, ChatColor.GREEN + "----------" + TabAPI.nextNull());
        TabAPI.setTabString((Plugin)this.plugin, p, 0, 2, ChatColor.GREEN + "----------" + TabAPI.nextNull());
        TabAPI.setTabString((Plugin)this.plugin, p, 2, 0, ChatColor.GREEN + "----------" + TabAPI.nextNull());
        TabAPI.setTabString((Plugin)this.plugin, p, 2, 1, ChatColor.GREEN + "----------" + TabAPI.nextNull());
        TabAPI.setTabString((Plugin)this.plugin, p, 2, 2, ChatColor.GREEN + "----------" + TabAPI.nextNull());
        TabAPI.setTabString((Plugin)this.plugin, p, 4, 0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Seekers!");
        TabAPI.setTabString((Plugin)this.plugin, p, 4, 1, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Hiders!");
        String team = "";
        if (GameManager.seekers.contains(p.getName())) {
            team = "Seeker";
        }
        if (GameManager.hiders.contains(p.getName())) {
            team = "Hider";
        }
        TabAPI.setTabString((Plugin)this.plugin, p, 4, 2, ChatColor.GOLD + "" + ChatColor.BOLD + "Your team:");
        TabAPI.setTabString((Plugin)this.plugin, p, 5, 2, ChatColor.YELLOW + team);
        if (this.plugin.dm.isDisguised(p)) {
            TabAPI.setTabString((Plugin)this.plugin, p, 6, 2, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Disguise:");
            if (this.plugin.dm.isDisguised(p)) {
                TabAPI.setTabString((Plugin)this.plugin, p, 7, 2, ChatColor.LIGHT_PURPLE + this.plugin.dm.getDisguiseName(p));
            }
        }
        if (PHScoreboard.disguisesBlown) {
            int y = 5;
            for (final String name : GameManager.seekers) {
                if (y == 20) {
                    continue;
                }
                TabAPI.setTabString((Plugin)this.plugin, p, y, 0, ChatColor.RED + name);
                ++y;
            }
            while (y < 20) {
                TabAPI.setTabString((Plugin)this.plugin, p, y, 0, " " + TabAPI.nextNull());
                ++y;
            }
            y = 5;
            for (final String name : GameManager.hiders) {
                if (y == 20) {
                    continue;
                }
                if (this.plugin.getServer().getPlayer(name) != null && this.plugin.getServer().getPlayer(name).isOnline() && this.plugin.dm.isDisguised(this.plugin.getServer().getPlayer(name))) {
                    TabAPI.setTabString((Plugin)this.plugin, p, y, 1, ChatColor.GREEN + this.plugin.dm.getDisguiseName(this.plugin.getServer().getPlayer(name)) + TabAPI.nextNull());
                    ++y;
                }
                if (!SolidBlockTracker.solidBlocks.containsKey(name)) {
                    continue;
                }
                TabAPI.setTabString((Plugin)this.plugin, p, y, 1, ChatColor.GREEN + Material.getMaterial(SolidBlockTracker.solidBlocks.get(name).id).name() + TabAPI.nextNull());
                ++y;
            }
            while (y < 20) {
                TabAPI.setTabString((Plugin)this.plugin, p, y, 1, " " + TabAPI.nextNull());
                ++y;
            }
        }
        else {
            int y = 5;
            for (final String name : GameManager.seekers) {
                if (y == 20) {
                    continue;
                }
                TabAPI.setTabString((Plugin)this.plugin, p, y, 0, ChatColor.RED + name);
                ++y;
            }
            while (y < 20) {
                TabAPI.setTabString((Plugin)this.plugin, p, y, 0, " " + TabAPI.nextNull());
                ++y;
            }
            y = 5;
            for (final String name : GameManager.hiders) {
                if (y == 20) {
                    continue;
                }
                TabAPI.setTabString((Plugin)this.plugin, p, y, 1, ChatColor.GREEN + name);
                ++y;
            }
            while (y < 20) {
                TabAPI.setTabString((Plugin)this.plugin, p, y, 1, " " + TabAPI.nextNull());
                ++y;
            }
        }
        TabAPI.setTabString((Plugin)this.plugin, p, 9, 2, ChatColor.BLUE + "" + ChatColor.BOLD + "Time left");
        TabAPI.setTabString((Plugin)this.plugin, p, 10, 2, ChatColor.BLUE + "" + GameManager.timeleft);
        TabAPI.updatePlayer(p);
    }
    
    public void removeTab(final Player p) {
        if (p.isOnline()) {
            TabAPI.setPriority((Plugin)this.plugin, p, -2);
            TabAPI.updatePlayer(p);
        }
    }
    
    static {
        PHScoreboard.disguisesBlown = false;
    }
}
