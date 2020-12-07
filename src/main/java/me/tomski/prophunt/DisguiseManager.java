package me.tomski.prophunt;

import me.tomski.arenas.ArenaConfig;
import me.tomski.objects.Loadout;
import me.tomski.objects.SimpleDisguise;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DisguiseManager implements Listener {
    protected PropHunt plugin;
    public static Map<Integer, SimpleDisguise> blockDisguises;
    public static Map<Player, SimpleDisguise> preChosenDisguise;
    public static Map<Player, Loadout> loadouts;
    boolean shouldDisable;

    public DisguiseManager(final PropHunt plugin) {
        this.plugin = plugin;
        shouldDisable = false;
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
        return shouldDisable;
    }

    public void toggleBlockLock(final PlayerToggleSneakEvent e) {
    }

    static {
        blockDisguises = new HashMap<>();
        preChosenDisguise = new HashMap<>();
        loadouts = new HashMap<>();
    }
}
