package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterItem extends FilterBase {
    protected boolean whitelistMode = true;
    protected String tagString = "";
    protected boolean tagMode = false;
    protected ItemStack filterStack = ItemStack.EMPTY;
    protected boolean fuzzyMatch = false;
    protected boolean matchCount = false;

    protected boolean filterBlocks = false;
    protected boolean filterItems = false;

    public boolean dataChanged = false;
    private TagKey<Item> tagCache = null;

    public FilterItem(EntityFilter filter) {
        super(filter);
    }

    private void clearCache() {
        tagCache = null;
    }

    public boolean isWhitelistMode() {
        return whitelistMode;
    }

    public String getTagString() {
        return tagString;
    }

    public boolean isTagMode() {
        return tagMode;
    }

    public ItemStack getFilterStack() {
        return filterStack;
    }

    public boolean isFuzzyMatch() {
        return fuzzyMatch;
    }

    public boolean isMatchCount() {
        return matchCount;
    }

    public boolean isFilterBlocks() {
        return filterBlocks;
    }

    public boolean isFilterItems() {
        return filterItems;
    }

    public void setWhitelistMode(boolean whitelistMode) {
        boolean prev = this.whitelistMode;
        this.whitelistMode = whitelistMode;
        getFilter().nodeModified(this);
        this.whitelistMode = prev;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
        clearCache();
        getFilter().nodeModified(this);
    }

    public void setTagMode(boolean tagMode) {
        this.tagMode = tagMode;
        getFilter().nodeModified(this);
    }

    public void setFilterStack(ItemStack filterStack) {
        this.filterStack = filterStack;
        getFilter().nodeModified(this);
    }

    public void setFuzzyMatch(boolean fuzzyMatch) {
        this.fuzzyMatch = fuzzyMatch;
        getFilter().nodeModified(this);
    }

    public void setMatchCount(boolean matchCount) {
        this.matchCount = matchCount;
        getFilter().nodeModified(this);
    }

    public void cycleItemsBlocks() {
        if (isFilterItems()) {
            setFilterItemsBlocks(false, true);
        } else if (isFilterBlocks()) {
            setFilterItemsBlocks(false, false);
        } else {
            setFilterItemsBlocks(true, false);
        }
    }

    public void setFilterItemsBlocks(boolean filterItems, boolean filterBlocks) {
        boolean prev = this.filterItems;
        boolean prevBlocks = this.filterBlocks;
        this.filterItems = filterItems;
        this.filterBlocks = filterBlocks;
        getFilter().nodeModified(this);
        this.filterItems = prev;
        this.filterBlocks = prevBlocks;
    }

    @Override
    public boolean test(Entity entity) {
        if (!(entity instanceof ItemEntity item)) {
            return !whitelistMode;
        }

        return testItem(item.getItem());
    }

    public boolean testItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return !whitelistMode;
        }

        boolean match = true;
        if (isTagMode()) {
            match = stack.is(getTag());
        } else if (!filterStack.isEmpty()) {
            match = fuzzyMatch ? ItemStack.isSameItem(filterStack, stack) : ItemStack.isSameItemSameComponents(filterStack, stack) && (filterStack.getCount() == stack.getCount() || !matchCount);
        }

        if (filterStack.isEmpty() && !isTagMode()) {
            if (filterBlocks) {
                match = stack.getItem() instanceof BlockItem;
            } else if (filterItems) {
                match = !(stack.getItem() instanceof BlockItem);
            }
        }
        return match == whitelistMode;
    }

    public TagKey<Item> getTag() {
        if (tagCache == null) {
            tagCache = ItemTags.create(Identifier.parse(tagString));
        }
        return tagCache;
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM_FILTER;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compound = super.serializeNBT(provider);
        compound.putBoolean("whitelist_mode", whitelistMode);
        compound.putString("tag_string", tagString);
        compound.putBoolean("tag_mode", tagMode);
        compound.put("filter_stack", ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), filterStack).getOrThrow());
        compound.putBoolean("fuzzy_match", fuzzyMatch);
        compound.putBoolean("match_count", matchCount);
        compound.putBoolean("filter_blocks", filterBlocks);
        compound.putBoolean("filter_items", filterItems);
        return compound;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compound) {
        super.deserializeNBT(provider, compound);
        whitelistMode = compound.getBooleanOr("whitelist_mode", false);
        tagString = compound.getStringOr("tag_string", "");
        tagMode = compound.getBooleanOr("tag_mode", false);
        filterStack = ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), compound.getCompoundOrEmpty("filter_stack")).result().orElse(ItemStack.EMPTY);
        fuzzyMatch = compound.getBooleanOr("fuzzy_match", false);
        matchCount = compound.getBooleanOr("match_count", false);
        filterBlocks = compound.getBooleanOr("filter_blocks", false);
        filterItems = compound.getBooleanOr("filter_items", false);
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistMode);
        output.writeString(tagString);
        output.writeBoolean(tagMode);
        output.writeItemStack(filterStack);
        output.writeBoolean(fuzzyMatch);
        output.writeBoolean(matchCount);
        output.writeBoolean(filterBlocks);
        output.writeBoolean(filterItems);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistMode = input.readBoolean();
        tagString = input.readString();
        tagMode = input.readBoolean();
        filterStack = input.readItemStack();
        fuzzyMatch = input.readBoolean();
        matchCount = input.readBoolean();
        filterBlocks = input.readBoolean();
        filterItems = input.readBoolean();
        dataChanged = true;
    }
}