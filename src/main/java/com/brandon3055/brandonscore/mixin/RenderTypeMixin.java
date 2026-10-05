package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.client.shader.BCRenderType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @WrapOperation(
            method = "draw(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms(Lcom/mojang/blaze3d/systems/RenderPass;)V"
            )
    )
    private void bindDefaultUniforms(RenderPass renderPass, Operation<Void> original) {
        original.call(renderPass);
        if ((Object) this instanceof BCRenderType renderType) {
            renderType.bindUniforms(renderPass);
        }
    }
}
