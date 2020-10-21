package me.tomski.classes;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class SeekerClass
{
    private ItemStack helmet;
    private ItemStack torso;
    private ItemStack legs;
    private ItemStack boots;
    private List<PotionEffect> peffects;
    private List<ItemStack> invent;

    public SeekerClass(final ItemStack helmet, final ItemStack torso, final ItemStack legs, final ItemStack boots, final List<PotionEffect> pfx, final List<ItemStack> inv) {
        this.helmet = helmet;
        this.torso = torso;
        this.legs = legs;
        this.boots = boots;
        this.peffects = pfx;
        this.invent = inv;
    }

    public void givePlayer(final Player p) {
        p.getInventory().setHelmet(this.helmet);
        p.getInventory().setChestplate(this.torso);
        p.getInventory().setLeggings(this.legs);
        p.getInventory().setBoots(this.boots);
        p.addPotionEffects(this.peffects);
        for (final ItemStack i : this.invent) {
            p.getInventory().addItem(new ItemStack[] { i });
        }
        p.updateInventory();
    }
}
