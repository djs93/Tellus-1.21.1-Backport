package com.yucareux.tellus.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder mixin accessor for GuiGraphics.
 * The GuiRenderState-based rendering was removed in the 1.21.1 port;
 * TerrainPreview now uses immediate-mode BufferBuilder rendering instead.
 */
@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
}
