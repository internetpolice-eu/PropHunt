package me.tomski.classes;

import org.bukkit.inventory.*;
import org.bukkit.potion.*;
import org.bukkit.entity.*;
import java.util.*;

public class HiderClass
{
    private ItemStack helmet;
    private ItemStack torso;
    private ItemStack legs;
    private ItemStack boots;
    private List<PotionEffect> peffects;
    private List<ItemStack> invent;
    
    public HiderClass(final ItemStack helmet, final ItemStack torso, final ItemStack legs, final ItemStack boots, final List<PotionEffect> pfx, final List<ItemStack> inv) {
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
        p.addPotionEffects((Collection)this.peffects);
        for (final ItemStack i : this.invent) {
            p.getInventory().addItem(new ItemStack[] { i });
        }
        p.updateInventory();
    }
}
