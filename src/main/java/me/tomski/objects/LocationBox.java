package me.tomski.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationBox
{
    private String configString;
    private Location location;

    public LocationBox(final Location loc) {
        this.location = loc;
    }

    public LocationBox(final String configString) {
        this.configString = configString;
    }

    public String box() {
        final String worldName = this.location.getWorld().getName();
        final double x = this.location.getX();
        final double y = this.location.getY();
        final double z = this.location.getZ();
        final float yaw = this.location.getYaw();
        final float pitch = this.location.getPitch();
        return worldName + "|" + x + "|" + y + "|" + z + "|" + yaw + "|" + pitch;
    }

    public Location unBox() {
        final String[] args = this.configString.split("\\|");
        final String worldName = args[0];
        final double x = Double.valueOf(args[1]);
        final double y = Double.valueOf(args[2]);
        final double z = Double.valueOf(args[3]);
        final float yaw = Float.valueOf(args[4]);
        final float pitch = Float.valueOf(args[5]);
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
