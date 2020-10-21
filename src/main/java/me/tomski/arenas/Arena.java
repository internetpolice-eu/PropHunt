package me.tomski.arenas;

import me.tomski.objects.LocationBox;
import me.tomski.prophunt.PropHunt;
import org.bukkit.Location;

public class Arena
{
    private String arenaName;
    private Location seekerSpawn;
    private Location hiderSpawn;
    private Location lobbySpawn;
    private Location spectatorSpawn;
    private Location exitSpawn;

    public Arena(final String name, final Location lobby, final Location exit, final Location seeker, final Location hider, final Location spec) {
        this.arenaName = null;
        this.seekerSpawn = null;
        this.hiderSpawn = null;
        this.lobbySpawn = null;
        this.spectatorSpawn = null;
        this.exitSpawn = null;
        this.arenaName = name;
        this.lobbySpawn = lobby;
        this.exitSpawn = exit;
        this.seekerSpawn = seeker;
        this.hiderSpawn = hider;
        this.spectatorSpawn = spec;
    }

    public String getArenaName() {
        return this.arenaName;
    }

    public void setArenaName(final String arenaName) {
        this.arenaName = arenaName;
    }

    public Location getSeekerSpawn() {
        return this.seekerSpawn;
    }

    public void setSeekerSpawn(final Location seekerSpawn) {
        this.seekerSpawn = seekerSpawn;
    }

    public Location getHiderSpawn() {
        return this.hiderSpawn;
    }

    public void setHiderSpawn(final Location hiderSpawn) {
        this.hiderSpawn = hiderSpawn;
    }

    public Location getLobbySpawn() {
        return this.lobbySpawn;
    }

    public void setLobbySpawn(final Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public Location getSpectatorSpawn() {
        return this.spectatorSpawn;
    }

    public void setSpectatorSpawn(final Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public void saveArenaToFile(final PropHunt plugin) {
        plugin.AS.getStorageFile().set("Arenas." + this.arenaName + ".lobbySpawn", new LocationBox(this.getLobbySpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + this.arenaName + ".seekerSpawn", new LocationBox(this.getSeekerSpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + this.arenaName + ".hiderSpawn", new LocationBox(this.getHiderSpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + this.arenaName + ".exitSpawn", new LocationBox(this.getExitSpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + this.arenaName + ".spectatorSpawn", new LocationBox(this.getSpectatorSpawn()).box());
        plugin.AS.saveStorageFile();
        plugin.AS.saveStorageFile();
        if (!plugin.getConfig().contains("CustomArenaConfigs." + this.arenaName + ".usingDefault")) {
            plugin.getConfig().set("CustomArenaConfigs." + this.arenaName + ".usingDefault", true);
            plugin.saveConfig();
        }
    }

    public boolean isComplete() {
        return this.seekerSpawn != null && this.hiderSpawn != null && this.lobbySpawn != null && this.spectatorSpawn != null && this.exitSpawn != null;
    }

    public Location getExitSpawn() {
        return this.exitSpawn;
    }

    public void setExitSpawn(final Location exitSpawn) {
        this.exitSpawn = exitSpawn;
    }
}
