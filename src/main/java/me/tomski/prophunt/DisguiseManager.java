package me.tomski.prophunt;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import me.tomski.objects.*;
import org.bukkit.*;
import me.tomski.arenas.*;
import org.bukkit.event.player.*;
import java.util.*;

public class DisguiseManager implements Listener
{
    private static PropHunt plugin;
    public static Map<Integer, SimpleDisguise> blockDisguises;
    public static Map<Player, SimpleDisguise> preChosenDisguise;
    public static Map<Player, Loadout> loadouts;
    boolean shouldDisable;
    
    public DisguiseManager(final PropHunt plugin) {
        this.shouldDisable = false;
    }
    
    public boolean isDisguised(final Player p) {
        return false;
    }
    
    public void disguisePlayer(final Player p, final SimpleDisguise d) {
    }
    
    public void undisguisePlayer(final Player p) {
    }
    
    public String getDisguiseName(final Player p) {
        return "";
    }
    
    private String parseIdToName(final int id) {
        return Material.getMaterial(id).name();
    }
    
    public void randomDisguise(final Player p, final ArenaConfig ac) {
    }
    
    public static String parseDisguiseToName(final SimpleDisguise ds) {
        return ds.getName();
    }
    
    public static SimpleDisguise getRandomDisguiseObject(final Map<Integer, SimpleDisguise> disguises) {
        final int size = disguises.size();
        final Random rnd = new Random();
        final int random = rnd.nextInt(size);
        return disguises.get(random);
    }
    
    public SimpleDisguise getSimpleDisguise(final Player p) {
        return null;
    }
    
    public boolean shouldDisable() {
        return this.shouldDisable;
    }
    
    public void toggleBlockLock(final PlayerToggleSneakEvent e) {
    }
    
    static {
        DisguiseManager.blockDisguises = new HashMap<Integer, SimpleDisguise>();
        DisguiseManager.preChosenDisguise = new HashMap<Player, SimpleDisguise>();
        DisguiseManager.loadouts = new HashMap<Player, Loadout>();
    }
}
