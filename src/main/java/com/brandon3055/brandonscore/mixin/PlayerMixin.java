package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.ElytraEnabledItem;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(Player.class)
public class PlayerMixin {

    private Player getThis() {
        return (Player) (Object) this;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(
            method = "tryToStartFallFlying",
            at = @At("HEAD"),
            cancellable = true
    )
    public void tryToStartFallFlying(CallbackInfoReturnable<Boolean> cir) {
        if (getThis().onGround() || getThis().isFallFlying() || getThis().isInWater() || getThis().hasEffect(MobEffects.LEVITATION)) return;
        ItemStack itemStack = getThis().getItemBySlot(EquipmentSlot.CHEST);
        if (itemStack.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(itemStack, getThis())) {
            getThis().startFallFlying();
            cir.setReturnValue(true);
        }
        if (BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e-> e.getItem() instanceof ElytraEnabledItem item && item.canElytraFlyBC(e, getThis()), getThis());
            if (!stack.isEmpty()) {
                getThis().startFallFlying();
                cir.setReturnValue(true);
            }
        }
    }

}
