package dev.namtoo.event.events;
import net.minecraft.client.gui.DrawContext;
public class HudRenderEvent {
    public final DrawContext ctx;
    public final float tickDelta;
    public HudRenderEvent(DrawContext c, float d) { ctx = c; tickDelta = d; }
}
