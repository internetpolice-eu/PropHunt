package me.tomski.prophunt;

import org.bukkit.inventory.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import java.util.*;

public class PlayerManagement
{
    private static Map<String, ItemStack[]> playerInvents;
    private static Map<String, Integer> playerXP;
    private static Map<String, ItemStack[]> playerArmour;
    
    public static void gameStartPlayer(final Player p) {
        saveArmour(p);
        clearEffects(p);
        healPlayer(p);
        saveInvent(p);
        saveXp(p);
    }
    
    private static void removeFromMaps(final Player p) {
        if (PlayerManagement.playerInvents.containsKey(p.getName())) {
            PlayerManagement.playerInvents.remove(p.getName());
        }
        if (PlayerManagement.playerXP.containsKey(p.getName())) {
            PlayerManagement.playerXP.remove(p.getName());
        }
        if (PlayerManagement.playerArmour.containsKey(p.getName())) {
            PlayerManagement.playerArmour.remove(p.getName());
        }
    }
    
    private static void saveArmour(final Player p) {
        PlayerManagement.playerArmour.put(p.getName(), p.getInventory().getArmorContents());
        p.updateInventory();
    }
    
    private static void saveXp(final Player p) {
        PlayerManagement.playerXP.put(p.getName(), p.getLevel());
        p.setLevel(0);
        p.updateInventory();
    }
    
    private static void saveInvent(final Player p) {
        PlayerManagement.playerInvents.put(p.getName(), p.getInventory().getContents());
        p.getInventory().clear();
        p.updateInventory();
    }
    
    private static void healPlayer(final Player p) {
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.updateInventory();
    }
    
    private static void clearEffects(final Player p) {
        for (final PotionEffect pe : p.getActivePotionEffects()) {
            p.removePotionEffect(pe.getType());
        }
    }
    
    public static void gameRestorePlayer(final Player p) {
        restoreXp(p);
        restoreInvent(p);
        restoreArmour(p);
        removeFromMaps(p);
        clearEffects(p);
    }
    
    private static void restoreInvent(final Player p) {
        if (PlayerManagement.playerInvents.containsKey(p.getName())) {
            p.getInventory().setContents((ItemStack[])PlayerManagement.playerInvents.get(p.getName()));
            p.getInventory();
        }
    }
    
    private static void restoreXp(final Player p) {
        if (PlayerManagement.playerXP.containsKey(p.getName())) {
            if (PlayerManagement.playerXP.get(p.getName()) == null) {
                return;
            }
            p.setTotalExperience(0);
            p.setLevel((int)PlayerManagement.playerXP.get(p.getName()));
            p.updateInventory();
        }
        else {
            p.setExp(0.0f);
            p.setLevel(0);
        }
    }
    
    private static void restoreArmour(final Player p) {
        p.getInventory().setArmorContents((ItemStack[])PlayerManagement.playerArmour.get(p.getName()));
        p.updateInventory();
    }
    
    static {
        PlayerManagement.playerInvents = new HashMap<String, ItemStack[]>();
        PlayerManagement.playerXP = new HashMap<String, Integer>();
        PlayerManagement.playerArmour = new HashMap<String, ItemStack[]>();
    }
}
