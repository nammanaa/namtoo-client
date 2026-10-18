// lang: Java  file: Freecam.java  target: Fabric 1.21.1
package dev.namtoo.module.player;

import dev.namtoo.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {

    public static boolean active = false;
    public static Vec3d   savedPos;
    public static float   savedYaw, savedPitch;
    // camera movement handled in MixinClientPlayerEntity

    public Freecam() { super("Freecam", "Player", GLFW.GLFW_KEY_C); }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        savedPos   = mc.player.getPos();
        savedYaw   = mc.player.getYaw();
        savedPitch = mc.player.getPitch();
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
        if (mc.player != null && savedPos != null) {
            mc.player.setPos(savedPos.x, savedPos.y, savedPos.z);
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
        }
    }
}
