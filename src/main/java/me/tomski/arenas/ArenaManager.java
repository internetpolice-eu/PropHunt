package me.tomski.arenas;

import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;
import me.tomski.prophunt.*;
import java.util.*;

public class ArenaManager
{
    PropHunt plugin;
    public static Map<Arena, ArenaConfig> arenaConfigs;
    public static Map<String, String> setupMap;
    public static Arena currentArena;
    public static Map<String, Arena> playableArenas;
    public List<Arena> arenasInRotation;
    public int rotationCounter;
    Random rand;
    int selection;
    
    public ArenaManager(final PropHunt ph) {
        this.arenasInRotation = new ArrayList<Arena>();
        this.rotationCounter = 0;
        this.rand = new Random();
        this.plugin = ph;
    }
    
    public void addSettingUp(final Player sender, final String name) {
        if (ArenaManager.setupMap.containsKey(sender.getName())) {
            return;
        }
        ArenaManager.setupMap.put(sender.getName(), name);
        this.giveSetupTools(sender);
        ArenaManager.currentArena = new Arena(name, null, null, null, null, null);
    }
    
    public void giveSetupTools(final Player p) {
        final ItemStack tool1 = new ItemStack(Material.WOOL, 1, (short)1);
        final ItemMeta t1meta = tool1.getItemMeta();
        t1meta.setDisplayName(ChatColor.DARK_RED + "Hiders Spawn Tool");
        t1meta.setLore(Arrays.asList(ChatColor.RED + "Place this to set Hider Spawn!"));
        tool1.setItemMeta(t1meta);
        final ItemStack tool2 = new ItemStack(Material.WOOL, 1, (short)2);
        final ItemMeta t2meta = tool2.getItemMeta();
        t2meta.setDisplayName(ChatColor.DARK_BLUE + "Seekers Spawn Tool");
        t2meta.setLore(Arrays.asList(ChatColor.BLUE + "Place this to set the Seeker Spawn!"));
        tool2.setItemMeta(t2meta);
        final ItemStack tool3 = new ItemStack(Material.WOOL, 1, (short)3);
        final ItemMeta t3meta = tool3.getItemMeta();
        t3meta.setDisplayName(ChatColor.DARK_GREEN + "Lobby Spawn Tool");
        t3meta.setLore(Arrays.asList(ChatColor.GREEN + "Click this at the Lobby spawn location", ChatColor.GREEN + "corner of your arena!"));
        tool3.setItemMeta(t3meta);
        final ItemStack tool4 = new ItemStack(Material.WOOL, 1, (short)4);
        final ItemMeta t4meta = tool4.getItemMeta();
        t4meta.setDisplayName(ChatColor.DARK_GREEN + "Spectator Spawn Tool");
        t4meta.setLore(Arrays.asList(ChatColor.GREEN + "Click this at the Spectator spawn location", ChatColor.GREEN + "corner of your arena!"));
        tool4.setItemMeta(t4meta);
        final ItemStack tool5 = new ItemStack(Material.WOOL, 1, (short)5);
        final ItemMeta t4meta2 = tool5.getItemMeta();
        t4meta2.setDisplayName(ChatColor.DARK_GREEN + "Exit Spawn Tool");
        t4meta2.setLore(Arrays.asList(ChatColor.GREEN + "Click this at the Exit spawn location", ChatColor.GREEN + "corner of your arena!"));
        tool5.setItemMeta(t4meta2);
        p.getInventory().addItem(new ItemStack[] { tool1, tool2, tool3, tool4, tool5 });
        p.updateInventory();
    }
    
    public boolean checkComplete() {
        return ArenaManager.currentArena != null && ArenaManager.currentArena.isComplete();
    }
    
    public boolean deleteArena(final String string) {
        String remove = null;
        for (final String a : ArenaManager.playableArenas.keySet()) {
            if (a.equalsIgnoreCase(string)) {
                remove = a;
                this.arenasInRotation.remove(ArenaManager.playableArenas.get(remove));
                this.plugin.AS.getStorageFile().set("Arenas." + a, null);
                this.plugin.AS.saveStorageFile();
                this.plugin.getConfig().set("CustomArenaConfigs." + a, null);
                this.plugin.saveConfig();
                break;
            }
        }
        if (remove != null) {
            ArenaManager.playableArenas.remove(remove);
            return true;
        }
        return false;
    }
    
    public Arena getNextInRotation() {
        if (GameManager.randomArenas) {
            this.selection = this.rand.nextInt(this.arenasInRotation.size());
            return this.arenasInRotation.get(this.selection);
        }
        if (this.rotationCounter >= this.arenasInRotation.size()) {
            this.rotationCounter = 0;
        }
        final Arena a = this.arenasInRotation.get(this.rotationCounter);
        ++this.rotationCounter;
        return a;
    }
    
    public boolean hasInvetorySpace(final Player p) {
        int counter = 0;
        for (final ItemStack item : p.getInventory().getContents()) {
            if (item == null) {
                ++counter;
            }
            if (counter >= 5) {
                return true;
            }
        }
        return counter >= 5;
    }
    
    static {
        ArenaManager.arenaConfigs = new HashMap<Arena, ArenaConfig>();
        ArenaManager.setupMap = new HashMap<String, String>();
        ArenaManager.currentArena = null;
        ArenaManager.playableArenas = new HashMap<String, Arena>();
    }
}
