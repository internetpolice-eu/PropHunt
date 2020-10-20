package me.tomski.prophunt;

import org.bukkit.entity.*;
import me.tomski.classes.*;
import java.util.logging.*;
import me.tomski.language.*;
import org.bukkit.plugin.*;
import me.tomski.arenas.*;
import me.tomski.objects.*;
import org.bukkit.scheduler.*;
import me.tomski.utils.*;
import me.tomski.blocks.*;
import java.lang.reflect.*;
import me.tomski.bungee.*;
import java.io.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.*;
import org.bukkit.*;
import java.util.*;
import me.tomski.enums.*;

public class GameManager
{
    public static boolean gameStatus;
    public static boolean isHosting;
    public static boolean canHost;
    public static int playersToStartGame;
    public static String firstSeeker;
    public static List<String> playersWaiting;
    public static List<String> hiders;
    public static List<String> seekers;
    public static List<String> spectators;
    public static List<String> playerstoundisguise;
    public static List<String> playersQuit;
    public static Map<Player, Integer> seekerLives;
    public static int seekerLivesAmount;
    public static HiderClass hiderCLASS;
    public static SeekerClass seekerCLASS;
    private static int SCOREBOARDTASKID;
    public static int interval;
    public static int starting_time;
    public static double seeker_damage;
    public static int timeleft;
    public static int time_reward;
    public static int TIMERID;
    public static boolean chooseNewSeeker;
    public static boolean randomArenas;
    private int TRACKERID;
    public static GameTimer GT;
    public static boolean automatic;
    public static boolean dedicated;
    private PropHunt plugin;
    private LobbyThread LT;
    private int DETRACKERID;
    public static SeekerDelay sd;
    public static PHScoreboard SB;
    public static Arena currentGameArena;
    public static boolean blowDisguises;
    public static boolean crouchBlockLock;
    public static boolean usingSolidBlock;
    public static int solidBlockTime;
    public static int seekerDelayTime;
    public static boolean usingHitmarkers;
    public static boolean usingHitsounds;
    public static boolean blindSeeker;
    public static boolean autoRespawn;
    public static boolean useSideStats;
    public static int lobbyTime;
    public static int currentLobbyTime;
    public long gameStartTime;
    
    public GameManager(final PropHunt plugin) {
        (this.plugin = plugin).setupClasses();
    }
    
    public void hostGame(final Player host, final Arena arena) {
        if (GameManager.automatic) {
            if (!this.checkReady(arena)) {
                this.plugin.getLogger().log(Level.WARNING, "Cant Host Arena not setup");
                return;
            }
            if (host != null) {
                PropHuntMessaging.sendMessage(host, MessageBank.HOSTING_AUTO_CANT_HOST.getMsg());
                return;
            }
            GameManager.isHosting = true;
            GameManager.currentGameArena = arena;
            if (GameManager.dedicated) {
                String msg = MessageBank.HOST_AUTO_BROADCAST_DEDI.getMsg();
                msg = LanguageManager.regex(msg, "\\{arena\\}", arena.getArenaName());
                PropHuntMessaging.broadcastMessage(msg);
            }
            else {
                String msg = MessageBank.HOST_AUTO_BROADCAST.getMsg();
                msg = LanguageManager.regex(msg, "\\{arena\\}", arena.getArenaName());
                PropHuntMessaging.broadcastMessage(msg);
            }
        }
        else {
            if (GameManager.gameStatus) {
                PropHuntMessaging.sendMessage(host, MessageBank.GAME_ALREADY_HOSTED.getMsg());
                return;
            }
            if (!this.checkReady(arena)) {
                PropHuntMessaging.sendMessage(host, MessageBank.ARENA_NOT_READY.getMsg());
                return;
            }
            if (GameManager.isHosting) {
                PropHuntMessaging.sendMessage(host, MessageBank.GAME_ALREADY_HOSTED.getMsg());
                return;
            }
            if (!GameManager.canHost) {
                PropHuntMessaging.sendMessage(host, MessageBank.GAME_CANT_HOST.getMsg());
                return;
            }
            GameManager.isHosting = true;
            PropHuntMessaging.sendMessage(host, MessageBank.GAME_HOST.getMsg());
            String msg = MessageBank.BROADCAST_HOST.getMsg();
            msg = LanguageManager.regex(msg, "\\{arena\\}", arena.getArenaName());
            msg = LanguageManager.regex(msg, "\\{host\\}", host.getName());
            PropHuntMessaging.broadcastMessage(msg);
            GameManager.currentGameArena = arena;
        }
    }
    
