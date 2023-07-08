#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D hdrBuffer;
uniform sampler2D bloomBuffer;
uniform float exposure;
uniform float bloomStrength = 0.04f;
uniform bool toneMapping;

vec3 bloom() {
    vec3 hdrColor = texture(hdrBuffer, TexCoords).rgb;
    vec3 bloomColor = texture(bloomBuffer, TexCoords).rgb;
    return mix(hdrColor, bloomColor, bloomStrength);
}

void main() {
    const float gamma = 2.2;
    vec3 hdrColor = texture(hdrBuffer, TexCoords).rgb;

    if (bloomStrength > 0.0) {
        hdrColor = bloom();
    }

    // Exposure tone mapping
    vec3 mapped = vec3(1.0) - exp(-hdrColor * exposure);
    // Gamma correction
    mapped = pow(mapped, vec3(1.0 / gamma));

    if (toneMapping) {
        FragColor = vec4(mapped, 1.0);
    } else {
        FragColor = vec4(hdrColor, 1.0);
    }
}
