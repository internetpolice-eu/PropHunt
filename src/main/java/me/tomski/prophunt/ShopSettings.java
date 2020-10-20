package me.tomski.prophunt;

import me.tomski.shop.*;
import me.tomski.enums.*;
import me.tomski.prophuntstorage.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.enchantments.*;
import java.util.*;

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
        final Set<String> keys = (Set<String>)shopConfig.getShopConfig().getConfigurationSection(path).getKeys(false);
        for (final String key : keys) {
            final String name = shopConfig.getShopConfig().getString(path + "." + key + ".Name");
            final String Id = shopConfig.getShopConfig().getString(path + "." + key + ".Id");
            final double cost = shopConfig.getShopConfig().getDouble(path + "." + key + ".Cost");
            final ItemStack stack = this.parseStringToStack(plugin, Id);
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
        final Set<String> keys = (Set<String>)shopConfig.getShopConfig().getConfigurationSection(path).getKeys(false);
        for (final String key : keys) {
            final String name = shopConfig.getShopConfig().getString(path + "." + key + ".Name");
            final String Id = shopConfig.getShopConfig().getString(path + "." + key + ".Id");
            final double cost = shopConfig.getShopConfig().getDouble(path + "." + key + ".Cost");
            final ItemStack stack = this.parseITEMStringToStack(Id);
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
    
    public static ItemStack getCustomItem(final String s) {
        final String mat = ShopSettings.staticPlugin.shopConfig.getShopConfig().getString("PropHuntItems." + s);
        if (mat.split(":").length == 2) {
            final int id = Integer.valueOf(s.split(":")[0]);
            final int damage = Integer.valueOf(s.split(":")[1]);
            final ItemStack stack = new ItemStack(id, 1, (short)(byte)damage);
            return stack;
        }
        if (mat.split(":").length != 1) {
            ShopSettings.staticPlugin.getLogger().warning("Error with Custom item: " + s);
            return null;
        }
        if (Material.getMaterial((int)Integer.valueOf(s)) != null) {
            final ItemStack stack = new ItemStack(Material.getMaterial((int)Integer.valueOf(s)), 1);
            return stack;
        }
        return null;
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
        if (currentItem.getData().getData() == 0) {
            return "prophunt.blockchooser." + currentItem.getTypeId();
        }
        return "prophunt.blockchooser." + currentItem.getTypeId() + "-" + currentItem.getData().getData();
    }
    
    private String getItemStackPermission(final ItemStack currentItem) {
        if (currentItem.getData().getData() == 0) {
            return "prophunt.loadout." + currentItem.getTypeId();
        }
        return "prophunt.loadout." + currentItem.getTypeId() + "-" + currentItem.getData().getData();
    }
    
    private ItemStack parseStringToStack(final PropHunt plugin, final String s) {
        if (s.split(":").length == 2) {
            final int id = Integer.valueOf(s.split(":")[0]);
            final int damage = Integer.valueOf(s.split(":")[1]);
            final ItemStack stack = new ItemStack(id, 1, (short)(byte)damage);
            return stack;
        }
        if (s.split(":").length != 1) {
            plugin.getLogger().warning("Error with shop item with ID: " + s);
            return null;
        }
        if (Material.getMaterial((int)Integer.valueOf(s)) != null) {
            final ItemStack stack = new ItemStack(Material.getMaterial((int)Integer.valueOf(s)), 1);
            return stack;
        }
        return null;
    }
    
    private ItemStack parseITEMStringToStack(final String s) {
        ItemStack stack = null;
        final String[] enchantsplit = s.split(" ");
        if (enchantsplit.length > 1) {
            final String item = enchantsplit[0];
            final String enchants = enchantsplit[1];
            final String[] totalenchants = enchants.split(";");
            int ENCHANTID = 0;
            int ENCHANTLEVEL = 0;
            final Map<Enchantment, Integer> TOTEnchants = new HashMap<Enchantment, Integer>();
            int itemint = 0;
            try {
                itemint = Integer.parseInt(item);
            }
            catch (NumberFormatException nfe) {
                return null;
            }
            for (int i = totalenchants.length, z = 0; z < i; ++z) {
                final String[] subsplit = totalenchants[z].split(":");
                try {
                    ENCHANTID = Integer.parseInt(subsplit[0]);
                    ENCHANTLEVEL = Integer.parseInt(subsplit[1]);
                }
                catch (NumberFormatException nfe2) {
                    return null;
                }
                TOTEnchants.put(Enchantment.getById(ENCHANTID), ENCHANTLEVEL);
            }
            stack = new ItemStack(itemint, 1);
            stack.addUnsafeEnchantments((Map)TOTEnchants);
            return stack;
        }
        final String[] damagesplit = s.split(":");
        if (damagesplit.length > 2) {
            final String id = damagesplit[0];
            final String damage = damagesplit[1];
            final String amount = damagesplit[2];
            int ID = 0;
            short DAMAGE = 0;
            int AMOUNT = 0;
            try {
                ID = Integer.parseInt(id);
                DAMAGE = Short.parseShort(damage);
                AMOUNT = Integer.parseInt(amount);
            }
            catch (NumberFormatException NFE) {
                return null;
            }
            stack = new ItemStack(Material.getMaterial(ID), AMOUNT, DAMAGE);
            return stack;
        }
        final String[] normalsplit = s.split(":");
        final String id2 = normalsplit[0];
        final String amount = normalsplit[1];
        int ID = 0;
        int AMOUNT2 = 0;
        try {
            ID = Integer.parseInt(id2);
            AMOUNT2 = Integer.parseInt(amount);
        }
        catch (NumberFormatException NFE2) {
            return null;
        }
        stack = new ItemStack(Material.getMaterial(ID), AMOUNT2);
        return stack;
    }
}
