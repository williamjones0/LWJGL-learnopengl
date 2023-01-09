#version 400 core
out vec4 FragColor;

struct PointLight {
    vec3 position;
    vec3 color;
    float intensity;
    bool enabled;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    vec3 color;
    float intensity;
    float cutoff;
    float outerCutoff;

    bool enabled;
};

struct DirLight {
    vec3 direction;
    vec3 color;
};

struct PBRMaterial {
    sampler2D albedo;
    sampler2D normal;
    sampler2D metallic;
    sampler2D roughness;
    sampler2D metallicRoughness;
    sampler2D ao;
    sampler2D emissive;

    vec3 albedoColor;
    float metallicFactor;
    float roughnessFactor;
    vec3 emissiveColor;

    bool uses_albedo_map;
    bool uses_normal_map;
    bool uses_metallic_map;
    bool uses_roughness_map;
    bool uses_metallicRoughness_map;
    bool uses_ao_map;
    bool uses_emissive_map;
};

struct Settings {
    bool specularOcclusion;
    bool horizonSpecularOcclusion;
    bool pointShadows;
    float pointShadowBias;
};

#define NUM_POINT_LIGHTS 8
#define NUM_SPOT_LIGHTS 4
#define PI 3.14159265359

in vec2 TexCoords;
in vec3 WorldPos;
in vec3 Normal;

uniform vec3 camPos;

uniform samplerCube irradianceMap;
uniform samplerCube prefilterMap;
uniform sampler2D brdfLUT;

uniform PBRMaterial material;

uniform PointLight pointLights[NUM_POINT_LIGHTS];
uniform SpotLight spotLights[NUM_SPOT_LIGHTS];
uniform DirLight dirLight;

uniform Settings settings;

uniform samplerCubeArray shadowMaps;
uniform float farPlane;

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
    float NdotH = max(dot(N, H), 0.0);
    float a = NdotH * roughness;

    float k = roughness / (1.0 - NdotH * NdotH + a * a);

    return k * k * (1.0 / PI);
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
    float a2 = roughness * roughness;
    float GGXV = NdotL * sqrt(NdotV * NdotV * (1.0 - a2) + a2);
    float GGXL = NdotV * sqrt(NdotL * NdotL * (1.0 - a2) + a2);

    return 0.5 / (GGXV + GGXL);
}

