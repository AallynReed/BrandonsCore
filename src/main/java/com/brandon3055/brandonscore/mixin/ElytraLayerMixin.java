package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.client.model.EquippedItemModelLayer;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(WingsLayer.class)
public class ElytraLayerMixin {

    private PlayerModel getThis() {
        return (PlayerModel) (Object) this;
    }

    @Inject(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidRenderState state, float yRot, float xRot, CallbackInfo ci) {
        LivingEntity entity = state.getRenderData(EquippedItemModelLayer.ENTITY);
        if (entity != null && ContributorHandler.shouldCancelElytra(entity)) {
            ci.cancel();
        }
    }
}
