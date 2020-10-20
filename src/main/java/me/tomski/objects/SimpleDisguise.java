package me.tomski.objects;

import org.bukkit.entity.*;
import org.bukkit.*;

public class SimpleDisguise
{
    private int id;
    private int damage;
    private EntityType ent;
    private String name;
    
    public SimpleDisguise(final int id, final int damage, final EntityType ent) {
        this.id = id;
        this.damage = damage;
        this.ent = ent;
        this.name = this.initName();
    }
    
    public SimpleDisguise(final String configString) {
        if (configString.startsWith("e:")) {
            this.ent = EntityType.fromName(configString.substring(2));
        }
        else if (configString.split(":").length == 2) {
            this.id = Integer.parseInt(configString.split(":")[0]);
            this.damage = Integer.parseInt(configString.split(":")[1]);
        }
        else {
            this.id = Integer.parseInt(configString);
        }
        this.name = this.initName();
    }
    
    private String initName() {
        if (this.ent == null) {
            return Material.getMaterial(this.id).name();
        }
        return this.ent.name().toLowerCase().replaceAll("_", " ");
    }
    
    public String getName() {
        return this.name;
    }
    
    public Integer getID() {
        return this.id;
    }
    
    public int getDamage() {
        return this.damage;
    }
    
    public EntityType getEntityType() {
        return this.ent;
    }
}
