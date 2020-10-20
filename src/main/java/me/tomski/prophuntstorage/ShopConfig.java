package me.tomski.prophuntstorage;

import me.tomski.prophunt.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.*;
import java.util.logging.*;
import java.io.*;

public class ShopConfig
{
    public FileConfiguration StorageFilef;
    private File customConfigFile;
    private PropHunt plugin;
    
    public ShopConfig(final PropHunt plugin) {
        this.StorageFilef = null;
        this.customConfigFile = null;
        this.plugin = plugin;
        this.getShopConfig().options().copyDefaults(true);
        this.saveShopConfig();
    }
    
    public void reloadShopConfig() {
        if (this.customConfigFile == null) {
            this.customConfigFile = new File(this.plugin.getDataFolder(), "Shop.yml");
        }
        this.StorageFilef = (FileConfiguration)YamlConfiguration.loadConfiguration(this.customConfigFile);
        final InputStream defConfigStream = this.plugin.getResource("Shop.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.StorageFilef.setDefaults((Configuration)defConfig);
        }
    }
    
    public FileConfiguration getShopConfig() {
        if (this.StorageFilef == null) {
            this.reloadShopConfig();
        }
        return this.StorageFilef;
    }
    
    public void saveShopConfig() {
        if (this.StorageFilef == null || this.customConfigFile == null) {
            return;
        }
        try {
            this.getShopConfig().save(this.customConfigFile);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.customConfigFile, ex);
        }
    }
}
