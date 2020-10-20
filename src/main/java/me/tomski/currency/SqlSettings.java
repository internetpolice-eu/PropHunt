package me.tomski.currency;

import me.tomski.prophunt.*;

public class SqlSettings
{
    private String username;
    private String host;
    private String pass;
    private String port;
    private String database;
    private String connector;
    private String url;
    private PropHunt plugin;
    
    public SqlSettings(final PropHunt plugin) {
        this.plugin = plugin;
        this.connector = "jdbc:mysql://";
        this.loadSettings();
    }
    
    private void loadSettings() {
        this.username = this.plugin.getConfig().getString("DatabaseSettings.username");
        this.host = this.plugin.getConfig().getString("DatabaseSettings.host");
        this.port = this.plugin.getConfig().getString("DatabaseSettings.port");
        this.pass = this.plugin.getConfig().getString("DatabaseSettings.password");
        this.database = this.plugin.getConfig().getString("DatabaseSettings.database");
        this.url = this.connector + this.host + ":" + this.port + "/" + this.database;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPass() {
        return this.pass;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public String getPort() {
        return this.port;
    }
    
    public String getDatabase() {
        return this.database;
    }
    
    public String getConnector() {
        return this.connector;
    }
    
    public String getUrl() {
        return this.url;
    }
}
