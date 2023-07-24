#version 460 core

layout (location = 0) out vec4 outputColor;

in vec2 mapCoord_FS;

uniform int lod;

uniform sampler2D normalMap;

const vec3 lightDirection = vec3(0.1, -1.0, 0.1);
const float intensity = 1.2;

float diffuse(vec3 direction, vec3 normal, float intensity) {
    return max(dot(-direction, normal) * intensity, 0.01);
}

void main() {
    vec3 normal = texture(normalMap, mapCoord_FS).rgb;

    float diffuse = diffuse(lightDirection, normal, intensity);

    outputColor = vec4(0.1, 1.0, 0.1, 1.0) * diffuse;
}
