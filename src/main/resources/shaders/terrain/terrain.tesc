#version 430 core

layout (vertices = 16) out;

const int AB = 2;
const int BC = 3;
const int CD = 0;
const int DA = 1;

void main() {

    if (gl_InvocationID == 0) {
        gl_TessLevelOuter[AB] = 16;
        gl_TessLevelOuter[BC] = 16;
        gl_TessLevelOuter[CD] = 16;
        gl_TessLevelOuter[DA] = 16;

        gl_TessLevelInner[0] = 16;
        gl_TessLevelInner[1] = 16;
    }

    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
}
