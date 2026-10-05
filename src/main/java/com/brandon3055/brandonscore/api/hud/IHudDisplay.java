package com.brandon3055.brandonscore.api.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Implementing this interface will do absolutely nothing. This is the base interface containing the base default methods used in both
 * {@link IHudItem} and {@link IHudBlock}
 * <p>
 * Created by brandon3055 on 19/8/21
 */
public interface IHudDisplay {
    
    default double computeHudWidth(Minecraft mc, List<Component> displayList) {
        double maxWidth = 0;
        for (Component text : displayList) {
            maxWidth = Math.max(maxWidth, mc.font.width(text));
        }
        return maxWidth + 8;
    }

    default double computeHudHeight(Minecraft mc, List<Component> displayList) {
        return (displayList.size() * 10) + 6;
    }

    default void renderHudBackground(GuiGraphicsExtractor render, double width, double height, List<Component> displayList) {
        render.cc$tooltipBackground(0, 0, width, height, 0xF0100010, 0xF0100010, 0x505000FF, 0x5028007f, false);
    }

    default void renderHudContent(GuiGraphicsExtractor render, double width, double height, List<Component> displayList) {
        render.pose().translate(4, 4);
        for (Component text : displayList) {
            render.cc$drawString(Minecraft.getInstance().font, text, 0, 0, 0xFFFFFF, true);
            render.pose().translate(0, 10);
        }
    }
}
