// lang: Java  file: FastPlace.java  target: Fabric 1.21.1
// item-use cooldown zeroed via MixinClientPlayerEntity
package dev.namtoo.module.player;

import dev.namtoo.module.Module;
import org.lwjgl.glfw.GLFW;

public class FastPlace extends Module {
    public FastPlace() { super("FastPlace", "Player", GLFW.GLFW_KEY_UNKNOWN); }
}
