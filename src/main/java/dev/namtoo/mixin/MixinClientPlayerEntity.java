// lang: Java  file: MixinClientPlayerEntity.java  target: Fabric 1.21.1
package dev.namtoo.mixin;

import dev.namtoo.NamtooClient;
import dev.namtoo.module.Module;
import dev.namtoo.module.combat.Velocity;
import dev.namtoo.module.player.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    private static final float FREECAM_SPEED = 0.4f;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object) this;
        MinecraftClient    mc   = MinecraftClient.getInstance();

        // ── FastPlace: zero item-use cooldown ────────────────────────────────
        Module fp = NamtooClient.INSTANCE.getModuleManager().getByName("FastPlace");
        if (fp != null && fp.isEnabled()) self.itemUseTimeLeft = 0;

        // ── Velocity: apply scaled knockback ─────────────────────────────────
        if (Velocity.VelocityState.pending) {
            Velocity.VelocityState.pending = false;
            self.setVelocity(
                    Velocity.VelocityState.velX,
                    Velocity.VelocityState.velY,
                    Velocity.VelocityState.velZ);
        }

        // ── Freecam: WASD / Space / Shift camera movement ─────────────────────
        if (Freecam.active && mc.getWindow() != null) {
            long win = mc.getWindow().getHandle();
            float yaw = (float) Math.toRadians(self.getYaw());

            float dx = 0, dy = 0, dz = 0;
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_W)     == GLFW.GLFW_PRESS) {
                dx -= (float) Math.sin(yaw) * FREECAM_SPEED;
                dz += (float) Math.cos(yaw) * FREECAM_SPEED;
            }
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_S)     == GLFW.GLFW_PRESS) {
                dx += (float) Math.sin(yaw) * FREECAM_SPEED;
                dz -= (float) Math.cos(yaw) * FREECAM_SPEED;
            }
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_A)     == GLFW.GLFW_PRESS) {
                dx -= (float) Math.cos(yaw) * FREECAM_SPEED;
                dz -= (float) Math.sin(yaw) * FREECAM_SPEED;
            }
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_D)     == GLFW.GLFW_PRESS) {
                dx += (float) Math.cos(yaw) * FREECAM_SPEED;
                dz += (float) Math.sin(yaw) * FREECAM_SPEED;
            }
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) dy += FREECAM_SPEED;
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) dy -= FREECAM_SPEED;

            if (dx != 0 || dy != 0 || dz != 0)
                self.setPos(self.getX()+dx, self.getY()+dy, self.getZ()+dz);

            // prevent physics while freecamming
            self.setVelocity(0, 0, 0);
        }
    }
}
