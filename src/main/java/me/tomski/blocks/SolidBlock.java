package me.tomski.blocks;

import org.bukkit.entity.*;
import com.comphenix.protocol.events.*;
import me.tomski.objects.*;
import me.tomski.prophunt.*;
import me.tomski.listeners.*;
import java.lang.reflect.*;
import com.comphenix.protocol.*;
import org.bukkit.*;
import com.comphenix.protocol.reflect.*;

public class SolidBlock
{
    public Player owner;
    public Location loc;
    public int id;
    int damage;
    ProtocolManager pm;
    PacketContainer blockChange;
    private SimpleDisguise d;
    public boolean dead;
    
    public SolidBlock(final Location loc, final Player p, final ProtocolManager pm, final PropHunt plugin) throws InvocationTargetException {
        this.dead = false;
        this.loc = loc.clone();
        this.pm = pm;
        this.d = plugin.dm.getSimpleDisguise(p);
        this.id = this.d.getID();
        this.damage = this.d.getDamage();
        this.blockChange = this.getBlockPacket();
        plugin.hidePlayer(this.owner = p, this.owner.getInventory().getArmorContents());
        PropHuntListener.tempIgnoreUndisguise.add(this.owner);
    }
    
    public boolean hasMoved(final PropHunt plugin) {
        if (this.owner.getLocation().getBlockX() != this.loc.getBlockX()) {
            return true;
        }
        if (this.owner.getLocation().getBlockZ() != this.loc.getBlockZ()) {
            return true;
        }
        if (this.owner.getLocation().getBlockY() != this.loc.getBlockY()) {
            return true;
        }
        try {
            this.sendPacket(plugin.getServer().getOnlinePlayers());
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private PacketContainer getBlockPacket() {
        this.blockChange = this.pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        try {
            this.blockChange.getIntegers().write(0, this.loc.getBlockX()).write(1, this.loc.getBlockY()).write(2, this.loc.getBlockZ()).write(3, this.damage);
            this.blockChange.getBlocks().write(0, Material.getMaterial(this.id));
        }
        catch (FieldAccessException e) {
            System.out.println("PropHunt: Error with block change packet");
        }
        return this.blockChange;
    }
    
    public void unSetBlock(final PropHunt plugin) throws InvocationTargetException {
        this.dead = true;
        this.blockChange = this.pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        try {
            this.blockChange.getIntegers().write(0, this.loc.getBlockX()).write(1, this.loc.getBlockY()).write(2, this.loc.getBlockZ()).write(3, 0);
            this.blockChange.getBlocks().write(0, Material.AIR);
        }
        catch (FieldAccessException e) {
            System.out.println("PropHunt: Error with block change packet");
        }
        PropHuntListener.tempIgnoreUndisguise.remove(this.owner);
        this.sendPacket(plugin.getServer().getOnlinePlayers());
        plugin.dm.disguisePlayer(this.owner, this.d);
    }
    
    public void sendPacket(final Player[] players) throws InvocationTargetException {
        for (final Player p : players) {
            if (!p.equals(this.owner)) {
                this.pm.sendServerPacket(p, this.blockChange);
            }
        }
    }
}
