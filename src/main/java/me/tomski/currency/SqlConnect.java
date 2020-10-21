package me.tomski.currency;

import me.tomski.enums.EconomyType;
import me.tomski.prophunt.PropHunt;
import me.tomski.prophunt.ShopSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlConnect
{
    private boolean enabled;
    private PropHunt plugin;
    private SqlSettings settings;
    private Connection connection;

    public SqlConnect(final PropHunt plugin) {
        this.plugin = plugin;
        this.settings = new SqlSettings(plugin);
        try {
            this.testConnection();
            this.enabled = true;
            ShopSettings.economyType = EconomyType.PROPHUNT;
        }
        catch (SQLException e) {
            plugin.getLogger().info("Sql not able to connect! Disabling Sql currency! STACK BELOW ;)");
            e.printStackTrace();
            ShopSettings.enabled = false;
            this.enabled = false;
        }
    }

    private void refreshConnect() throws SQLException {
        if (this.connection == null) {
            this.connection = DriverManager.getConnection(this.settings.getUrl(), this.settings.getUsername(), this.settings.getPass());
        }
    }

    private void testConnection() throws SQLException {
        this.connection = DriverManager.getConnection(this.settings.getConnector() + this.settings.getHost() + ":" + this.settings.getPort() + "/", this.settings.getUsername(), this.settings.getPass());
        final PreparedStatement sampleQueryStatement = this.connection.prepareStatement("CREATE DATABASE IF NOT EXISTS `" + this.settings.getDatabase() + "`");
        sampleQueryStatement.execute();
        sampleQueryStatement.executeUpdate("USE `" + this.settings.getDatabase() + "`");
        sampleQueryStatement.executeUpdate("CREATE TABLE IF NOT EXISTS PropHuntCurrency (playerName VARCHAR(255) PRIMARY KEY,credits INT)");
        sampleQueryStatement.executeUpdate();
        sampleQueryStatement.close();
    }

    public int getCredits(final String playerName) {
        try {
            this.refreshConnect();
            final PreparedStatement findStatement = this.connection.prepareStatement("SELECT * from PropHuntCurrency WHERE playerName=?");
            findStatement.setString(1, playerName);
            final ResultSet rs = findStatement.executeQuery();
            int counter = 0;
            if (rs != null) {
                while (rs.next()) {
                    ++counter;
                }
            }
            if (rs == null || counter == 0) {
                this.plugin.getLogger().info("Creating new Player file for: " + playerName);
                this.setCredits(playerName, 0);
                return 0;
            }
            if (counter <= 1) {
                rs.first();
                return rs.getInt(2);
            }
            this.plugin.getLogger().info("Error with database! Multiple files with the same name");
        }
        catch (SQLException ex) {
            this.plugin.getLogger().info("" + ex);
        }
        return 0;
    }

    public void setCredits(final String playerName, final int amount) {
        try {
            this.refreshConnect();
            final Statement st = this.connection.createStatement();
            st.executeUpdate("INSERT INTO PropHuntCurrency (`playerName`, credits) VALUES ('" + playerName + "', " + amount + ")" + " ON DUPLICATE KEY UPDATE playerName='" + playerName + "', credits=" + amount + "");
        }
        catch (SQLException ex) {
            this.plugin.getLogger().info("" + ex);
        }
    }
}
