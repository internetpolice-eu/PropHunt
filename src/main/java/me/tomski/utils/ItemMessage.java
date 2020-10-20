package me.tomski.utils;

import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.apache.commons.lang.*;
import org.bukkit.metadata.*;
import java.util.*;
import org.bukkit.scheduler.*;
import java.lang.ref.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.*;
import org.bukkit.event.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.*;
import java.lang.reflect.*;

public class ItemMessage
{
    private static final int INTERVAL = 20;
    private static final int DEFAULT_DURATION = 2;
    private static final int DEFAULT_PRIORITY = 0;
    private static final String DEF_FORMAT_1 = "%s";
    private static final String DEF_FORMAT_2 = " %s ";
    private static final String METADATA_Q_KEY = "item-message:msg-queue";
    private static final String METADATA_ID_KEY = "item-message:id";
    private final Plugin plugin;
    private String[] formats;
    
    public ItemMessage(final Plugin plugin) {
        this.formats = new String[] { "%s", " %s " };
        final Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (p == null || !p.isEnabled()) {
            throw new IllegalStateException("ItemMessage can not be used without ProtocolLib");
        }
        this.plugin = plugin;
    }
    
    public void sendMessage(final Player player, final String message) {
        this.sendMessage(player, message, 2, 0);
    }
    
    public void sendMessage(final Player player, final String message, final int duration) {
        this.sendMessage(player, message, duration, 0);
    }
    
