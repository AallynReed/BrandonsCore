package com.brandon3055.brandonscore.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

public class BCRenderType extends RenderType {

    private final RenderSetup setup;
    private final BCShader<?> shader;
    private final byte @Nullable [] uniforms;
    private @Nullable GpuBufferSlice uploaded;

    BCRenderType(String name, RenderSetup setup, BCShader<?> shader, byte @Nullable [] uniforms) {
        super(name, setup);
        this.setup = setup;
        this.shader = shader;
        this.uniforms = uniforms;
    }

    public BCShader<?> getShader() {
        return shader;
    }

    public BCRenderType withCurrentUniforms() {
        return new BCRenderType(name, setup, shader, shader.captureUniforms());
    }

    public void uploadUniforms() {
        uploaded = shader.uploadUniforms(uniforms != null ? uniforms : shader.captureUniforms());
    }

    public void bindUniforms(RenderPass renderPass) {
        renderPass.setUniform(BCShader.UNIFORM_BLOCK, uploaded);
    }
}
