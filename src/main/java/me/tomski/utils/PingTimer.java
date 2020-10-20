package me.tomski.utils;

import me.tomski.bungee.*;
import java.io.*;

public class PingTimer implements Runnable
{
    private Pinger pinger;
    
    public PingTimer(final Pinger ping) {
        this.pinger = ping;
    }
    
    @Override
    public void run() {
        try {
            this.pinger.sendServerData();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
