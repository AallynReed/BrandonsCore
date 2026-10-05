package com.brandon3055.brandonscore.client.render;

import codechicken.lib.gui.modular.SpriteSupplier;
import codechicken.lib.math.MathHelper;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    private static final Map<Identifier, FullSprite> FULL_SPRITES = new HashMap<>();

//    /**
//     * * @return The buffer source used for GUI rendering. You must ALWAYS call endBatch on this when you are done with it.
//     */
//    @Deprecated
//    public static MultiBufferSource.BufferSource getGuiBuffers() {
//        return MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//    }

//    @Deprecated
//    public static MultiBufferSource.BufferSource getBuffers() {
//        return Minecraft.getInstance().renderBuffers().bufferSource();
//    }

    @Deprecated
    public static void endBatch(MultiBufferSource getter) {
        if (getter instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource) getter).endBatch();
        }
    }

    public static void drawPieProgress(GuiGraphicsExtractor render, double x, double y, double diameter, double progress, double offsetAngle, int colour) {
        drawPieProgress(render, x, y, diameter, progress, offsetAngle, colour, colour);
    }

    public static void drawPieProgress(GuiGraphicsExtractor render, double x, double y, double diameter, double progress, double offsetAngle, int innerColour, int outerColour) {
        float radius = (float) diameter / 2;
        render.cc$submitCustom(RenderPipelines.GUI, TextureSetup.noTexture(), x, x + diameter, y, y + diameter, (builder, pose) -> {
            float lastX = 0;
            float lastY = 0;
            for (double d = 0; d <= 1; d += 1D / 30D) {
                float angle = (float) ((d * progress) + 0.5F - progress);
                angle *= (float) Math.PI * 2;
                angle += (float) MathHelper.torad * (float) offsetAngle;
                float vertX = (float) (x + radius + Math.sin(angle) * radius);
                float vertY = (float) (y + radius + Math.cos(angle) * radius);
                if (d > 0) {
                    builder.addVertexWith2DPose(pose, (float) (x + radius), (float) (y + radius)).setColor(innerColour);
                    builder.addVertexWith2DPose(pose, lastX, lastY).setColor(outerColour);
                    builder.addVertexWith2DPose(pose, vertX, vertY).setColor(outerColour);
                    builder.addVertexWith2DPose(pose, vertX, vertY).setColor(outerColour);
                }
                lastX = vertX;
                lastY = vertY;
            }
        });
    }

    public static SpriteSupplier fromRawTexture(Identifier texture) {
        FullSprite sprite = FULL_SPRITES.computeIfAbsent(texture, FullSprite::new);
        return () -> sprite;
    }

    public static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    private static class FullSprite extends TextureAtlasSprite {
        private FullSprite(Identifier location) {
            super(location, new SpriteContents(location, new FrameSize(1, 1), new NativeImage(1, 1, false)), 1, 1, 0, 0, 0);
        }

        @Override
        public float getU(float u)
        {
            return u / 16;
        }

        @Override
        public float getV(float v)
        {
            return v / 16;
        }
    }
}