package me.tomski.listeners;

import me.tomski.blocks.*;
import org.bukkit.plugin.*;
import me.tomski.arenas.*;
import org.bukkit.event.*;
import me.tomski.language.*;
import java.lang.reflect.*;
import java.io.*;
import me.tomski.utils.*;
import org.bukkit.event.block.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;
import me.tomski.prophunt.*;
import me.tomski.bungee.*;
import java.util.*;
import me.tomski.enums.*;
import org.bukkit.*;

public class PropHuntListener implements Listener
{
    public static List<Player> tempIgnoreUndisguise;
    private GameManager GM;
    private PropHunt PH;
    private List<String> allowedcmds;
    public static Map<Player, Integer> playerOnBlocks;
    
    public PropHuntListener(final PropHunt plugin, final GameManager Gamem) {
        this.GM = null;
        this.PH = null;
        this.allowedcmds = new ArrayList<String>();
        this.PH = plugin;
        this.GM = Gamem;
        this.allowedcmds.add("/ph leave");
        this.allowedcmds.add("/ph status");
        this.allowedcmds.add("/ph balance");
        this.allowedcmds.add("/ph shop");
        this.allowedcmds.add("/ph chooser");
        this.allowedcmds.add("/ph balance");
        this.allowedcmds.add("/prophunt leave");
        this.allowedcmds.add("/prophunt status");
        this.allowedcmds.add("/prophunt balance");
        this.allowedcmds.add("/prophunt shop");
        this.allowedcmds.add("/prophunt chooser");
        this.allowedcmds.add("/prophunt balance");
        this.allowedcmds.add("/prophunt start");
        this.allowedcmds.add("/prophunt stop");
        this.allowedcmds.add("/ph start");
        this.allowedcmds.add("/ph stop");
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        if (GameManager.seekers.contains(e.getPlayer().getName()) && this.cancelItemUse(e)) {
            e.setCancelled(true);
        }
        if (GameManager.hiders.contains(e.getPlayer().getName()) && this.cancelItemUse(e)) {
            e.setCancelled(true);
        }
        if (GameManager.playersWaiting.contains(e.getPlayer().getName()) && this.cancelItemUse(e)) {
            e.setCancelled(true);
        }
        if (GameManager.spectators.contains(e.getPlayer().getName()) && this.cancelItemUse(e)) {
            e.setCancelled(true);
        }
    }
    
