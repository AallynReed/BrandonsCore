package com.brandon3055.brandonscore.client.shader;

import codechicken.lib.math.MathHelper;
import codechicken.lib.util.ClientUtils;
import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Created by brandon3055 on 15/05/2022
 */
public class BCShaders {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final ChaosEntityShader CHAOS_ENTITY_SHADER = new ChaosEntityShader("chaos_entity", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler0", "Sampler1", "Sampler2")
            .onShaderApplied(e -> {
                Player player = Minecraft.getInstance().player;
                e.getTimeUniform().glUniform1f((float) ClientUtils.getRenderTime());
                e.getYawUniform().glUniform1f((float) (player.getYRot() * MathHelper.torad));
                e.getPitchUniform().glUniform1f((float) -(player.getXRot() * MathHelper.torad));
            });

    public static final ContribShader CONTRIB_BASE_SHADER = new ContribShader("contributor/contrib_base", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler0", "Sampler1", "Sampler2")
            .withDefaults(e -> {
                e.getDisableLightUniform().glUniform1b(false);
                e.getDisableOverlayUniform().glUniform1b(false);
                e.getUv1OverrideUniform().glUniform2i(0, 0);
                e.getUv2OverrideUniform().glUniform2i(0, 0);
            });
    public static final ContribShader WINGS_WEB_SHADER = new ContribShader("contributor/wings_web", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler1", "Sampler2")
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader WINGS_BONE_SHADER = new ContribShader("contributor/wings_bone", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler1", "Sampler2")
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final ContribShader VET_BADGE_SHADER = new ContribShader("contributor/vet_badge", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler0", "Sampler1", "Sampler2")
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final ContribShader BADGE_OUTLINE_SHADER = new ContribShader("contributor/badge_outline", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler0", "Sampler1", "Sampler2")
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader BADGE_CORE_SHADER = new ContribShader("contributor/patreon_core", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler0", "Sampler1", "Sampler2")
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader BADGE_FOIL_SHADER = new ContribShader("contributor/badge_foil", DefaultVertexFormat.ENTITY)
            .withSamplers("Sampler0", "Sampler1", "Sampler2")
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 40)));

    public static final VertexFormat ENERGY_BAR_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV1", VertexFormatElement.UV1)
            .add("UV2", VertexFormatElement.UV2)
            .build();
    public static final RenderPipeline ENERGY_BAR = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "pipeline/energy_bar"))
            .withVertexShader(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "core/energy_bar"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "core/energy_bar"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(ENERGY_BAR_FORMAT, VertexFormat.Mode.QUADS)
            .build();

    public static final RenderPipeline.Snippet POS_COLOUR_TEX_ALPHA0 = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "core/position_color_tex_alpha0"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "core/position_color_tex_alpha0"))
            .withSampler("Sampler0")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .buildSnippet();

    public static void init(IEventBus modBus) {
        LOCK.lock();
        CONTRIB_BASE_SHADER.register(modBus);
        WINGS_WEB_SHADER.register(modBus);
        WINGS_BONE_SHADER.register(modBus);
        VET_BADGE_SHADER.register(modBus);
        CHAOS_ENTITY_SHADER.register(modBus);
        BADGE_OUTLINE_SHADER.register(modBus);
        BADGE_CORE_SHADER.register(modBus);
        BADGE_FOIL_SHADER.register(modBus);

        modBus.addListener(BCShaders::onRegisterPipelines);
    }

    private static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ENERGY_BAR);
    }

}
