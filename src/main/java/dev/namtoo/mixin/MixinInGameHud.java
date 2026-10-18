// lang: Java  file: MixinInGameHud.java  target: Fabric 1.21.1
package dev.namtoo.mixin;

import dev.namtoo.NamtooClient;
import dev.namtoo.event.events.HudRenderEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext ctx, RenderTickCounter counter, CallbackInfo ci) {
        NamtooClient.INSTANCE.getEventBus()
                .post(new HudRenderEvent(ctx, counter.getTickDelta(false)));
    }
}
