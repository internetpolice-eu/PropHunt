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
        this.plugin.getServer().getPluginManager().registerEvents(this.mainShop, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.disguiseShop, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.itemShop, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.loadoutChooser, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.blockChooser, this.plugin);
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
