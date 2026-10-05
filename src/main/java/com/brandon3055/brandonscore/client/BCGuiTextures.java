package com.brandon3055.brandonscore.client;

import codechicken.lib.gui.modular.SpriteSupplier;
import com.brandon3055.brandonscore.BCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;

import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class BCGuiTextures {

    public static final Identifier ATLAS = Identifier.fromNamespaceAndPath(MODID, "gui");
    public static final Identifier ATLAS_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "textures/atlas/gui.png");

    public static void init(IEventBus modBus) {
        modBus.addListener(BCGuiTextures::registerAtlas);
    }

    private static void registerAtlas(RegisterTextureAtlasesEvent event) {
        event.register(new AtlasManager.AtlasConfig(ATLAS_TEXTURE, ATLAS, false));
    }

    public static SpriteSupplier get(String texture) {
        return getter(() -> texture);
    }

    public static SpriteSupplier getter(Supplier<String> texture) {
        return () -> Minecraft.getInstance().getAtlasManager().get(new SpriteId(ATLAS, Identifier.fromNamespaceAndPath(MODID, "gui/" + texture.get())));
    }

    public static SpriteSupplier getter(String texture) {
        return () -> get(texture).get();
    }

    public static SpriteSupplier getThemed(String location) {
        return get((BCConfig.darkMode ? "dark/" : "light/") + location);
    }

    public static SpriteSupplier themedGetter(String location) {
        return () -> get((BCConfig.darkMode ? "dark/" : "light/") + location).get();
    }
}
