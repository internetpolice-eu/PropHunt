package me.tomski.language;

import me.tomski.prophunt.PropHunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class LanguageManager
{
    static FileConfiguration currentLanguageFile;
    private PropHunt plugin;
    private File customConfigFile;
    private String fileName;
    private FileConfiguration languageConfig;
    private File customLanguageFile;

    public LanguageManager(final PropHunt ph) throws IOException {
        this.plugin = ph;
        this.fileName = this.plugin.getConfig().getString("UseLanguageFile");
        this.getLanguageFile().options().copyDefaults(true);
        this.saveLanguageFile();
        this.initfile();
        this.copyDefaultsToCurrentfile();
    }

    private void copyDefaultsToCurrentfile() throws IOException {
        for (final String key : this.languageConfig.getKeys(false)) {
            if (LanguageManager.currentLanguageFile.contains(key)) {
                continue;
            }
            LanguageManager.currentLanguageFile.set(key, this.languageConfig.getString(key));
        }
        LanguageManager.currentLanguageFile.save(this.customConfigFile);
    }

    public void initfile() {
        this.getLanguageFileInUse(this.fileName);
    }

    public static String regex(final String msg, final String regex, final String replacement) {
        return msg.replaceAll(regex, replacement);
    }

    private void getLanguageFileInUse(final String name) {
        if (this.customConfigFile == null) {
            this.customConfigFile = new File(this.plugin.getDataFolder(), name);
        }
        LanguageManager.currentLanguageFile = YamlConfiguration.loadConfiguration(this.customConfigFile);
    }

    public void reloadLanguage() {
        if (customLanguageFile == null) {
            customLanguageFile = new File(plugin.getDataFolder(), "Language.yml");
        }
        languageConfig = YamlConfiguration.loadConfiguration(customLanguageFile);
        InputStream defConfigStream = plugin.getResource("Language.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            languageConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getLanguageFile() {
        if (this.languageConfig == null) {
            this.reloadLanguage();
        }
        return this.languageConfig;
    }

    public void saveLanguageFile() {
        if (this.languageConfig == null || this.customLanguageFile == null) {
            return;
        }
        try {
            this.getLanguageFile().save(this.customLanguageFile);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.customLanguageFile, ex);
        }
    }

    public static String getMessageFromFile(final String string) {
        return LanguageManager.currentLanguageFile.getString(string);
    }
}
