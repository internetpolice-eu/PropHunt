package me.tomski.utils;

import me.tomski.prophunt.*;
import me.tomski.language.*;
import java.io.*;
import java.util.*;

public class GameTimer implements Runnable
{
    public int ID;
    public PropHunt plugin;
    public double damage;
    public int startingtime;
    public int timeleft;
    public int interval;
    public GameManager GM;
    private int intervalcounter;
    private SideBarStats sbs;
    
    public GameTimer(final GameManager gm, final PropHunt plugin, final double seeker_damage, final int interval, final int startingtime, final SideBarStats stats) {
        this.intervalcounter = 0;
        this.plugin = plugin;
        this.damage = seeker_damage;
        this.startingtime = startingtime;
        this.timeleft = startingtime;
        this.interval = interval;
        this.GM = gm;
        this.sbs = stats;
    }
    
    @Override
    public void run() {
        --this.timeleft;
        if (this.timeleft == 30) {
            if (GameManager.blowDisguises && !PHScoreboard.disguisesBlown) {
                PHScoreboard.disguisesBlown = true;
                PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, MessageBank.DISGUISES_BLOWN.getMsg());
            }
            return;
        }
        ++this.intervalcounter;
        GameManager.timeleft = this.timeleft;
        if (this.timeleft != 0) {
            if (this.timeleft >= 0) {
                this.intervalPlayers();
                if (this.interval != 0 && this.intervalcounter % this.interval == 0) {
                    this.intervalSeekers();
                }
                return;
            }
        }
        try {
            this.GM.endGame(Reason.TIME, false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void intervalSeekers() {
        PropHuntMessaging.broadcastMessageToPlayers(GameManager.hiders, GameManager.seekers, this.timeleft + MessageBank.GAME_TIME_LEFT.getMsg());
        for (final String seekers : GameManager.seekers) {
            if (this.plugin.getServer().getPlayer(seekers) != null && this.damage != 0.0) {
                this.plugin.getServer().getPlayer(seekers).damage(this.damage);
            }
        }
    }
    
    private void intervalPlayers() {
        for (final String hiders : GameManager.hiders) {
            if (this.plugin.getServer().getPlayer(hiders) != null) {
                this.plugin.getServer().getPlayer(hiders).setLevel(this.timeleft);
                this.plugin.getServer().getPlayer(hiders).setExp((this.timeleft / GameManager.starting_time > 1) ? 1.0f : (this.timeleft / GameManager.starting_time));
            }
        }
        for (final String seekers : GameManager.seekers) {
            if (this.plugin.getServer().getPlayer(seekers) != null) {
                this.plugin.getServer().getPlayer(seekers).setLevel(this.timeleft);
                this.plugin.getServer().getPlayer(seekers).setExp((this.timeleft / GameManager.starting_time > 1) ? 1.0f : (this.timeleft / GameManager.starting_time));
            }
        }
    }
}
