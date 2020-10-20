package me.tomski.prophunt;

import org.bukkit.event.*;
import me.tomski.objects.*;
import pgDev.bukkit.DisguiseCraft.api.*;
import pgDev.bukkit.DisguiseCraft.*;
import java.util.logging.*;
import pgDev.bukkit.DisguiseCraft.disguise.*;
import org.bukkit.*;
import me.tomski.arenas.*;
import me.tomski.language.*;
import me.tomski.utils.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import java.util.*;

public class DisguiseCraftManager extends DisguiseManager implements Listener
{
    private PropHunt plugin;
    public static Map<Integer, SimpleDisguise> blockDisguises;
    public static Map<Player, SimpleDisguise> preChosenDisguise;
    public static Map<Player, Loadout> loadouts;
    public DisguiseCraftAPI dcAPI;
    
    public DisguiseCraftManager(final PropHunt plugin) {
        super(plugin);
        this.dcAPI = DisguiseCraft.getAPI();
        final int i = plugin.loadBlockDisguises();
        plugin.getLogger().log(Level.INFO, "PropHunt: " + i + " disgiuses loaded");
    }
    
    public DisguiseCraftAPI getDcAPI() {
        return this.dcAPI;
    }
    
    private Disguise getDisguise(final SimpleDisguise sd) {
        if (sd.getEntityType() == null) {
            final LinkedList<String> data = new LinkedList<String>();
            data.add("blockID:" + sd.getID());
            data.add("blockData:" + sd.getDamage());
            final int id = this.getDcAPI().newEntityID();
            return new Disguise(id, data, DisguiseType.FallingBlock);
        }
        final int id2 = this.getDcAPI().newEntityID();
        return new Disguise(id2, "", DisguiseType.fromString(sd.getEntityType().name()));
    }
    
    @Override
    public boolean isDisguised(final Player p) {
        return this.dcAPI.isDisguised(p);
    }
    
    @Override
    public void disguisePlayer(final Player p, final SimpleDisguise d) {
        this.dcAPI.disguisePlayer(p, this.getDisguise(d));
    }
    
    @Override
    public void undisguisePlayer(final Player p) {
        this.dcAPI.undisguisePlayer(p, false);
    }
    
    @Override
    public String getDisguiseName(final Player p) {
        if (this.isDisguised(p) && this.getSimpleDisguise(p) != null) {
            return this.parseIdToName(this.dcAPI.getDisguise(p).getBlockID());
        }
        return "None";
    }
    
    private String parseIdToName(final int id) {
        return Material.getMaterial(id).name();
    }
    
    @Override
    public void randomDisguise(final Player p, final ArenaConfig ac) {
        if (DisguiseCraftManager.preChosenDisguise.containsKey(p)) {
            final SimpleDisguise simpleDisguise = DisguiseCraftManager.preChosenDisguise.get(p);
            final Disguise ds = this.getDisguise(simpleDisguise);
            ds.setEntityID(this.getDcAPI().newEntityID());
            if (this.isDisguised(p)) {
                this.getDcAPI().changePlayerDisguise(p, ds);
                PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + DisguiseManager.parseDisguiseToName(simpleDisguise));
            }
            else {
                this.getDcAPI().disguisePlayer(p, ds);
                PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + DisguiseManager.parseDisguiseToName(simpleDisguise));
            }
            DisguiseCraftManager.preChosenDisguise.remove(p);
            return;
        }
        final SimpleDisguise ds2 = DisguiseManager.getRandomDisguiseObject(ac.getArenaDisguises());
        if (ds2 == null) {
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_ERROR.getMsg());
            return;
        }
        if (this.isDisguised(p)) {
            this.getDcAPI().changePlayerDisguise(p, this.getDisguise(ds2));
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + DisguiseManager.parseDisguiseToName(ds2));
        }
        else {
            this.getDcAPI().disguisePlayer(p, this.getDisguise(ds2));
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + DisguiseManager.parseDisguiseToName(ds2));
        }
    }
    
    @Override
    public void toggleBlockLock(final PlayerToggleSneakEvent e) {
        final Disguise d = this.getDcAPI().getDisguise(e.getPlayer());
        if (d.type.equals(DisguiseType.FallingBlock)) {
            if (e.isSneaking()) {
                d.addSingleData("blocklock");
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.TOGGLE_BLOCK_LOCK_ON.getMsg());
            }
            else {
                d.data.remove("blocklock");
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.TOGGLE_BLOCK_LOCK_OFF.getMsg());
            }
        }
    }
    
    @Override
    public SimpleDisguise getSimpleDisguise(final Player p) {
        if (this.dcAPI.getDisguise(p).type.equals(DisguiseType.FallingBlock)) {
            return new SimpleDisguise(this.dcAPI.getDisguise(p).getBlockID(), this.dcAPI.getDisguise(p).getBlockData(), null);
        }
        return null;
    }
    
    static {
        DisguiseCraftManager.blockDisguises = new HashMap<Integer, SimpleDisguise>();
        DisguiseCraftManager.preChosenDisguise = new HashMap<Player, SimpleDisguise>();
        DisguiseCraftManager.loadouts = new HashMap<Player, Loadout>();
    }
}