    public void sendMessage(final Player player, final String message, final int duration, final int priority) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(message);
        }
        else {
            final PriorityQueue<MessageRecord> msgQueue = this.getMessageQueue(player);
            msgQueue.add(new MessageRecord(message, duration, priority, this.getNextId(player)));
            if (msgQueue.size() == 1) {
                new NamerTask(player, msgQueue.peek()).runTaskTimer(this.plugin, 1L, 20L);
            }
        }
    }
    
    public void setFormats(final String f1, final String f2) {
        Validate.isTrue(!f1.equals(f2), "format strings must be different");
        Validate.isTrue(f1.contains("%s"), "format string 1 must contain a %s");
        Validate.isTrue(f2.contains("%s"), "format string 2 must contain a %s");
        this.formats[0] = f1;
        this.formats[1] = f2;
    }
    
    private long getNextId(final Player player) {
        long id;
        if (player.hasMetadata("item-message:id")) {
            final List<MetadataValue> l = player.getMetadata("item-message:id");
            id = ((l.size() >= 1) ? l.get(0).asLong() : 1L);
        }
        else {
            id = 1L;
        }
        player.setMetadata("item-message:id", new FixedMetadataValue(this.plugin, id + 1L));
        return id;
    }
    
    private PriorityQueue<MessageRecord> getMessageQueue(final Player player) {
        if (!player.hasMetadata("item-message:msg-queue")) {
            player.setMetadata("item-message:msg-queue", new FixedMetadataValue(this.plugin, new PriorityQueue()));
        }
        for (final MetadataValue v : player.getMetadata("item-message:msg-queue")) {
            if (v.value() instanceof PriorityQueue) {
                return (PriorityQueue<MessageRecord>)v.value();
            }
        }
        return null;
    }
    
    private void notifyDone(final Player player) {
        final PriorityQueue<MessageRecord> msgQueue = this.getMessageQueue(player);
        msgQueue.poll();
        if (!msgQueue.isEmpty()) {
            final MessageRecord rec = this.importOtherMessageRecord(msgQueue.peek());
            new NamerTask(player, rec).runTaskTimer(this.plugin, 1L, 20L);
        }
    }
    
    private MessageRecord importOtherMessageRecord(final Object other) {
        if (other instanceof MessageRecord) {
            return (MessageRecord)other;
        }
        if (other.getClass().getName().endsWith(".ItemMessage$MessageRecord")) {
            try {
                final Method m1 = other.getClass().getMethod("getId", new Class[0]);
                final Method m2 = other.getClass().getMethod("getPriority", new Class[0]);
                final Method m3 = other.getClass().getMethod("getMessage", new Class[0]);
                final Method m4 = other.getClass().getMethod("getDuration", new Class[0]);
                final long otherId = (Long)m1.invoke(other, new Object[0]);
                final int otherPriority = (Integer)m2.invoke(other, new Object[0]);
                final String otherMessage = (String)m3.invoke(other, new Object[0]);
                final int otherDuration = (Integer)m4.invoke(other, new Object[0]);
                return new MessageRecord(otherMessage, otherDuration, otherPriority, otherId);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    
    private class NamerTask extends BukkitRunnable implements Listener
    {
        private final WeakReference<Player> playerRef;
        private final String message;
        private int slot;
        private int iterations;
        
        public NamerTask(final Player player, final MessageRecord rec) {
            this.playerRef = new WeakReference<Player>(player);
            this.iterations = Math.max(1, rec.getDuration() * 20 / 20);
            this.slot = player.getInventory().getHeldItemSlot();
            this.message = rec.getMessage();
            Bukkit.getPluginManager().registerEvents(this, ItemMessage.this.plugin);
        }
        
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemHeldChange(final PlayerItemHeldEvent event) {
            final Player player = event.getPlayer();
            if (player.equals(this.playerRef.get())) {
                this.sendItemSlotChange(player, event.getPreviousSlot(), player.getInventory().getItem(event.getPreviousSlot()));
                this.slot = event.getNewSlot();
                this.refresh(event.getPlayer());
            }
        }
        
        @EventHandler
        public void onPluginDisable(final PluginDisableEvent event) {
            final Player player = this.playerRef.get();
            if (event.getPlugin() == ItemMessage.this.plugin && player != null) {
                ItemMessage.this.getMessageQueue(player).clear();
                this.finish(this.playerRef.get());
            }
        }
        
        public void run() {
            final Player player = this.playerRef.get();
            if (player != null) {
                if (this.iterations-- <= 0) {
                    this.finish(player);
                }
                else {
                    this.refresh(player);
                }
            }
            else {
                this.cleanup();
            }
        }
        
        private void refresh(final Player player) {
            this.sendItemSlotChange(player, this.slot, this.makeStack(player));
        }
        
        private void finish(final Player player) {
            this.sendItemSlotChange(player, this.slot, player.getInventory().getItem(this.slot));
            ItemMessage.this.notifyDone(player);
            this.cleanup();
        }
        
        private void cleanup() {
            this.cancel();
            HandlerList.unregisterAll(this);
        }
        
        private ItemStack makeStack(final Player player) {
            final ItemStack stack0 = player.getInventory().getItem(this.slot);
            ItemStack stack2;
            if (stack0 == null || stack0.getType() == Material.AIR) {
                stack2 = new ItemStack(Material.SNOW, 1);
            }
            else {
                stack2 = new ItemStack(stack0.getType(), stack0.getAmount(), stack0.getDurability());
            }
            final ItemMeta meta = Bukkit.getItemFactory().getItemMeta(stack2.getType());
            meta.setDisplayName(String.format(ItemMessage.this.formats[this.iterations % 2], this.message));
            stack2.setItemMeta(meta);
            return stack2;
        }
        
        private void sendItemSlotChange(final Player player, final int slot, final ItemStack stack) {
            final PacketContainer setSlot = new PacketContainer(103);
            setSlot.getIntegers().write(0, 0).write(1, slot + 36);
            setSlot.getItemModifier().write(0, stack);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, setSlot);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    public class MessageRecord implements Comparable<Object>
    {
        private final String message;
        private final int duration;
        private final int priority;
        private final long id;
        
        public MessageRecord(final String message, final int duration, final int priority, final long id) {
            this.message = message;
            this.duration = duration;
            this.priority = priority;
            this.id = id;
        }
        
        public String getMessage() {
            return this.message;
        }
        
        public int getDuration() {
            return this.duration;
        }
        
        public int getPriority() {
            return this.priority;
        }
        
        public long getId() {
            return this.id;
        }
        
        @Override
        public int compareTo(final Object other) {
            final MessageRecord rec = ItemMessage.this.importOtherMessageRecord(other);
            if (rec == null) {
                return 0;
            }
            if (this.priority == rec.getPriority()) {
                return Long.valueOf(this.id).compareTo(Long.valueOf(rec.getId()));
            }
            return Integer.valueOf(this.priority).compareTo(Integer.valueOf(rec.getPriority()));
        }
    }
}
