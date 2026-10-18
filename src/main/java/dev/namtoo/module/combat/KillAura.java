// lang: Java  file: KillAura.java  target: Fabric 1.21.1
package dev.namtoo.module.combat;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.TickEvent;
import dev.namtoo.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class KillAura extends Module {

    public final Setting reach    = addSetting(new Setting("Reach",    3.5, 2.0, 6.0, 0.1));
    public final Setting delay    = addSetting(new Setting("Delay",    10,  1,   20,  1  ));
    public final Setting players  = addSetting(new Setting("Players",  true));
    public final Setting mobs     = addSetting(new Setting("Mobs",     true));

    private int ticker = 0;

    public KillAura() { super("KillAura", "Combat", GLFW.GLFW_KEY_R); }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (++ticker < (int) delay.value) return;
        ticker = 0;

        Entity target = findTarget();
        if (target == null) return;

        double dx = target.getX() - mc.player.getX();
        double dy = (target.getEyeY() - 0.5) - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double h  = Math.sqrt(dx * dx + dz * dz);

        mc.player.setYaw((float)  Math.toDegrees(Math.atan2(-dx, dz)));
        mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, h)));

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private Entity findTarget() {
        double rr = reach.value * reach.value;
        Entity best = null;
        double bestD = rr;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le)) continue;
            if (le.isDead() || le.getHealth() <= 0) continue;
            if (e instanceof PlayerEntity pe) {
                if (!players.boolValue) continue;
                if (pe.isSpectator()) continue;
            } else {
                if (!mobs.boolValue) continue;
            }
            double d = mc.player.squaredDistanceTo(e);
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }
}
