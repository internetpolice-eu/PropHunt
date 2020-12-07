package me.tomski.prophunt;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.tomski.arenas.ArenaConfig;
import me.tomski.language.MessageBank;
import me.tomski.objects.Loadout;
import me.tomski.objects.SimpleDisguise;
import me.tomski.utils.PropHuntMessaging;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LibsDisguiseManager extends DisguiseManager {
    public static Map<Integer, SimpleDisguise> blockDisguises;
    public static Map<Player, SimpleDisguise> preChosenDisguise;
    public static Map<Player, Loadout> loadouts;

    public LibsDisguiseManager(final PropHunt plugin) {
        super(plugin);
        final int i = plugin.loadBlockDisguises();
        plugin.getLogger().log(Level.INFO, "PropHunt: " + i + " disgiuses loaded");
    }

    private Disguise getLibsDisguise(final SimpleDisguise sd) {
        return new MiscDisguise(DisguiseType.FALLING_BLOCK, sd.getMaterial());
    }

    @Override
    public boolean isDisguised(final Player p) {
        return DisguiseAPI.isDisguised(p);
    }

    @Override
    public void disguisePlayer(final Player p, final SimpleDisguise d) {
        final Disguise dis = getLibsDisguise(d);
        DisguiseAPI.disguiseToAll(p, dis);
    }

    @Override
    public void undisguisePlayer(final Player p) {
        DisguiseAPI.undisguiseToAll(p);
    }

    @Override
    public String getDisguiseName(final Player p) {
        if (DisguiseAPI.getDisguise(p).getType() == DisguiseType.FALLING_BLOCK) {
            FallingBlockWatcher watcher = (FallingBlockWatcher) DisguiseAPI.getDisguise(p).getWatcher();
            return watcher.getBlock().getType().name();
        }
        return DisguiseAPI.getDisguise(p).getEntity().getType().name();
    }

    @Override
    public void randomDisguise(final Player p, final ArenaConfig ac) {
        if (preChosenDisguise.containsKey(p)) {
            final SimpleDisguise simpleDisguise = preChosenDisguise.get(p);
            disguisePlayer(p, simpleDisguise);
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + parseDisguiseToName(simpleDisguise));
            preChosenDisguise.remove(p);
            return;
        }
        final SimpleDisguise ds = DisguiseManager.getRandomDisguiseObject(ac.getArenaDisguises());
        if (ds == null) {
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_ERROR.getMsg());
            return;
        }
        disguisePlayer(p, ds);
        PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + parseDisguiseToName(ds));
        preChosenDisguise.remove(p);
    }

    public static String parseDisguiseToName(final SimpleDisguise ds) {
        return ds.getName();
    }

    @Override
    public SimpleDisguise getSimpleDisguise(final Player p) {
        if (DisguiseAPI.getDisguise(p).getType().equals(DisguiseType.FALLING_BLOCK)) {
            FallingBlockWatcher watcher = (FallingBlockWatcher) (DisguiseAPI.getDisguise(p)).getWatcher();
            return new SimpleDisguise(watcher.getBlock().getType());
        }
        return null;
    }

    static {
        blockDisguises = new HashMap<>();
        preChosenDisguise = new HashMap<>();
        loadouts = new HashMap<>();
    }
}
