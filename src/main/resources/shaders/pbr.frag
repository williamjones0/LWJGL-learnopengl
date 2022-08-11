#version 330 core
out vec4 FragColor;

struct PointLight {
    vec3 position;
    vec3 color;
};

struct PBRMaterial {
    sampler2D albedo;
    sampler2D normal;
    sampler2D metallic;
    sampler2D roughness;
    sampler2D ao;
};

#define NUM_POINT_LIGHTS 4
#define PI 3.14159265359

in vec2 TexCoords;
in vec3 WorldPos;
in vec3 Normal;

uniform vec3 camPos;

uniform PBRMaterial material;

uniform PointLight pointLights[NUM_POINT_LIGHTS];

vec3 getNormalFromMap() {
    vec3 tangentNormal = texture(material.normal, TexCoords).rgb * 2.0 - 1.0;

    vec3 Q1 = dFdx(WorldPos);
    vec3 Q2 = dFdy(WorldPos);
    vec2 st1 = dFdx(TexCoords);
    vec2 st2 = dFdy(TexCoords);

    vec3 N = normalize(Normal);
    vec3 T = normalize(Q1 * st2.t - Q2 * st1.t);
    vec3 B = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);

    return normalize(TBN * tangentNormal);
}

float DistributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;

    float num  = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return num / denom;
}

float GeometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r * r) / 8.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return nom / denom;
}

float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = GeometrySchlickGGX(NdotV, roughness);
    float ggx1 = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

void main() {
    vec3 albedo = pow(texture(material.albedo, TexCoords).rgb, vec3(2.2));
    vec3 N = getNormalFromMap();
    float metallic = texture(material.metallic, TexCoords).r;
    float roughness = texture(material.roughness, TexCoords).r;
    float ao = texture(material.ao, TexCoords).r;

    vec3 V = normalize(camPos - WorldPos);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    // Reflectance equation
    vec3 Lo = vec3(0.0);
    for (int i = 0; i < NUM_POINT_LIGHTS; ++i) {
        // Per-light radiance
        vec3 L = normalize(pointLights[i].position - WorldPos);
        vec3 H = normalize(V + L);

        float distance = length(pointLights[i].position - WorldPos);
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance = pointLights[i].color * attenuation;

        // Cook-Torrance BRDF
        float NDF = DistributionGGX(N, H, roughness);
        float G = GeometrySmith(N, V, L, roughness);
        vec3 F = fresnelSchlick(clamp(dot(H, V), 0.0, 1.0), F0);

        vec3 numerator = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.0001;
        vec3 specular = numerator / denominator;

        // kS is equal to Fresnel
        vec3 kS = F;
        // kD is equal to 1 - kS
        vec3 kD = vec3(1.0) - kS;
        // Multiply kD by the inverse metalness so that only non-metals have diffuse lighting
        kD *= 1.0 - metallic;

        // Scale light by NdotL
        float NdotL = max(dot(N, L), 0.0);

        // Add to outgoing radiance Lo
        // Already multiplied the BRDF by kS, so don't need to multiply again
        Lo += (kD * albedo / PI + specular) * radiance * NdotL;
    }

    // Ambient
    vec3 ambient = vec3(0.03) * albedo * ao;

    vec3 color = ambient + Lo;

    FragColor = vec4(color, 1.0);
}
