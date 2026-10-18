// lang: Java  file: MixinWorldRenderer.java  target: Fabric 1.21.1
package dev.namtoo.mixin;

import dev.namtoo.NamtooClient;
import dev.namtoo.event.events.RenderEvent;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(float tickDelta, long limitTime, boolean renderBlockOutline,
                          Camera camera, GameRenderer gameRenderer,
                          LightmapTextureManager lightmap,
                          Matrix4f posMatrix, Matrix4f projMatrix,
                          CallbackInfo ci) {
        NamtooClient.INSTANCE.getEventBus().post(new RenderEvent(tickDelta));
    }
}
