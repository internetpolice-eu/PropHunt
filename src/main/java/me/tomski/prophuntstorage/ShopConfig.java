package me.tomski.prophuntstorage;

import me.tomski.prophunt.PropHunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), "Shop.yml");
        }
        StorageFilef = YamlConfiguration.loadConfiguration(customConfigFile);
        InputStream defConfigStream = plugin.getResource("Shop.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            StorageFilef.setDefaults(defConfig);
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
