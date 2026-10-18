// lang: Java  file: SusChunkFinder.java  target: Fabric 1.21.1
//
// "Sus chunk" = a chunk whose tile-entity count or chest count is unusually high
// compared to adjacent loaded chunks — classic stash-finder technique.
//
// Algorithm:
//   1. Walk every loaded chunk in render distance.
//   2. Count BlockEntities that match the configured filter (chests, barrels, etc.).
//   3. Compute the mean + stddev of counts across all loaded chunks.
//   4. Flag any chunk whose count exceeds mean + (threshold * stddev).
//   5. Render highlighted columns over flagged chunks in world-space.
//   6. Display a chat-style list on the HUD (top-right area, below the module list).
//
// "Sus" threshold, scan radius, and what counts as suspicious are all configurable
// via the GUI settings panel.

package dev.namtoo.module.world;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.HudRenderEvent;
import dev.namtoo.event.events.RenderEvent;
import dev.namtoo.event.events.TickEvent;
import dev.namtoo.module.Module;
import net.minecraft.block.entity.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class SusChunkFinder extends Module {

    // ── settings exposed in ClickGUI ─────────────────────────────────────────
    public final Setting threshold  = addSetting(new Setting("Threshold",  2.0, 0.5, 5.0, 0.5));
    public final Setting scanRadius = addSetting(new Setting("Radius",     5,   1,   16,  1  ));
    public final Setting chestOnly  = addSetting(new Setting("ChestOnly",  true));
    public final Setting showHUD    = addSetting(new Setting("ShowHUD",    true));
    public final Setting showESP    = addSetting(new Setting("ShowESP",    true));

    // ── internal state ────────────────────────────────────────────────────────
    private final List<SusChunk>  results   = new ArrayList<>();
    private       int             scanTimer = 0;
    private static final int      SCAN_INTERVAL = 40; // ticks between rescans

    public SusChunkFinder() { super("SusChunk", "World", GLFW.GLFW_KEY_H); }

    // ── scan every SCAN_INTERVAL ticks ───────────────────────────────────────
    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (++scanTimer < SCAN_INTERVAL) return;
        scanTimer = 0;
        scan();
    }

    // ── world-space ESP column render ─────────────────────────────────────────
    @EventHandler
    public void onRender(RenderEvent e) {
        if (!showESP.boolValue || results.isEmpty()) return;
        if (mc.player == null) return;

        Camera cam = mc.gameRenderer.getCamera();
        MatrixStack ms = new MatrixStack();
        ms.translate(-cam.getPos().x, -cam.getPos().y, -cam.getPos().z);

        Tessellator  tess = Tessellator.getInstance();
        BufferBuilder buf  = tess.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR);

        Matrix4f mat = ms.peek().getPositionMatrix();
        for (SusChunk sc : results) {
            float intensity = Math.min(1f, (float)((sc.score - 1.0) / 3.0));
            float r = intensity;
            float gv = 1f - intensity;
            drawColumn(buf, mat, sc.pos, r, gv, 0f, 0.8f);
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2f);
        tess.draw();
        RenderSystem.enableDepthTest();
    }

    // ── HUD list render ────────────────────────────────────────────────────────
    @EventHandler
    public void onHud(HudRenderEvent e) {
        if (!showHUD.boolValue || results.isEmpty()) return;

        var tr  = mc.textRenderer;
        var ctx = e.ctx;
        int sw  = mc.getWindow().getScaledWidth();
        int y   = mc.getWindow().getScaledHeight() / 2 - 10;

        String header = "§e[SusChunk] §7" + results.size() + " found";
        ctx.drawTextWithShadow(tr, header, sw / 2 - tr.getWidth(header) / 2, y, 0xFFFFFF);
        y += 12;

        int shown = 0;
        for (SusChunk sc : results) {
            if (shown++ >= 8) { ctx.drawTextWithShadow(tr, "§7...more", sw/2 - 15, y, 0xAAAAAA); break; }
            String line = "§c" + sc.pos.x + "," + sc.pos.z
                    + " §7| §e" + sc.count + " §7BE | §fx" + String.format("%.1f", sc.score) + "σ";
            ctx.drawTextWithShadow(tr, line, sw / 2 - tr.getWidth(line) / 2, y, 0xFFFFFF);
            y += 10;
        }
    }

    // ── core scan ─────────────────────────────────────────────────────────────
    private void scan() {
        if (mc.world == null || mc.player == null) return;

        ChunkPos center = mc.player.getChunkPos();
        int      radius = (int) scanRadius.value;

        // collect counts per chunk
        Map<ChunkPos, Integer> counts = new LinkedHashMap<>();
        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                ChunkPos  cp    = new ChunkPos(cx, cz);
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                if (chunk == null) continue;

                int cnt = 0;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (chestOnly.boolValue) {
                        if (be instanceof ChestBlockEntity
                         || be instanceof BarrelBlockEntity
                         || be instanceof ShulkerBoxBlockEntity
                         || be instanceof HopperBlockEntity
                         || be instanceof DispenserBlockEntity) cnt++;
                    } else {
                        cnt++; // count every block entity
                    }
                }
                counts.put(cp, cnt);
            }
        }

        if (counts.size() < 2) { results.clear(); return; }

        // mean + stddev
        double mean = counts.values().stream().mapToInt(i -> i).average().orElse(0);
        double variance = counts.values().stream()
                .mapToDouble(i -> Math.pow(i - mean, 2))
                .average().orElse(0);
        double stddev = Math.sqrt(variance);

        // flag outliers
        double thresh = threshold.value;
        List<SusChunk> found = new ArrayList<>();
        for (var entry : counts.entrySet()) {
            int cnt = entry.getValue();
            if (cnt == 0) continue;
            double score = stddev > 0 ? (cnt - mean) / stddev : 0;
            if (score >= thresh) {
                found.add(new SusChunk(entry.getKey(), cnt, score));
            }
        }

        // sort by score descending
        found.sort(Comparator.comparingDouble((SusChunk sc) -> sc.score).reversed());
        results.clear();
        results.addAll(found);
    }

    // ── draw a vertical column 256 blocks tall over the chunk ─────────────────
    private void drawColumn(BufferBuilder buf, Matrix4f mat, ChunkPos cp,
                             float r, float g, float b, float a) {
        float x0 = cp.x * 16f, z0 = cp.z * 16f;
        float x1 = x0 + 16f,   z1 = z0 + 16f;
        float yB  = -64f, yT = 320f;

        // four vertical edges
        line(buf, mat, x0,yB,z0, x0,yT,z0, r,g,b,a);
        line(buf, mat, x1,yB,z0, x1,yT,z0, r,g,b,a);
        line(buf, mat, x0,yB,z1, x0,yT,z1, r,g,b,a);
        line(buf, mat, x1,yB,z1, x1,yT,z1, r,g,b,a);
        // bottom cap
        line(buf, mat, x0,yB,z0, x1,yB,z0, r,g,b,a);
        line(buf, mat, x1,yB,z0, x1,yB,z1, r,g,b,a);
        line(buf, mat, x1,yB,z1, x0,yB,z1, r,g,b,a);
        line(buf, mat, x0,yB,z1, x0,yB,z0, r,g,b,a);
        // top cap
        line(buf, mat, x0,yT,z0, x1,yT,z0, r,g,b,a);
        line(buf, mat, x1,yT,z0, x1,yT,z1, r,g,b,a);
        line(buf, mat, x1,yT,z1, x0,yT,z1, r,g,b,a);
        line(buf, mat, x0,yT,z1, x0,yT,z0, r,g,b,a);
    }

    private void line(BufferBuilder b, Matrix4f m,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float r,  float g,  float bv, float a) {
        b.vertex(m, x0, y0, z0).color(r, g, bv, a);
        b.vertex(m, x1, y1, z1).color(r, g, bv, a);
    }

    // ── result record ─────────────────────────────────────────────────────────
    public static class SusChunk {
        public final ChunkPos pos;
        public final int      count;
        public final double   score; // sigma above mean

        public SusChunk(ChunkPos p, int c, double s) {
            pos = p; count = c; score = s;
        }
    }
}
