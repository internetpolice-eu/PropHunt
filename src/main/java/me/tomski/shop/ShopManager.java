package me.tomski.shop;

import me.tomski.prophunt.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;

public class ShopManager
{
    PropHunt plugin;
    MainShop mainShop;
    DisguiseShop disguiseShop;
    ItemShop itemShop;
    LoadoutChooser loadoutChooser;
    BlockChooser blockChooser;
    
    public ShopManager(final PropHunt plugin) {
        this.plugin = plugin;
        this.init();
    }
    
    private void init() {
        this.mainShop = new MainShop(this.plugin);
        this.disguiseShop = new DisguiseShop(this.plugin);
        this.itemShop = new ItemShop(this.plugin);
        this.loadoutChooser = new LoadoutChooser(this.plugin);
        this.blockChooser = new BlockChooser(this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this.mainShop, (Plugin)this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this.disguiseShop, (Plugin)this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this.itemShop, (Plugin)this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this.loadoutChooser, (Plugin)this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this.blockChooser, (Plugin)this.plugin);
    }
    
    public MainShop getMainShop() {
        return this.mainShop;
    }
    
    public BlockChooser getBlockChooser() {
        return this.blockChooser;
    }
    
    public DisguiseShop getDisguiseShop() {
        return this.disguiseShop;
    }
    
    public ItemShop getItemShop() {
        return this.itemShop;
    }
    
    public LoadoutChooser getLoadoutChooser() {
        return this.loadoutChooser;
    }
}
