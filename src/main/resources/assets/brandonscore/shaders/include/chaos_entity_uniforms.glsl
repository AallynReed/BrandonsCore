#version 330

layout(std140) uniform BCUniforms {
    mat4 ModelMat;
    float Time;
    float Decay;
    float Yaw;
    float Pitch;
    float Alpha;
    bool SimpleLight;
    bool DisableLight;
    bool DisableOverlay;
};
