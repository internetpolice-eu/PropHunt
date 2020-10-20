package me.tomski.utils;

import org.bukkit.entity.*;
import org.bukkit.*;
import me.tomski.prophunt.*;
import org.bukkit.potion.*;
import me.tomski.language.*;
import java.util.*;

public class SeekerDelay implements Runnable
{
    private int ID;
    private List<Player> PLAYERS;
    int COUNTER;
    private PropHunt PLUGIN;
    private Location LOCATION;
    public boolean isDelaying;
    
    public SeekerDelay(final Player firstSeeker, final int time, final PropHunt plugin) {
        (this.PLAYERS = new ArrayList<Player>()).add(firstSeeker);
        this.COUNTER = time;
        this.PLUGIN = plugin;
        this.LOCATION = firstSeeker.getLocation().clone();
        this.isDelaying = true;
    }
    
    public void setID(final int delayID) {
        this.ID = delayID;
    }
    
    public void addPlayer(final Player seeker) {
        this.PLAYERS.add(seeker);
    }
    
    @Override
    public void run() {
        for (final Player p : this.PLAYERS) {
            if (!GameManager.seekers.contains(p.getName())) {
                continue;
            }
            if (this.COUNTER == GameManager.seekerDelayTime) {
                if (GameManager.blindSeeker) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * GameManager.seekerDelayTime, 1));
                }
                PropHuntMessaging.sendMessage(p, MessageBank.SEEKER_DELAY.getMsg());
            }
            p.teleport(this.LOCATION);
            if (this.COUNTER > 0) {
                continue;
            }
            PropHuntMessaging.sendMessage(p, MessageBank.SEEKER_DELAY_END.getMsg());
            this.PLUGIN.SBS.addPlayerToGame(this.PLUGIN, p);
        }
        if (this.COUNTER <= 0) {
            this.isDelaying = false;
            this.PLUGIN.getServer().getScheduler().cancelTask(this.ID);
        }
        --this.COUNTER;
    }
}
