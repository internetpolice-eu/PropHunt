package me.tomski.language;

import me.tomski.prophunt.PropHunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ScoreboardTranslate
{
    static FileConfiguration currentLanguageFile;
    private PropHunt plugin;
    private FileConfiguration translateConfig;
    private File customLanguageFile;
    public boolean usingTranslations;
    public String player_Translate;
    public String seeker_Translate;
    public String hider_Translate;
    public String spectator_Translate;
    public String time_Left_Translate;
    public String starting_In_Translate;
    public String solid_Time_Translate;
    public String solid_Translate;
    public Map<String, String> disguise_Translations;

    public ScoreboardTranslate(final PropHunt ph) throws IOException {
        this.usingTranslations = false;
        this.player_Translate = "Players";
        this.seeker_Translate = "Seekers";
        this.hider_Translate = "Hiders";
        this.spectator_Translate = "spectators";
        this.time_Left_Translate = "TimeLeft";
        this.starting_In_Translate = "Starting in";
        this.solid_Time_Translate = "SolidTime";
        this.solid_Translate = "SOLID";
        this.disguise_Translations = new HashMap<String, String>();
        this.plugin = ph;
        this.getTranslateFile().options().copyDefaults(true);
        this.saveTranslateFile();
        this.initTranslates();
    }

    public void initTranslates() {
        if (!(this.usingTranslations = this.getTranslateFile().getBoolean("use-translate"))) {
            this.plugin.getLogger().info("Using default scoreboard messages");
            return;
        }
        this.plugin.getLogger().info("Loading custom scoreboard messages");
        final String key = "Translate-Words.";
        if (this.getTranslateFile().contains(key + "Players")) {
            this.maxLength(this.player_Translate = this.getTranslateFile().getString(key + "Players"));
        }
        if (this.getTranslateFile().contains(key + "Seekers")) {
            this.maxLength(this.seeker_Translate = this.getTranslateFile().getString(key + "Seekers"));
        }
        if (this.getTranslateFile().contains(key + "Hiders")) {
            this.maxLength(this.hider_Translate = this.getTranslateFile().getString(key + "Hiders"));
        }
        if (this.getTranslateFile().contains(key + "Spectators")) {
            this.maxLength(this.spectator_Translate = this.getTranslateFile().getString(key + "Spectators"));
        }
        if (this.getTranslateFile().contains(key + "Time-Left")) {
            this.maxLength(this.time_Left_Translate = this.getTranslateFile().getString(key + "Time-Left"));
        }
        if (this.getTranslateFile().contains(key + "Starting-In")) {
            this.maxLength(this.starting_In_Translate = this.getTranslateFile().getString(key + "Starting-In"));
        }
        if (this.getTranslateFile().contains(key + "Solid-Time")) {
            this.maxLength(this.solid_Time_Translate = this.getTranslateFile().getString(key + "Solid-Time"));
        }
        if (this.getTranslateFile().contains(key + "Solid")) {
            this.solid_Translate = this.getTranslateFile().getString(key + "Solid");
            this.maxLength(this.solid_Time_Translate);
        }
        for (final String keyy : this.getTranslateFile().getConfigurationSection("Disguise-Translations").getKeys(false)) {
            this.disguise_Translations.put(keyy, this.maxLength(this.getTranslateFile().getString("Disguise-Translations." + keyy)));
        }
    }

    private String maxLength(final String s) {
        final String finalS = (s.length() < 12) ? s : s.substring(0, 12);
        return finalS;
    }

    public String getDisguiseTranslate(final String matName) {
        if (!this.usingTranslations) {
            return matName;
        }
        if (this.disguise_Translations.containsKey(matName)) {
            return this.disguise_Translations.get(matName);
        }
        return matName;
    }

    public void reloadTranslate() {
        if (customLanguageFile == null) {
            customLanguageFile = new File(plugin.getDataFolder(), "ScoreboardTranslate.yml");
        }
        translateConfig = YamlConfiguration.loadConfiguration(customLanguageFile);
        InputStream defConfigStream = plugin.getResource("ScoreboardTranslate.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            translateConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getTranslateFile() {
        if (this.translateConfig == null) {
            this.reloadTranslate();
        }
        return this.translateConfig;
    }

    public void saveTranslateFile() {
        if (this.translateConfig == null || this.customLanguageFile == null) {
            return;
        }
        try {
            this.getTranslateFile().save(this.customLanguageFile);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.customLanguageFile, ex);
        }
    }
}
