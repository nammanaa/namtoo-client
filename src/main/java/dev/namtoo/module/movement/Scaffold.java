// lang: Java  file: Scaffold.java  target: Fabric 1.21.1
package dev.namtoo.module.movement;

import dev.namtoo.event.EventHandler;
import dev.namtoo.event.events.TickEvent;
import dev.namtoo.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Scaffold extends Module {

    public Scaffold() { super("Scaffold", "Movement", GLFW.GLFW_KEY_X); }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = BlockPos.ofFloored(mc.player.getPos()).down();
        if (!mc.world.getBlockState(below).isAir()) return;

        int slot = findBlock();
        if (slot == -1) return;

        int prev   = mc.player.getInventory().selectedSlot;
        float savedPitch = mc.player.getPitch();

        mc.player.getInventory().selectedSlot = slot;
        mc.player.setPitch(80f);

        Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
        mc.interactionManager.interactBlock(
                mc.player, Hand.MAIN_HAND,
                new BlockHitResult(hitVec, Direction.UP, below, false));
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.setPitch(savedPitch);
        mc.player.getInventory().selectedSlot = prev;
    }

    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
