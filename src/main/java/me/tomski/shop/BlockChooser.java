package me.tomski.shop;

import me.tomski.language.MessageBank;
import me.tomski.objects.SimpleDisguise;
import me.tomski.prophunt.DisguiseManager;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;
import me.tomski.utils.PropHuntMessaging;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlockChooser implements Listener
{
    private PropHunt plugin;
    private List<Player> inChooser;

    public BlockChooser(final PropHunt plugin) {
        this.inChooser = new ArrayList<Player>();
        this.plugin = plugin;
    }

    public void openBlockShop(final Player p) {
        if (!GameManager.playersWaiting.contains(p.getName())) {
            PropHuntMessaging.sendMessage(p, MessageBank.NOT_IN_LOBBY.getMsg());
            return;
        }
        final Inventory inv = Bukkit.createInventory(p, this.getShopSize(this.plugin.getShopSettings().blockChoices.size()), MessageBank.DISGUISE_NAME.getMsg());
        for (final ShopItem sI : this.plugin.getShopSettings().blockChoices) {
            sI.addToInventory(inv, p);
        }
        p.openInventory(inv);
        this.inChooser.add(p);
    }

    @EventHandler
    public void onInventClick(final InventoryClickEvent e) {
        if (this.inChooser.contains(e.getWhoClicked()) && e.getCurrentItem() != null) {
            if (!this.hasPermsForBlock((Player)e.getWhoClicked(), e.getCurrentItem())) {
                PropHuntMessaging.sendMessage((Player)e.getWhoClicked(), MessageBank.NO_BLOCK_CHOICE_PERMISSION.getMsg());
                e.setCancelled(true);
                return;
            }
            if (e.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (GameManager.playersWaiting.contains(e.getWhoClicked().getName())) {
                DisguiseManager.preChosenDisguise.put((Player)e.getWhoClicked(), this.parseItemToDisguise(e.getCurrentItem()));
                PropHuntMessaging.sendMessage((Player)e.getWhoClicked(), MessageBank.SHOP_CHOSEN_DISGUISE.getMsg() + e.getCurrentItem().getItemMeta().getDisplayName());
                e.getView().close();
            }
            else {
                PropHuntMessaging.sendMessage((Player)e.getWhoClicked(), MessageBank.BLOCK_ACCESS_IN_GAME.getMsg());
            }
        }
    }

    private boolean hasPermsForBlock(final Player player, final ItemStack currentItem) {
        if (currentItem.getData().getData() == 0) {
            return this.plugin.vaultUtils.permission.has(player, "prophunt.blockchooser." + currentItem.getTypeId());
        }
        return this.plugin.vaultUtils.permission.has(player, "prophunt.blockchooser." + currentItem.getTypeId() + "-" + currentItem.getData().getData());
    }

    private SimpleDisguise parseItemToDisguise(final ItemStack itemStack) {
        return new SimpleDisguise(itemStack.getType(), null);
    }

    @EventHandler
    public void inventoryClose(final InventoryCloseEvent e) {
        if (this.inChooser.contains(e.getPlayer())) {
            this.inChooser.remove(e.getPlayer());
        }
    }

    private int getShopSize(final int n) {
        return (int)Math.ceil(n / 9.0) * 9;
    }
}
