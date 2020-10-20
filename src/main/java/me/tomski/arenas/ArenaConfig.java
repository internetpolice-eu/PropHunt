package me.tomski.arenas;

import me.tomski.objects.*;
import me.tomski.classes.*;
import java.util.*;

public class ArenaConfig
{
    Map<Integer, SimpleDisguise> arenaDisguises;
    HiderClass arenaHiderClass;
    SeekerClass arenaSeekerClass;
    boolean usingDefault;
    
    public ArenaConfig(final Map<Integer, SimpleDisguise> dis, final HiderClass hC, final SeekerClass sC, final boolean def) {
        this.arenaDisguises = new HashMap<Integer, SimpleDisguise>();
        this.arenaDisguises = dis;
        this.arenaHiderClass = hC;
        this.arenaSeekerClass = sC;
        this.usingDefault = def;
    }
    
    public Map<Integer, SimpleDisguise> getArenaDisguises() {
        return this.arenaDisguises;
    }
    
    public void setArenaDisguises(final Map<Integer, SimpleDisguise> arenaDisguises) {
        this.arenaDisguises = arenaDisguises;
    }
    
    public HiderClass getArenaHiderClass() {
        return this.arenaHiderClass;
    }
    
    public void setArenaHiderClass(final HiderClass arenaHiderClass) {
        this.arenaHiderClass = arenaHiderClass;
    }
    
    public SeekerClass getArenaSeekerClass() {
        return this.arenaSeekerClass;
    }
    
    public void setArenaSeekerClass(final SeekerClass arenaSeekerClass) {
        this.arenaSeekerClass = arenaSeekerClass;
    }
    
    public boolean isUsingDefault() {
        return this.usingDefault;
    }
    
    public void setUsingDefault(final boolean usingDefault) {
        this.usingDefault = usingDefault;
    }
}
