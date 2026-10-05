package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Created by brandon3055 on 24/01/2023
 */
public class SighEditHandler {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");
    private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(Registries.GAME_RULE, BrandonsCore.MODID);
    private static final DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SIGN_EDIT = GAME_RULES.register("allow_sign_editing", () -> new GameRule<>(GameRuleCategory.MISC, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, b -> b ? 1 : 0, false, FeatureFlagSet.of()));

    public static void init(IEventBus modBus) {
        LOCK.lock();
        GAME_RULES.register(modBus);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, SighEditHandler::onBlockInteract);
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.isCanceled()) {
            return;
        }

        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || !player.getAbilities().mayBuild || !((ServerLevel) level).getGameRules().get(ALLOW_SIGN_EDIT.get()) || !player.getItemInHand(event.getHand()).isEmpty()) {
            return;
        }

        BlockEntity entity = level.getBlockEntity(event.getHitVec().getBlockPos());
        if (entity instanceof SignBlockEntity signEntity) {
//            signEntity.setEditable(true);
            player.openTextEdit(signEntity, true); //TODO Side
            event.setCanceled(true);
        }
    }
}
