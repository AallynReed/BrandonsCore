#version 150

#moj_import <fog.glsl>
#moj_import <dynamictransforms.glsl>
#moj_import <brandonscore:math.glsl>
#moj_import <brandonscore:contrib_uniforms.glsl>

uniform sampler2D Sampler0;

in vec3 fPos;
in vec3 vPos;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 vNorm;

out vec4 fragColor;

void main() {
    vec4 texCol = texture(Sampler0, texCoord0) * ColorModulator;
    if (texCol.a <= 0 || texCol.a > Transition) {
        discard;
    }

    if (texCol.a > Transition * 0.85) {
        fragColor = vec4(0, 0, 0, 1);
        return;
    }

    vec3 coord = vec3(texCoord0, 0.0);
    vec4 baseColour = BaseColor;
    float value = BaseColor.w;

    float noise = snoise(vec3(coord.x, coord.y, Time * 0.05), 16);
    value = 1 + noise;
    value *= texCol.a * 2;

    vec3 rgb = hsv2rgb(vec3(Hue + (abs(noise) * 0.125), 1, 1));
    vec4 color = vec4(rgb, min(texCol.a * 2, 1));

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}