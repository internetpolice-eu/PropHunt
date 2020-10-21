package me.tomski.bungee;

import me.tomski.prophunt.BungeeSettings;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;
import me.tomski.prophunt.ServerManager;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

public class Pinger
{
    private PropHunt plugin;
    public boolean sentData;

    public Pinger(final PropHunt plugin) {
        this.sentData = false;
        this.plugin = plugin;
    }

    public void connectToServer(final Player p, final String hub) throws IOException {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(b);
        out.writeUTF("Connect");
        out.writeUTF(hub);
        p.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
    }

    public void sendServerDataEmpty() throws IOException {
        if (this.sentData && this.plugin.getServer().getOnlinePlayers().size() > 0) {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Forward");
            out.writeUTF(BungeeSettings.hubname);
            out.writeUTF("PropHunt");
            final ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            final DataOutputStream msgout = new DataOutputStream(msgbytes);
            msgout.writeUTF(BungeeSettings.bungeeName);
            msgout.writeInt(this.plugin.getServer().getMaxPlayers());
            msgout.writeInt(0);
            msgout.writeBoolean(GameManager.gameStatus);
            if (ServerManager.blockAccessWhilstInGame && GameManager.gameStatus) {
                msgout.writeBoolean(false);
            }
            else {
                msgout.writeBoolean(true);
            }
            if (GameManager.gameStatus) {
                msgout.writeUTF(GameManager.currentGameArena.getArenaName());
                msgout.writeInt(GameManager.timeleft);
                msgout.writeInt(GameManager.seekers.size());
                msgout.writeInt(GameManager.hiders.size());
                msgout.writeInt(GameManager.spectators.size());
            }
            else {
                msgout.writeInt(0);
            }
            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
            plugin.getServer().getOnlinePlayers().iterator().next().sendPluginMessage(
                this.plugin, "BungeeCord", b.toByteArray());
        }
    }

    public void sendServerData() throws IOException {
        if (this.plugin.getServer().getOnlinePlayers().size() > 0) {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Forward");
            out.writeUTF(BungeeSettings.hubname);
            out.writeUTF("PropHunt");
            final ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            final DataOutputStream msgout = new DataOutputStream(msgbytes);
            msgout.writeUTF(BungeeSettings.bungeeName);
            if (ServerManager.forceMaxPlayers) {
                msgout.writeInt(ServerManager.forceMaxPlayersSize);
            }
            else {
                msgout.writeInt(this.plugin.getServer().getMaxPlayers());
            }
            msgout.writeInt(this.plugin.getServer().getOnlinePlayers().size());
            msgout.writeBoolean(GameManager.gameStatus);
            if (ServerManager.blockAccessWhilstInGame && GameManager.gameStatus) {
                msgout.writeBoolean(false);
            }
            else {
                msgout.writeBoolean(true);
            }
            if (GameManager.gameStatus) {
                msgout.writeUTF(GameManager.currentGameArena.getArenaName());
                msgout.writeInt(GameManager.timeleft);
                msgout.writeInt(GameManager.seekers.size());
                msgout.writeInt(GameManager.hiders.size());
                msgout.writeInt(GameManager.spectators.size());
            }
            else {
                msgout.writeInt(GameManager.playersWaiting.size());
            }
            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());
            plugin.getServer().getOnlinePlayers().iterator().next().sendPluginMessage(
                this.plugin, "BungeeCord", b.toByteArray());
        }
    }
}
