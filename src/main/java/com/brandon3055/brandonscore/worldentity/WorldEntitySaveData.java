package com.brandon3055.brandonscore.worldentity;

import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 16/12/20
 */
public class WorldEntitySaveData extends SavedData {
    public static final String FILE_ID = "brandonscore_world_entity";
    public static final SavedDataType<WorldEntitySaveData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "world_entity"), WorldEntitySaveData::new, CompoundTag.CODEC.xmap(WorldEntitySaveData::load, WorldEntitySaveData::save));
    private List<WorldEntity> entities = new ArrayList<>();
    private Runnable saveCallback;

    public void updateEntities(List<WorldEntity> entities) {
        this.entities.clear();
        if (entities != null){
            this.entities.addAll(entities);
        }
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    public List<WorldEntity> getEntities() {
        return entities;
    }

    public static WorldEntitySaveData load(CompoundTag nbt) {
        WorldEntitySaveData data = new WorldEntitySaveData();
        ListTag list = nbt.getListOrEmpty("entities");
        for (Tag inbt : list) {
            WorldEntity entity = WorldEntity.readWorldEntity((CompoundTag) inbt);
            if (entity != null) {
                data.entities.add(entity);
            }
        }
        return data;
    }

    private CompoundTag save() {
        saveCallback.run();
        CompoundTag compound = new CompoundTag();
        ListTag list = new ListTag();
        for (WorldEntity entity : entities) {
            CompoundTag entityTag = new CompoundTag();
            entity.write(entityTag);
            list.add(entityTag);
        }
        compound.put("entities", list);
        return compound;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
