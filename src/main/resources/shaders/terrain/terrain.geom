#version 430 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in vec2 mapCoord_GS[];

out vec2 mapCoord_FS;

uniform mat4 view;
uniform mat4 projection;

void main() {

    mat4 viewProjection = projection * view;

    for (int i = 0; i < gl_in.length(); ++i) {
        vec4 position = gl_in[i].gl_Position;
        gl_Position = viewProjection * position;
        mapCoord_FS = mapCoord_GS[i];
        EmitVertex();
    }

    EndPrimitive();
}
