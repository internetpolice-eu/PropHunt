package me.tomski.prophunt;

import me.tomski.bungee.Pinger;
import me.tomski.language.LanguageManager;
import me.tomski.language.MessageBank;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.IOException;

public class ServerManager implements Listener
{
    public static boolean forceMOTD;
    public static boolean forceMaxPlayers;
    public static int forceMaxPlayersSize;
    public static boolean blockAccessWhilstInGame;
    private PropHunt plugin;

    public ServerManager(final PropHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerKick(final PlayerLoginEvent e) throws IOException {
        if (GameManager.gameStatus && ServerManager.blockAccessWhilstInGame) {
            if (e.getPlayer().isOp() || e.getPlayer().hasPermission("prophunt.joinoverride")) {
                return;
            }
            if (BungeeSettings.usingPropHuntSigns && BungeeSettings.kickToHub) {
                final Pinger ping = new Pinger(this.plugin);
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ping.connectToServer(e.getPlayer(), BungeeSettings.hubname);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 5L);
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', MessageBank.BLOCK_ACCESS_IN_GAME.getMsg()));
                return;
            }
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', MessageBank.BLOCK_ACCESS_IN_GAME.getMsg()));
        }
        if (ServerManager.forceMaxPlayers && (!e.getPlayer().isOp() || e.getPlayer().hasPermission("prophunt.joinoverride")) && this.plugin.getServer().getOnlinePlayers().size() >= ServerManager.forceMaxPlayersSize) {
            e.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.translateAlternateColorCodes('&', MessageBank.SERVER_FULL_MESSAGE.getMsg()));
        }
    }

    @EventHandler
    public void playerPing(final ServerListPingEvent e) {
        if (ServerManager.forceMOTD) {
            e.setMotd(ChatColor.translateAlternateColorCodes('&', this.getMOTD()));
        }
        if (ServerManager.forceMaxPlayers) {
            e.setMaxPlayers(ServerManager.forceMaxPlayersSize);
        }
    }

    private String getMOTD() {
        final boolean status = GameManager.gameStatus;
        final int time = GameManager.timeleft;
        final int hiders = GameManager.hiders.size();
        final int seekers = GameManager.seekers.size();
        String MOTD;
        if (status) {
            MOTD = MessageBank.SERVER_STATUS_IN_GAME_MESSAGE.getMsg();
            MOTD = LanguageManager.regex(MOTD, "\\{seekers\\}", String.valueOf(seekers));
            MOTD = LanguageManager.regex(MOTD, "\\{hiders\\}", String.valueOf(hiders));
            MOTD = LanguageManager.regex(MOTD, "\\{time\\}", String.valueOf(time));
        }
        else {
            MOTD = MessageBank.SERVER_STATUS_IN_LOBBY_MESSAGE.getMsg();
            MOTD = LanguageManager.regex(MOTD, "\\{lobbyplayers\\}", String.valueOf(GameManager.playersWaiting.size()));
        }
        return MOTD;
    }
}
