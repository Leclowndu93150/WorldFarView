#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV;
in vec4 Color;
in vec3 Normal;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int FogShape;

out float vertexDistance;
out vec2 uv;
out vec4 vertexColor;
out vec4 normal;
out vec3 worldPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    uv = UV;
    vertexColor = Color;
    vertexDistance = fog_distance(ModelViewMat, Position, FogShape);
    normal = vec4(Normal, 0.0);
    worldPos = Position;
}
