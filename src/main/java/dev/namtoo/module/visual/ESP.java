// lang: Java  file: ESP.java  target: Fabric 1.21.1
// Renders axis-aligned bounding boxes via VertexConsumer (Sodium-compatible)
package dev.namtoo.module.visual;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.RenderEvent;
import dev.namtoo.module.Module;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class ESP extends Module {

    public final Setting players = addSetting(new Setting("Players", true));
    public final Setting mobs    = addSetting(new Setting("Mobs",    true));

    public ESP() { super("ESP", "Visual", GLFW.GLFW_KEY_B); }

    @EventHandler
    public void onRender(RenderEvent e) {
        if (mc.player == null || mc.world == null) return;

        Camera cam = mc.gameRenderer.getCamera();
        MatrixStack ms = new MatrixStack();
        ms.translate(-cam.getPos().x, -cam.getPos().y, -cam.getPos().z);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR);

        for (Entity ent : mc.world.getEntities()) {
            if (ent == mc.player || !(ent instanceof LivingEntity le)) continue;
            if (le.isDead() || le.getHealth() <= 0) continue;
            boolean isPlayer = ent instanceof PlayerEntity;
            if (isPlayer && !players.boolValue) continue;
            if (!isPlayer && !mobs.boolValue)   continue;

            float r = isPlayer ? 1f : 0f;
            float g = isPlayer ? 0f : 1f;
            float b = 0f;

            Box box = le.getBoundingBox().offset(
                    le.lerp(e.tickDelta, le.prevX, le.getX()) - le.getX(),
                    le.lerp(e.tickDelta, le.prevY, le.getY()) - le.getY(),
                    le.lerp(e.tickDelta, le.prevZ, le.getZ()) - le.getZ()
            );

            Matrix4f mat = ms.peek().getPositionMatrix();
            drawBox(buf, mat, box, r, g, b, 1f);
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);
        tess.draw();
        RenderSystem.enableDepthTest();
    }

    private void drawBox(BufferBuilder buf, Matrix4f mat, Box b,
                         float r, float g, float bv, float a) {
        float x0 = (float) b.minX, y0 = (float) b.minY, z0 = (float) b.minZ;
        float x1 = (float) b.maxX, y1 = (float) b.maxY, z1 = (float) b.maxZ;

        // bottom
        line(buf, mat, x0,y0,z0, x1,y0,z0, r,g,bv,a);
        line(buf, mat, x1,y0,z0, x1,y0,z1, r,g,bv,a);
        line(buf, mat, x1,y0,z1, x0,y0,z1, r,g,bv,a);
        line(buf, mat, x0,y0,z1, x0,y0,z0, r,g,bv,a);
        // top
        line(buf, mat, x0,y1,z0, x1,y1,z0, r,g,bv,a);
        line(buf, mat, x1,y1,z0, x1,y1,z1, r,g,bv,a);
        line(buf, mat, x1,y1,z1, x0,y1,z1, r,g,bv,a);
        line(buf, mat, x0,y1,z1, x0,y1,z0, r,g,bv,a);
        // verticals
        line(buf, mat, x0,y0,z0, x0,y1,z0, r,g,bv,a);
        line(buf, mat, x1,y0,z0, x1,y1,z0, r,g,bv,a);
        line(buf, mat, x1,y0,z1, x1,y1,z1, r,g,bv,a);
        line(buf, mat, x0,y0,z1, x0,y1,z1, r,g,bv,a);
    }

    private void line(BufferBuilder b, Matrix4f m,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float r,  float g,  float bv, float a) {
        b.vertex(m, x0, y0, z0).color(r, g, bv, a);
        b.vertex(m, x1, y1, z1).color(r, g, bv, a);
    }
}
