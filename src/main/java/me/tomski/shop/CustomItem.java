package me.tomski.shop;

import me.tomski.prophunt.ShopSettings;
import org.bukkit.inventory.ItemStack;

public enum CustomItem
{
    FIRST_SEEKER("First Seeker", getCfgItem("FirstSeeker")),
    FORCE_HIDER("Force Hider", getCfgItem("ForceHider"));

    private String itemName;
    private ItemStack item;

    private CustomItem(final String itemName, final ItemStack item) {
        this.itemName = itemName;
        this.item = item;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public String getItemName() {
        return this.itemName;
    }

    public CustomItem getFromItemStack(final ItemStack testStack) {
        for (final CustomItem item : values()) {
            if (testStack.getType().equals(item.getItem().getType()) && testStack.getData().getData() == item.getItem().getData().getData()) {
                return item;
            }
        }
        return null;
    }

    private static ItemStack getCfgItem(final String s) {
        return ShopSettings.getCustomItem(s);
    }
}
