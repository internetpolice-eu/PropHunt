package me.tomski.prophunt;

import me.tomski.enums.EconomyType;
import me.tomski.prophuntstorage.ShopConfig;
import me.tomski.shop.ShopItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShopSettings
{
    public List<ShopItem> blockChoices;
    public List<ShopItem> itemChoices;
    public static String currencyName;
    public static boolean usingVault;
    public static boolean enabled;
    public static EconomyType economyType;
    public static double pricePerHiderKill;
    public static double pricePerSeekerKill;
    public static double pricePerSecondsHidden;
    public static double priceSeekerWin;
    public static double priceHiderWin;
    public static double vipBonus;
    private PropHunt plugin;
    private static PropHunt staticPlugin;

    public ShopSettings(final PropHunt plugin) {
        this.blockChoices = new ArrayList<ShopItem>();
        this.itemChoices = new ArrayList<ShopItem>();
        ShopSettings.staticPlugin = plugin;
        this.plugin = plugin;
    }

    public List<ShopItem> generateBlockChoices(final PropHunt plugin, final ShopConfig shopConfig) {
        final String path = "Disguises";
        final Set<String> keys = shopConfig.getShopConfig().getConfigurationSection(path).getKeys(false);
        for (final String key : keys) {
            final String name = shopConfig.getShopConfig().getString(path + "." + key + ".Name");
            final String Id = shopConfig.getShopConfig().getString(path + "." + key + ".Id");
            final double cost = shopConfig.getShopConfig().getDouble(path + "." + key + ".Cost");
            final ItemStack stack = this.parseStringToStack(Id);
            if (stack == null) {
                plugin.getLogger().warning("DISABLING SHOP, error with item : " + name);
                return this.blockChoices;
            }
            final ShopItem item = new ShopItem(plugin, stack, name, (int)cost, this.getStackPermission(stack));
            this.blockChoices.add(item);
            plugin.getLogger().info("Loaded Shop Disguise: " + Id);
        }
        return this.blockChoices;
    }

    public List<ShopItem> generateItemChoices(final PropHunt plugin, final ShopConfig shopConfig) {
        final String path = "Items";
        final Set<String> keys = shopConfig.getShopConfig().getConfigurationSection(path).getKeys(false);
        for (final String key : keys) {
            final String name = shopConfig.getShopConfig().getString(path + "." + key + ".Name");
            final String Id = shopConfig.getShopConfig().getString(path + "." + key + ".Id");
            final double cost = shopConfig.getShopConfig().getDouble(path + "." + key + ".Cost");
            final ItemStack stack = plugin.parseITEMStringToStack(Id);
            if (stack == null) {
                plugin.getLogger().warning("DISABLING SHOP, error with item : " + name);
                return this.itemChoices;
            }
            final ShopItem item = new ShopItem(plugin, stack, name, (int)cost, this.getItemStackPermission(stack));
            this.itemChoices.add(item);
            plugin.getLogger().info("Loaded Shop item: " + Id);
        }
        return this.itemChoices;
    }

    public void loadShopItems(final PropHunt plugin) {
        this.blockChoices = this.generateBlockChoices(plugin, plugin.shopConfig);
        if (this.blockChoices == null) {
            ShopSettings.enabled = false;
        }
        this.itemChoices = this.generateItemChoices(plugin, plugin.shopConfig);
        if (this.itemChoices == null) {
            ShopSettings.enabled = false;
        }
    }

    private String getStackPermission(final ItemStack currentItem) {
        return "prophunt.blockchooser." + currentItem.getType();
    }

    private String getItemStackPermission(final ItemStack currentItem) {
        return "prophunt.loadout." + currentItem.getType();
    }

    private ItemStack parseStringToStack(final String s) {
        Material material = Material.matchMaterial(s);
        if (material != null) {
            return new ItemStack(material);
        }
        return null;
    }
}
