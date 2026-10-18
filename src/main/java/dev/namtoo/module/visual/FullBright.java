// lang: Java  file: FullBright.java  target: Fabric 1.21.1
package dev.namtoo.module.visual;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.TickEvent;
import dev.namtoo.module.Module;
import org.lwjgl.glfw.GLFW;

public class FullBright extends Module {

    private double savedGamma;

    public FullBright() { super("FullBright", "Visual", GLFW.GLFW_KEY_G); }

    @Override
    public void onEnable() {
        if (mc.options == null) return;
        savedGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(16.0);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.getGamma().setValue(savedGamma);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.options != null) mc.options.getGamma().setValue(16.0);
    }
}
