#version 330

layout(std140) uniform BCUniforms {
    mat4 ModelMat;
    float Time;
    float Decay;
    bool SimpleLight;
    bool DisableLight;
    bool DisableOverlay;
    ivec2 UV1Override;
    ivec2 UV2Override;
    vec4 BaseColor;
    float Transition;
    float Hue;
};
