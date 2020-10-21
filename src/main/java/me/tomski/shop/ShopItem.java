package me.tomski.shop;

import me.tomski.language.MessageBank;
import me.tomski.prophunt.PropHunt;
import me.tomski.prophunt.ShopSettings;
import me.tomski.utils.PropHuntMessaging;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopItem
{
    ItemStack itemStack;
    String itemName;
    int itemCost;
    String itemPermission;
    List<String> description;
    private PropHunt plugin;

    public ShopItem(final PropHunt plugin, final ItemStack stack, final String name, final int cost, final String permission) {
        this.plugin = plugin;
        this.itemStack = stack;
        this.itemName = name;
        this.itemCost = cost;
        this.itemPermission = permission;
        this.itemStack = this.makeItem();
        this.description = new ArrayList<String>();
    }

    public void addToInventory(final Inventory i, final Player p) {
        if (p.hasPermission(this.itemPermission)) {
            this.description.clear();
            this.description.add(MessageBank.ITEM_BOUGHT_DESC.getMsg());
        }
        else {
            this.description.clear();
            this.description.add(MessageBank.ITEM_COST.getMsg() + this.itemCost);
        }
        final ItemStack stack = this.itemStack.clone();
        final ItemMeta newMeta = stack.getItemMeta();
        newMeta.setLore(this.description);
        stack.setItemMeta(newMeta);
        i.addItem(new ItemStack[] { stack });
    }

    private ItemStack makeItem() {
        final ItemMeta im = this.itemStack.getItemMeta();
        final String name = this.itemStack.getType().name().toLowerCase().replaceAll("_", " ");
        final String finalName = name.substring(0, 1).toUpperCase() + name.substring(1);
        im.setDisplayName(ChatColor.GOLD + finalName);
        this.itemStack.setItemMeta(im);
        return this.itemStack;
    }

    public boolean buyItem(final Player p) {
        if (p.hasPermission(this.itemPermission)) {
            PropHuntMessaging.sendMessage(p, MessageBank.ALREADY_PURCHASED_ITEM.getMsg());
            return false;
        }
        return this.attemptPurchase(p);
    }

    private boolean attemptPurchase(final Player p) {
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                if (this.plugin.SQL.getCredits(p.getName()) >= this.itemCost) {
                    int credits = this.plugin.SQL.getCredits(p.getName());
                    credits -= this.itemCost;
                    this.plugin.SQL.setCredits(p.getName(), credits);
                    this.plugin.vaultUtils.permission.playerAdd(p, this.itemPermission);
                    PropHuntMessaging.sendMessage(p, MessageBank.PURCHASE_COMPLETE.getMsg() + this.itemName);
                    return true;
                }
                PropHuntMessaging.sendMessage(p, MessageBank.NOT_ENOUGH_CURRENCY.getMsg());
                return false;
            }
            case VAULT: {
                if (this.plugin.vaultUtils.economy.getBalance(p.getName()) >= this.itemCost) {
                    this.plugin.vaultUtils.economy.withdrawPlayer(p.getName(), this.itemCost);
                    this.plugin.vaultUtils.permission.playerAdd(p, this.itemPermission);
                    PropHuntMessaging.sendMessage(p, MessageBank.PURCHASE_COMPLETE.getMsg() + this.itemName);
                    return true;
                }
                PropHuntMessaging.sendMessage(p, MessageBank.NOT_ENOUGH_CURRENCY.getMsg());
                return false;
            }
            default: {
                return false;
            }
        }
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}