    public boolean checkReady(final Arena a) {
        return a != null && (a.getExitSpawn() != null && a.getHiderSpawn() != null && a.getLobbySpawn() != null && a.getSeekerSpawn() != null && a.getSpectatorSpawn() != null);
    }
    
    public void startGame(final Player p) {
        if (GameManager.playersWaiting.size() < GameManager.playersToStartGame) {
            if (p != null) {
                String msg = MessageBank.NOT_ENOUGH_PLAYERS.getMsg();
                msg = LanguageManager.regex(msg, "\\{playeramount\\}", String.valueOf(GameManager.playersToStartGame));
                PropHuntMessaging.sendMessage(p, msg);
            }
            else if (GameManager.automatic) {
                this.hostGame(null, this.plugin.AM.getNextInRotation());
                if (GameManager.dedicated) {
                    for (final Player pe : this.plugin.getServer().getOnlinePlayers()) {
                        this.addPlayerToGame(pe.getName());
                    }
                }
            }
            return;
        }
        GameManager.GT = new GameTimer(this, this.plugin, GameManager.seeker_damage, GameManager.interval, GameManager.starting_time, this.plugin.SBS);
        GameManager.TIMERID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)GameManager.GT, 20L, 20L);
        GameManager.GT.ID = GameManager.TIMERID;
        GameManager.timeleft = GameManager.starting_time;
        if (GameManager.usingSolidBlock) {
            final SolidBlockTracker SBT = new SolidBlockTracker(this.plugin);
            this.TRACKERID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)SBT, 0L, 20L);
            final DeSolidifyThread DST = new DeSolidifyThread(this.plugin);
            this.DETRACKERID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)DST, 0L, 2L);
        }
        this.freshPlayers();
        this.chooseSeekerAndSortPlayers();
        this.teleportPlayersStart();
        this.teleportSeekerStart(this.plugin.getServer().getPlayerExact(GameManager.firstSeeker));
        GameManager.seekerLives.put(this.plugin.getServer().getPlayer(GameManager.firstSeeker), GameManager.seekerLivesAmount);
        if (GameManager.seekerDelayTime != 0) {
            GameManager.sd = new SeekerDelay(this.plugin.getServer().getPlayer(GameManager.firstSeeker), GameManager.seekerDelayTime, this.plugin);
            final int delayID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)GameManager.sd, 0L, 20L);
            GameManager.sd.setID(delayID);
        }
        else {
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
                @Override
                public void run() {
                    GameManager.this.plugin.SBS.addPlayerToGame(GameManager.this.plugin, GameManager.this.plugin.getServer().getPlayerExact(GameManager.firstSeeker));
                }
            }, 1L);
        }
        this.givePlayersLoadOuts(GameManager.currentGameArena);
        this.disguisePlayers(GameManager.currentGameArena);
        GameManager.gameStatus = true;
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                for (final String hider : GameManager.hiders) {
                    GameManager.this.plugin.SBS.addPlayerToGame(GameManager.this.plugin, GameManager.this.plugin.getServer().getPlayer(hider));
                }
            }
        }, 20L);
        if (PropHunt.usingTABAPI) {
            this.setupScoreBoard();
        }
        this.gameStartTime = System.currentTimeMillis();
        DisguiseManager.preChosenDisguise.clear();
    }
    
    private void setupScoreBoard() {
        System.out.print("Setting UP Scoreboard");
        GameManager.SB = new PHScoreboard(this.plugin);
        for (final String name : GameManager.seekers) {
            if (this.plugin.getServer().getPlayer(name) != null) {
                GameManager.SB.updateTab(this.plugin.getServer().getPlayer(name));
            }
        }
        for (final String name : GameManager.spectators) {
            if (this.plugin.getServer().getPlayer(name) != null) {
                GameManager.SB.updateTab(this.plugin.getServer().getPlayer(name));
            }
        }
        for (final String name : GameManager.hiders) {
            if (this.plugin.getServer().getPlayer(name) != null) {
                GameManager.SB.updateTab(this.plugin.getServer().getPlayer(name));
            }
        }
        GameManager.SCOREBOARDTASKID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                for (final String name : GameManager.seekers) {
                    if (GameManager.this.plugin.getServer().getPlayer(name) != null) {
                        GameManager.SB.updateTab(GameManager.this.plugin.getServer().getPlayer(name));
                    }
                }
                for (final String name : GameManager.spectators) {
                    if (GameManager.this.plugin.getServer().getPlayer(name) != null) {
                        GameManager.SB.updateTab(GameManager.this.plugin.getServer().getPlayer(name));
                    }
                }
                for (final String name : GameManager.hiders) {
                    if (GameManager.this.plugin.getServer().getPlayer(name) != null) {
                        GameManager.SB.updateTab(GameManager.this.plugin.getServer().getPlayer(name));
                    }
                }
            }
        }, 100L, 100L);
    }
    
    private void givePlayersLoadOuts(final Arena a) {
        for (final String seek : GameManager.seekers) {
            if (this.plugin.getServer().getPlayer(seek) != null) {
                final Player p = this.plugin.getServer().getPlayer(seek);
                ArenaManager.arenaConfigs.get(a).getArenaSeekerClass().givePlayer(p);
                if (!DisguiseManager.loadouts.containsKey(p)) {
                    continue;
                }
                DisguiseManager.loadouts.get(p).giveLoadout();
                DisguiseManager.loadouts.remove(p);
            }
        }
        for (final String hider : GameManager.hiders) {
            if (this.plugin.getServer().getPlayer(hider) != null) {
                final Player p = this.plugin.getServer().getPlayer(hider);
                ArenaManager.arenaConfigs.get(a).getArenaHiderClass().givePlayer(p);
                if (!DisguiseManager.loadouts.containsKey(p)) {
                    continue;
                }
                DisguiseManager.loadouts.get(p).giveLoadout();
                DisguiseManager.loadouts.remove(p);
            }
        }
    }
    
    private void freshPlayers() {
        for (final String s : GameManager.playersWaiting) {
            if (this.plugin.getServer().getPlayer(s) != null) {
                if (this.plugin.getServer().getPlayer(s).getGameMode().equals((Object)GameMode.CREATIVE)) {
                    this.plugin.getServer().getPlayer(s).setGameMode(GameMode.SURVIVAL);
                }
                PlayerManagement.gameStartPlayer(this.plugin.getServer().getPlayer(s));
            }
        }
    }
    
    private void disguisePlayers(final Arena a) {
        for (final String s : GameManager.hiders) {
            if (!GameManager.seekers.contains(s)) {
                if (GameManager.firstSeeker.equals(s)) {
                    continue;
                }
                if (this.plugin.getServer().getPlayerExact(s) == null) {
                    continue;
                }
                final Player player = this.plugin.getServer().getPlayerExact(s);
                if (DisguiseManager.preChosenDisguise.containsKey(player)) {
                    this.plugin.dm.disguisePlayer(player, DisguiseManager.preChosenDisguise.get(player));
                }
                else {
                    this.plugin.dm.randomDisguise(player, ArenaManager.arenaConfigs.get(a));
                }
            }
        }
    }
    
    private void chooseSeekerAndSortPlayers() {
        final int playersize = GameManager.playersWaiting.size();
        GameManager.hiders.clear();
        GameManager.seekers.clear();
        final Random rnd = new Random();
        final int randomnum = rnd.nextInt(playersize);
        final String seeker = GameManager.firstSeeker = GameManager.playersWaiting.get(randomnum);
        GameManager.seekers.add(seeker);
        GameManager.playersWaiting.remove(seeker);
        for (final String hider : GameManager.playersWaiting) {
            if (hider.equals(GameManager.firstSeeker)) {
                continue;
            }
            GameManager.hiders.add(hider);
        }
        GameManager.playersWaiting.clear();
        String msg = MessageBank.BROADCAST_FIRST_SEEKER.getMsg();
        msg = LanguageManager.regex(msg, "\\{seeker\\}", seeker);
        PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, msg);
    }
    
    private void giveCredits(final Player p, final double amount) {
        if (amount <= 0.0) {
            return;
        }
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                int credits = this.plugin.SQL.getCredits(p.getName());
                credits += (int)amount;
                this.plugin.SQL.setCredits(p.getName(), credits);
                break;
            }
            case VAULT: {
                this.plugin.vaultUtils.economy.depositPlayer(p.getName(), amount);
                break;
            }
        }
        final ItemMessage im = new ItemMessage((Plugin)this.plugin);
        String message = MessageBank.CREDITS_EARN_POPUP.getMsg();
        message = message.replaceAll("credits", amount + " " + ShopSettings.currencyName);
        im.sendMessage(p, ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public void endGame(final Reason reason, final boolean shutdown) throws IOException {
        final BukkitRunnable endGameTask = new BukkitRunnable() {
            public void run() {
                GameManager.this.plugin.getServer().getScheduler().cancelTask(GameManager.TIMERID);
                final String bcreason = GameManager.this.broadcastEndReason(reason);
                PropHuntMessaging.broadcastMessage(bcreason);
                if (reason.equals(Reason.HIDERSQUIT) || reason.equals(Reason.SEEKERWON)) {
                    if (ShopSettings.enabled) {
                        for (final String seeker : GameManager.seekers) {
                            if (GameManager.this.plugin.getServer().getPlayerExact(seeker) != null) {
                                if (GameManager.this.plugin.getServer().getPlayerExact(seeker).hasPermission("prophunt.currency.vip")) {
                                    GameManager.this.giveCredits(GameManager.this.plugin.getServer().getPlayerExact(seeker), ShopSettings.vipBonus * ShopSettings.priceSeekerWin);
                                }
                                else {
                                    GameManager.this.giveCredits(GameManager.this.plugin.getServer().getPlayerExact(seeker), ShopSettings.priceSeekerWin);
                                }
                            }
                        }
                    }
                }
                else if ((reason.equals(Reason.SEEKERQUIT) || reason.equals(Reason.TIME) || reason.equals(Reason.HIDERSWON) || reason.equals(Reason.SEEKERDIED)) && ShopSettings.enabled) {
                    double timeBonus = (System.currentTimeMillis() - GameManager.this.gameStartTime) / 1000L;
                    timeBonus *= ShopSettings.pricePerSecondsHidden;
                    for (final String hider : GameManager.hiders) {
                        if (GameManager.this.plugin.getServer().getPlayerExact(hider) != null) {
                            if (GameManager.this.plugin.getServer().getPlayerExact(hider).hasPermission("prophunt.currency.vip")) {
                                GameManager.this.giveCredits(GameManager.this.plugin.getServer().getPlayerExact(hider), ShopSettings.vipBonus * (ShopSettings.priceHiderWin + timeBonus));
                            }
                            else {
                                GameManager.this.giveCredits(GameManager.this.plugin.getServer().getPlayerExact(hider), ShopSettings.priceHiderWin + timeBonus);
                            }
                        }
                    }
                }
                for (final String hider2 : GameManager.hiders) {
                    if (GameManager.this.plugin.getServer().getPlayer(hider2) != null) {
                        GameManager.this.plugin.showPlayer(GameManager.this.plugin.getServer().getPlayer(hider2), shutdown);
                        GameManager.this.teleportToExit(GameManager.this.plugin.getServer().getPlayer(hider2), false);
                        PlayerManagement.gameRestorePlayer(GameManager.this.plugin.getServer().getPlayer(hider2));
                        if (PropHunt.usingTABAPI) {
                            GameManager.SB.removeTab(GameManager.this.plugin.getServer().getPlayer(hider2));
                        }
                        if (GameManager.useSideStats) {
                            GameManager.this.plugin.SBS.removeScoreboard(GameManager.this.plugin, GameManager.this.plugin.getServer().getPlayer(hider2));
                        }
                        if (shutdown) {
                            if (GameManager.this.plugin.getServer().getPlayer(hider2) != null && GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayer(hider2))) {
                                GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayer(hider2));
                            }
                        }
                        else {
                            GameManager.this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)GameManager.this.plugin, (Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    if (GameManager.this.plugin.getServer().getPlayer(hider2) != null && GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayer(hider2))) {
                                        GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayer(hider2));
                                    }
                                }
                            }, 20L);
                        }
                    }
                    GameManager.playerstoundisguise.add(hider2);
                }
                for (final String seeker : GameManager.seekers) {
                    if (GameManager.this.plugin.getServer().getPlayerExact(seeker) != null) {
                        GameManager.this.plugin.showPlayer(GameManager.this.plugin.getServer().getPlayerExact(seeker), shutdown);
                        GameManager.this.teleportToExit(GameManager.this.plugin.getServer().getPlayerExact(seeker), false);
                        PlayerManagement.gameRestorePlayer(GameManager.this.plugin.getServer().getPlayerExact(seeker));
                        if (PropHunt.usingTABAPI) {
                            GameManager.SB.removeTab(GameManager.this.plugin.getServer().getPlayerExact(seeker));
                        }
                        if (GameManager.useSideStats) {
                            GameManager.this.plugin.SBS.removeScoreboard(GameManager.this.plugin, GameManager.this.plugin.getServer().getPlayerExact(seeker));
                        }
                        if (shutdown) {
                            if (GameManager.this.plugin.getServer().getPlayerExact(seeker) != null && GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayerExact(seeker))) {
                                GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayerExact(seeker));
                            }
                        }
                        else {
                            GameManager.this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)GameManager.this.plugin, (Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    if (GameManager.this.plugin.getServer().getPlayerExact(seeker) != null && GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayerExact(seeker))) {
                                        GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayerExact(seeker));
                                    }
                                }
                            }, 20L);
                        }
                    }
                    GameManager.playerstoundisguise.add(seeker);
                }
                for (final String spectator : GameManager.spectators) {
                    if (GameManager.this.plugin.getServer().getPlayerExact(spectator) != null) {
                        GameManager.this.teleportToExit(GameManager.this.plugin.getServer().getPlayerExact(spectator), false);
                        PlayerManagement.gameRestorePlayer(GameManager.this.plugin.getServer().getPlayerExact(spectator));
                        if (PropHunt.usingTABAPI) {
                            GameManager.SB.removeTab(GameManager.this.plugin.getServer().getPlayerExact(spectator));
                        }
                        if (GameManager.useSideStats) {
                            GameManager.this.plugin.SBS.removeScoreboard(GameManager.this.plugin, GameManager.this.plugin.getServer().getPlayerExact(spectator));
                        }
                        if (GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayerExact(spectator))) {
                            GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayerExact(spectator));
                        }
                    }
                    GameManager.playerstoundisguise.add(spectator);
                }
                for (final String player : GameManager.playerstoundisguise) {
                    if (GameManager.this.plugin.getServer().getPlayerExact(player) != null && GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayerExact(player))) {
                        GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayerExact(player));
                    }
                }
                for (final String player : GameManager.playersWaiting) {
                    if (GameManager.this.plugin.getServer().getPlayerExact(player) != null) {
                        GameManager.this.teleportToExit(GameManager.this.plugin.getServer().getPlayerExact(player), false);
                    }
                    if (GameManager.useSideStats) {
                        GameManager.this.plugin.SBS.removeScoreboard(GameManager.this.plugin, GameManager.this.plugin.getServer().getPlayerExact(player));
                    }
                }
                if (GameManager.SCOREBOARDTASKID != 0) {
                    GameManager.this.plugin.getServer().getScheduler().cancelTask(GameManager.SCOREBOARDTASKID);
                }
                if (GameManager.usingSolidBlock) {
                    SolidBlockTracker.solidBlocks.clear();
                    SolidBlockTracker.currentLocation.clear();
                    SolidBlockTracker.movementTracker.clear();
                    GameManager.this.plugin.getServer().getScheduler().cancelTask(GameManager.this.TRACKERID);
                    GameManager.this.plugin.getServer().getScheduler().cancelTask(GameManager.this.DETRACKERID);
                }
                GameManager.playerstoundisguise.clear();
                GameManager.playersWaiting.clear();
                GameManager.hiders.clear();
                GameManager.seekers.clear();
                GameManager.spectators.clear();
                GameManager.firstSeeker = null;
                GameManager.gameStatus = false;
                GameManager.isHosting = false;
                GameManager.timeleft = 0;
                PHScoreboard.disguisesBlown = false;
                SideBarStats.playerBoards.clear();
                for (final SolidBlock sb : SolidBlockTracker.solidBlocks.values()) {
                    try {
                        sb.unSetBlock(GameManager.this.plugin);
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                if (BungeeSettings.usingPropHuntSigns && BungeeSettings.kickToHub) {
                    final Pinger ping = new Pinger(GameManager.this.plugin);
                    for (final Player p : GameManager.this.plugin.getServer().getOnlinePlayers()) {
                        if (p.isDead()) {
                            GameManager.this.respawnQuick(p);
                        }
                        try {
                            ping.connectToServer(p, BungeeSettings.hubname);
                        }
                        catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                PropHuntMessaging.broadcastMessage(ChatColor.GREEN + "------------------------");
                if (GameManager.automatic) {
                    if (AutomationSettings.runChecks(GameManager.this.plugin)) {
                        return;
                    }
                    GameManager.this.hostGame(null, GameManager.this.plugin.AM.getNextInRotation());
                    if (GameManager.dedicated) {
                        for (final Player p2 : GameManager.this.plugin.getServer().getOnlinePlayers()) {
                            GameManager.this.addPlayerToGameDedi(p2.getName());
                        }
                    }
                }
            }
        };
        if (shutdown) {
            endGameTask.runTask((Plugin)this.plugin);
        }
        else {
            endGameTask.runTaskLater((Plugin)this.plugin, 20L);
        }
    }
    
    private void respawnQuick(final Player player) {
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                final PacketContainer packet = new PacketContainer(PacketType.Play.Client.CLIENT_COMMAND);
                packet.getClientCommands().write(0, (Object)EnumWrappers.ClientCommand.PERFORM_RESPAWN);
                try {
                    ProtocolLibrary.getProtocolManager().recieveClientPacket(player, packet);
                }
                catch (Exception e) {
                    throw new RuntimeException("Cannot recieve packet.", e);
                }
            }
        }, 5L);
    }
    
    private String broadcastEndReason(final Reason reason) {
        String reasonmsg = "";
        switch (reason) {
            case TIME: {
                reasonmsg = MessageBank.HIDERS_WON_TIME.getMsg();
                break;
            }
            case HOSTENDED: {
                reasonmsg = MessageBank.HOST_ENDED.getMsg();
                break;
            }
            case HIDERSQUIT: {
                reasonmsg = MessageBank.SEEKERS_WON_HIDERS_QUIT.getMsg();
                break;
            }
            case SEEKERDIED: {
                reasonmsg = MessageBank.HIDERS_WON_KILLS.getMsg();
                break;
            }
            case SEEKERQUIT: {
                reasonmsg = MessageBank.HIDERS_WON_SEEKERS_QUIT.getMsg();
                break;
            }
            case SEEKERWON: {
                reasonmsg = MessageBank.SEEKERS_WON.getMsg();
                break;
            }
            case HIDERSWON: {
                reasonmsg = MessageBank.HIDERS_WON.getMsg();
                break;
            }
        }
        return reasonmsg;
    }
    
    public void kickPlayer(final String name, final boolean logOff) throws IOException {
        if (this.plugin.getServer().getPlayer(name) != null) {
            this.teleportToExit(this.plugin.getServer().getPlayer(name), true);
            if (PropHunt.usingTABAPI && GameManager.SB != null) {
                GameManager.SB.removeTab(this.plugin.getServer().getPlayer(name));
            }
            if (GameManager.useSideStats) {
                this.plugin.SBS.removeScoreboard(this.plugin, this.plugin.getServer().getPlayer(name));
            }
            if (logOff) {
                if (this.plugin.dm.isDisguised(this.plugin.getServer().getPlayer(name))) {
                    this.plugin.dm.undisguisePlayer(this.plugin.getServer().getPlayer(name));
                }
                PlayerManagement.gameRestorePlayer(this.plugin.getServer().getPlayer(name));
            }
            else {
                PlayerManagement.gameRestorePlayer(this.plugin.getServer().getPlayer(name));
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
                    @Override
                    public void run() {
                        if (GameManager.this.plugin.getServer().getPlayer(name) != null && GameManager.this.plugin.dm.isDisguised(GameManager.this.plugin.getServer().getPlayer(name))) {
                            GameManager.this.plugin.dm.undisguisePlayer(GameManager.this.plugin.getServer().getPlayer(name));
                        }
                    }
                }, 20L);
            }
        }
        if (GameManager.spectators.contains(name)) {
            GameManager.spectators.remove(name);
        }
        if (GameManager.playersWaiting.contains(name)) {
            GameManager.playersWaiting.remove(name);
        }
        if (GameManager.hiders.contains(name)) {
            GameManager.hiders.remove(name);
        }
        if (GameManager.seekers.contains(name)) {
            GameManager.seekers.remove(name);
        }
        if (GameManager.gameStatus) {
            if (GameManager.seekers.size() == 0) {
                if (GameManager.firstSeeker.equalsIgnoreCase(name)) {
                    if (this.plugin.GM.chooseNewSeekerMeth()) {
                        return;
                    }
                    this.endGame(Reason.SEEKERQUIT, false);
                }
                else {
                    this.endGame(Reason.SEEKERQUIT, false);
                }
                return;
            }
            if (GameManager.hiders.size() == 0) {
                this.endGame(Reason.HIDERSQUIT, false);
                return;
            }
            this.checkEnd();
        }
    }
    
    public boolean chooseNewSeekerMeth() {
        if (GameManager.hiders.size() <= 0) {
            return false;
        }
        final Random rand = new Random();
        final String newSeeker = GameManager.hiders.get(rand.nextInt(GameManager.hiders.size()));
        if (Bukkit.getPlayer(newSeeker) != null) {
            final Player newSeekerPlayer = Bukkit.getPlayer(newSeeker);
            newSeekerPlayer.setHealth(0.0);
            PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, MessageBank.NEW_SEEKER_CHOSEN.getMsg() + newSeeker);
            return true;
        }
        return true;
    }
    
    public void addPlayerToGameDedi(final String name) {
        if (!this.safeToJoin(name)) {
            PropHuntMessaging.sendMessage(this.plugin.getServer().getPlayer(name), "You are not safe to teleport");
            return;
        }
        if (GameManager.gameStatus) {
            this.plugin.SBS.addPlayerToLobby(this.plugin, this.plugin.getServer().getPlayer(name));
        }
        else {
            this.plugin.SBS.addPlayerToLobby(this.plugin, this.plugin.getServer().getPlayer(name));
        }
        if (GameManager.playersWaiting.contains(name)) {
            return;
        }
        GameManager.playersWaiting.add(name);
        if (this.plugin.getServer().getPlayer(name) != null) {
            this.teleportToLobby(this.plugin.getServer().getPlayer(name), false);
        }
        if (GameManager.automatic && !GameManager.gameStatus && GameManager.playersWaiting.size() >= GameManager.playersToStartGame) {
            if (this.LT == null) {
                this.LT = new LobbyThread(this.plugin, GameManager.lobbyTime);
            }
            if (!this.LT.isRunning) {
                if (GameManager.dedicated) {
                    String msg = MessageBank.STARTING_IN_60_DEDI.getMsg();
                    msg = LanguageManager.regex(msg, "\\{time\\}", String.valueOf(GameManager.lobbyTime));
                    PropHuntMessaging.broadcastMessage(msg);
                }
                else {
                    String msg = MessageBank.STARTING_IN_60.getMsg();
                    msg = LanguageManager.regex(msg, "\\{time\\}", String.valueOf(GameManager.lobbyTime));
                    PropHuntMessaging.broadcastMessage(msg);
                }
                this.LT = new LobbyThread(this.plugin, GameManager.lobbyTime);
                this.LT.isRunning = true;
                final int id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)this.LT, 0L, 20L);
                this.LT.setId(id);
            }
        }
    }
    
    private boolean safeToJoin(final String name) {
        final Player p = this.plugin.getServer().getPlayer(name);
        return !p.isInsideVehicle();
    }
    
    public void addPlayerToGame(final String name) {
        if (GameManager.useSideStats) {
            this.plugin.SBS.addPlayerToLobby(this.plugin, this.plugin.getServer().getPlayer(name));
        }
        if (GameManager.playersWaiting.contains(name)) {
            return;
        }
        GameManager.playersWaiting.add(name);
        if (this.plugin.getServer().getPlayer(name) != null) {
            this.teleportToLobby(this.plugin.getServer().getPlayer(name), true);
            PropHuntMessaging.broadcastMessageToPlayers(GameManager.playersWaiting, GameManager.seekers, name + MessageBank.PLAYER_JOIN_LOBBY.getMsg());
        }
        if (GameManager.automatic && !GameManager.gameStatus && GameManager.playersWaiting.size() >= GameManager.playersToStartGame) {
            if (this.LT == null) {
                this.LT = new LobbyThread(this.plugin, GameManager.lobbyTime);
            }
            if (!this.LT.isRunning) {
                if (GameManager.dedicated) {
                    String msg = MessageBank.STARTING_IN_60_DEDI.getMsg();
                    msg = LanguageManager.regex(msg, "\\{time\\}", String.valueOf(GameManager.lobbyTime));
                    PropHuntMessaging.broadcastMessage(msg);
                }
                else {
                    String msg = MessageBank.STARTING_IN_60.getMsg();
                    msg = LanguageManager.regex(msg, "\\{time\\}", String.valueOf(GameManager.lobbyTime));
                    PropHuntMessaging.broadcastMessage(msg);
                }
                this.LT = new LobbyThread(this.plugin, GameManager.lobbyTime);
                this.LT.isRunning = true;
                final int id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)this.LT, 0L, 20L);
                this.LT.setId(id);
            }
        }
    }
    
    public void teleportPlayersStart() {
        GameManager.currentGameArena.getHiderSpawn().getChunk().load();
        for (final String s : GameManager.hiders) {
            if (this.plugin.getServer().getPlayer(s) != null) {
                final Player p = this.plugin.getServer().getPlayer(s);
                p.teleport(GameManager.currentGameArena.getHiderSpawn());
                PropHuntMessaging.sendMessage(p, MessageBank.GAME_START_MESSAGE_HIDERS.getMsg());
            }
        }
    }
    
    public void teleportSeekerStart(final Player p) {
        GameManager.currentGameArena.getSeekerSpawn().getChunk().load();
        p.teleport(GameManager.currentGameArena.getSeekerSpawn());
        PropHuntMessaging.sendMessage(p, MessageBank.GAME_START_MESSAGE_SEEKERS.getMsg());
    }
    
    public void teleportToSpectator(final Player p) {
        p.teleport(GameManager.currentGameArena.getSpectatorSpawn());
        PropHuntMessaging.sendMessage(p, MessageBank.SPECTATING.getMsg());
    }
    
    public void teleportToLobby(final Player p, final boolean message) {
        p.teleport(GameManager.currentGameArena.getLobbySpawn());
        if (message) {
            PropHuntMessaging.sendMessage(p, MessageBank.JOIN_LOBBY_MESSAGE.getMsg());
        }
    }
    
    public void teleportToExit(final Player p, final boolean message) {
        p.teleport(GameManager.currentGameArena.getExitSpawn());
        if (message) {
            PropHuntMessaging.sendMessage(p, MessageBank.QUIT_GAME_MESSAGE.getMsg());
        }
    }
    
    public void checkEnd() throws IOException {
        if (GameManager.seekers.isEmpty()) {
            this.endGame(Reason.HIDERSWON, false);
            return;
        }
        if (GameManager.hiders.isEmpty()) {
            this.endGame(Reason.SEEKERWON, false);
        }
    }
    
    public void spectateGame(final Player p) {
        if (GameManager.gameStatus) {
            this.teleportToSpectator(p);
            GameManager.spectators.add(p.getName());
        }
    }
    
    static {
        GameManager.gameStatus = false;
        GameManager.isHosting = false;
        GameManager.canHost = true;
        GameManager.playersToStartGame = 0;
        GameManager.firstSeeker = null;
        GameManager.playersWaiting = new ArrayList<String>();
        GameManager.hiders = new ArrayList<String>();
        GameManager.seekers = new ArrayList<String>();
        GameManager.spectators = new ArrayList<String>();
        GameManager.playerstoundisguise = new ArrayList<String>();
        GameManager.playersQuit = new ArrayList<String>();
        GameManager.seekerLives = new HashMap<Player, Integer>();
        GameManager.SCOREBOARDTASKID = 0;
        GameManager.automatic = false;
        GameManager.dedicated = false;
        GameManager.currentGameArena = null;
        GameManager.crouchBlockLock = false;
        GameManager.currentLobbyTime = 0;
    }
}
