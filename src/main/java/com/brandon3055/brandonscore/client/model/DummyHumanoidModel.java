package com.brandon3055.brandonscore.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Created by brandon3055 on 19/11/2022
 */
public class DummyHumanoidModel<T extends HumanoidRenderState> extends HumanoidModel<T> {
    public static final DummyHumanoidModel<?> INSTANCE = new DummyHumanoidModel<>();
    public static final IClientItemExtensions DUMMY_ITEM_RENDER_PROPS = new IClientItemExtensions() {
        @Override
        public @NotNull Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
            return INSTANCE;
        }
    };

    public DummyHumanoidModel() {
        super(createMesh(new CubeDeformation(1), 0).getRoot().bake(64, 64));
        root().visible = false;
    }

    @Override
    public void setupAnim(T p_102866_) {}

    @Override
    public void translateToHand(HumanoidRenderState p_102853_, HumanoidArm p_102854_, PoseStack p_102855_) {}

    @Override
    public ModelPart getArm(HumanoidArm p_102852_) {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    @Override
    public ModelPart getHead() {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }
}
