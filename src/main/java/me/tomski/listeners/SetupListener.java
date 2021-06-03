package me.tomski.listeners;

import me.tomski.arenas.ArenaConfig;
import me.tomski.arenas.ArenaManager;
import me.tomski.language.MessageBank;
import me.tomski.prophunt.DisguiseManager;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;
import me.tomski.utils.PropHuntMessaging;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SetupListener implements Listener {
    private final PropHunt plugin;

    public SetupListener(final PropHunt ph) {
        this.plugin = ph;
    }

    @EventHandler
    public void onBlockPlace(PlayerInteractEvent e) {
        if (ArenaManager.setupMap.containsKey(e.getPlayer().getName())) {
            if (e.getItem() == null) {
                return;
            } else if (e.getItem().getType() == Material.ORANGE_WOOL) {
                ArenaManager.currentArena.setHiderSpawn(e.getPlayer().getLocation());
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.HIDER_SPAWN_SET.getMsg());
                e.setCancelled(true);
                ifCompleteFinish(e);
            } else if (e.getItem().getType() == Material.MAGENTA_WOOL) {
                ArenaManager.currentArena.setSeekerSpawn(e.getPlayer().getLocation());
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.SEEKER_SPAWN_SET.getMsg());
                e.setCancelled(true);
                ifCompleteFinish(e);
            } else if (e.getItem().getType() == Material.LIGHT_BLUE_WOOL) {
                ArenaManager.currentArena.setLobbySpawn(e.getPlayer().getLocation());
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.LOBBY_SPAWN_SET.getMsg());
                e.setCancelled(true);
                ifCompleteFinish(e);
            } else if (e.getItem().getType() == Material.YELLOW_WOOL) {
                ArenaManager.currentArena.setSpectatorSpawn(e.getPlayer().getLocation());
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.SPECTATOR_SPAWN_SET.getMsg());
                e.setCancelled(true);
                ifCompleteFinish(e);
            } else if (e.getItem().getType() == Material.LIME_WOOL) {
                ArenaManager.currentArena.setExitSpawn(e.getPlayer().getLocation());
                PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.EXIT_SPAWN_SET.getMsg());
                e.setCancelled(true);
                ifCompleteFinish(e);
            }
        }
    }

    private void ifCompleteFinish(PlayerInteractEvent e) {
        if (plugin.AM.checkComplete()) {
            ArenaManager.currentArena.saveArenaToFile(plugin);
            PropHuntMessaging.sendMessage(e.getPlayer(), MessageBank.ARENA_COMPLETE.getMsg());
            ArenaManager.playableArenas.put(ArenaManager.currentArena.getArenaName(), ArenaManager.currentArena);
            plugin.AM.arenasInRotation.add(ArenaManager.currentArena);
            final ArenaConfig AC = new ArenaConfig(DisguiseManager.blockDisguises, GameManager.hiderCLASS, GameManager.seekerCLASS, true);
            ArenaManager.arenaConfigs.put(ArenaManager.currentArena, AC);
            ArenaManager.currentArena = null;
            ArenaManager.setupMap.remove(e.getPlayer().getName());
        }
    }
}
