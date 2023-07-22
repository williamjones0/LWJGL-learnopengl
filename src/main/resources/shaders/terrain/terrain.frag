#version 460 core

uniform int lod;

layout (location = 0) out vec4 outputColor;

void main() {
    switch (lod) {
        case 0:
            outputColor = vec4(1.0, 0.01, 0.01, 1.0);
            break;
        case 1:
            outputColor = vec4(0.01, 1.0, 0.01, 1.0);
            break;
        case 2:
            outputColor = vec4(0.01, 0.01, 1.0, 1.0);
            break;
        case 3:
            outputColor = vec4(1.0, 1.0, 0.01, 1.0);
            break;
        case 4:
            outputColor = vec4(1.0, 0.01, 1.0, 1.0);
            break;
        case 5:
            outputColor = vec4(0.01, 1.0, 1.0, 1.0);
            break;
        case 6:
            outputColor = vec4(1.0, 1.0, 1.0, 1.0);
            break;
        default:
            outputColor = vec4(0.01, 0.01, 0.01, 1.0);
            break;
    }
//    outputColor = vec4(0.01, 1.0, 0.01, 1.0);
}
