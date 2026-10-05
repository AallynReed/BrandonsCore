#version 150

#moj_import <fog.glsl>
#moj_import <dynamictransforms.glsl>
#moj_import <brandonscore:math.glsl>
#moj_import <brandonscore:chaos.glsl>
#moj_import <brandonscore:chaos_entity_uniforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler3;

in vec3 fPos;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;
in vec2 posMod;

out vec4 fragColor;

void main() {
    if (Decay > 0 && (snoise(vec3(texCoord0, 0.0), 128) + 1) / 2 < Decay) {
        discard;
    }
    vec4 color = chaos(Sampler0, Time, Yaw, Pitch, Alpha, fPos, posMod);

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
