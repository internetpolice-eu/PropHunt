package me.tomski.objects;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class SimpleDisguise {
    private @Nullable Material material;
    private EntityType ent;
    private String name;

    public SimpleDisguise(Material material, EntityType ent) {
        this.material = material;
        this.ent = ent;
        this.name = initName();
    }

    public SimpleDisguise(String configString) {
        if (configString.startsWith("e:")) {
            ent = EntityType.fromName(configString.substring(2));
        } else {
            material = Material.getMaterial(Integer.parseInt(configString));
        }
        name = initName();
    }

    private String initName() {
        if (ent == null) {
            return material.name();
        }
        return ent.name().toLowerCase().replaceAll("_", " ");
    }

    public String getName() {
        return name;
    }

    public @Nullable Material getMaterial() {
        return material;
    }

    public EntityType getEntityType() {
        return ent;
    }
}
