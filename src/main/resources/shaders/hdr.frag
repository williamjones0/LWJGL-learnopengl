#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D hdrBuffer;
uniform sampler2D bloomBuffer;
uniform float exposure;
uniform float bloomStrength = 0.04f;
uniform bool toneMapping;
uniform int toneMappingType;

vec3 bloom() {
    vec3 hdrColor = texture(hdrBuffer, TexCoords).rgb;
    vec3 bloomColor = texture(bloomBuffer, TexCoords).rgb;
    return mix(hdrColor, bloomColor, bloomStrength);
}

float luminance(vec3 v) {
    return dot(v, vec3(0.2126f, 0.7152f, 0.0722f));
}

vec3 RRTAndODTFit(vec3 x) {
    vec3 a = x * (x + 0.0245786f) - 0.000090537f;
    vec3 b = x * (0.983729f * x + 0.4329510f) + 0.238081f;
    return a / b;
}

vec3 ACES_Fitted(vec3 x) {
    mat3 ACESInputMat = mat3(
        0.59719f, 0.35458f, 0.04823f,
        0.07600f, 0.90834f, 0.01566f,
        0.02840f, 0.13383f, 0.83777f
    );

    mat3 ACESOutputMat = mat3(
        1.60475f, -0.53108f, -0.07367f,
        -0.10208f, 1.10813f, -0.00605f,
        -0.00327f, -0.07276f, 1.07602f
    );

    x = x * ACESInputMat;
    x = RRTAndODTFit(x);
    x = x * ACESOutputMat;

    return clamp(x, 0.0f, 1.0f);
}

vec3 ACES_Approx(vec3 x) {
    x *= 0.6f;
    float a = 2.51f;
    float b = 0.03f;
    float c = 2.43f;
    float d = 0.59f;
    float e = 0.14f;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0f, 1.0f);
}

vec3 Reinhard(vec3 x) {
    return x / (x + vec3(1.0f));
}

vec3 Reinhard_Jodie(vec3 x) {
    float l = luminance(x);
    vec3 tv = x / (1.0f + x);
    return mix(x / (1.0f + l), tv, tv);
}

vec3 Uncharted2_Partial(vec3 x) {
    float A = 0.15f;
    float B = 0.50f;
    float C = 0.10f;
    float D = 0.20f;
    float E = 0.02f;
    float F = 0.30f;
    return ((x * (A * x + C * B) + D * E) / (x * (A * x + B) + D * F)) - E / F;
}

vec3 Uncharted2_Filmic(vec3 x) {
    float exposure_bias = 2.0f;
    vec3 curr = Uncharted2_Partial(x * exposure_bias);

    vec3 whiteScale = 1.0f / Uncharted2_Partial(vec3(11.2f));
    return curr * whiteScale;
}

void main() {
    const float gamma = 2.2;
    vec3 hdrColor = texture(hdrBuffer, TexCoords).rgb;

    if (bloomStrength > 0.0) {
        hdrColor = bloom();
    }

    vec3 mapped;

    switch (toneMappingType) {
        case 0:
            mapped = hdrColor;
            break;

        case 1:
            mapped = vec3(1.0 - exp(-hdrColor * exposure));
            break;

        case 2:
            mapped = ACES_Fitted(hdrColor);
            break;

        case 3:
            mapped = ACES_Approx(hdrColor);
            break;

        case 4:
            mapped = Reinhard(hdrColor);
            break;

        case 5:
            mapped = Reinhard_Jodie(hdrColor);
            break;

        case 6:
            mapped = Uncharted2_Filmic(hdrColor);
            break;

        default:
            mapped = hdrColor;
            break;
    }

    mapped = pow(mapped, vec3(1.0 / gamma));

    if (toneMapping) {
        FragColor = vec4(mapped, 1.0);
    } else {
        FragColor = vec4(hdrColor, 1.0);
    }
}