float GeometrySmithFast(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float a = roughness;

    float GGXV = NdotL * (NdotV * (1.0 - a) + a);
    float GGXL = NdotV * (NdotL * (1.0 - a) + a);

    return 0.5 / (GGXV + GGXL);
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

float Fd_Lambert() {
    return 1.0 / PI;
}

float F_Schlick(float u, float f0, float f90) {
    return f0 + (f90 - f0) * pow(1.0 - u, 5.0);
}

float Fd_Burley(float NoV, float NoL, float LoH, float roughness) {
    float f90 = 0.5 + 2.0 * roughness * LoH * LoH;
    float lightScatter = F_Schlick(NoL, 1.0, f90);
    float viewScatter = F_Schlick(NoV, 1.0, f90);
    return lightScatter * viewScatter * (1.0 / PI);
}

float computeSpecularAO(float NdotV, float ao, float roughness) {
    return clamp(pow(NdotV + ao, exp2(-16.0 * roughness - 1.0)) - 1.0 + ao, 0.0, 1.0);
}

float shadowCalculation(vec3 fragPos, int i) {
    vec3 fragToLight = fragPos - pointLights[i].position;
    float currentDepth = length(fragToLight);

    // Early exit
    if (currentDepth > farPlane) {
        return 0.0;
    }

    float closestDepth = texture(shadowMaps, vec4(fragToLight, i)).r;
    closestDepth *= farPlane;  // Transform to [0, farPlane]

    float bias = settings.pointShadowBias;
    float shadow = currentDepth - bias > closestDepth ? 1.0 : 0.0;

    return shadow;
}

void main() {
    // Material properties
    vec3 albedo;
    vec3 N;
    float metallic;
    float roughness;
    vec3 emissive;
    float ao;

    if (material.uses_albedo_map) {
        albedo = pow(texture(material.albedo, TexCoords).rgb, vec3(2.2));
    } else {
        albedo = material.albedoColor;
    }

    if (material.uses_normal_map) {
        N = getNormalFromMap();
    } else {
        N = normalize(Normal);
    }

    if (material.uses_metallicRoughness_map) {
        metallic = texture(material.metallicRoughness, TexCoords).b + texture(material.metallic, TexCoords).b * 0.00001;
        roughness = texture(material.metallicRoughness, TexCoords).g + texture(material.roughness, TexCoords).b * 0.00001;
    } else {
        if (material.uses_metallic_map) {
            metallic = texture(material.metallic, TexCoords).r;
        } else {
            metallic = material.metallicFactor;
        }

        if (material.uses_roughness_map) {
            roughness = texture(material.roughness, TexCoords).r;
        } else {
            roughness = material.roughnessFactor;
        }
    }

    if (material.uses_ao_map) {
        ao = texture(material.ao, TexCoords).r;
    } else {
        ao = 1.0;
    }

    if (material.uses_emissive_map) {
        emissive = texture(material.emissive, TexCoords).rgb;
    } else {
        emissive = material.emissiveColor;
    }

    // Lighting data input
    vec3 V = normalize(camPos - WorldPos);
    vec3 R = reflect(-V, N);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    // Reflectance equation
    vec3 Lo = vec3(0.0);
    for (int i = 0; i < NUM_POINT_LIGHTS; ++i) {
        // Check if fragment is in shadow
        if (settings.pointShadows && pointLights[i].enabled) {
            float shadow = shadowCalculation(WorldPos, i);
            if (shadow > 0.0) {
                continue;
            }
        }

        // Per-light radiance
        vec3 L = normalize(pointLights[i].position - WorldPos);
        vec3 H = normalize(V + L);

        float distance = length(pointLights[i].position - WorldPos);
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance = pointLights[i].color * pointLights[i].intensity * attenuation;

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
        vec3 diffuseColor = kD * albedo;
//        vec3 Fd = diffuseColor * Fd_Lambert();
        vec3 Fd = diffuseColor * Fd_Burley(max(dot(N, V), 0.0), NdotL, max(dot(L, H), 0.0), roughness);

        Lo += (Fd + specular) * radiance * NdotL;
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

            vec3 radiance = spotLights[i].color * spotLights[i].intensity * attenuation * intensity;

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
            vec3 diffuseColor = kD * albedo;
//            vec3 Fd = diffuseColor * Fd_Lambert();
            vec3 Fd = diffuseColor * Fd_Burley(max(dot(N, V), 0.0), NdotL, max(dot(L, H), 0.0), roughness);

            Lo += (Fd + specular) * radiance * NdotL;
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

        // Add to outgoing radiance Lo
        // Already multiplied the BRDF by kS, so don't need to multiply again
        vec3 diffuseColor = kD * albedo;
//        vec3 Fd = diffuseColor * Fd_Lambert();
        vec3 Fd = diffuseColor * Fd_Burley(max(dot(N, V), 0.0), NdotL, max(dot(L, H), 0.0), roughness);

        Lo += (Fd + specular) * dirLight.color * NdotL;
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

    if (settings.specularOcclusion) {
        prefilteredColor *= computeSpecularAO(max(dot(N, V), 0.0), ao, roughness);
    }

    if (settings.horizonSpecularOcclusion) {
        float horizon = min(1.0 + dot(R, N), 1.0);
        prefilteredColor *= horizon * horizon;
    }

    vec2 environmentBRDF = texture(brdfLUT, vec2(max(dot(N, V), 0.0), roughness)).rg;
    vec3 specular = prefilteredColor * (kS * environmentBRDF.x + environmentBRDF.y);

    vec3 ambient = (kD * diffuse + specular) * ao;
    ambient += emissive;

    vec3 color = ambient + Lo;

    FragColor = vec4(color, 1.0);
}
