// lang: Java  file: Sprint.java  target: Fabric 1.21.1
package dev.namtoo.module.movement;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.TickEvent;
import dev.namtoo.module.Module;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {

    public Sprint() { super("Sprint", "Movement", GLFW.GLFW_KEY_UNKNOWN); }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null) return;
        if (mc.player.forwardSpeed > 0 && !mc.player.isSprinting())
            mc.player.setSprinting(true);
    }
}
