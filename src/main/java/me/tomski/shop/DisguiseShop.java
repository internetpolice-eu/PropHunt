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

import java.util.ArrayList;
import java.util.List;

public class DisguiseShop implements Listener
{
    private PropHunt plugin;
    public List<Player> inMenu;

    public DisguiseShop(final PropHunt plugin) {
        this.inMenu = new ArrayList<Player>();
        this.plugin = plugin;
    }

    public void openDisguiseShop(final Player p) {
        final Inventory i = Bukkit.createInventory(p, this.getShopSize(this.plugin.getShopSettings().blockChoices.size()), MessageBank.DISGUISE_SHOP_NAME.getMsg());
        for (final ShopItem item : this.plugin.getShopSettings().blockChoices) {
            item.addToInventory(i, p);
        }
        this.addCurrencyItem(i, p);
        p.openInventory(i);
        this.inMenu.add(p);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (this.inMenu.contains(e.getWhoClicked()) && e.getCurrentItem() != null) {
            for (final ShopItem item : this.plugin.getShopSettings().blockChoices) {
                if (item.itemStack.getType().equals(e.getCurrentItem().getType())) {
                    if ((item.itemStack.getData() != null || item.itemStack.getData().getData() != 0) && item.itemStack.getData().getData() == e.getCurrentItem().getData().getData()) {
                        item.buyItem((Player)e.getWhoClicked());
                        e.getView().close();
                        return;
                    }
                    item.buyItem((Player)e.getWhoClicked());
                    e.getView().close();
                    return;
                }
            }
            if (e.getCurrentItem().getType().equals(Material.EMERALD)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (this.inMenu.contains(e.getPlayer())) {
            this.inMenu.remove(e.getPlayer());
        }
    }

    private void addCurrencyItem(final Inventory i, final Player p) {
        final ItemStack currency = new ItemStack(Material.EMERALD);
        final ItemMeta currencyMeta = currency.getItemMeta();
        currencyMeta.setDisplayName(ChatColor.GOLD + ShopSettings.currencyName);
        final List<String> currencyLore = new ArrayList<String>();
        currencyLore.add(ChatColor.GREEN + "" + this.getCurrencyBalance(p));
        currencyMeta.setLore(currencyLore);
        currency.setItemMeta(currencyMeta);
        i.setItem(i.getSize() - 1, currency);
    }

    private int getShopSize(final int n) {
        return (int)Math.ceil(n / 9.0) * 9;
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
