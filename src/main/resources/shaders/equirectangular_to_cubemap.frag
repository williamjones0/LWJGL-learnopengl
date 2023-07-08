#version 330 core
out vec4 FragColor;
in vec3 WorldPos;

uniform sampler2D equirectangularMap;

const vec2 invAtan = vec2(0.1591, 0.3183);  // (reciprocal of 2pi, reciprocal of pi)
vec2 SampleSphericalMap(vec3 v) {
    vec2 uv = vec2(atan(v.z, v.x), asin(v.y));  // Convert from cartesian to spherical coordinates
    uv *= invAtan;  // Map to [-0.5, 0.5] range
    uv += 0.5;  // Map to [0, 1] range
    return uv;
}

void main() {
    vec2 uv = SampleSphericalMap(normalize(WorldPos));
    vec3 color = texture(equirectangularMap, uv).rgb;

    FragColor = vec4(color, 1.0);
}
