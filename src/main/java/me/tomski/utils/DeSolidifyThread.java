package me.tomski.utils;

import org.bukkit.entity.*;
import me.tomski.blocks.*;
import me.tomski.prophunt.*;
import me.tomski.language.*;
import java.lang.reflect.*;
import me.tomski.listeners.*;
import java.util.*;

public class DeSolidifyThread implements Runnable
{
    private PropHunt plugin;
    List<String> removeList;
    List<Player> playerRemoveList;
    
    public DeSolidifyThread(final PropHunt plugin) {
        this.removeList = new ArrayList<String>();
        this.playerRemoveList = new ArrayList<Player>();
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (final Map.Entry<String, SolidBlock> sb : SolidBlockTracker.solidBlocks.entrySet()) {
            if (!sb.getValue().hasMoved(this.plugin)) {
                if (!GameManager.seekers.contains(SolidBlockTracker.solidBlocks.get(sb.getKey()).owner.getName())) {
                    continue;
                }
            }
            try {
                PropHuntMessaging.sendMessage(sb.getValue().owner, MessageBank.BROKEN_SOLID_BLOCK.getMsg());
                sb.getValue().unSetBlock(this.plugin);
                this.removeList.add(sb.getValue().owner.getName());
                SolidBlockTracker.currentLocation.put(sb.getValue().owner.getName(), sb.getValue().owner.getLocation());
                SolidBlockTracker.movementTracker.put(sb.getValue().owner.getName(), 0);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (final String s : this.removeList) {
            SolidBlockTracker.solidBlocks.remove(s);
        }
        this.removeList.clear();
        for (final Player p : PropHuntListener.playerOnBlocks.keySet()) {
            PropHuntListener.playerOnBlocks.put(p, PropHuntListener.playerOnBlocks.get(p) - 1);
            if (PropHuntListener.playerOnBlocks.get(p) <= 0) {
                this.playerRemoveList.add(p);
            }
        }
        for (final Player s2 : this.playerRemoveList) {
            PropHuntListener.playerOnBlocks.remove(s2);
        }
        this.playerRemoveList.clear();
    }
}
