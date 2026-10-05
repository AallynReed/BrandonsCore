package com.brandon3055.brandonscore.client.shader;

import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 1/10/22.
 */
public final class ChaosEntityShader extends BCShader<ChaosEntityShader> {

    private BCUniform yawUniform;
    private BCUniform pitchUniform;
    private BCUniform alphaUniform;
    private BCUniform simpleLightUniform;
    private BCUniform disableLightUniform;
    private BCUniform disableOverlayUniform;

    public ChaosEntityShader(String path, VertexFormat format) {
        this(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, path), format);
    }

    public ChaosEntityShader(Identifier location, VertexFormat format) {
        super(location, format);
        yawUniform = uniform("Yaw", BCUniform.Type.FLOAT);
        pitchUniform = uniform("Pitch", BCUniform.Type.FLOAT);
        alphaUniform = uniform("Alpha", BCUniform.Type.FLOAT);
        simpleLightUniform = uniform("SimpleLight", BCUniform.Type.BOOL);
        disableLightUniform = uniform("DisableLight", BCUniform.Type.BOOL);
        disableOverlayUniform = uniform("DisableOverlay", BCUniform.Type.BOOL);
    }

    // @formatter:off
    public final BCUniform getYawUniform() { return requireNonNull(yawUniform, missingUniformMessage("Yaw")); }
    public final boolean hasYawUniform() { return yawUniform != null; }
    public final BCUniform getPitchUniform() { return requireNonNull(pitchUniform, missingUniformMessage("Pitch")); }
    public final boolean hasPitchUniform() { return pitchUniform != null; }
    public final BCUniform getAlphaUniform() { return requireNonNull(alphaUniform, missingUniformMessage("Alpha")); }
    public final boolean hasAlphaUniform() { return alphaUniform != null; }
    public final BCUniform getSimpleLightUniform() { return requireNonNull(simpleLightUniform, missingUniformMessage("SimpleLight")); }
    public final boolean hasSimpleLightUniform() { return simpleLightUniform != null; }
    public final BCUniform getDisableLightUniform() { return requireNonNull(disableLightUniform, missingUniformMessage("DisableLight")); }
    public final boolean hasDisableLightUniform() { return disableLightUniform != null; }
    public final BCUniform getDisableOverlayUniform() { return requireNonNull(disableOverlayUniform, missingUniformMessage("DisableOverlay")); }
    public final boolean hasDisableOverlayUniform() { return disableOverlayUniform != null; }
    // @formatter:on
}
