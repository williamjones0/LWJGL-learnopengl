#version 430 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in vec2 mapCoord_GS[];

in vec3 WorldPos_GS[];
in vec4 FragPosLightSpace_GS[];
in vec4 FragPosSpotlightSpace_GS[];

out vec2 mapCoord_FS;

out vec3 WorldPos;
out vec4 FragPosLightSpace;
out vec4 FragPosSpotlightSpace;

uniform mat4 view;
uniform mat4 projection;

void main() {

    mat4 viewProjection = projection * view;

    for (int i = 0; i < gl_in.length(); ++i) {
        vec4 position = gl_in[i].gl_Position;
        gl_Position = viewProjection * position;
        mapCoord_FS = mapCoord_GS[i];

        WorldPos = WorldPos_GS[i];
        FragPosLightSpace = FragPosLightSpace_GS[i];
        FragPosSpotlightSpace = FragPosSpotlightSpace_GS[i];

        EmitVertex();
    }

    EndPrimitive();
}
