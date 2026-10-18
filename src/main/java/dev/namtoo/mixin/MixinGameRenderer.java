// lang: Java  file: MixinGameRenderer.java  target: Fabric 1.21.1
package dev.namtoo.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

// Reserved for future GameRenderer-level hooks (e.g. FOV override, custom shader inject).
@Mixin(GameRenderer.class)
public class MixinGameRenderer {}
