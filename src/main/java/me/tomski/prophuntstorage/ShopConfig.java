package me.tomski.prophuntstorage;

import me.tomski.prophunt.PropHunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

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
        this.StorageFilef = YamlConfiguration.loadConfiguration(this.customConfigFile);
        final InputStream defConfigStream = this.plugin.getResource("Shop.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.StorageFilef.setDefaults(defConfig);
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
