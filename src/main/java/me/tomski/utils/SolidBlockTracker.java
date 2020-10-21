package me.tomski.utils;

import me.tomski.blocks.SolidBlock;
import me.tomski.language.MessageBank;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolidBlockTracker implements Runnable
{
    public static Map<String, Integer> movementTracker;
    public static Map<String, Location> currentLocation;
    public static Map<String, SolidBlock> solidBlocks;
    List<String> removeList;
    private PropHunt plugin;

    public SolidBlockTracker(final PropHunt plugin) {
        this.removeList = new ArrayList<String>();
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (final String s : SolidBlockTracker.movementTracker.keySet()) {
            if (Bukkit.getPlayer(s) == null) {
                this.removeList.add(s);
            }
            else if (!this.plugin.dm.isDisguised(Bukkit.getPlayer(s))) {
                this.removeList.add(s);
            }
            else {
                if (SolidBlockTracker.solidBlocks.containsKey(s) && !SolidBlockTracker.solidBlocks.get(s).dead) {
                    continue;
                }
                if (!this.shouldBeSolid(SolidBlockTracker.movementTracker.get(s))) {
                    continue;
                }
                SolidBlock sb = null;
                try {
                    if (this.plugin.dm.getSimpleDisguise(Bukkit.getPlayer(s)) == null) {
                        continue;
                    }
                    if (this.plugin.dm.getSimpleDisguise(Bukkit.getPlayer(s)) != null) {
                        sb = new SolidBlock(SolidBlockTracker.currentLocation.get(s), Bukkit.getPlayer(s), PropHunt.protocolManager, this.plugin);
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (sb == null) {
                    continue;
                }
                SolidBlockTracker.solidBlocks.put(s, sb);
                try {
                    PropHuntMessaging.sendMessage(sb.owner, MessageBank.SOLID_BLOCK.getMsg());
                    sb.sendPacket(Bukkit.getOnlinePlayers());
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        for (final String s : this.removeList) {
            SolidBlockTracker.movementTracker.remove(s);
        }
        this.removeList.clear();
        for (final String p : GameManager.hiders) {
            if (GameManager.seekers.contains(p)) {
                continue;
            }
            if (Bukkit.getPlayer(p) == null) {
                continue;
            }
            if (SolidBlockTracker.solidBlocks.containsKey(p)) {
                continue;
            }
            if (SolidBlockTracker.currentLocation.containsKey(p) && SolidBlockTracker.movementTracker.containsKey(p)) {
                if (this.hasMoved(SolidBlockTracker.currentLocation.get(p), Bukkit.getPlayer(p).getLocation())) {
                    SolidBlockTracker.currentLocation.put(p, Bukkit.getPlayer(p).getLocation().clone());
                    SolidBlockTracker.movementTracker.put(p, 0);
                }
                else {
                    SolidBlockTracker.movementTracker.put(p, SolidBlockTracker.movementTracker.get(p) + 1);
                }
            }
            else {
                SolidBlockTracker.currentLocation.put(p, Bukkit.getPlayer(p).getLocation());
                SolidBlockTracker.movementTracker.put(p, 0);
            }
        }
    }

    public boolean hasMoved(final Location loc, final Location test) {
        return test.getBlockX() != loc.getBlockX() || test.getBlockZ() != loc.getBlockZ() || test.getBlockY() != loc.getBlockY();
    }

    private boolean shouldBeSolid(final int i) {
        return i >= GameManager.solidBlockTime;
    }

    static {
        SolidBlockTracker.movementTracker = new HashMap<String, Integer>();
        SolidBlockTracker.currentLocation = new HashMap<String, Location>();
        SolidBlockTracker.solidBlocks = new HashMap<String, SolidBlock>();
    }
}
