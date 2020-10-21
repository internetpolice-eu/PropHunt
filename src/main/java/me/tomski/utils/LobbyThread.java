package me.tomski.utils;

import me.tomski.language.LanguageManager;
import me.tomski.language.MessageBank;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;

public class LobbyThread implements Runnable
{
    int time;
    int id;
    private PropHunt plugin;
    public boolean isRunning;

    public LobbyThread(final PropHunt plugin, final int startingTime) {
        this.isRunning = false;
        this.plugin = plugin;
        this.time = new Integer(startingTime);
    }

    @Override
    public void run() {
        if (!this.isRunning) {
            return;
        }
        --this.time;
        GameManager.currentLobbyTime = this.time;
        if (this.time > 0) {
            return;
        }
        GameManager.currentLobbyTime = 0;
        if (GameManager.gameStatus) {
            return;
        }
        if (GameManager.playersToStartGame <= GameManager.playersWaiting.size()) {
            this.plugin.GM.startGame(null);
            this.isRunning = false;
            this.plugin.getServer().getScheduler().cancelTask(this.id);
            return;
        }
        String regex = MessageBank.NOT_ENOUGH_PLAYERS.getMsg();
        regex = LanguageManager.regex(regex, "\\{playeramount\\}", String.valueOf(GameManager.playersToStartGame));
        PropHuntMessaging.broadcastLobby(regex);
        this.time = GameManager.lobbyTime;
    }

    public void setId(final int ID) {
        this.id = ID;
    }
}
