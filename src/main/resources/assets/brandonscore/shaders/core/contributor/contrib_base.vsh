#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>
#moj_import <dynamictransforms.glsl>
#moj_import <projection.glsl>
#moj_import <sample_lightmap.glsl>
#moj_import <brandonscore:contrib_uniforms.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

out vec3 fPos;
out vec3 vPos;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec4 normal;
out vec3 vNorm;

void main() {
    vPos = Position;
    fPos = (ModelViewMat * ModelMat * vec4(Position, 1.0)).xyz;
    gl_Position = ProjMat * ModelViewMat * ModelMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

    ivec2 uv1 = UV1;
    if (UV1Override.x != -1 && UV1Override.y != -1) {
        uv1 = UV1Override;
    }
    ivec2 uv2 = UV2;
    if (UV2Override.x != -1 && UV2Override.y != -1) {
        uv2 = UV2Override;
    }

    if (DisableLight) {
        vertexColor = Color;
        lightMapColor = vec4(1.0);
    } else if (SimpleLight) {
        vertexColor = Color * sample_lightmap(Sampler2, uv2);
        lightMapColor = vec4(1.0);
    } else {
        vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
        lightMapColor = sample_lightmap(Sampler2, uv2);
    }

    if (DisableOverlay) {
        overlayColor = vec4(1.0);
    } else {
        overlayColor = texelFetch(Sampler1, uv1, 0);
    }

    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * ModelMat * vec4(Normal, 0.0);
    vNorm = Normal;
}
