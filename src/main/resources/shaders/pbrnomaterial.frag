#version 330 core
out vec4 FragColor;

struct PointLight {
    vec3 position;
    vec3 color;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    vec3 color;
    float cutoff;
    float outerCutoff;

    bool enabled;
};

struct DirLight {
    vec3 direction;
    vec3 color;
};

struct PBRValues {
    vec3 albedo;
    float metallic;
    float roughness;
    float ao;
};

#define NUM_POINT_LIGHTS 4
#define NUM_SPOT_LIGHTS 1
#define PI 3.14159265359

in vec2 TexCoords;
in vec3 WorldPos;
in vec3 Normal;

uniform vec3 camPos;

uniform samplerCube irradianceMap;
uniform samplerCube prefilterMap;
uniform sampler2D brdfLUT;

uniform PBRValues values;

uniform PointLight pointLights[NUM_POINT_LIGHTS];
uniform SpotLight spotLights[NUM_SPOT_LIGHTS];
uniform DirLight dirLight;

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

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

void main() {
    // Material properties
    vec3 albedo = values.albedo;
    float metallic = values.metallic;
    float roughness = values.roughness;
    float ao = values.ao;

    // Lighting data input
    vec3 N = Normal;
    vec3 V = normalize(camPos - WorldPos);
    vec3 R = reflect(-V, N);

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

    // Spot lights
    for (int i = 0; i < NUM_SPOT_LIGHTS; ++i) {
        if (spotLights[i].enabled) {
            // Per-light radiance
            vec3 L = normalize(spotLights[i].position - WorldPos);
            vec3 H = normalize(V + L);

            float distance = length(spotLights[i].position - WorldPos);
            float attenuation = 1.0 / (distance * distance);

            float theta = dot(L, normalize(-spotLights[i].direction));
            float epsilon = spotLights[i].cutoff - spotLights[i].outerCutoff;
            float intensity = clamp((theta - spotLights[i].outerCutoff) / epsilon, 0.0, 1.0);

            vec3 radiance = spotLights[i].color * attenuation * intensity;

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
    }

    // Directional lighting
    for (int i = 0; i < 1; i++) {
        vec3 L = normalize(dirLight.direction);
        vec3 H = normalize(V + L);

        float NDF = DistributionGGX(N, H, roughness);
        float G = GeometrySmith(N, V, L, roughness);
        vec3 F = fresnelSchlick(clamp(dot(H, V), 0.0, 1.0), F0);

        vec3 numerator = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.0001;
        vec3 specular = numerator / denominator;

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;

        float NdotL = max(dot(N, L), 0.0);

        Lo += (kD * albedo / PI + specular) * dirLight.color * NdotL;
    }

    // Ambient lighting
    vec3 kS = fresnelSchlickRoughness(max(dot(N, V), 0.0), F0, roughness);
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - metallic;

    vec3 irradiance = texture(irradianceMap, N).rgb;
    vec3 diffuse = irradiance * albedo;

    // Sample prefilter map and BRDF LUT and combine to get the IBL specular part
    const float MAX_REFLECTION_LOD = 4.0;
    vec3 prefilteredColor = textureLod(prefilterMap, R, roughness * MAX_REFLECTION_LOD).rgb;
    vec2 environmentBRDF = texture(brdfLUT, vec2(max(dot(N, V), 0.0), roughness)).rg;
    vec3 specular = prefilteredColor * (kS * environmentBRDF.x + environmentBRDF.y);

    vec3 ambient = (kD * diffuse + specular) * ao;

    vec3 color = ambient + Lo;

    FragColor = vec4(color, 1.0);
}
