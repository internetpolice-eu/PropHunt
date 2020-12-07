package me.tomski.blocks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.tomski.listeners.PropHuntListener;
import me.tomski.objects.SimpleDisguise;
import me.tomski.prophunt.PropHunt;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class SolidBlock {
    public Player owner;
    public Location loc;
    ProtocolManager pm;
    PacketContainer blockChange;
    public SimpleDisguise d;
    public boolean dead;

    public SolidBlock(final Location loc, final Player p, final ProtocolManager pm, final PropHunt plugin) throws InvocationTargetException {
        this.dead = false;
        this.loc = loc.clone();
        this.pm = pm;
        this.d = plugin.dm.getSimpleDisguise(p);
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
            BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            WrappedBlockData data = WrappedBlockData.createData(d.getMaterial());

            blockChange.getBlockPositionModifier().write(0, pos);
            blockChange.getBlockData().write(0, data);
        } catch (FieldAccessException e) {
            e.printStackTrace();
            System.out.println("PropHunt: Error with block change packet");
        }
        return this.blockChange;
    }

    public void unSetBlock(final PropHunt plugin) throws InvocationTargetException {
        this.dead = true;
        this.blockChange = this.pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        try {
            // TODO: Restore original data for this location, e.g. when standing in a sign?
            BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            WrappedBlockData data = WrappedBlockData.createData(Material.AIR);

            blockChange.getBlockPositionModifier().write(0, pos);
            blockChange.getBlockData().write(0, data);
        } catch (FieldAccessException e) {
            e.printStackTrace();
            System.out.println("PropHunt: Error with block change packet");
        }
        PropHuntListener.tempIgnoreUndisguise.remove(this.owner);
        this.sendPacket(plugin.getServer().getOnlinePlayers());
        plugin.dm.disguisePlayer(this.owner, this.d);
        plugin.showPlayer(this.owner, false);
    }

    public void sendPacket(Collection<? extends Player> players) throws InvocationTargetException {
        for (final Player p : players) {
            if (!p.equals(this.owner)) {
                this.pm.sendServerPacket(p, this.blockChange);
            }
        }
    }
}
