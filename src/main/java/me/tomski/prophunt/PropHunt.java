package me.tomski.prophunt;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.tomski.arenas.ArenaManager;
import me.tomski.blocks.ProtocolTask;
import me.tomski.bungee.Pinger;
import me.tomski.classes.HiderClass;
import me.tomski.classes.SeekerClass;
import me.tomski.currency.SqlConnect;
import me.tomski.language.LanguageManager;
import me.tomski.language.MessageBank;
import me.tomski.language.ScoreboardTranslate;
import me.tomski.listeners.PropHuntListener;
import me.tomski.listeners.SetupListener;
import me.tomski.objects.SimpleDisguise;
import me.tomski.prophuntstorage.ArenaStorage;
import me.tomski.prophuntstorage.ShopConfig;
import me.tomski.shop.ShopManager;
import me.tomski.utils.LogFilter;
import me.tomski.utils.MetricsLite;
import me.tomski.utils.PingTimer;
import me.tomski.utils.PropHuntMessaging;
import me.tomski.utils.Reason;
import me.tomski.utils.SideBarStats;
import me.tomski.utils.SideTabTimer;
import me.tomski.utils.VaultUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PropHunt extends JavaPlugin implements Listener
{
    public DisguiseManager dm;
    public static ProtocolManager protocolManager;
    public ArenaStorage AS;
    public GameManager GM;
    public ArenaManager AM;
    private LanguageManager LM;
    public ScoreboardTranslate ST;
    public SideBarStats SBS;
    public static boolean usingTABAPI;
    public VaultUtils vaultUtils;
    public SqlConnect SQL;
    public ShopConfig shopConfig;
    private ShopSettings shopSettings;
    private ShopManager shopManager;
    boolean shouldDisable;

    public PropHunt() {
        this.shouldDisable = false;
    }

    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        try {
            this.init();
            if (this.shouldDisable) {
                this.getLogger().warning("Disabling plugin. Reason: DisguiseCraft or LibsDisguises not found, please install then reboot");
                this.getPluginLoader().disablePlugin(this);
                return;
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            final MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        this.getServer().getLogger().setFilter(new LogFilter());
    }

    public void onDisable() {
        if (GameManager.gameStatus) {
            try {
                this.GM.endGame(Reason.HOSTENDED, true);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.AS.saveData();
    }

    public void init() throws IOException {
        this.GM = new GameManager(this);
        this.AS = new ArenaStorage(this, this.GM);
        this.AM = new ArenaManager(this);
        if (this.getServer().getPluginManager().isPluginEnabled("DisguiseCraft")) {
            this.dm = new DisguiseCraftManager(this);
            this.getServer().getPluginManager().registerEvents(this.dm, this);
        }
        else {
            if (!this.getServer().getPluginManager().isPluginEnabled("LibsDisguises")) {
                this.shouldDisable = true;
                return;
            }
            this.dm = new LibsDisguiseManager(this);
        }
        this.shopManager = new ShopManager(this);
        this.loadProtocolManager();
        final ProtocolTask pt = new ProtocolTask(this);
        pt.initProtocol();
        this.getServer().getPluginManager().registerEvents(pt, this);
        this.loadConfigSettings();
        AutomationSettings.initSettings(this);
        this.LM = new LanguageManager(this);
        this.ST = new ScoreboardTranslate(this);
        this.AS.loadData();
        if (GameManager.useSideStats) {
            this.SBS = new SideBarStats(this);
            final SideTabTimer stt = new SideTabTimer(this.SBS);
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, stt, 20L, 20L);
        }
        this.getServer().getPluginManager().registerEvents(new PropHuntListener(this, this.GM), this);
        this.getServer().getPluginManager().registerEvents(new SetupListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ServerManager(this), this);
        this.usingCustomTab();
        if (this.usingPropHuntSigns()) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            final Pinger ping = new Pinger(this);
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PingTimer(ping), 20L, BungeeSettings.pingInterval);
            this.getLogger().info("Ping timer initiated at " + BungeeSettings.pingInterval + " ticks per ping");
        }
        this.loadEconomySettings();
        if (GameManager.automatic) {
            this.checkAUTOReady();
        }
    }

    private void loadEconomySettings() {
        if (!ShopSettings.enabled) {
            this.getLogger().info("Not using shop!");
            return;
        }
        this.shopSettings = new ShopSettings(this);
        this.shopConfig = new ShopConfig(this);
        this.shopSettings.loadShopItems(this);
        if (ShopSettings.usingVault) {
            this.vaultUtils = new VaultUtils(this);
        }
        else {
            this.SQL = new SqlConnect(this);
            this.vaultUtils = new VaultUtils(this, true);
        }
    }

    public void loadProtocolManager() {
        PropHunt.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    private boolean checkAUTOReady() {
        if (this.AM.arenasInRotation == null) {
            this.getLogger().log(Level.WARNING, "Arena Not Setup, automatic hosting disabled");
            return false;
        }
        if (this.AM.arenasInRotation.size() == 0) {
            GameManager.automatic = false;
            this.getLogger().log(Level.WARNING, "No arena setup, automatic hosting disabled");
            return false;
        }
        if (!this.GM.checkReady(this.AM.arenasInRotation.get(0))) {
            GameManager.automatic = false;
            this.getLogger().log(Level.WARNING, "Arena Not Setup, automatic hosting disabled");
            return false;
        }
        this.getLogger().log(Level.INFO, "Arena Setup, automatic hosting starting");
        this.GM.hostGame(null, this.AM.arenasInRotation.get(0));
        return true;
    }

    private boolean usingPropHuntSigns() {
        if (this.getConfig().getBoolean("BungeeSettings.using-bungee")) {
            BungeeSettings.usingBungee = true;
            this.getLogger().log(Level.INFO, "Using PropHunt bungee signs :)");
            BungeeSettings.usingPropHuntSigns = true;
            BungeeSettings.hubname = this.getConfig().getString("BungeeSettings.hub-name");
            BungeeSettings.pingInterval = this.getConfig().getInt("BungeeSettings.ping-interval-ticks");
            BungeeSettings.bungeeName = this.getConfig().getString("BungeeSettings.this-bungee-server-name");
            BungeeSettings.kickToHub = this.getConfig().getBoolean("BungeeSettings.kick-back-to-hub");
            return true;
        }
        return false;
    }

    public ShopManager getShopManager() {
        return this.shopManager;
    }

    public ShopSettings getShopSettings() {
        return this.shopSettings;
    }

    private void usingCustomTab() {
        final Plugin p = this.getServer().getPluginManager().getPlugin("TabAPI");
        if (p == null) {
            PropHunt.usingTABAPI = false;
            return;
        }
        if (p.isEnabled() && this.getConfig().getBoolean("using-custom-tab")) {
            PropHunt.usingTABAPI = true;
            this.getLogger().log(Level.INFO, "Using Custom TAB :)");
            return;
        }
        if (GameManager.blowDisguises) {
            this.getLogger().log(Level.INFO, "Preventing blowing disguises as you are not using TabAPI");
            GameManager.blowDisguises = false;
        }
        else {
            this.getLogger().log(Level.INFO, "Not using Custom TAB");
        }
    }

    private void loadConfigSettings() {
        if (this.getConfig().contains("automatic")) {
            GameManager.automatic = this.getConfig().getBoolean("automatic");
        }
        if (this.getConfig().contains("dedicated")) {
            GameManager.dedicated = this.getConfig().getBoolean("dedicated");
            if (GameManager.dedicated) {
                GameManager.automatic = true;
            }
        }
        if (this.getConfig().contains("random-arenas")) {
            GameManager.randomArenas = this.getConfig().getBoolean("random-arenas");
        }
        if (this.getConfig().contains("players-to-start")) {
            GameManager.playersToStartGame = this.getConfig().getInt("players-to-start");
        }
        if (this.getConfig().contains("starting-time")) {
            GameManager.starting_time = this.getConfig().getInt("starting-time");
        }
        if (this.getConfig().contains("interval")) {
            GameManager.interval = this.getConfig().getInt("interval");
        }
        if (this.getConfig().contains("lobby-time")) {
            GameManager.lobbyTime = this.getConfig().getInt("lobby-time");
        }
        if (this.getConfig().contains("seeker-damage")) {
            GameManager.seeker_damage = this.getConfig().getDouble("seeker-damage");
        }
        if (this.getConfig().contains("time-reward")) {
            GameManager.time_reward = this.getConfig().getInt("time-reward");
        }
        if (this.getConfig().contains("blow-disguises-last-30-seconds")) {
            GameManager.blowDisguises = this.getConfig().getBoolean("blow-disguises-last-30-seconds");
        }
        this.getLogger().log(Level.INFO, "Prop Hunt settings Loaded");
        if (this.getConfig().contains("crouching-block-lock")) {
            GameManager.crouchBlockLock = this.getConfig().getBoolean("crouching-block-lock");
        }
        if (this.getConfig().contains("use-solid-block")) {
            GameManager.usingSolidBlock = this.getConfig().getBoolean("use-solid-block");
            GameManager.solidBlockTime = this.getConfig().getInt("solid-block-time");
        }
        if (this.getConfig().contains("seeker-delay-time")) {
            GameManager.seekerDelayTime = this.getConfig().getInt("seeker-delay-time");
        }
        if (this.getConfig().contains("seeker-lives")) {
            GameManager.seekerLivesAmount = this.getConfig().getInt("seeker-lives");
        }
        if (this.getConfig().contains("using-custom-tab")) {
            PropHunt.usingTABAPI = this.getConfig().getBoolean("using-custom-tab");
        }
        if (this.getConfig().contains("use-hitmarkers")) {
            GameManager.usingHitmarkers = this.getConfig().getBoolean("use-hitmarkers");
        }
        if (this.getConfig().contains("use-hitsounds")) {
            GameManager.usingHitsounds = this.getConfig().getBoolean("use-hitsounds");
        }
        if (this.getConfig().contains("blind-seeker-in-delay")) {
            GameManager.blindSeeker = this.getConfig().getBoolean("blind-seeker-in-delay");
        }
        if (this.getConfig().contains("auto-respawn")) {
            GameManager.autoRespawn = this.getConfig().getBoolean("auto-respawn");
        }
        if (this.getConfig().contains("use-side-scoreboard-stats")) {
            GameManager.useSideStats = this.getConfig().getBoolean("use-side-scoreboard-stats");
        }
        if (this.getConfig().contains("choose-new-seeker-if-original-dies")) {
            GameManager.chooseNewSeeker = this.getConfig().getBoolean("choose-new-seeker-if-original-dies");
        }
        if (this.getConfig().contains("ShopSettings")) {
            ShopSettings.enabled = this.getConfig().getBoolean("ShopSettings.use-shop");
            ShopSettings.usingVault = this.getConfig().getBoolean("ShopSettings.use-vault-for-currency");
            ShopSettings.currencyName = this.getConfig().getString("ShopSettings.currency-name");
            ShopSettings.pricePerHiderKill = this.getConfig().getDouble("ShopSettings.points-per-hider-kill");
            ShopSettings.pricePerSeekerKill = this.getConfig().getDouble("ShopSettings.points-per-seeker-kill");
            ShopSettings.pricePerSecondsHidden = this.getConfig().getDouble("ShopSettings.points-per-second-hidden");
            ShopSettings.priceHiderWin = this.getConfig().getDouble("ShopSettings.points-hiders-win");
            ShopSettings.priceSeekerWin = this.getConfig().getDouble("ShopSettings.points-seekers-win");
            ShopSettings.vipBonus = this.getConfig().getDouble("ShopSettings.vip-bonus");
        }
        if (this.getConfig().contains("ServerSettings")) {
            ServerManager.forceMOTD = this.getConfig().getBoolean("ServerSettings.force-motd-prophunt");
            ServerManager.forceMaxPlayers = this.getConfig().getBoolean("ServerSettings.force-max-players");
            ServerManager.forceMaxPlayersSize = this.getConfig().getInt("ServerSettings.force-max-players-size");
            ServerManager.blockAccessWhilstInGame = this.getConfig().getBoolean("ServerSettings.block-access-whilst-in-game");
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("prophunt")) {
                if (args.length == 0) {
                    if (p.hasPermission("prophunt.hostcommand.host")) {
                        PropHuntMessaging.sendMessage(p, "Use /ph host <ArenaName>");
                        PropHuntMessaging.sendAvailableArenas(p, ArenaManager.playableArenas);
                        return true;
                    }
                    PropHuntMessaging.sendPlayerHelp(p);
                    return true;
                }
                else {
                    if (args.length >= 1) {
                        if (args[0].equalsIgnoreCase("balance")) {
                            if (!p.hasPermission("prophunt.currency.balance")) {
                                PropHuntMessaging.sendMessage(p, "You don't have permission to check your balance");
                                return true;
                            }
                            if (ShopSettings.enabled) {
                                PropHuntMessaging.sendMessage(p, MessageBank.CURRENCY_BALANCE.getMsg() + this.getCurrencyBalance(p));
                                return true;
                            }
                            PropHuntMessaging.sendMessage(p, MessageBank.SHOP_NOT_ENABLED.getMsg());
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("currency")) {
                            if (args.length == 2 && args[1].equalsIgnoreCase("balance")) {
                                if (!p.hasPermission("prophunt.currency.balance")) {
                                    PropHuntMessaging.sendMessage(p, "You don't have permission to check your balance");
                                    return true;
                                }
                                if (ShopSettings.enabled) {
                                    PropHuntMessaging.sendMessage(p, MessageBank.CURRENCY_BALANCE.getMsg() + this.getCurrencyBalance(p));
                                    return true;
                                }
                                PropHuntMessaging.sendMessage(p, MessageBank.SHOP_NOT_ENABLED.getMsg());
                                return true;
                            }
                            else {
                                if (args.length == 4) {
                                    this.handleEconomyCommand(p, args);
                                    return true;
                                }
                                PropHuntMessaging.sendEconomyHelp(p);
                                return true;
                            }
                        }
                    }
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("configreload")) {
                            if (!sender.hasPermission("prophunt.admin.configreload")) {
                                PropHuntMessaging.sendMessage(p, "You do not have permission to reload the config");
                                return true;
                            }
                            this.loadConfigSettings();
                            this.loadBlockDisguises();
                            this.setupClasses();
                            this.AS.loadData();
                            this.LM.initfile();
                            if (GameManager.automatic && !this.checkAUTOReady()) {
                                PropHuntMessaging.sendMessage(p, "Arena not setup, automatic hosting disabled");
                            }
                            PropHuntMessaging.sendMessage(p, "Config reloaded");
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("start")) {
                            if (!sender.hasPermission("prophunt.hostcommand.start")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to start a PropHunt Game");
                                return true;
                            }
                            this.GM.startGame(p);
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("stop")) {
                            if (!sender.hasPermission("prophunt.hostcommand.stop")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to stop a PropHunt Game");
                                return true;
                            }
                            try {
                                this.GM.endGame(Reason.HOSTENDED, false);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("join")) {
                            if (!sender.hasPermission("prophunt.command.join")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to join a PropHunt Game");
                                return true;
                            }
                            if (GameManager.dedicated) {
                                PropHuntMessaging.sendMessage(p, "Disabled in dedicated mode");
                                return true;
                            }
                            if (GameManager.gameStatus) {
                                PropHuntMessaging.sendMessage(p, "Game is in progress");
                                return true;
                            }
                            if (GameManager.isHosting) {
                                this.GM.addPlayerToGame(p.getName());
                            }
                            else {
                                PropHuntMessaging.sendMessage(p, "There is no game being hosted");
                            }
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("leave")) {
                            if (!sender.hasPermission("prophunt.command.leave")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to leave a PropHunt Game");
                                return true;
                            }
                            if (GameManager.dedicated) {
                                PropHuntMessaging.sendMessage(p, "Disabled in dedicated mode");
                                return true;
                            }
                            if (GameManager.gameStatus) {
                                PlayerManagement.gameRestorePlayer(p);
                                try {
                                    this.GM.kickPlayer(p.getName(), false);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if (GameManager.playersWaiting.contains(p.getName())) {
                                GameManager.playersWaiting.remove(p.getName());
                                try {
                                    this.GM.kickPlayer(p.getName(), false);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("status")) {
                            if (!sender.hasPermission("prophunt.command.status")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to check the status of a PropHunt Game");
                                return true;
                            }
                            PropHuntMessaging.sendGameStatus(p);
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("shop")) {
                            if (!sender.hasPermission("prophunt.command.shop")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to use the shop");
                                return true;
                            }
                            this.getShopManager().getMainShop().openMainShop(p);
                            return true;
                        }
                        else {
                            if (args[0].equalsIgnoreCase("chooser")) {
                                if (!sender.hasPermission("prophunt.command.chooser")) {
                                    PropHuntMessaging.sendMessage(p, "You do not have the permission to use this chooser");
                                    return true;
                                }
                                if (GameManager.isHosting && GameManager.playersWaiting.contains(p.getName())) {
                                    this.getShopManager().getBlockChooser().openBlockShop(p);
                                    return true;
                                }
                            }
                            if (args[0].equalsIgnoreCase("loadout")) {
                                if (!sender.hasPermission("prophunt.command.loadout")) {
                                    PropHuntMessaging.sendMessage(p, "You do not have the permission to use this loadout");
                                    return true;
                                }
                                if (GameManager.isHosting && GameManager.playersWaiting.contains(p.getName())) {
                                    this.getShopManager().getLoadoutChooser().openBlockShop(p);
                                    return true;
                                }
                            }
                            if (args[0].equalsIgnoreCase("debug") && sender.isOp()) {
                                sender.sendMessage("Debug for Tomski");
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("spectate")) {
                                if (!sender.hasPermission("prophunt.command.spectate")) {
                                    PropHuntMessaging.sendMessage(p, "You do not have the permission to spectate a PropHunt Game");
                                    return true;
                                }
                                if (GameManager.gameStatus) {
                                    this.GM.spectateGame(p);
                                }
                                else {
                                    PropHuntMessaging.sendMessage(p, "There is no game in progress");
                                }
                                return true;
                            }
                            else {
                                if (p.hasPermission("prophunt.hostcommand.host")) {
                                    PropHuntMessaging.sendHostHelp(p);
                                    return true;
                                }
                                PropHuntMessaging.sendPlayerHelp(p);
                                return true;
                            }
                        }
                    }
                    else if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("setup")) {
                            if (!sender.hasPermission("prophunt.admin.setup")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to setup a PropHunt Arena");
                                return true;
                            }
                            if (!this.AM.hasInvetorySpace(p)) {
                                PropHuntMessaging.sendMessage(p, "Please empty your inventory, you dont have enough space for the setup items!");
                                return true;
                            }
                            this.AM.addSettingUp(p, args[1]);
                            PropHuntMessaging.sendMessage(p, "You are setting up the arena: " + args[1]);
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("host")) {
                            if (!sender.hasPermission("prophunt.hostcommand.host")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to host a PropHunt Game");
                                return true;
                            }
                            if (ArenaManager.playableArenas.containsKey(args[1])) {
                                this.GM.hostGame(p, ArenaManager.playableArenas.get(args[1]));
                            }
                            else {
                                PropHuntMessaging.sendAvailableArenas(p, ArenaManager.playableArenas);
                            }
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("delete")) {
                            if (!sender.hasPermission("prophunt.admin.delete")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to delete a PropHunt Arena");
                                return true;
                            }
                            if (this.AM.deleteArena(args[1])) {
                                PropHuntMessaging.sendMessage(p, "Arena deleted");
                                return true;
                            }
                            PropHuntMessaging.sendMessage(p, "That arena does not exist");
                            PropHuntMessaging.sendAvailableArenas(p, ArenaManager.playableArenas);
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("kick")) {
                            if (!sender.hasPermission("prophunt.hostcommand.kick")) {
                                PropHuntMessaging.sendMessage(p, "You do not have the permission to kick a player from Prophunt");
                                return true;
                            }
                            if (this.getServer().getPlayer(args[1]) != null) {
                                try {
                                    this.GM.kickPlayer(args[1], false);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                                PropHuntMessaging.sendMessage(p, "You have kicked :&f" + args[1] + "&a from the game");
                                return true;
                            }
                        }
                    }
                }
            }
            if (p.hasPermission("prophunt.hostcommand.host")) {
                PropHuntMessaging.sendHostHelp(p);
                return true;
            }
            PropHuntMessaging.sendPlayerHelp(p);
            return true;
        }
        else {
            if (!args[0].equalsIgnoreCase("currency")) {
                return false;
            }
            if (args.length == 2) {
                return true;
            }
            if (args.length == 4) {
                this.handleEconomyCommand(sender, args);
                return true;
            }
            return true;
        }
    }

    private void handleEconomyCommand(final CommandSender p, final String[] args) {
        final String playerName = args[1];
        final String type = args[2];
        final String amount = args[3];
        if (!this.isInt(amount)) {
            p.sendMessage("Please supply an integer");
            return;
        }
        if (!p.hasPermission("prophunt.economy" + type.toLowerCase())) {
            p.sendMessage("You dont have permission for " + type);
            return;
        }
        if (type.equalsIgnoreCase("set")) {
            this.setCurrencyBalance(p, playerName, Integer.parseInt(amount));
        }
        else if (type.equalsIgnoreCase("give")) {
            int currentAmount = this.getCurrencyBalance(p, playerName);
            currentAmount += Math.abs(Integer.parseInt(amount));
            this.setCurrencyBalance(p, playerName, currentAmount);
        }
        else if (type.equalsIgnoreCase("remove")) {
            int currentAmount = this.getCurrencyBalance(p, playerName);
            currentAmount -= Math.abs(Integer.parseInt(amount));
            if (currentAmount <= 0) {
                currentAmount = 0;
            }
            this.setCurrencyBalance(p, playerName, currentAmount);
        }
    }

    private void handleEconomyCommand(final Player p, final String[] args) {
        final String playerName = args[1];
        final String type = args[2];
        final String amount = args[3];
        if (!this.isInt(amount)) {
            PropHuntMessaging.sendMessage(p, "Please supply an integer");
            return;
        }
        if (!p.hasPermission("prophunt.economy" + type.toLowerCase())) {
            PropHuntMessaging.sendMessage(p, "You dont have permission for " + type);
            return;
        }
        if (type.equalsIgnoreCase("set")) {
            this.setCurrencyBalance(p, playerName, Integer.parseInt(amount));
        }
        else if (type.equalsIgnoreCase("give")) {
            int currentAmount = this.getCurrencyBalance(p, playerName);
            currentAmount += Math.abs(Integer.parseInt(amount));
            this.setCurrencyBalance(p, playerName, currentAmount);
        }
        else if (type.equalsIgnoreCase("remove")) {
            int currentAmount = this.getCurrencyBalance(p, playerName);
            currentAmount -= Math.abs(Integer.parseInt(amount));
            if (currentAmount <= 0) {
                currentAmount = 0;
            }
            this.setCurrencyBalance(p, playerName, currentAmount);
        }
    }

    private void setCurrencyBalance(final CommandSender setter, final String p, final int amount) {
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                this.SQL.setCredits(p, amount);
                if (setter instanceof Player) {
                    PropHuntMessaging.sendMessage((Player)setter, p + " now has " + amount + " " + ShopSettings.currencyName);
                    break;
                }
                setter.sendMessage(p + " now has " + amount + " " + ShopSettings.currencyName);
                break;
            }
            case VAULT: {
                if (setter instanceof Player) {
                    PropHuntMessaging.sendMessage((Player)setter, "Use your economy commands");
                    break;
                }
                setter.sendMessage("Use your normal economy commands");
                break;
            }
        }
    }

    private int getCurrencyBalance(final CommandSender setter, final String p) {
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                return this.SQL.getCredits(p);
            }
            case VAULT: {
                if (setter instanceof Player) {
                    PropHuntMessaging.sendMessage((Player)setter, "Use your economy commands");
                    break;
                }
                setter.sendMessage("Use your normal economy commands");
                break;
            }
        }
        return 0;
    }

    private String getCurrencyBalance(final Player p) {
        switch (ShopSettings.economyType) {
            case PROPHUNT: {
                return this.SQL.getCredits(p.getName()) + " " + ShopSettings.currencyName;
            }
            case VAULT: {
                return this.vaultUtils.economy.getBalance(p.getName()) + " " + this.vaultUtils.economy.currencyNamePlural();
            }
            default: {
                return "0";
            }
        }
    }

    public int loadBlockDisguises() {
        int i = 0;
        if (this.getConfig().contains("block-disguises")) {
            final List<String> blockIds = this.getConfig().getStringList("block-disguises");
            for (final String item : blockIds) {
                DisguiseManager.blockDisguises.put(i, new SimpleDisguise(item));
                ++i;
            }
        }
        return i;
    }

    private String parseDisguise(final String item) {
        final String[] split = item.split(":");
        if (split.length == 2 && this.isInt(split[0]) && this.isInt(split[1])) {
            return item;
        }
        if (this.isInt(item)) {
            return item;
        }
        return null;
    }

    private boolean isInt(final String item) {
        int i = 0;
        try {
            i = Integer.parseInt(item);
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean isItem(final int item) {
        return Material.getMaterial(item) != null && Material.getMaterial(item).isBlock();
    }

    public void setupClasses() {
        if (this.getConfig().contains("SeekerClass")) {
            ItemStack helmet = null;
            ItemStack chest = null;
            ItemStack legs = null;
            ItemStack boots = null;
            final List<ItemStack> inv = new ArrayList<ItemStack>();
            List<PotionEffect> thislist = null;
            if (this.loadEffectsList("SeekerClass") != null) {
                thislist = this.loadEffectsList("SeekerClass");
                this.getLogger().log(Level.INFO, "loaded Seeker Effects List");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Helmet")) != null) {
                helmet = this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Helmet"));
                this.getLogger().log(Level.INFO, "loaded Seeker Helmet");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Chestplate")) != null) {
                chest = this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Chestplate"));
                this.getLogger().log(Level.INFO, "loaded Seeker Chestplate");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Leggings")) != null) {
                legs = this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Leggings"));
                this.getLogger().log(Level.INFO, "loaded Seeker Leggings");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Boots")) != null) {
                boots = this.parseITEMStringToStack(this.getConfig().getString("SeekerClass.Boots"));
                this.getLogger().log(Level.INFO, "loaded Seeker Boots");
            }
            final String path = "SeekerClass.Inventory";
            final String items = this.getConfig().getString(path);
            final String[] SplitItems = items.split("\\,");
            for (int itemstacksize = SplitItems.length, i = 0; i < itemstacksize; ++i) {
                inv.add(this.parseITEMStringToStack(SplitItems[i]));
            }
            if (helmet != null && chest != null && legs != null && boots != null && inv != null && thislist != null) {
                GameManager.seekerCLASS = new SeekerClass(helmet, chest, legs, boots, thislist, inv);
                this.getLogger().log(Level.INFO, "Loaded Seeker Class fully");
            }
            else {
                if (helmet == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect Helmet");
                }
                if (chest == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect chest");
                }
                if (boots == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect boots");
                }
                if (legs == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect legs");
                }
                if (inv == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect inventory");
                }
                if (thislist == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect potion effects");
                }
                this.getLogger().log(Level.WARNING, "Incorrect config for SeekerClass, re read instructions");
            }
        }
        if (this.getConfig().contains("HiderClass")) {
            ItemStack helmet = null;
            ItemStack chest = null;
            ItemStack legs = null;
            ItemStack boots = null;
            final List<ItemStack> inv = new ArrayList<ItemStack>();
            List<PotionEffect> thislist = null;
            if (this.loadEffectsList("HiderClass") != null) {
                thislist = this.loadEffectsList("HiderClass");
                this.getLogger().log(Level.INFO, "loaded hider Effects List");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Helmet")) != null) {
                helmet = this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Helmet"));
                this.getLogger().log(Level.INFO, "loaded hider Helmet");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Chestplate")) != null) {
                chest = this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Chestplate"));
                this.getLogger().log(Level.INFO, "loaded hider Chestplate");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Leggings")) != null) {
                legs = this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Leggings"));
                this.getLogger().log(Level.INFO, "loaded hider Leggings");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Boots")) != null) {
                boots = this.parseITEMStringToStack(this.getConfig().getString("HiderClass.Boots"));
                this.getLogger().log(Level.INFO, "loaded hider Boots");
            }
            final String path = "HiderClass.Inventory";
            final String items = this.getConfig().getString(path);
            final String[] SplitItems = items.split("\\,");
            for (int itemstacksize = SplitItems.length, i = 0; i < itemstacksize; ++i) {
                inv.add(this.parseITEMStringToStack(SplitItems[i]));
            }
            if (helmet != null && chest != null && legs != null && boots != null && inv != null && thislist != null) {
                GameManager.hiderCLASS = new HiderClass(helmet, chest, legs, boots, thislist, inv);
                this.getLogger().log(Level.INFO, "Loaded hider Class fully");
            }
            else {
                if (helmet == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect Helmet");
                }
                if (chest == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect chest");
                }
                if (boots == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect boots");
                }
                if (legs == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect legs");
                }
                if (inv == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect inventory");
                }
                if (thislist == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect potion effects");
                }
                this.getLogger().log(Level.WARNING, "Incorrect config for HiderClass, re read instructions");
            }
        }
    }

    private ItemStack parseITEMStringToStack(final String s) {
        ItemStack stack = null;
        final String[] enchantsplit = s.split(" ");
        if (enchantsplit.length > 1) {
            final String item = enchantsplit[0];
            final String enchants = enchantsplit[1];
            final String[] totalenchants = enchants.split(";");
            int ENCHANTID = 0;
            int ENCHANTLEVEL = 0;
            final Map<Enchantment, Integer> TOTEnchants = new HashMap<Enchantment, Integer>();
            int itemint = 0;
            try {
                itemint = Integer.parseInt(item);
            }
            catch (NumberFormatException nfe) {
                return null;
            }
            for (int i = totalenchants.length, z = 0; z < i; ++z) {
                final String[] subsplit = totalenchants[z].split(":");
                try {
                    ENCHANTID = Integer.parseInt(subsplit[0]);
                    ENCHANTLEVEL = Integer.parseInt(subsplit[1]);
                }
                catch (NumberFormatException nfe2) {
                    return null;
                }
                TOTEnchants.put(Enchantment.getById(ENCHANTID), ENCHANTLEVEL);
            }
            stack = new ItemStack(itemint, 1);
            stack.addUnsafeEnchantments(TOTEnchants);
            return stack;
        }
        final String[] damagesplit = s.split(":");
        if (damagesplit.length > 2) {
            final String id = damagesplit[0];
            final String damage = damagesplit[1];
            final String amount = damagesplit[2];
            int ID = 0;
            short DAMAGE = 0;
            int AMOUNT = 0;
            try {
                ID = Integer.parseInt(id);
                DAMAGE = Short.parseShort(damage);
                AMOUNT = Integer.parseInt(amount);
            }
            catch (NumberFormatException NFE) {
                return null;
            }
            stack = new ItemStack(Material.getMaterial(ID), AMOUNT, DAMAGE);
            return stack;
        }
        final String[] normalsplit = s.split(":");
        final String id2 = normalsplit[0];
        final String amount = normalsplit[1];
        int ID = 0;
        int AMOUNT2 = 0;
        try {
            ID = Integer.parseInt(id2);
            AMOUNT2 = Integer.parseInt(amount);
        }
        catch (NumberFormatException NFE2) {
            return null;
        }
        stack = new ItemStack(Material.getMaterial(ID), AMOUNT2);
        return stack;
    }

    private List<PotionEffect> loadEffectsList(final String path) {
        final List<PotionEffect> plist = new ArrayList<PotionEffect>();
        if (this.getConfig().contains(path + ".Effects")) {
            final String effects = this.getConfig().getString(path + ".Effects");
            final String[] effectsplit = effects.split("\\,");
            for (int i = 0; i < effectsplit.length; ++i) {
                final String[] singlesplit = effectsplit[i].split(":");
                final String id = singlesplit[0];
                final String duration = singlesplit[1];
                final String potency = singlesplit[2];
                int ID = 0;
                int DURATION = 0;
                int POTENCY = 0;
                try {
                    ID = Integer.parseInt(id);
                    DURATION = Integer.parseInt(duration);
                    POTENCY = Integer.parseInt(potency);
                }
                catch (NumberFormatException nfe) {
                    System.out.print("Wrong effect format");
                    return null;
                }
                final PotionEffect pe = new PotionEffect(PotionEffectType.getById(ID), DURATION, POTENCY);
                plist.add(pe);
            }
            return plist;
        }
        return null;
    }

    public Map<Integer, SimpleDisguise> getCustomDisguises(final String arenaName) {
        int i = 0;
        final Map<Integer, SimpleDisguise> disguiseMap = new HashMap<Integer, SimpleDisguise>();
        if (this.getConfig().contains("CustomArenaConfigs." + arenaName + ".block-disguises")) {
            final List<String> blockIds = this.getConfig().getStringList("CustomArenaConfigs." + arenaName + ".block-disguises");
            for (final String item : blockIds) {
                disguiseMap.put(i, new SimpleDisguise(item));
                ++i;
            }
        }
        this.getLogger().info("Custom disguises loaded: " + disguiseMap.size());
        return disguiseMap;
    }

    public HiderClass getCustomHiderClass(final String arenaName) {
        final String path = "CustomArenaConfigs." + arenaName + ".";
        HiderClass hc = null;
        if (this.getConfig().contains(path + "HiderClass")) {
            ItemStack helmet = null;
            ItemStack chest = null;
            ItemStack legs = null;
            ItemStack boots = null;
            final List<ItemStack> inv = new ArrayList<ItemStack>();
            List<PotionEffect> thislist = null;
            if (this.loadEffectsList(path + "HiderClass") != null) {
                thislist = this.loadEffectsList(path + "HiderClass");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Helmet")) != null) {
                helmet = this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Helmet"));
                this.getLogger().log(Level.INFO, "loaded Hider Helmet");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Chestplate")) != null) {
                chest = this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Chestplate"));
                this.getLogger().log(Level.INFO, "loaded Hider Chestplate");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Leggings")) != null) {
                legs = this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Leggings"));
                this.getLogger().log(Level.INFO, "loaded Hider Leggings");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Boots")) != null) {
                boots = this.parseITEMStringToStack(this.getConfig().getString(path + "HiderClass.Boots"));
                this.getLogger().log(Level.INFO, "loaded Hider Boots");
            }
            final String path2 = path + "HiderClass.Inventory";
            final String items = this.getConfig().getString(path2);
            final String[] SplitItems = items.split("\\,");
            for (int itemstacksize = SplitItems.length, i = 0; i < itemstacksize; ++i) {
                inv.add(this.parseITEMStringToStack(SplitItems[i]));
            }
            if (helmet != null && chest != null && legs != null && boots != null && inv != null && thislist != null) {
                hc = new HiderClass(helmet, chest, legs, boots, thislist, inv);
                this.getLogger().log(Level.INFO, "Loaded Hider Class fully");
            }
            else {
                if (helmet == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect Helmet");
                }
                if (chest == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect chest");
                }
                if (boots == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect boots");
                }
                if (legs == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect legs");
                }
                if (inv == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect inventory");
                }
                if (thislist == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect potion effects");
                }
                if (hc == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect config for HiderClass, re read instructions");
                    return null;
                }
            }
        }
        return hc;
    }

    public SeekerClass getCustomSeekerClass(final String arenaName) {
        final String path = "CustomArenaConfigs." + arenaName + ".";
        SeekerClass sc = null;
        if (this.getConfig().contains(path + "SeekerClass")) {
            ItemStack helmet = null;
            ItemStack chest = null;
            ItemStack legs = null;
            ItemStack boots = null;
            final List<ItemStack> inv = new ArrayList<ItemStack>();
            List<PotionEffect> thislist = null;
            if (this.loadEffectsList(path + "SeekerClass") != null) {
                thislist = this.loadEffectsList(path + "SeekerClass");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Helmet")) != null) {
                helmet = this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Helmet"));
                this.getLogger().log(Level.INFO, "loaded Seeker Helmet");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Chestplate")) != null) {
                chest = this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Chestplate"));
                this.getLogger().log(Level.INFO, "loaded Seeker Chestplate");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Leggings")) != null) {
                legs = this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Leggings"));
                this.getLogger().log(Level.INFO, "loaded Seeker Leggings");
            }
            if (this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Boots")) != null) {
                boots = this.parseITEMStringToStack(this.getConfig().getString(path + "SeekerClass.Boots"));
                this.getLogger().log(Level.INFO, "loaded Seeker Boots");
            }
            final String path2 = path + "SeekerClass.Inventory";
            final String items = this.getConfig().getString(path2);
            final String[] SplitItems = items.split("\\,");
            for (int itemstacksize = SplitItems.length, i = 0; i < itemstacksize; ++i) {
                inv.add(this.parseITEMStringToStack(SplitItems[i]));
            }
            if (helmet != null && chest != null && legs != null && boots != null && inv != null && thislist != null) {
                sc = new SeekerClass(helmet, chest, legs, boots, thislist, inv);
                this.getLogger().log(Level.INFO, "Loaded Seeker Class fully");
            }
            else {
                if (helmet == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect Helmet");
                }
                if (chest == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect chest");
                }
                if (boots == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect boots");
                }
                if (legs == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect legs");
                }
                if (inv == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect inventory");
                }
                if (thislist == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect potion effects");
                }
                if (sc == null) {
                    this.getLogger().log(Level.WARNING, "Incorrect config for SeekerClass, re read instructions");
                    return null;
                }
            }
        }
        return sc;
    }

    public void hidePlayer(final Player owner, final ItemStack[] armour) {
        for (final Player viewer : owner.getServer().getOnlinePlayers()) {
            if (viewer != owner) {
                viewer.hidePlayer(owner);
            }
        }
        this.dm.undisguisePlayer(owner);
    }

    public void showPlayer(final Player owner, final boolean shutdown) {
        if (shutdown) {
            for (final Player viewer : owner.getServer().getOnlinePlayers()) {
                if (viewer != owner) {
                    viewer.showPlayer(owner);
                }
            }
        }
        else {
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    for (final Player viewer : owner.getServer().getOnlinePlayers()) {
                        if (viewer != owner) {
                            viewer.showPlayer(owner);
                        }
                    }
                }
            }, 1L);
        }
    }

    static {
        PropHunt.usingTABAPI = false;
    }
}
