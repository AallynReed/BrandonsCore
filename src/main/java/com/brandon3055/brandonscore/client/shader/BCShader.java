package com.brandon3055.brandonscore.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.FlipFrameEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Created by covers1624 on 1/10/22.
 */
public class BCShader<T extends BCShader<T>> {

    public static final String UNIFORM_BLOCK = "BCUniforms";
    private static final List<BCShader<?>> SHADERS = new ArrayList<>();

    static {
        NeoForge.EVENT_BUS.addListener(FlipFrameEvent.class, BCShader::onFlipFrame);
    }

    private final Identifier location;
    private final VertexFormat format;
    private final List<Consumer<T>> applyCallbacks = new LinkedList<>();
    private final List<BCUniform> uniforms = new ArrayList<>();
    private final List<String> samplers = new ArrayList<>();
    private final List<RenderPipeline> pipelines = new ArrayList<>();
    private Identifier vertexShader;
    private int blockSize;
    private boolean uniformsLocked;
    private @Nullable DynamicUniformStorage<UniformBlock> uniformStorage;

    protected BCUniform modelMatUniform;
    protected BCUniform timeUniform;
    private BCUniform decayUniform;

    public BCShader(Identifier location, VertexFormat format) {
        this.location = location;
        this.format = format;
        this.vertexShader = location;
        modelMatUniform = uniform("ModelMat", BCUniform.Type.MAT4);
        timeUniform = uniform("Time", BCUniform.Type.FLOAT);
        decayUniform = uniform("Decay", BCUniform.Type.FLOAT);
        modelMatUniform.glUniformMatrix4f(new Matrix4f());
        SHADERS.add(this);
    }

    public final void register(IEventBus bus) {
        bus.addListener(this::onRegisterPipelines);
    }

    public final T onShaderApplied(Consumer<T> cons) {
        applyCallbacks.add(cons);
        //noinspection unchecked
        return (T) this;
    }

    public final T withVertexShader(String path) {
        vertexShader = location.withPath(path);
        //noinspection unchecked
        return (T) this;
    }

    public final T withSamplers(String... samplers) {
        this.samplers.addAll(List.of(samplers));
        //noinspection unchecked
        return (T) this;
    }

    public final T withDefaults(Consumer<T> defaults) {
        //noinspection unchecked
        defaults.accept((T) this);
        //noinspection unchecked
        return (T) this;
    }

    // @formatter:off
    public final BCUniform getModelMatUniform() { return Objects.requireNonNull(modelMatUniform, missingUniformMessage("ModelMat"));}
    public final boolean hasModelMatUniform() { return modelMatUniform != null; }
    public final BCUniform getTimeUniform() { return Objects.requireNonNull(timeUniform, missingUniformMessage("Time")); }
    public final boolean hasTimeUniform() { return timeUniform != null; }
    public final BCUniform getDecayUniform() { return Objects.requireNonNull(decayUniform, missingUniformMessage("Decay")); }
    public final boolean hasDecayUniform() { return decayUniform != null; }
    // @formatter:on

    public final BCUniform uniform(String name, BCUniform.Type type) {
        if (uniformsLocked) {
            throw new IllegalStateException("Uniform " + name + " must be declared before the shader is first used.");
        }
        int offset = Mth.roundToward(blockSize, type.alignment());
        BCUniform uniform = new BCUniform(name, type, offset);
        blockSize = offset + type.size();
        uniforms.add(uniform);
        return uniform;
    }

    public final RenderPipeline.Builder pipelineBuilder() {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
                .withVertexShader(vertexShader.withPrefix("core/"))
                .withFragmentShader(location.withPrefix("core/"))
                .withUniform(UNIFORM_BLOCK, UniformType.UNIFORM_BUFFER)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .withVertexFormat(format, VertexFormat.Mode.QUADS);
        samplers.forEach(builder::withSampler);
        return builder;
    }

    public final RenderPipeline pipeline(String name, UnaryOperator<RenderPipeline.Builder> setup) {
        RenderPipeline pipeline = setup.apply(pipelineBuilder().withLocation(location.withPrefix("pipeline/").withSuffix("/" + name))).build();
        pipelines.add(pipeline);
        return pipeline;
    }

    public final BCRenderType renderType(String name, RenderSetup setup) {
        return new BCRenderType(name, setup, this, null);
    }

    private void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        pipelines.forEach(event::registerPipeline);
    }

    byte[] captureUniforms() {
        uniformsLocked = true;
        for (Consumer<T> applyCallback : applyCallbacks) {
            //noinspection unchecked
            applyCallback.accept((T) this);
        }
        ByteBuffer buffer = ByteBuffer.allocate(Mth.roundToward(blockSize, 16)).order(ByteOrder.nativeOrder());
        for (BCUniform uniform : uniforms) {
            uniform.write(buffer);
        }
        return buffer.array();
    }

    GpuBufferSlice uploadUniforms(byte[] data) {
        if (uniformStorage == null) {
            uniformStorage = new DynamicUniformStorage<>(location + " uniforms", data.length, 16);
        }
        return uniformStorage.writeUniform(new UniformBlock(data));
    }

    private static void onFlipFrame(FlipFrameEvent event) {
        for (BCShader<?> shader : SHADERS) {
            if (shader.uniformStorage != null) {
                shader.uniformStorage.endFrame();
            }
        }
    }

    protected final String missingUniformMessage(String name) {
        return "Shader does not have '" + name + "' uniform.";
    }

    private record UniformBlock(byte[] data) implements DynamicUniformStorage.DynamicUniform {

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(data);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UniformBlock other && Arrays.equals(data, other.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }
}
