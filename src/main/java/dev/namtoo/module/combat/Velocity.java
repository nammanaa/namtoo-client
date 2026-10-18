// lang: Java  file: Velocity.java  target: Fabric 1.21.1
package dev.namtoo.module.combat;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.PacketEvent;
import dev.namtoo.module.Module;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.lwjgl.glfw.GLFW;

public class Velocity extends Module {

    public final Setting horizontal = addSetting(new Setting("Horizontal", 0.0, 0.0, 1.0, 0.05));
    public final Setting vertical   = addSetting(new Setting("Vertical",   0.0, 0.0, 1.0, 0.05));

    public Velocity() { super("Velocity", "Combat", GLFW.GLFW_KEY_V); }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (mc.player == null) return;
        if (!(e.packet instanceof EntityVelocityUpdateS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;
        e.cancelled = true;
        VelocityState.velX       = pkt.getVelocityX() / 8000.0 * horizontal.value;
        VelocityState.velY       = pkt.getVelocityY() / 8000.0 * vertical.value;
        VelocityState.velZ       = pkt.getVelocityZ() / 8000.0 * horizontal.value;
        VelocityState.pending    = true;
    }

    public static class VelocityState {
        public static boolean pending = false;
        public static double  velX, velY, velZ;
    }
}
