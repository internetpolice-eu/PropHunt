package me.tomski.shop;

import org.bukkit.entity.*;
import me.tomski.language.*;
import me.tomski.utils.*;
import java.util.*;
import org.bukkit.*;
import me.tomski.prophunt.*;
import me.tomski.objects.*;
import org.bukkit.event.*;
import org.bukkit.inventory.*;
import org.bukkit.event.inventory.*;

public class LoadoutChooser implements Listener
{
    private PropHunt plugin;
    private List<Player> inInventory;
    
    public LoadoutChooser(final PropHunt plugin) {
        this.inInventory = new ArrayList<Player>();
        this.plugin = plugin;
    }
    
    public void openBlockShop(final Player p) {
        if (!GameManager.playersWaiting.contains(p.getName())) {
            PropHuntMessaging.sendMessage(p, MessageBank.NOT_IN_LOBBY.getMsg());
            return;
        }
        final Inventory inv = Bukkit.createInventory(p, this.getShopSize(this.plugin.getShopSettings().itemChoices.size()), MessageBank.LOADOUT_NAME.getMsg());
        for (final ShopItem sI : this.plugin.getShopSettings().itemChoices) {
            sI.addToInventory(inv, p);
        }
        p.openInventory(inv);
        this.inInventory.add(p);
    }
    
    @EventHandler
    public void onInventClick(final InventoryClickEvent e) {
        if (this.inInventory.contains(e.getWhoClicked()) && e.getCurrentItem() != null) {
            if (!this.hasPermsForItem((Player)e.getWhoClicked(), e.getCurrentItem())) {
                PropHuntMessaging.sendMessage((Player)e.getWhoClicked(), MessageBank.NO_ITEM_CHOICE_PERMISSION.getMsg());
                e.setCancelled(true);
                return;
            }
            if (e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (GameManager.playersWaiting.contains(e.getWhoClicked().getName())) {
                if (!DisguiseManager.loadouts.containsKey(e.getWhoClicked())) {
                    DisguiseManager.loadouts.put((Player)e.getWhoClicked(), new Loadout((Player)e.getWhoClicked()));
                    DisguiseManager.loadouts.get(e.getWhoClicked()).addItem(e.getCurrentItem());
                }
                else {
                    DisguiseManager.loadouts.get(e.getWhoClicked()).addItem(e.getCurrentItem());
                }
                PropHuntMessaging.sendMessage((Player)e.getWhoClicked(), MessageBank.ITEM_CHOSEN.getMsg() + e.getCurrentItem().getItemMeta().getDisplayName());
                e.getView().close();
            }
            else {
                PropHuntMessaging.sendMessage((Player)e.getWhoClicked(), MessageBank.BLOCK_ACCESS_IN_GAME.getMsg());
            }
        }
    }
    
    private boolean hasPermsForItem(final Player player, final ItemStack currentItem) {
        if (currentItem.getData().getData() == 0) {
            return this.plugin.vaultUtils.permission.has(player, "prophunt.loadout." + currentItem.getTypeId());
        }
        return this.plugin.vaultUtils.permission.has(player, "prophunt.loadout." + currentItem.getTypeId() + "-" + currentItem.getData().getData());
    }
    
    @EventHandler
    public void inventoryClose(final InventoryCloseEvent e) {
        if (this.inInventory.contains(e.getPlayer())) {
            this.inInventory.remove(e.getPlayer());
        }
    }
    
    private int getShopSize(final int n) {
        return (int)Math.ceil(n / 9.0) * 9;
    }
}
