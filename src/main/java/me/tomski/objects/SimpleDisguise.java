package me.tomski.objects;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class SimpleDisguise {
    private final Material material;

    public SimpleDisguise(Material material) {
        this.material = material;
    }

    public String getName() {
        return material.name();
    }

    public @NotNull Material getMaterial() {
        return material;
    }
}
