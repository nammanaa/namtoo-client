// lang: Java  file: ClickGUI.java  target: Fabric 1.21.1
//
// RSHIFT = hardcoded open/close (wired in MixinMinecraftClient)
//
// Layout:
//   One panel per category, draggable by header.
//   Left-click module row → toggle ON/OFF.
//   Right-click module row → expand/collapse settings sub-panel.
//   Settings:
//     TOGGLE  → click label to flip
//     SLIDER  → click+drag horizontally
//     ENUM    → click to cycle

package dev.namtoo.gui;

import dev.namtoo.NamtooClient;
import dev.namtoo.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

public class ClickGUI extends Screen {

    // ── colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG        = 0xCC111118;
    private static final int COL_HEADER    = 0xFF2A2A50;
    private static final int COL_HEADER_H  = 0xFF3A3A70;
    private static final int COL_MOD_ON    = 0x664455CC;
    private static final int COL_MOD_OFF   = 0x33FFFFFF;
    private static final int COL_SETTINGS  = 0xBB0D0D1A;
    private static final int COL_SLIDER_BG = 0xFF333355;
    private static final int COL_SLIDER_FG = 0xFF5577FF;
    private static final int COL_WHITE     = 0xFFFFFFFF;
    private static final int COL_GREY      = 0xFFAAAAAA;
    private static final int COL_ACCENT    = 0xFF55AAFF;

    private static final int PANEL_W  = 110;
    private static final int HEADER_H = 14;
    private static final int ROW_H    = 12;
    private static final int SET_ROW  = 11;

    // ── state ─────────────────────────────────────────────────────────────────
    private boolean open = false;

    /** per-category panel x,y */
    private final Map<String, int[]>   panelPos   = new LinkedHashMap<>();
    /** modules whose settings are expanded */
    private final Set<String>          expanded   = new HashSet<>();

    /** drag state */
    private String draggingPanel = null;
    private int    dragOX, dragOY;

    /** slider drag state */
    private Module.Setting  draggingSlider    = null;
    private int             sliderStartX;
    private double          sliderStartValue;

    public ClickGUI() {
        super(Text.literal("Namtoo Client"));
    }

    // ── open / close ──────────────────────────────────────────────────────────
    public boolean isOpen() { return open; }

    public void open() {
        open = true;
        MinecraftClient.getInstance().setScreen(this);
    }

    public void close() {
        open = false;
        MinecraftClient.getInstance().setScreen(null);
    }

    // called from mixin on RSHIFT press
    public void toggle() { if (open) close(); else open(); }

    // ── init panels on first open ─────────────────────────────────────────────
    @Override
    public void init() {
        if (panelPos.isEmpty()) {
            int x = 6;
            for (String cat : NamtooClient.INSTANCE.getModuleManager().getCategories()) {
                panelPos.put(cat, new int[]{x, 6});
                x += PANEL_W + 4;
            }
        }
    }

    // ── render ────────────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // dim background
        ctx.fill(0, 0, width, height, 0x88000000);

