#version 460 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 18) out;

layout (std140, binding = 0) uniform ShadowMatrices {
    mat4 shadowMatrices[6];
};

uniform int cubemapLayer;

out vec4 FragPos;

void main() {
    for (int face = 0; face < 6; ++face) {
        gl_Layer = cubemapLayer * 6 + face;  // Controls which layer of the framebuffer is rendered to
        for (int i = 0; i < 3; ++i) {
            FragPos = gl_in[i].gl_Position;
            gl_Position = shadowMatrices[face] * FragPos;
            EmitVertex();
        }
        EndPrimitive();
    }
}
