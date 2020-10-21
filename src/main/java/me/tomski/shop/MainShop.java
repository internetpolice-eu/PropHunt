package me.tomski.shop;

import me.tomski.language.MessageBank;
import me.tomski.prophunt.PropHunt;
import me.tomski.prophunt.ShopSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MainShop implements Listener
{
    private PropHunt plugin;
    public List<Player> inMenu;

    public MainShop(final PropHunt plugin) {
        this.inMenu = new ArrayList<Player>();
        this.plugin = plugin;
    }

    public void openMainShop(final Player p) {
        final Inventory inv = Bukkit.createInventory(p, 9, MessageBank.SHOP_TITLE.getMsg());
        final ItemStack customItems = new ItemStack(Material.DIAMOND_SWORD);
        final ItemMeta itemMeta = customItems.getItemMeta();
        itemMeta.setDisplayName(MessageBank.ITEM_SHOP_NAME.getMsg());
        final List<String> itemLore = new ArrayList<String>();
        itemLore.add(MessageBank.ITEM_SHOP_DESC.getMsg());
        itemMeta.setLore(itemLore);
        customItems.setItemMeta(itemMeta);
        final ItemStack customDisguises = new ItemStack(Material.GOLD_BLOCK);
        final ItemMeta disguiseMeta = customDisguises.getItemMeta();
        disguiseMeta.setDisplayName(MessageBank.DISGUISE_SHOP_NAME.getMsg());
        final List<String> disLore = new ArrayList<String>();
        disLore.add(MessageBank.DISGUISE_SHOP_DESC.getMsg());
        disguiseMeta.setLore(disLore);
        customDisguises.setItemMeta(disguiseMeta);
        final ItemStack placeHolder = new ItemStack(Material.ENDER_CHEST);
        final ItemMeta placeMeta = placeHolder.getItemMeta();
        placeMeta.setDisplayName(MessageBank.DISGUISE_NAME.getMsg());
        final List<String> placeLore = new ArrayList<String>();
        placeLore.add(MessageBank.DISGUISE_DESC.getMsg());
        placeMeta.setLore(placeLore);
        placeHolder.setItemMeta(placeMeta);
        final ItemStack loadout = new ItemStack(Material.CHEST);
        final ItemMeta loadMeta = loadout.getItemMeta();
        loadMeta.setDisplayName(MessageBank.LOADOUT_NAME.getMsg());
        final List<String> loadLore = new ArrayList<String>();
        loadLore.add(MessageBank.LOADOUT_DESC.getMsg());
        loadMeta.setLore(loadLore);
        loadout.setItemMeta(loadMeta);
        final ItemStack currency = new ItemStack(Material.EMERALD);
        final ItemMeta currencyMeta = currency.getItemMeta();
        currencyMeta.setDisplayName(ChatColor.GOLD + ShopSettings.currencyName);
        final List<String> currencyLore = new ArrayList<String>();
        currencyLore.add(ChatColor.GREEN + "" + this.getCurrencyBalance(p));
        currencyMeta.setLore(currencyLore);
        currency.setItemMeta(currencyMeta);
        inv.setItem(2, customItems);
        inv.setItem(3, customDisguises);
        inv.setItem(4, placeHolder);
        inv.setItem(5, loadout);
        inv.setItem(8, currency);
        this.inMenu.add(p);
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (this.inMenu.contains(e.getWhoClicked()) && e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
            if (e.getCurrentItem().getType().equals(Material.ENDER_CHEST)) {
                e.setCancelled(true);
                e.getView().close();
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new BukkitRunnable() {
                    public void run() {
                        MainShop.this.plugin.getShopManager().getBlockChooser().openBlockShop((Player)e.getWhoClicked());
                    }
                }, 2L);
            }
            else if (e.getCurrentItem().getType().equals(Material.GOLD_BLOCK)) {
                e.setCancelled(true);
                e.getView().close();
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new BukkitRunnable() {
                    public void run() {
                        MainShop.this.plugin.getShopManager().getDisguiseShop().openDisguiseShop((Player)e.getWhoClicked());
                    }
                }, 2L);
            }
            else if (e.getCurrentItem().getType().equals(Material.DIAMOND_SWORD)) {
                e.setCancelled(true);
                e.getView().close();
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new BukkitRunnable() {
                    public void run() {
                        MainShop.this.plugin.getShopManager().getItemShop().openMainShop((Player)e.getWhoClicked());
                    }
                }, 2L);
            }
            else if (e.getCurrentItem().getType().equals(Material.CHEST)) {
                e.setCancelled(true);
                e.getView().close();
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new BukkitRunnable() {
                    public void run() {
                        MainShop.this.plugin.getShopManager().getLoadoutChooser().openBlockShop((Player)e.getWhoClicked());
                    }
                }, 2L);
            }
            else {
                e.setCancelled(true);
                e.getView().close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (this.inMenu.contains(e.getPlayer())) {
            this.inMenu.remove(e.getPlayer());
        }
    }

    public int getCurrencyBalance(final Player p) {
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                return this.plugin.SQL.getCredits(p.getName());
            }
            case VAULT: {
                return (int)this.plugin.vaultUtils.economy.getBalance(p.getName());
            }
            default: {
                return 0;
            }
        }
    }
}
