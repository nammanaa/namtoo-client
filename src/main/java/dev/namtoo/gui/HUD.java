// lang: Java  file: HUD.java  target: Fabric 1.21.1
package dev.namtoo.gui;

import dev.namtoo.NamtooClient;
import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.HudRenderEvent;
import dev.namtoo.module.Module;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class HUD {

    @EventHandler
    public void onHud(HudRenderEvent e) {
        var mc  = NamtooClient.mc;
        var tr  = mc.textRenderer;
        if (tr == null) return;

        DrawContext ctx = e.ctx;
        int sw = mc.getWindow().getScaledWidth();

        // ── watermark ─────────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "§bNamtoo §7Client", 3, 3, 0xFFFFFF);

        // ── active module list (top-right) ────────────────────────────────────
        List<Module> active = NamtooClient.INSTANCE.getModuleManager().getAll()
                .stream().filter(Module::isEnabled).toList();

        int y = 2;
        for (Module m : active) {
            String txt = m.getName();
            int    w   = tr.getWidth(txt);
            int    x   = sw - w - 3;
            ctx.fill(x-1, y-1, x+w+1, y+9, 0x55000000);
            ctx.drawTextWithShadow(tr, "§b" + txt, x, y, 0xFFFFFF);
            y += 11;
        }
    }
}
