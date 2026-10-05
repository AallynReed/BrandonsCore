package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.lib.ITilePlaceListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base item block for all custom item blocks.
 */
public class ItemBlockBCore extends BlockItem {

    public ItemBlockBCore(Block block, Item.Properties builder) {
        super(block, builder);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);

        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (placed && tile instanceof ITilePlaceListener) {
            ((ITilePlaceListener) tile).onTilePlaced(context, state);
        }

        return placed;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (getBlock() instanceof BlockBCore block) {
            List<Component> tooltip = new ArrayList<>();
            block.appendHoverText(stack, context, tooltip, flag);
            tooltip.forEach(builder);
        }
    }

//    @Override
//    public CompoundTag getShareTag(ItemStack stack) {
//        if (getBlock() instanceof IBCoreBlock && ((IBCoreBlock) getBlock()).overrideShareTag()) {
//            return ((IBCoreBlock) getBlock()).getNBTShareTag(stack);
//        }
//        return super.getShareTag(stack);
//    }
}