    private boolean cancelItemUse(final PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return false;
        }
        switch (e.getClickedBlock().getType()) {
            case ENDER_CHEST: {
                return true;
            }
            case CHEST: {
                return true;
            }
            case STORAGE_MINECART: {
                return true;
            }
            case LOCKED_CHEST: {
                return true;
            }
            case TRAPPED_CHEST: {
                return true;
            }
            case DISPENSER: {
                return true;
            }
            case POWERED_MINECART: {
                return true;
            }
            case ANVIL: {
                return true;
            }
            case BREWING_STAND: {
                return true;
            }
            case HOPPER: {
                return true;
            }
            case HOPPER_MINECART: {
                return true;
            }
            case DROPPER: {
                return true;
            }
            case BEACON: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @EventHandler
    public void playerKickEvent(final PlayerKickEvent e) {
        if (e.getReason().contains("Flying")) {
            if (GameManager.hiders.contains(e.getPlayer().getName())) {
                if (PropHuntListener.playerOnBlocks.containsKey(e.getPlayer())) {
                    e.setCancelled(true);
                    return;
                }
                final int x = e.getPlayer().getLocation().getBlockX();
                final int y = e.getPlayer().getLocation().getBlockY() - 1;
                final int z = e.getPlayer().getLocation().getBlockZ();
                for (final SolidBlock s : SolidBlockTracker.solidBlocks.values()) {
                    if (s.loc.getBlockX() < x + 2 && s.loc.getBlockX() > x - 2 && s.loc.getBlockY() < y + 2 && s.loc.getBlockY() > y - 2 && s.loc.getBlockZ() < z + 2 && s.loc.getBlockZ() > z - 2) {
                        if (!PropHuntListener.playerOnBlocks.containsKey(e.getPlayer())) {
                            PropHuntListener.playerOnBlocks.put(e.getPlayer(), 20);
                        }
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            if (GameManager.seekers.contains(e.getPlayer().getName())) {
                if (PropHuntListener.playerOnBlocks.containsKey(e.getPlayer())) {
                    e.setCancelled(true);
                    return;
                }
                final int x = e.getPlayer().getLocation().getBlockX();
                final int y = e.getPlayer().getLocation().getBlockY() - 1;
                final int z = e.getPlayer().getLocation().getBlockZ();
                for (final SolidBlock s : SolidBlockTracker.solidBlocks.values()) {
                    if (s.loc.getBlockX() < x + 2 && s.loc.getBlockX() > x - 2 && s.loc.getBlockY() < y + 2 && s.loc.getBlockY() > y - 2 && s.loc.getBlockZ() < z + 2 && s.loc.getBlockZ() > z - 2) {
                        if (!PropHuntListener.playerOnBlocks.containsKey(e.getPlayer())) {
                            PropHuntListener.playerOnBlocks.put(e.getPlayer(), 20);
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onCrouchEvent(final PlayerToggleSneakEvent e) {
        if (GameManager.crouchBlockLock && GameManager.hiders.contains(e.getPlayer().getName())) {
            if (SolidBlockTracker.solidBlocks.containsKey(e.getPlayer().getName())) {
                return;
            }
            if (this.PH.dm.isDisguised(e.getPlayer())) {
                this.PH.dm.toggleBlockLock(e);
            }
        }
    }
    
    @EventHandler
    public void onPLayerDrop(final PlayerDropItemEvent e) {
        if (GameManager.hiders.contains(e.getPlayer().getName()) || GameManager.seekers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.NO_ITEM_SHARING.getMsg());
        }
    }
    
    @EventHandler
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission("prophunt.admin.commandoverride")) {
            return;
        }
        if (GameManager.playersWaiting.contains(e.getPlayer().getName()) && !this.allowedcmds.contains(e.getMessage().toLowerCase())) {
            PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.NO_GAME_COMMANDS.getMsg());
            e.setCancelled(true);
        }
        if (GameManager.hiders.contains(e.getPlayer().getName()) && !this.allowedcmds.contains(e.getMessage().toLowerCase())) {
            PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.NO_GAME_COMMANDS.getMsg());
            e.setCancelled(true);
        }
        if (GameManager.seekers.contains(e.getPlayer().getName()) && !this.allowedcmds.contains(e.getMessage().toLowerCase())) {
            PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.NO_GAME_COMMANDS.getMsg());
            e.setCancelled(true);
        }
    }
    
    private void refreshDisguises() {
        this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
            @Override
            public void run() {
                for (final Player p : PropHuntListener.this.PH.getServer().getOnlinePlayers()) {
                    if (p.isOnline() && PropHuntListener.this.PH.dm.isDisguised(p) && GameManager.seekers.contains(p.getName())) {
                        PropHuntListener.this.PH.dm.undisguisePlayer(p);
                    }
                }
            }
        }, 20L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        if (GameManager.playersQuit.contains(e.getPlayer().getName())) {
            e.setRespawnLocation(GameManager.currentGameArena.getExitSpawn());
            this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
                @Override
                public void run() {
                    PlayerManagement.gameRestorePlayer(e.getPlayer());
                    if (PropHunt.usingTABAPI) {
                        GameManager.SB.removeTab(e.getPlayer());
                    }
                    if (GameManager.useSideStats) {
                        PropHuntListener.this.PH.SBS.removeScoreboard(PropHuntListener.this.PH, e.getPlayer());
                    }
                }
            }, 20L);
            GameManager.playersQuit.remove(e.getPlayer().getName());
            this.refreshDisguises();
            if (this.PH.dm.isDisguised(e.getPlayer())) {
                this.PH.dm.undisguisePlayer(e.getPlayer());
                return;
            }
        }
        if (GameManager.spectators.contains(e.getPlayer().getName())) {
            this.PH.SBS.addPlayerToGame(this.PH, e.getPlayer());
            e.setRespawnLocation(GameManager.currentGameArena.getSpectatorSpawn());
            if (this.PH.dm.isDisguised(e.getPlayer())) {
                this.PH.dm.undisguisePlayer(e.getPlayer());
            }
            this.refreshDisguises();
            return;
        }
        if (GameManager.seekers.contains(e.getPlayer().getName())) {
            e.setRespawnLocation(GameManager.currentGameArena.getSeekerSpawn());
            if (GameManager.seekerDelayTime != 0 && GameManager.sd.isDelaying) {
                GameManager.sd.addPlayer(e.getPlayer());
            }
            this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
                @Override
                public void run() {
                    PropHuntListener.this.PH.showPlayer(e.getPlayer(), false);
                    if (PropHuntListener.this.PH.dm.isDisguised(e.getPlayer())) {
                        PropHuntListener.this.PH.dm.undisguisePlayer(e.getPlayer());
                    }
                    ArenaManager.arenaConfigs.get(GameManager.currentGameArena).getArenaSeekerClass().givePlayer(e.getPlayer());
                    PropHuntListener.this.PH.SBS.addPlayerToGame(PropHuntListener.this.PH, e.getPlayer());
                }
            }, 20L);
            this.refreshDisguises();
            return;
        }
        if (GameManager.hiders.contains(e.getPlayer().getName())) {
            this.PH.SBS.addPlayerToGame(this.PH, e.getPlayer());
            e.setRespawnLocation(GameManager.currentGameArena.getSeekerSpawn());
            ArenaManager.arenaConfigs.get(GameManager.currentGameArena).getArenaHiderClass().givePlayer(e.getPlayer());
            this.refreshDisguises();
            return;
        }
        if (this.PH.dm.isDisguised(e.getPlayer())) {
            this.PH.dm.undisguisePlayer(e.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) throws IllegalAccessException, InvocationTargetException, IOException {
        if (!GameManager.hiders.contains(e.getEntity().getName())) {
            if (GameManager.seekers.contains(e.getEntity().getName())) {
                if (e.getEntity().getKiller() != null && ShopSettings.enabled) {
                    this.giveSeekerKillWinnings(e.getEntity().getKiller());
                }
                e.getDrops().clear();
                if (this.isLastSeeker()) {
                    if (GameManager.useSideStats) {
                        this.PH.SBS.removeScoreboard(this.PH, e.getEntity());
                    }
                    if (GameManager.chooseNewSeeker && GameManager.firstSeeker.equalsIgnoreCase(e.getEntity().getName())) {
                        GameManager.playersQuit.add(e.getEntity().getName());
                        GameManager.seekers.remove(e.getEntity().getName());
                        this.respawnQuick(e.getEntity());
                        if (this.GM.chooseNewSeekerMeth()) {
                            return;
                        }
                        this.GM.endGame(Reason.HIDERSWON, false);
                    }
                    else {
                        if (this.noLivesLeft(e.getEntity())) {
                            GameManager.playersQuit.add(e.getEntity().getName());
                            GameManager.seekers.remove(e.getEntity().getName());
                            this.respawnQuick(e.getEntity());
                            this.GM.endGame(Reason.HIDERSWON, false);
                            return;
                        }
                        String msg = MessageBank.SEEKER_LIVES_MESSAGE.getMsg();
                        msg = LanguageManager.regex(msg, "\\{seeker\\}", e.getEntity().getName());
                        msg = LanguageManager.regex(msg, "\\{lives\\}", GameManager.seekerLives.get(e.getEntity()).toString());
                        PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, msg);
                        this.respawnQuick(e.getEntity());
                    }
                }
                else if (this.noLivesLeft(e.getEntity())) {
                    if (GameManager.useSideStats) {
                        this.PH.SBS.removeScoreboard(this.PH, e.getEntity());
                    }
                    PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, MessageBank.SEEKER_DEATH_MESSAGE.getMsg());
                    GameManager.spectators.add(e.getEntity().getName());
                    GameManager.seekers.remove(e.getEntity().getName());
                    this.respawnQuick(e.getEntity());
                }
                else {
                    String msg = MessageBank.SEEKER_LIVES_MESSAGE.getMsg();
                    msg = LanguageManager.regex(msg, "\\{seeker\\}", e.getEntity().getName());
                    msg = LanguageManager.regex(msg, "\\{lives\\}", GameManager.seekerLives.get(e.getEntity()).toString());
                    PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, msg);
                    this.respawnQuick(e.getEntity());
                }
            }
            return;
        }
        if (e.getEntity().getKiller() != null && ShopSettings.enabled) {
            this.giveHiderKillWinnings(e.getEntity().getKiller());
        }
        if (ShopSettings.enabled) {
            this.giveHiderBonusTimeWinnings(e.getEntity());
        }
        e.getDrops().clear();
        if (this.isLastHider()) {
            if (GameManager.useSideStats) {
                this.PH.SBS.removeScoreboard(this.PH, e.getEntity());
            }
            GameManager.playersQuit.add(e.getEntity().getName());
            GameManager.hiders.remove(e.getEntity().getName());
            this.respawnQuick(e.getEntity());
            this.GM.endGame(Reason.SEEKERWON, false);
            return;
        }
        GameManager.hiders.remove(e.getEntity().getName());
        GameManager.seekers.add(e.getEntity().getName());
        GameManager.seekerLives.put(e.getEntity(), GameManager.seekerLivesAmount);
        if (SolidBlockTracker.solidBlocks.containsKey(e.getEntity().getName())) {
            SolidBlockTracker.solidBlocks.get(e.getEntity().getName()).unSetBlock(this.PH);
        }
        this.respawnQuick(e.getEntity());
        PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, e.getEntity().getName() + MessageBank.HIDER_DEATH_MESSAGE.getMsg());
        GameManager.GT.timeleft += GameManager.time_reward;
        if (GameManager.time_reward != 0) {
            PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, MessageBank.TIME_INCREASE_MESSAGE.getMsg() + GameManager.time_reward);
        }
    }
    
    private void giveCredits(final Player p, final double amount) {
        if (amount <= 0.0 || !ShopSettings.enabled) {
            return;
        }
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                int credits = this.PH.SQL.getCredits(p.getName());
                credits += (int)amount;
                this.PH.SQL.setCredits(p.getName(), credits);
                break;
            }
            case VAULT: {
                this.PH.vaultUtils.economy.depositPlayer(p.getName(), amount);
                break;
            }
        }
        final ItemMessage im = new ItemMessage(this.PH);
        String message = MessageBank.CREDITS_EARN_POPUP.getMsg();
        message = message.replace("\\{credits\\}", amount + " " + ShopSettings.currencyName);
        im.sendMessage(p, ChatColor.translateAlternateColorCodes('&', message));
    }
    
    private void giveHiderKillWinnings(final Player p) {
        if (p.hasPermission("prophunt.currency.vip")) {
            this.giveCredits(p, ShopSettings.vipBonus * ShopSettings.pricePerHiderKill);
        }
        else {
            this.giveCredits(p, ShopSettings.pricePerHiderKill);
        }
    }
    
    private void giveHiderBonusTimeWinnings(final Player p) {
        double bonusTime = (System.currentTimeMillis() - this.GM.gameStartTime) / 1000L;
        bonusTime *= ShopSettings.pricePerSecondsHidden;
        this.giveCredits(p, bonusTime);
    }
    
    private void giveSeekerKillWinnings(final Player p) {
        if (p.hasPermission("prophunt.currency.vip")) {
            this.giveCredits(p, ShopSettings.vipBonus * ShopSettings.pricePerSeekerKill);
        }
        else {
            this.giveCredits(p, ShopSettings.pricePerSeekerKill);
        }
    }
    
    private boolean noLivesLeft(final Player p) {
        if (GameManager.seekerLives.get(p) <= 1) {
            return true;
        }
        GameManager.seekerLives.put(p, GameManager.seekerLives.get(p) - 1);
        return false;
    }
    
    @EventHandler
    public void blockBreakEvent(final BlockBreakEvent e) {
        if (GameManager.spectators.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            return;
        }
        if (GameManager.seekers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            return;
        }
        if (GameManager.hiders.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            return;
        }
        if (GameManager.playersWaiting.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void blockPlaceEvent(final BlockPlaceEvent e) {
        if (GameManager.spectators.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            return;
        }
        if (GameManager.seekers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            return;
        }
        if (GameManager.hiders.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            return;
        }
        if (GameManager.playersWaiting.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }
    
    private boolean isLastHider() {
        return GameManager.hiders.size() == 1;
    }
    
    private boolean isLastSeeker() {
        return GameManager.seekers.size() == 1;
    }
    
    private void respawnQuick(final Player player) {
        this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
            @Override
            public void run() {
                final PacketContainer packet = new PacketContainer(PacketType.Play.Client.CLIENT_COMMAND);
                packet.getClientCommands().write(0, EnumWrappers.ClientCommand.PERFORM_RESPAWN);
                try {
                    ProtocolLibrary.getProtocolManager().recieveClientPacket(player, packet);
                }
                catch (Exception e) {
                    throw new RuntimeException("Cannot recieve packet.", e);
                }
            }
        }, 5L);
    }
    
    private void playHitMarkerEffect(final Location loc) {
        if (GameManager.usingHitmarkers) {
            loc.setY(loc.getY() + 1.0);
            loc.getWorld().playEffect(loc, Effect.POTION_BREAK, 19);
        }
    }
    
    private void playerHitSoundEffect(final Location loc) {
        if (GameManager.usingHitsounds) {
            loc.getWorld().playSound(loc, Sound.ORB_PICKUP, 1.0f, 1.0f);
        }
    }
    
    @EventHandler
    public void playerDamange(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            final Player defend = (Player)e.getEntity();
            final Player attacker = (Player)e.getDamager();
            if (GameManager.hiders.contains(defend.getName()) && GameManager.hiders.contains(attacker.getName())) {
                e.setCancelled(true);
                return;
            }
            if (GameManager.seekers.contains(defend.getName()) && GameManager.seekers.contains(attacker.getName())) {
                e.setCancelled(true);
                return;
            }
            if (GameManager.spectators.contains(attacker.getName())) {
                e.setCancelled(true);
                return;
            }
            if (GameManager.playersWaiting.contains(attacker.getName())) {
                e.setCancelled(true);
                return;
            }
            if (GameManager.hiders.contains(defend.getName())) {
                this.playHitMarkerEffect(e.getEntity().getLocation());
                this.playerHitSoundEffect(e.getEntity().getLocation());
            }
        }
        if (e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player) {
            final Player attacker2 = (Player)((Projectile)e.getDamager()).getShooter();
            if (e.getEntity() instanceof Player) {
                final Player defend2 = (Player)e.getEntity();
                if (GameManager.hiders.contains(defend2.getName()) && GameManager.hiders.contains(attacker2.getName())) {
                    e.setCancelled(true);
                    return;
                }
                if (GameManager.seekers.contains(defend2.getName()) && GameManager.seekers.contains(attacker2.getName())) {
                    e.setCancelled(true);
                    return;
                }
                if (GameManager.spectators.contains(attacker2.getName())) {
                    e.setCancelled(true);
                    return;
                }
                if (GameManager.playersWaiting.contains(attacker2.getName())) {
                    e.setCancelled(true);
                    return;
                }
                if (GameManager.hiders.contains(defend2.getName())) {
                    this.playHitMarkerEffect(e.getEntity().getLocation());
                    this.playerHitSoundEffect(e.getEntity().getLocation());
                }
            }
        }
    }
    
    @EventHandler
    public void onlogin(final PlayerJoinEvent e) {
        if (GameManager.dedicated) {
            this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
                @Override
                public void run() {
                    PropHuntListener.this.GM.addPlayerToGame(e.getPlayer().getName());
                }
            }, 10L);
        }
        if (GameManager.playersQuit.contains(e.getPlayer().getName())) {
            this.GM.teleportToExit(e.getPlayer(), false);
            PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.QUIT_GAME_MESSAGE.getMsg());
            this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
                @Override
                public void run() {
                    PlayerManagement.gameRestorePlayer(e.getPlayer());
                }
            }, 20L);
            GameManager.playersQuit.remove(e.getPlayer().getName());
        }
    }
    
    @EventHandler
    public void onLogout(final PlayerQuitEvent e) throws IOException {
        if (GameManager.useSideStats) {
            this.PH.SBS.removeScoreboard(this.PH, e.getPlayer());
        }
        if (BungeeSettings.usingBungee && this.PH.getServer().getOnlinePlayers().length == 1) {
            final Pinger p = new Pinger(this.PH);
            p.sentData = true;
            p.sendServerDataEmpty();
            this.PH.getServer().getScheduler().scheduleSyncDelayedTask(this.PH, new Runnable() {
                @Override
                public void run() {
                    p.sentData = false;
                }
            }, 20L);
        }
        if (GameManager.dedicated && GameManager.playersWaiting.contains(e.getPlayer().getName())) {
            GameManager.playersWaiting.remove(e.getPlayer().getName());
        }
        if (GameManager.playersWaiting.contains(e.getPlayer().getName())) {
            GameManager.playersWaiting.remove(e.getPlayer().getName());
            GameManager.playersQuit.add(e.getPlayer().getName());
        }
        if (GameManager.hiders.contains(e.getPlayer().getName())) {
            this.GM.kickPlayer(e.getPlayer().getName(), true);
            GameManager.playersQuit.add(e.getPlayer().getName());
        }
        if (GameManager.seekers.contains(e.getPlayer().getName())) {
            this.GM.kickPlayer(e.getPlayer().getName(), true);
            GameManager.playersQuit.add(e.getPlayer().getName());
        }
    }
    
    static {
        PropHuntListener.tempIgnoreUndisguise = new ArrayList<Player>();
        PropHuntListener.playerOnBlocks = new HashMap<Player, Integer>();
    }
}
