package me.tomski.prophunt;

import me.tomski.objects.*;
import java.util.logging.*;
import me.libraryaddict.disguise.*;
import me.libraryaddict.disguise.disguisetypes.*;
import org.bukkit.*;
import me.tomski.arenas.*;
import me.tomski.language.*;
import me.tomski.utils.*;
import org.bukkit.entity.*;
import java.util.*;

public class LibsDisguiseManager extends DisguiseManager
{
    private static PropHunt plugin;
    public static Map<Integer, SimpleDisguise> blockDisguises;
    public static Map<Player, SimpleDisguise> preChosenDisguise;
    public static Map<Player, Loadout> loadouts;
    
    public LibsDisguiseManager(final PropHunt plugin) {
        super(plugin);
        final int i = plugin.loadBlockDisguises();
        plugin.getLogger().log(Level.INFO, "PropHunt: " + i + " disgiuses loaded");
        DisguiseAPI.setViewDisguises(true);
    }
    
    private Disguise getLibsDisguise(final SimpleDisguise sd) {
        if (sd.getEntityType() == null) {
            return (Disguise)new MiscDisguise(DisguiseType.FALLING_BLOCK, (int)sd.getID(), sd.getDamage());
        }
        return (Disguise)new MobDisguise(DisguiseType.getType(sd.getEntityType()));
    }
    
    @Override
    public boolean isDisguised(final Player p) {
        return DisguiseAPI.isDisguised((Entity)p);
    }
    
    @Override
    public void disguisePlayer(final Player p, final SimpleDisguise d) {
        final Disguise dis = this.getLibsDisguise(d);
        dis.setViewSelfDisguise(true);
        DisguiseAPI.disguiseToAll((Entity)p, dis);
    }
    
    @Override
    public void undisguisePlayer(final Player p) {
        DisguiseAPI.undisguiseToAll((Entity)p);
    }
    
    @Override
    public String getDisguiseName(final Player p) {
        return DisguiseAPI.getDisguise((Entity)p).getType().equals((Object)DisguiseType.FALLING_BLOCK) ? this.parseIdToName(((MiscDisguise)DisguiseAPI.getDisguise((Entity)p)).getId()) : DisguiseAPI.getDisguise((Entity)p).getEntity().getType().name();
    }
    
    private String parseIdToName(final int id) {
        return Material.getMaterial(id).name();
    }
    
    @Override
    public void randomDisguise(final Player p, final ArenaConfig ac) {
        if (LibsDisguiseManager.preChosenDisguise.containsKey(p)) {
            final SimpleDisguise simpleDisguise = LibsDisguiseManager.preChosenDisguise.get(p);
            this.disguisePlayer(p, simpleDisguise);
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + parseDisguiseToName(simpleDisguise));
            LibsDisguiseManager.preChosenDisguise.remove(p);
            return;
        }
        final SimpleDisguise ds = DisguiseManager.getRandomDisguiseObject(ac.getArenaDisguises());
        if (ds == null) {
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_ERROR.getMsg());
            return;
        }
        this.disguisePlayer(p, ds);
        PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + parseDisguiseToName(ds));
        LibsDisguiseManager.preChosenDisguise.remove(p);
    }
    
    public static String parseDisguiseToName(final SimpleDisguise ds) {
        return ds.getName();
    }
    
    @Override
    public SimpleDisguise getSimpleDisguise(final Player p) {
        if (DisguiseAPI.getDisguise((Entity)p).getType().equals((Object)DisguiseType.FALLING_BLOCK)) {
            return new SimpleDisguise(((MiscDisguise)DisguiseAPI.getDisguise((Entity)p)).getId(), ((MiscDisguise)DisguiseAPI.getDisguise((Entity)p)).getData(), null);
        }
        return null;
    }
    
    static {
        LibsDisguiseManager.blockDisguises = new HashMap<Integer, SimpleDisguise>();
        LibsDisguiseManager.preChosenDisguise = new HashMap<Player, SimpleDisguise>();
        LibsDisguiseManager.loadouts = new HashMap<Player, Loadout>();
    }
}
