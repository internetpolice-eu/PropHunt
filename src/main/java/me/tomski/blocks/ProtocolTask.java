package me.tomski.blocks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.tomski.prophunt.GameManager;
import me.tomski.prophunt.PropHunt;
import me.tomski.utils.SolidBlockTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

// 1.8 protocol spec: https://wiki.vg/index.php?title=Protocol&oldid=7407#Use_Entity
public class ProtocolTask implements Listener {
    private final PropHunt plugin;

    public ProtocolTask(PropHunt plugin) {
        this.plugin = plugin;
    }

    public void initProtocol() {
        PropHunt.protocolManager.getAsynchronousManager().registerAsyncHandler(new PacketAdapter(this.plugin, PacketType.Play.Client.BLOCK_PLACE) {
            public void onPacketReceiving(final PacketEvent event) {
                if (GameManager.hiders.contains(event.getPlayer().getName()) ||
                    GameManager.seekers.contains(event.getPlayer().getName())) {
                    BlockPosition pos = event.getPacket().getBlockPositionModifier().read(0);
                    for (final SolidBlock s : SolidBlockTracker.solidBlocks.values()) {
                        if (s.loc.getBlockX() == pos.getX() && s.loc.getBlockY() == pos.getY() && s.loc.getBlockZ() == pos.getZ()) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }).syncStart();
        PropHunt.protocolManager.getAsynchronousManager().registerAsyncHandler(new PacketAdapter(this.plugin, PacketType.Play.Client.ARM_ANIMATION) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player observer = event.getPlayer();
                final Location observerPos = observer.getEyeLocation();
                final Vector3D observerDir = new Vector3D(observerPos.getDirection());
                final Vector3D observerStart = new Vector3D(observerPos);
                final Vector3D observerEnd = observerStart.add(observerDir.multiply(3));
                Player hit = null;
                try {
                    for (final Player target : PropHunt.protocolManager.getEntityTrackers(observer)) {
                        if (!observer.canSee(target)) {
                            final Vector3D targetPos = new Vector3D(target.getLocation());
                            final Vector3D minimum = targetPos.add(-0.5, 0.0, -0.5);
                            final Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);
                            if (!ProtocolTask.this.hasIntersection(observerStart, observerEnd, minimum, maximum) ||
                                (hit != null && hit.getLocation().distanceSquared(observerPos) <= target.getLocation().distanceSquared(observerPos))) {
                                continue;
                            }
                            hit = target;
                        }
                    }
                } catch (FieldAccessException ex) {
                    ex.printStackTrace();
                }
                if (hit != null) {
                    final PacketContainer useEntity = PropHunt.protocolManager.createPacket(PacketType.Play.Client.USE_ENTITY);
                    useEntity.getIntegers().write(0, hit.getEntityId());
                    useEntity.getEntityUseActions().write(0, EnumWrappers.EntityUseAction.ATTACK);
                    try {
                        PropHunt.protocolManager.recieveClientPacket(event.getPlayer(), useEntity);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).syncStart();
    }

    private boolean hasIntersection(final Vector3D p1, final Vector3D p2, final Vector3D min, final Vector3D max) {
        final double epsilon = 9.999999747378752E-5;
        final Vector3D d = p2.subtract(p1).multiply(0.5);
        final Vector3D e = max.subtract(min).multiply(0.5);
        final Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        final Vector3D ad = d.abs();
        return Math.abs(c.x) <= e.x + ad.x && Math.abs(c.y) <= e.y + ad.y && Math.abs(c.z) <= e.z + ad.z && Math.abs(d.y * c.z - d.z * c.y) <= e.y * ad.z + e.z * ad.y + 9.999999747378752E-5 && Math.abs(d.z * c.x - d.x * c.z) <= e.z * ad.x + e.x * ad.z + 9.999999747378752E-5 && Math.abs(d.x * c.y - d.y * c.x) <= e.x * ad.y + e.y * ad.x + 9.999999747378752E-5;
    }
}
