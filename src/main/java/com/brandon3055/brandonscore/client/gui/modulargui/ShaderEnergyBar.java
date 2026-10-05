package com.brandon3055.brandonscore.client.gui.modulargui;

import codechicken.lib.gui.modular.elements.GuiEnergyBar;
import codechicken.lib.gui.modular.elements.GuiRectangle;
import codechicken.lib.gui.modular.lib.geometry.GuiParent;
import codechicken.lib.math.MathHelper;
import codechicken.lib.util.FormatUtil;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.power.IOInfo;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.BCGuiTextures;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static net.minecraft.ChatFormatting.*;

/**
 * Created by brandon3055 on 13/02/2024
 */
public class ShaderEnergyBar extends GuiEnergyBar {
    private static final RenderPipeline TEX_COL_NO_CULL = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "pipeline/gui_textured_no_cull"))
            .withCull(false)
            .build();

    private Supplier<Boolean> shaderEnabled = () -> true;
    private Supplier<Boolean> disabled = () -> false;

    public ShaderEnergyBar(@NotNull GuiParent<?> parent) {
        super(parent);
    }

    public static BiFunction<Long, Long, List<Component>> opEnergyFormatter(@Nullable IOPStorage storage) {
        return (energy, capacity) -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.operational_potential").withStyle(DARK_AQUA));
            boolean shift = Minecraft.getInstance().hasShiftDown();
            tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.capacity")
                    .withStyle(GOLD)
                    .append(" ")
                    .append(Component.literal(shift ? FormatUtil.addCommas(capacity) : FormatUtil.formatNumber(capacity))
                            .withStyle(GRAY)
                            .append(" ")
                            .append(Component.translatable("mod_gui.brandonscore.energy_bar.op")
                                    .withStyle(GRAY)
                            )
                    )
            );
            tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.stored")
                    .withStyle(GOLD)
                    .append(" ")
                    .append(Component.literal(shift ? FormatUtil.addCommas(energy) : FormatUtil.formatNumber(energy))
                            .withStyle(GRAY)
                    )
                    .append(" ")
                    .append(Component.translatable("mod_gui.brandonscore.energy_bar.op")
                            .withStyle(GRAY)
                    )
                    .append(Component.literal(String.format(" (%.2f%%)", ((double) energy / (double) capacity) * 100D))
                            .withStyle(GRAY)
                    )
            );
            if (storage != null && storage.getIOInfo() != null) {
                IOInfo ioInfo = storage.getIOInfo();
                if (Minecraft.getInstance().hasShiftDown()) {
                    tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.input")
                            .withStyle(GOLD)
                            .append(Component.literal(" +")
                                    .withStyle(GREEN)
                                    .append(Utils.formatNumber(ioInfo.currentInput()))
                            )
                    );
                    tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.output")
                            .withStyle(GOLD)
                            .append(Component.literal(" -")
                                    .withStyle(RED)
                                    .append(Utils.formatNumber(ioInfo.currentOutput()))
                            )
                    );
                } else {
                    long io = ioInfo.currentInput() - ioInfo.currentOutput();
                    tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.io")
                            .withStyle(GOLD)
                            .append(Component.literal((io > 0 ? " +" : " ") + io + " ")
                                    .withStyle(io > 0 ? GREEN : io < 0 ? RED : GRAY)
                                    .append(Component.translatable("mod_gui.brandonscore.energy_bar.op"))
                            )
                    );
                }
            }
            return tooltip;
        };
    }

    public ShaderEnergyBar setShaderEnabled(Supplier<Boolean> shaderEnabled) {
        this.shaderEnabled = shaderEnabled;
        return this;
    }

    public GuiEnergyBar setDisabled(Supplier<Boolean> disabled) {
        this.disabled = disabled;
        return this;
    }

    public ShaderEnergyBar bindOpStorage(@Nullable IOPStorage storage) {
        if (storage == null) {
            setEnergy(0).setCapacity(0);
        } else {
            setEnergy(storage::getOPStored).setCapacity(storage::getMaxOPStored);
        }
        return this;
    }

    public ShaderEnergyBar setItemSupplier(Supplier<ItemStack> stackSupplier) {
        setCapacity(() -> EnergyUtils.isEnergyItem(stackSupplier.get()) ? EnergyUtils.getMaxEnergyStored(stackSupplier.get()) : 0);
        setEnergy(() -> EnergyUtils.isEnergyItem(stackSupplier.get()) ? EnergyUtils.getEnergyStored(stackSupplier.get()) : 0);
        return this;
    }

    @Override
    public void renderBehind(GuiGraphicsExtractor graphics, double mouseX, double mouseY, float partialTicks) {
        boolean horizontal = xSize() > ySize();
        double barLength = horizontal ? xSize() : ySize();
        double barWidth = horizontal ? ySize() : xSize();
        double charge = getCapacity() <= 0 ? 0 : getEnergy() / (double) getCapacity();
        if (Double.isNaN(charge)) charge = 0;
        double draw = charge * barLength;

        double posY = yMin();
        double posX = xMin();



        if (horizontal) {
            double x = posY;
            posY = posX;
            posX = x;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) (barLength + (posY * 2)), 0);
            graphics.pose().rotate((float) (90 * MathHelper.torad));
        }

        double x = posX;
        double y = posY;
        if (disabled.get()) {
            graphics.cc$fill(x, y, x + barWidth, y + barLength, 0xFF000000);
        } else if (!shaderEnabled.get()) {
            TextureAtlasSprite base = BCGuiTextures.get("bars/energy_empty").get();
            TextureAtlasSprite overlay = BCGuiTextures.get("bars/energy_full").get();
            AbstractTexture atlas = mc().getTextureManager().getTexture(BCGuiTextures.ATLAS_TEXTURE);
            graphics.cc$submitCustom(TEX_COL_NO_CULL, TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler()), x, x + barWidth, y, y + barLength, (buffer, pose) -> {
                sliceSprite(buffer, pose, x, y, barWidth, barLength, base);
                sliceSprite(buffer, pose, x, y + barLength - draw, barWidth, draw, overlay);
            });
        } else {
            Rectangle rect = toScreenSpace(xMin(), yMin(), xSize(), ySize());
            float shaderCharge = (float) charge * 1.01F;
            float time = BCClientEventHandler.elapsedTicks / 10F;
            graphics.cc$submitCustom(BCShaders.ENERGY_BAR, TextureSetup.noTexture(), x, x + barWidth, y, y + barLength, (buffer, pose) -> drawShaderRect(buffer, pose, (float) x, (float) y, (float) barWidth, (float) barLength, shaderCharge, time, rect));
        }
        if (horizontal) {
            graphics.pose().popMatrix();
        }
    }

    private void drawShaderRect(VertexConsumer buffer, Matrix3x2f pose, float x, float y, float width, float height, float charge, float time, Rectangle rect) {
        //@formatter:off
        buffer.addVertexWith2DPose(pose, x,          y + height).setUv(charge, time).setUv1(rect.x, rect.y).setUv2(rect.width, rect.height);
        buffer.addVertexWith2DPose(pose, x + width,  y + height).setUv(charge, time).setUv1(rect.x, rect.y).setUv2(rect.width, rect.height);
        buffer.addVertexWith2DPose(pose, x + width,  y         ).setUv(charge, time).setUv1(rect.x, rect.y).setUv2(rect.width, rect.height);
        buffer.addVertexWith2DPose(pose, x,          y         ).setUv(charge, time).setUv1(rect.x, rect.y).setUv2(rect.width, rect.height);
        //@formatter:on
    }

    public void sliceSprite(VertexConsumer buffer, Matrix3x2f pose, double xPos, double yPos, double xSize, double ySize, TextureAtlasSprite sprite) {
        float texU = sprite.getU0();
        float texV = sprite.getV0();
        int texWidth = sprite.contents().width();
        int texHeight = sprite.contents().height();
        float uScale = (sprite.getU1() - texU) / texWidth;
        float vScale = (sprite.getV1() - texV) / texHeight;
        for (double i = 0; i < ySize; i += Math.min(texHeight - 2, ySize - i)) {
            double partSize = Math.min(texHeight, ySize - i);
            bufferRect(buffer, pose, (float) xPos, (float) yPos + (float) ySize - (float) i, (float) xSize, (float) -partSize, sprite.getU0(), sprite.getV0(), (float) xSize * uScale, (float) partSize * vScale);
        }
    }

    private void bufferRect(VertexConsumer buffer, Matrix3x2f pose, float x, float y, float width, float height, float minU, float minV, float tWidth, float tHeight) {
        //@formatter:off
        buffer.addVertexWith2DPose(pose, x,           y + height).setColor(1F, 1F, 1F, 1F).setUv(minU, minV + tHeight);
        buffer.addVertexWith2DPose(pose, x + width,   y + height).setColor(1F, 1F, 1F, 1F).setUv(minU + tWidth, minV + tHeight);
        buffer.addVertexWith2DPose(pose, x + width,   y         ).setColor(1F, 1F, 1F, 1F).setUv(minU + tWidth, minV);
        buffer.addVertexWith2DPose(pose, x,           y         ).setColor(1F, 1F, 1F, 1F).setUv(minU, minV);
        //@formatter:on
    }

    public Rectangle toScreenSpace(double xPos, double yPos, double xSize, double ySize) {
        double yResScale = (double) mc().getWindow().getHeight() / scaledScreenHeight();
        double xResScale = (double) mc().getWindow().getWidth() / scaledScreenWidth();
        double scaledWidth = xSize * xResScale;
        double scaledHeight = ySize * yResScale;
        int x = (int) (xPos * xResScale);
        int y = (int) (mc().getWindow().getHeight() - (yPos * yResScale) - scaledHeight);
        return new Rectangle(x, y, (int) scaledWidth, (int) scaledHeight);
    }

    public record EnergyBar(GuiRectangle container, ShaderEnergyBar bar) {}

}
