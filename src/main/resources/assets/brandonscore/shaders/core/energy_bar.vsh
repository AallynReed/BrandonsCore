#version 150

#moj_import <dynamictransforms.glsl>
#moj_import <projection.glsl>
#moj_import <globals.glsl>

in vec3 Position;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;

flat out float time;
flat out float charge;
flat out ivec2 ePos;
flat out ivec2 eSize;
flat out ivec2 screenSize;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    charge = UV0.x;
    time = UV0.y;
    ePos = UV1;
    eSize = UV2;
    screenSize = ivec2(ScreenSize);
}
