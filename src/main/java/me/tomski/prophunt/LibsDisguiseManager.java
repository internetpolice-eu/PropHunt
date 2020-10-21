package me.tomski.prophunt;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.tomski.arenas.ArenaConfig;
import me.tomski.language.MessageBank;
import me.tomski.objects.Loadout;
import me.tomski.objects.SimpleDisguise;
import me.tomski.utils.PropHuntMessaging;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LibsDisguiseManager extends DisguiseManager
{
    private static PropHunt plugin;
    public static Map<Integer, SimpleDisguise> blockDisguises;
    public static Map<Player, SimpleDisguise> preChosenDisguise;
    public static Map<Player, Loadout> loadouts;

    public LibsDisguiseManager(final PropHunt plugin) {
        super(plugin);
        final int i = plugin.loadBlockDisguises();
        plugin.getLogger().log(Level.INFO, "PropHunt: " + i + " disgiuses loaded");
        DisguiseAPI.setViewDisguises(true);
    }

    private Disguise getLibsDisguise(final SimpleDisguise sd) {
        if (sd.getEntityType() == null) {
            return new MiscDisguise(DisguiseType.FALLING_BLOCK, sd.getID(), sd.getDamage());
        }
        return new MobDisguise(DisguiseType.getType(sd.getEntityType()));
    }

    @Override
    public boolean isDisguised(final Player p) {
        return DisguiseAPI.isDisguised(p);
    }

    @Override
    public void disguisePlayer(final Player p, final SimpleDisguise d) {
        final Disguise dis = this.getLibsDisguise(d);
        dis.setViewSelfDisguise(true);
        DisguiseAPI.disguiseToAll(p, dis);
    }

    @Override
    public void undisguisePlayer(final Player p) {
        DisguiseAPI.undisguiseToAll(p);
    }

    @Override
    public String getDisguiseName(final Player p) {
        return DisguiseAPI.getDisguise(p).getType().equals(DisguiseType.FALLING_BLOCK) ? this.parseIdToName(((MiscDisguise)DisguiseAPI.getDisguise(p)).getId()) : DisguiseAPI.getDisguise(p).getEntity().getType().name();
    }

    private String parseIdToName(final int id) {
        return Material.getMaterial(id).name();
    }

    @Override
    public void randomDisguise(final Player p, final ArenaConfig ac) {
        if (LibsDisguiseManager.preChosenDisguise.containsKey(p)) {
            final SimpleDisguise simpleDisguise = LibsDisguiseManager.preChosenDisguise.get(p);
            this.disguisePlayer(p, simpleDisguise);
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + parseDisguiseToName(simpleDisguise));
            LibsDisguiseManager.preChosenDisguise.remove(p);
            return;
        }
        final SimpleDisguise ds = DisguiseManager.getRandomDisguiseObject(ac.getArenaDisguises());
        if (ds == null) {
            PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_ERROR.getMsg());
            return;
        }
        this.disguisePlayer(p, ds);
        PropHuntMessaging.sendMessage(p, MessageBank.DISGUISE_MESSAGE.getMsg() + parseDisguiseToName(ds));
        LibsDisguiseManager.preChosenDisguise.remove(p);
    }

    public static String parseDisguiseToName(final SimpleDisguise ds) {
        return ds.getName();
    }

    @Override
    public SimpleDisguise getSimpleDisguise(final Player p) {
        if (DisguiseAPI.getDisguise(p).getType().equals(DisguiseType.FALLING_BLOCK)) {
            return new SimpleDisguise(((MiscDisguise)DisguiseAPI.getDisguise(p)).getId(), ((MiscDisguise)DisguiseAPI.getDisguise(p)).getData(), null);
        }
        return null;
    }

    static {
        LibsDisguiseManager.blockDisguises = new HashMap<Integer, SimpleDisguise>();
        LibsDisguiseManager.preChosenDisguise = new HashMap<Player, SimpleDisguise>();
        LibsDisguiseManager.loadouts = new HashMap<Player, Loadout>();
    }
}
