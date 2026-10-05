package com.brandon3055.brandonscore.client.render;

import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.buffer.DelegatingVertexConsumer;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.brandonscore.multiblock.MultiBlockPart;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Created by brandon3055 on 25/07/2022
 */
public class MultiBlockRenderers {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final RenderType outlineType = RenderType.create("invalid_outline", RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "pipeline/invalid_outline"))
                    .withDepthStencilState(Optional.empty())
                    .build())
            .bufferSize(256)
            .createRenderSetup()
    );

    public static void renderBuildGuide(Level level, BlockPos inWorldOrigin, PoseStack poseStack, SubmitNodeCollector getter, MultiBlockDefinition structure, int packedLight, float partialTicks) {
        Map<BlockPos, MultiBlockPart> blocks = structure.getBlocks();
        Map<MultiBlockPart, BlockState> stateMap = new IdentityHashMap<>();
        Player player = Minecraft.getInstance().player;

        int time = TimeKeeper.getClientTick() % 80;
        int anim = 5;
        if (time >= 70) {
            time -= 70;
            anim += (int) (Math.sin((time / 10F) * Math.PI) * 5);
        }

        List<BlockPos> invalidBlocks = new ArrayList<>();
        for (BlockPos pos : blocks.keySet()) {
            MultiBlockPart part = blocks.get(pos);
            if (part.validBlocks().isEmpty()) continue;
            BlockPos worldPos = inWorldOrigin.offset(pos);

            if (level.isEmptyBlock(worldPos)) {
                if (player != null && worldPos.distToCenterSqr(player.getEyePosition()) < (4*4)) continue;
                BlockState state = stateMap.computeIfAbsent(part, e -> List.copyOf(e.validBlocks()).get((TimeKeeper.getClientTick() / 40) % e.validBlocks().size()).defaultBlockState());
                if (state.isAir()) continue;
                poseStack.pushPose();
                poseStack.translate(pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.1);
                poseStack.scale(0.8F, 0.8F, 0.8F);
                BlockModelRenderState blockModel = new BlockModelRenderState();
                Minecraft.getInstance().getBlockModelResolver().update(blockModel, state, DISPLAY_CONTEXT);
                blockModel.submit(poseStack, getter, packedLight, OverlayTexture.pack(anim, 10), 0);
                poseStack.popPose();
            } else if (!part.isMatch(level, worldPos)) {
                invalidBlocks.add(pos);
            }
        }

        if (!invalidBlocks.isEmpty()) {
            for (BlockPos pos : invalidBlocks) {
                poseStack.pushPose();
                poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                double invalidAnim = Math.sin((TimeKeeper.getClientTick() + partialTicks) / 25D * 3.141593);
                invalidAnim = Math.max(0.1, Math.abs(invalidAnim));
                Cuboid6 box = new Cuboid6().expand(invalidAnim / 2);
                getter.order(1).submitCustomGeometry(poseStack, outlineType, (pose, buffer) -> {
                    VertexConsumer builder = new TransformingVertexConsumer(new DelegatingVertexConsumer(buffer) {
                        @Override
                        public VertexConsumer addVertex(float x, float y, float z) {
                            return super.addVertex(x, y, z).setLineWidth(4.0F);
                        }
                    }, new Matrix4(pose));
                    RenderUtils.bufferCuboidOutline(builder, box, 1F, 0F, 0F, 1F);
                });
                poseStack.popPose();
            }
        }
    }

}
