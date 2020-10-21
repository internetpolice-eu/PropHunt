package me.tomski.utils;

import me.tomski.prophunt.GameManager;

public class SideTabTimer implements Runnable
{
    private SideBarStats sbs;

    public SideTabTimer(final SideBarStats sbs) {
        this.sbs = sbs;
    }

    @Override
    public void run() {
        if (GameManager.useSideStats && this.sbs != null) {
            this.sbs.updateBoard();
        }
    }
}
