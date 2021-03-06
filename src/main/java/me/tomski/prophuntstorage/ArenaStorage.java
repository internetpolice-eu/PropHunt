package me.tomski.prophuntstorage;

import me.tomski.arenas.Arena;
import me.tomski.arenas.ArenaConfig;
import me.tomski.arenas.ArenaManager;
import me.tomski.objects.ArenaFileStructureWrapper;
import me.tomski.objects.LocationBox;
import me.tomski.prophunt.DisguiseManager;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class ArenaStorage
{
    public FileConfiguration StorageFilef;
    private File customConfigFile;
    private PropHunt plugin;
    private GameManager GM;

    public ArenaStorage(final PropHunt plugin, final GameManager GM) {
        this.StorageFilef = null;
        this.customConfigFile = null;
        this.plugin = plugin;
        this.GM = GM;
    }

    public void loadData() {
        if (!this.getStorageFile().contains("Arenas")) {
            this.plugin.getLogger().log(Level.WARNING, "No arenas have been setup!");
            return;
        }
        for (final String key : this.getStorageFile().getConfigurationSection("Arenas").getKeys(false)) {
            final String path = "Arenas." + key + ".";
            Arena a;
            if (ArenaFileStructureWrapper.usingOldFormat(this.getStorageFile(), key)) {
                final World world = this.plugin.getServer().getWorld(this.getStorageFile().getString(path + "worldname"));
                final Location lobby = this.getStorageFile().getVector(path + "lobbyVec").toLocation(world);
                final Location exit = this.getStorageFile().getVector(path + "exitVec").toLocation(world);
                final Location seeker = this.getStorageFile().getVector(path + "seekerVec").toLocation(world);
                final Location hider = this.getStorageFile().getVector(path + "hiderVec").toLocation(world);
                final Location spec = this.getStorageFile().getVector(path + "spectatorVec").toLocation(world);
                a = new Arena(key, lobby, exit, seeker, hider, spec);
                ArenaManager.playableArenas.put(key, a);
                ArenaFileStructureWrapper.translateToNewStorageFormat(this.plugin, this.getStorageFile(), a);
                this.plugin.getLogger().log(Level.INFO, key + " arena loaded and translated to the new file format");
            }
            else {
                final Location lobby2 = new LocationBox(this.getStorageFile().getString(path + "lobbySpawn")).unBox();
                final Location seeker2 = new LocationBox(this.getStorageFile().getString(path + "seekerSpawn")).unBox();
                final Location exit = new LocationBox(this.getStorageFile().getString(path + "exitSpawn")).unBox();
                final Location hider2 = new LocationBox(this.getStorageFile().getString(path + "hiderSpawn")).unBox();
                final Location spec2 = new LocationBox(this.getStorageFile().getString(path + "spectatorSpawn")).unBox();
                a = new Arena(key, lobby2, exit, seeker2, hider2, spec2);
                ArenaManager.playableArenas.put(key, a);
                this.plugin.getLogger().log(Level.INFO, key + " arena loaded");
            }
            if (!this.plugin.getConfig().contains("CustomArenaConfigs." + key)) {
                a.saveArenaToFile(this.plugin);
            }
        }
        for (final Arena ar : ArenaManager.playableArenas.values()) {
            this.plugin.AM.arenasInRotation.add(ar);
            this.loadCustomSettings(ar);
        }
        this.plugin.getLogger().log(Level.INFO, this.plugin.AM.arenasInRotation.size() + " arenas loaded");
    }

    private void loadCustomSettings(final Arena a) {
        if (this.plugin.getConfig().getBoolean("CustomArenaConfigs." + a.getArenaName() + ".usingDefault")) {
            final ArenaConfig AC = new ArenaConfig(DisguiseManager.blockDisguises, GameManager.hiderCLASS, GameManager.seekerCLASS, true);
            ArenaManager.arenaConfigs.put(a, AC);
            this.plugin.getLogger().log(Level.INFO, a.getArenaName() + " is using default arena Config");
        }
        else {
            this.plugin.getLogger().log(Level.INFO, a.getArenaName() + ": attempting to load custom config");
            final ArenaConfig AC = new ArenaConfig(this.plugin.getCustomDisguises(a.getArenaName()), this.plugin.getCustomHiderClass(a.getArenaName()), this.plugin.getCustomSeekerClass(a.getArenaName()), false);
            ArenaManager.arenaConfigs.put(a, AC);
            this.plugin.getLogger().log(Level.INFO, a.getArenaName() + " is using a custom arena Config");
        }
    }

    public void saveData() {
        for (final Arena a : ArenaManager.playableArenas.values()) {
            a.saveArenaToFile(this.plugin);
        }
    }

    public void reloadStorageFile() {
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), "StorageFile.yml");
        }
        StorageFilef = YamlConfiguration.loadConfiguration(customConfigFile);
        InputStream defConfigStream = plugin.getResource("StorageFile.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            StorageFilef.setDefaults(defConfig);
        }
    }

    public FileConfiguration getStorageFile() {
        if (this.StorageFilef == null) {
            this.reloadStorageFile();
        }
        return this.StorageFilef;
    }

    public void saveStorageFile() {
        if (this.StorageFilef == null || this.customConfigFile == null) {
            return;
        }
        try {
            this.getStorageFile().save(this.customConfigFile);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.customConfigFile, ex);
        }
    }
}
