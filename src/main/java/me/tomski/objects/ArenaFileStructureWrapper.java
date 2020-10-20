package me.tomski.objects;

import org.bukkit.configuration.file.*;
import me.tomski.prophunt.*;
import me.tomski.arenas.*;

public class ArenaFileStructureWrapper
{
    public static boolean usingOldFormat(final FileConfiguration storage, final String arenaName) {
        return storage.isVector("Arenas." + arenaName + ".lobbyVec");
    }
    
    public static void translateToNewStorageFormat(final PropHunt plugin, final FileConfiguration storage, final Arena a) {
        plugin.AS.getStorageFile().set("Arenas." + a.getArenaName(), (Object)null);
        plugin.AS.saveStorageFile();
        plugin.AS.getStorageFile().set("Arenas." + a.getArenaName() + ".lobbySpawn", (Object)new LocationBox(a.getLobbySpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + a.getArenaName() + ".seekerSpawn", (Object)new LocationBox(a.getSeekerSpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + a.getArenaName() + ".hiderSpawn", (Object)new LocationBox(a.getHiderSpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + a.getArenaName() + ".exitSpawn", (Object)new LocationBox(a.getExitSpawn()).box());
        plugin.AS.getStorageFile().set("Arenas." + a.getArenaName() + ".spectatorSpawn", (Object)new LocationBox(a.getSpectatorSpawn()).box());
        plugin.AS.saveStorageFile();
    }
}
