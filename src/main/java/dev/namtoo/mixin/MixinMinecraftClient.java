// lang: Java  file: MixinMinecraftClient.java  target: Fabric 1.21.1
package dev.namtoo.mixin;

import dev.namtoo.NamtooClient;
import dev.namtoo.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    private boolean guiKeyWasDown = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient self = (MinecraftClient)(Object) this;

        // post tick event
        NamtooClient.INSTANCE.getEventBus().post(new TickEvent());

        // RSHIFT — hardcoded GUI toggle, rising-edge so it fires once per press
        if (self.currentScreen == null || NamtooClient.INSTANCE.getClickGUI().isOpen()) {
            boolean down = self.getWindow() != null
                    && GLFW.glfwGetKey(self.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)
                       == GLFW.GLFW_PRESS;
            if (down && !guiKeyWasDown) {
                NamtooClient.INSTANCE.getClickGUI().toggle();
            }
            guiKeyWasDown = down;
        }
    }
}
