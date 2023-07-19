#version 330 core
out vec4 FragColor;
in vec3 WorldPos;

uniform samplerCube environmentMap;
uniform bool fastIrradiance;

const float PI = 3.14159265359;

float VanDerCorput(uint bits) {
    bits = (bits << 16u) | (bits >> 16u);
    bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
    bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
    bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
    bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);
    return float(bits) * 2.3283064365386963e-10; // / 0x100000000
}

vec2 Hammersley(uint i, uint N) {
    return vec2(float(i) / float(N), VanDerCorput(i));
}

vec3 sample_cosine_hemisphere (vec3 N, vec2 Xi) {
    float phi = 2.0 * PI * Xi.x;
    Xi.y = Xi.y * 2.0 - 1.0;

    vec3 sphere = vec3(0.0);
    sphere.xy = vec2(cos(phi), sin(phi)) * sqrt(1.0 - Xi.y * Xi.y);
    sphere.z = Xi.y;

    return normalize(N + sphere);
}

vec3 get_irradiance(vec3 N) {
    int SAMPLE_COUNT = 128;
    vec3 irradiance = vec3(0.0);
    for (int i = 0; i < SAMPLE_COUNT; i++) {
        vec2 Xi = Hammersley(uint(i), uint(SAMPLE_COUNT));
        vec3 direction = sample_cosine_hemisphere(N, Xi);
        irradiance += texture(environmentMap, direction).rgb;
    }
    return irradiance / float(SAMPLE_COUNT);
}

void main() {
    // The world vector acts as the normal of a tangent surface
    // from the origin, aligned to WorldPos. Given this normal, calculate all
    // incoming radiance of the environment. The result of this radiance
    // is the radiance of light coming from -Normal direction, which is what
    // we use in the PBR shader to sample irradiance.
    vec3 N = normalize(WorldPos);

    if (fastIrradiance) {
        FragColor = vec4(get_irradiance(N), 1.0);
        return;
    }

    vec3 irradiance = vec3(0.0);

    // tangent space calculation from origin point
    vec3 up    = vec3(0.0, 1.0, 0.0);
    vec3 right = normalize(cross(up, N));
    up         = normalize(cross(N, right));

    float sampleDelta = 0.1;
    float nrSamples = 0.0;
    for (float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta) {
        for (float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta) {
            // spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi), sin(theta) * sin(phi), cos(theta));
            // tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * N;

            irradiance += texture(environmentMap, sampleVec).rgb * cos(theta) * sin(theta);
            nrSamples++;
        }
    }
    irradiance = PI * irradiance * (1.0 / float(nrSamples));

    FragColor = vec4(irradiance, 1.0);
}
