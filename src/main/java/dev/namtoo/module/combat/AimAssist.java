// lang: Java  file: AimAssist.java  target: Fabric 1.21.1
package dev.namtoo.module.combat;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.TickEvent;
import dev.namtoo.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public class AimAssist extends Module {

    public final Setting range = addSetting(new Setting("Range", 6.0, 1.0, 10.0, 0.5));
    public final Setting speed = addSetting(new Setting("Speed", 3.0, 0.5, 10.0, 0.5));
    public final Setting fov   = addSetting(new Setting("FOV",  45.0, 5.0, 180.0, 5.0));

    public AimAssist() { super("AimAssist", "Combat", GLFW.GLFW_KEY_Z); }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        Entity target = findClosest();
        if (target == null) return;

        double dx = target.getX() - mc.player.getX();
        double dy = (target.getEyeY() - 0.5) - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double h  = Math.sqrt(dx * dx + dz * dz);

        float wY = (float)  Math.toDegrees(Math.atan2(-dx, dz));
        float wP = (float) -Math.toDegrees(Math.atan2(dy, h));

        float cY = mc.player.getYaw();
        float cP = mc.player.getPitch();
        float s  = (float) speed.value;

        mc.player.setYaw(cY   + clamp(wrap(wY - cY), -s, s));
        mc.player.setPitch(cP + clamp(wrap(wP - cP), -s, s));
    }

    private Entity findClosest() {
        Entity best = null;
        double bestD = range.value * range.value;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity le)) continue;
            if (le.isDead() || le.getHealth() <= 0) continue;
            if (mc.player.squaredDistanceTo(e) > bestD) continue;
            double dx = e.getX() - mc.player.getX();
            double dz = e.getZ() - mc.player.getZ();
            float  ang = (float) Math.toDegrees(Math.atan2(-dx, dz));
            if (Math.abs(wrap(ang - mc.player.getYaw())) > fov.value) continue;
            bestD = mc.player.squaredDistanceTo(e);
            best  = e;
        }
        return best;
    }

    private static float wrap(float d) {
        d %= 360f;
        if (d >= 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }
    private static float clamp(float v, float mn, float mx) {
        return Math.max(mn, Math.min(mx, v));
    }
}
