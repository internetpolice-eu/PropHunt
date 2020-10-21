package me.tomski.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Loadout
{
    private Player player;
    List<ItemStack> loadout;

    public Loadout(final Player p) {
        this.loadout = new ArrayList<ItemStack>();
        this.player = p;
    }

    public void addItem(final ItemStack stack) {
        this.loadout.add(stack);
    }

    public void giveLoadout() {
        for (final ItemStack stack : this.loadout) {
            this.player.getInventory().addItem(new ItemStack[] { stack });
        }
        this.player.updateInventory();
    }
}
