package me.tomski.prophunt;

import me.tomski.bungee.Pinger;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public class AutomationSettings
{
    public static boolean dispatchCommands;
    public static int gamesTillReset;
    public static int gamesPlayed;
    public static List<String> commandsToRun;

    public static void initSettings(final PropHunt plugin) {
        AutomationSettings.dispatchCommands = plugin.getConfig().getBoolean("AutomationSettings.dispatch-commands-after-x-games");
        AutomationSettings.gamesTillReset = plugin.getConfig().getInt("AutomationSettings.number-of-games");
        AutomationSettings.commandsToRun = plugin.getConfig().getStringList("AutomationSettings.commands");
    }

    public static boolean runChecks(final PropHunt plugin) {
        if (!AutomationSettings.dispatchCommands) {
            return false;
        }
        ++AutomationSettings.gamesPlayed;
        if (AutomationSettings.gamesPlayed == AutomationSettings.gamesTillReset) {
            for (final String command : AutomationSettings.commandsToRun) {
                if (command.equalsIgnoreCase("kickalltohub")) {
                    final Pinger ping = new Pinger(plugin);
                    for (final Player p : plugin.getServer().getOnlinePlayers()) {
                        try {
                            ping.connectToServer(p, BungeeSettings.hubname);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    if (command.equalsIgnoreCase("stop")) {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.getServer().shutdown();
                            }
                        }, 100L);
                        return true;
                    }
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
            }
            AutomationSettings.gamesPlayed = 0;
            return false;
        }
        return false;
    }

    static {
        AutomationSettings.gamesPlayed = 0;
    }
}
