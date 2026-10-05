package com.brandon3055.brandonscore.init;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.*;
import com.brandon3055.brandonscore.client.hud.HudManager;
import com.brandon3055.brandonscore.client.model.EquippedItemModelLayer;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import com.google.common.reflect.TypeToken;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Created by brandon3055 on 15/11/2022
 */
public class BCClient {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(BCClient::clientSetupEvent);
        modBus.addListener(BCClient::onAddRenderLayers);
        modBus.addListener(BCClient::onRegisterRenderStateModifiers);

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> ContributorHandler.onClientLogin(event.getPlayer()));
        ProcessHandlerClient.init();
        HudManager.init(modBus);
        BCShaders.init(modBus);
        BCProfiler.init();
        DLRSCache.init();
        BCGuiTextures.init(modBus);
    }

    private static void clientSetupEvent(FMLClientSetupEvent event) {
        BCClientEventHandler.init();
    }

    private static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {}, (entity, state) -> state.setRenderData(EquippedItemModelLayer.ENTITY, entity));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void onAddRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            LivingEntityRenderer renderer = event.getPlayerRenderer(skin);
            assert renderer != null;
            renderer.addLayer(new EquippedItemModelLayer(renderer, skin == PlayerModelType.SLIM)); //TODO Does this work?
        }

        for (EntityRenderer r : Minecraft.getInstance().getEntityRenderDispatcher().renderers.values()) {
            if (r instanceof LivingEntityRenderer<?, ?, ?> renderer && renderer.getModel() instanceof HumanoidModel) {
                renderer.addLayer(new EquippedItemModelLayer(renderer, false));
            }
        }
    }
}