        for (String cat : panelPos.keySet()) {
            int[]        pos  = panelPos.get(cat);
            List<Module> mods = NamtooClient.INSTANCE.getModuleManager().getByCategory(cat);

            int totalH = panelHeight(cat, mods);

            // panel bg
            ctx.fill(pos[0], pos[1], pos[0]+PANEL_W, pos[1]+totalH, COL_BG);

            // header
            boolean hHover = mx >= pos[0] && mx < pos[0]+PANEL_W
                          && my >= pos[1] && my < pos[1]+HEADER_H;
            ctx.fill(pos[0], pos[1], pos[0]+PANEL_W, pos[1]+HEADER_H,
                     hHover ? COL_HEADER_H : COL_HEADER);
            ctx.drawTextWithShadow(textRenderer,
                    "§b" + cat, pos[0]+4, pos[1]+3, COL_WHITE);

            int rowY = pos[1] + HEADER_H;

            for (Module m : mods) {
                boolean on    = m.isEnabled();
                boolean hover = mx >= pos[0] && mx < pos[0]+PANEL_W
                             && my >= rowY   && my < rowY+ROW_H;

                ctx.fill(pos[0], rowY, pos[0]+PANEL_W, rowY+ROW_H,
                         on ? COL_MOD_ON : (hover ? 0x22FFFFFF : COL_MOD_OFF));

                // left indicator bar
                if (on) ctx.fill(pos[0], rowY, pos[0]+2, rowY+ROW_H, 0xFF5577FF);

                ctx.drawTextWithShadow(textRenderer,
                        m.getName(), pos[0]+5, rowY+2,
                        on ? COL_ACCENT : COL_GREY);

                // chevron if has settings
                if (!m.getSettings().isEmpty()) {
                    String ch = expanded.contains(m.getName()) ? "▼" : "▶";
                    ctx.drawTextWithShadow(textRenderer, "§7"+ch,
                            pos[0]+PANEL_W-10, rowY+2, COL_GREY);
                }

                rowY += ROW_H;

                // settings rows
                if (expanded.contains(m.getName())) {
                    for (Module.Setting s : m.getSettings()) {
                        rowY = renderSetting(ctx, s, pos[0], rowY, mx, my);
                    }
                }
            }

            // panel border
            drawBorder(ctx, pos[0], pos[1], PANEL_W, totalH, 0xFF333355);
        }
    }

    private int renderSetting(DrawContext ctx, Module.Setting s,
                               int px, int ry, int mx, int my) {
        ctx.fill(px, ry, px+PANEL_W, ry+SET_ROW, COL_SETTINGS);

        switch (s.type) {
            case TOGGLE -> {
                ctx.drawTextWithShadow(textRenderer,
                        "  §7" + s.name + ": " + (s.boolValue ? "§aON" : "§cOFF"),
                        px+2, ry+2, COL_WHITE);
            }
            case SLIDER -> {
                double pct  = (s.value - s.min) / (s.max - s.min);
                int    barW = PANEL_W - 4;
                int    fill = (int)(barW * pct);
                ctx.fill(px+2, ry+6, px+2+barW, ry+9, COL_SLIDER_BG);
                ctx.fill(px+2, ry+6, px+2+fill, ry+9, COL_SLIDER_FG);
                String lbl = "  §7" + s.name + ": §f" + fmt(s.value);
                ctx.drawTextWithShadow(textRenderer, lbl, px+2, ry+1, COL_WHITE);
            }
            case ENUM -> {
                ctx.drawTextWithShadow(textRenderer,
                        "  §7" + s.name + ": §e" + s.currentEnum(),
                        px+2, ry+2, COL_WHITE);
            }
        }
        return ry + SET_ROW;
    }

    // ── mouse events ──────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (String cat : panelPos.keySet()) {
            int[]        pos  = panelPos.get(cat);
            List<Module> mods = NamtooClient.INSTANCE.getModuleManager().getByCategory(cat);

            // header drag start
            if (hit(mx, my, pos[0], pos[1], PANEL_W, HEADER_H)) {
                draggingPanel = cat;
                dragOX = (int)mx - pos[0];
                dragOY = (int)my - pos[1];
                return true;
            }

            int rowY = pos[1] + HEADER_H;
            for (Module m : mods) {
                if (hit(mx, my, pos[0], rowY, PANEL_W, ROW_H)) {
                    if (btn == 0) m.toggle();            // left → toggle
                    if (btn == 1) {                      // right → expand settings
                        if (!m.getSettings().isEmpty()) {
                            if (expanded.contains(m.getName())) expanded.remove(m.getName());
                            else expanded.add(m.getName());
                        }
                    }
                    return true;
                }
                rowY += ROW_H;

                if (expanded.contains(m.getName())) {
                    for (Module.Setting s : m.getSettings()) {
                        if (hit(mx, my, pos[0], rowY, PANEL_W, SET_ROW)) {
                            handleSettingClick(s, (int)mx, pos[0], btn);
                            return true;
                        }
                        rowY += SET_ROW;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private void handleSettingClick(Module.Setting s, int mx, int px, int btn) {
        switch (s.type) {
            case TOGGLE -> s.boolValue = !s.boolValue;
            case SLIDER -> {
                draggingSlider   = s;
                sliderStartX     = mx;
                sliderStartValue = s.value;
            }
            case ENUM   -> s.cycleEnum();
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (draggingPanel != null) {
            int[] p = panelPos.get(draggingPanel);
            p[0] = (int)mx - dragOX;
            p[1] = (int)my - dragOY;
            return true;
        }
        if (draggingSlider != null) {
            Module.Setting s = draggingSlider;
            double range = s.max - s.min;
            double delta2 = (mx - sliderStartX) / (double)(PANEL_W - 4) * range;
            s.value = Math.max(s.min, Math.min(s.max,
                    sliderStartValue + Math.round(delta2 / s.step) * s.step));
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        draggingPanel  = null;
        draggingSlider = null;
        return super.mouseReleased(mx, my, btn);
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private int panelHeight(String cat, List<Module> mods) {
        int h = HEADER_H;
        for (Module m : mods) {
            h += ROW_H;
            if (expanded.contains(m.getName()))
                h += m.getSettings().size() * SET_ROW;
        }
        return h;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x+w && my >= y && my < y+h;
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int col) {
        ctx.fill(x,   y,   x+w, y+1, col);
        ctx.fill(x,   y+h-1, x+w, y+h, col);
        ctx.fill(x,   y,   x+1, y+h, col);
        ctx.fill(x+w-1, y, x+w, y+h, col);
    }

    private static String fmt(double v) {
        return v == (long)v ? String.valueOf((long)v) : String.format("%.2f", v);
    }

    @Override public boolean shouldPause()       { return false; }
    @Override public boolean shouldCloseOnEsc()  { return true;  }
    @Override public void    closeScreen()       { close();       }
    @Override public void    removed()           { open = false;  }
}
