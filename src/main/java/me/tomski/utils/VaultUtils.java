package me.tomski.utils;

import me.tomski.enums.EconomyType;
import me.tomski.prophunt.PropHunt;
import me.tomski.prophunt.ShopSettings;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtils
{
    public Permission permission;
    public Economy economy;
    private PropHunt plugin;

    public VaultUtils(final PropHunt plugin) {
        this.permission = null;
        this.economy = null;
        this.plugin = plugin;
        if (this.setupPermissions()) {
            ShopSettings.enabled = true;
            plugin.getLogger().info("Vault permissions found!");
            if (this.setupEconomy()) {
                ShopSettings.enabled = true;
                ShopSettings.currencyName = this.economy.currencyNamePlural();
                plugin.getLogger().info("Vault Economy found!");
                ShopSettings.economyType = EconomyType.VAULT;
            }
            else {
                ShopSettings.enabled = false;
                plugin.getLogger().info("Vault Economy not found! Shop disabling!");
            }
            return;
        }
        ShopSettings.enabled = false;
        plugin.getLogger().info("Vault permissions not found! Shop disabling!");
    }

    public VaultUtils(final PropHunt plugin, final boolean usingPropHunt) {
        this.permission = null;
        this.economy = null;
        if (usingPropHunt) {
            this.plugin = plugin;
            if (this.setupPermissions()) {
                ShopSettings.enabled = true;
                plugin.getLogger().info("Vault permissions found!");
            }
            else {
                ShopSettings.enabled = false;
                plugin.getLogger().info("Vault permissions not found! Shop disabling!");
            }
        }
    }

    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> permissionProvider = (RegisteredServiceProvider<Permission>)this.plugin.getServer().getServicesManager().getRegistration((Class)Permission.class);
        if (permissionProvider != null) {
            this.permission = permissionProvider.getProvider();
        }
        return this.permission != null;
    }

    private boolean setupEconomy() {
        final RegisteredServiceProvider<Economy> economyProvider = (RegisteredServiceProvider<Economy>)this.plugin.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }
        return this.economy != null;
    }
}
