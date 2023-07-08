#version 460 core

#extension GL_ARB_bindless_texture : enable

in vec3 WorldPos;
in vec3 Normal;
in vec2 TexCoords;

in flat uint ModelMeshMaterialID;
in flat float ModelMeshEmissionStrength;

in vec4 FragPosLightSpace;
in vec4 FragPosSpotlightSpace;

layout (location = 0) out vec4 FragColor;

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

struct GPUMaterial {
    vec4 albedoColor;
    vec4 emissiveColor;

    sampler2D albedo;
    sampler2D normal;

    sampler2D metallic;
    sampler2D roughness;

    sampler2D metallicRoughness;
    sampler2D ao;

    sampler2D emissive;
    float metallicFactor;
    float roughnessFactor;

    uint uses_albedo_map;
    uint uses_normal_map;
    uint uses_metallic_map;
    uint uses_roughness_map;

    uint uses_metallicRoughness_map;
    uint uses_ao_map;
    uint uses_emissive_map;
    uint _pad0;
};

layout (binding = 1, std430) buffer MaterialBuffer {
    GPUMaterial Materials[];
} materialBuffer;

struct Settings {
    bool specularOcclusion;
    bool horizonSpecularOcclusion;
    bool pointShadows;
    float pointShadowBias;
    float shadowMinBias;
    float shadowMaxBias;
};

#define NUM_POINT_LIGHTS 8
#define NUM_SPOT_LIGHTS 4
#define PI 3.14159265359
#define EPSILON 0.0001

uniform vec3 camPos;

uniform samplerCube irradianceMap;
uniform samplerCube prefilterMap;
uniform sampler2D brdfLUT;

uniform PointLight pointLights[NUM_POINT_LIGHTS];
uniform SpotLight spotLights[NUM_SPOT_LIGHTS];
uniform DirLight dirLight;

uniform Settings settings;

uniform samplerCubeArray pointShadowMaps;
uniform sampler2DArray spotShadowMaps;
uniform sampler2D directionalShadowMap;
uniform float farPlane;

vec3 getNormalFromMap(GPUMaterial material) {
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
    float NdotH = max(dot(N, H), EPSILON);

    float denom = (NdotH * NdotH * (a2 - 1.0) + 1.0);

    return a2 / (PI * denom * denom);
}

float GeometrySchlickGGX(float NdotV, float roughness) {
    float k = ((roughness + 1.0) * (roughness + 1.0)) / 8.0;

    return NdotV / (NdotV * (1.0 - k) + k);
}

float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), EPSILON);
    float NdotL = max(dot(N, L), EPSILON);

    return GeometrySchlickGGX(NdotL, roughness) * GeometrySchlickGGX(NdotV, roughness);
}

//vec3 fresnelSchlick(float VdotH, vec3 F0) {
//    return F0 + (1.0 - F0) * pow(clamp(1.0 - VdotH, 0.0, 1.0), 5.0);
//}

vec3 fresnelSchlick(float VdotH, vec3 F0) {
    return F0 + (1.0 - F0) * pow(2, (-5.55473 * VdotH - 6.98316) * VdotH);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

float computeSpecularAO(float NdotV, float ao, float roughness) {
    return clamp(pow(NdotV + ao, exp2(-16.0 * roughness - 1.0)) - 1.0 + ao, 0.0, 1.0);
}

float pointShadowCalculation(vec3 fragPos, int i) {
    vec3 fragToLight = fragPos - pointLights[i].position;
    float currentDepth = length(fragToLight);

    // Early exit
    if (currentDepth > farPlane) {
        return 0.0;
    }

    float closestDepth = texture(pointShadowMaps, vec4(fragToLight, i)).r;
    closestDepth *= farPlane;  // Transform to [0, farPlane]

    float bias = settings.pointShadowBias;
    float shadow = currentDepth - bias > closestDepth ? 1.0 : 0.0;

    return shadow;
}

float spotShadowCalculation(vec4 fragPosLightSpace, int i) {
    // Perspective division - transforms to [-1, 1]
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    float closestDepth = texture(spotShadowMaps, vec3(projCoords.xy, i)).r;

    float currentDepth = projCoords.z;
//    return currentDepth + closestDepth * 0.000001;
//    return closestDepth;

    if (currentDepth > 1.0) {
        return 0.0;
    }

    float bias = max(settings.shadowMaxBias * (1.0 - dot(Normal, spotLights[i].direction)), settings.shadowMinBias);
//    float bias = 0.0;

    float shadow = currentDepth - bias > closestDepth ? 1.0 : 0.0;
    return shadow;
}

float orthoShadowCalculation(vec4 fragPosLightSpace) {
    // Perspective division - transforms to [-1, 1]
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    float closestDepth = texture(directionalShadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
//    return currentDepth + (closestDepth + settings.shadowMinBias + settings.shadowMaxBias) * 0.00000001;

    if (currentDepth > 1.0) {
        return 0.0;
    }

    float bias = max(settings.shadowMaxBias * (1.0 - dot(Normal, dirLight.direction)), settings.shadowMinBias);

    float shadow = currentDepth - bias > closestDepth ? 1.0 : 0.0;
    return shadow;
}

void main() {
    GPUMaterial material = materialBuffer.Materials[ModelMeshMaterialID];

    // Material properties
    vec3 albedo;
    vec3 N;
    float metallic;
    float roughness;
    vec3 emissive;
    float ao;

    // Per-mesh material modifiers
    float emissionStrength = ModelMeshEmissionStrength;

    if (material.uses_albedo_map > 0.5) {
        albedo = pow(texture(material.albedo, TexCoords).rgb, vec3(2.2));
    } else {
        albedo = material.albedoColor.xyz;
    }

    if (material.uses_normal_map > 0.5) {
        N = getNormalFromMap(material);
    } else {
        N = normalize(Normal);
    }

    if (material.uses_metallicRoughness_map > 0.5) {
        metallic = texture(material.metallicRoughness, TexCoords).b + texture(material.metallic, TexCoords).b * EPSILON;
        roughness = texture(material.metallicRoughness, TexCoords).g + texture(material.roughness, TexCoords).b * EPSILON;
    } else {
        if (material.uses_metallic_map > 0.5) {
            metallic = texture(material.metallic, TexCoords).r;
        } else {
            metallic = material.metallicFactor;
        }

        if (material.uses_roughness_map > 0.5) {
            roughness = texture(material.roughness, TexCoords).r;
        } else {
            roughness = material.roughnessFactor;
        }
    }

    if (material.uses_ao_map > 0.5) {
        ao = texture(material.ao, TexCoords).r;
    } else {
        ao = 1.0;
    }

    if (material.uses_emissive_map > 0.5) {
        emissive = texture(material.emissive, TexCoords).rgb;
    } else {
        emissive = material.emissiveColor.xyz;
    }

    // Per-mesh material modifiers
    emissive *= emissionStrength;

    // Lighting data input
    vec3 V = normalize(camPos - WorldPos);
    vec3 R = reflect(-V, N);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    // Reflectance equation
    vec4 TEST_FRAG_COLOR = vec4(0.01234);

    vec3 Lo = vec3(0.0);
    for (int i = 0; i < NUM_POINT_LIGHTS; ++i) {
        // Check if fragment is in shadow
        if (settings.pointShadows && pointLights[i].enabled) {
            float shadow = pointShadowCalculation(WorldPos, i);
//            TEST_FRAG_COLOR = vec4(shadow, shadow, shadow, 1.0);

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
        vec3 F = fresnelSchlick(clamp(dot(H, V), EPSILON, 1.0), F0);

        vec3 numerator = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + EPSILON;
        vec3 specular = numerator / denominator;

        // kS is equal to Fresnel
        vec3 kS = F;
        // kD is equal to 1 - kS
        vec3 kD = vec3(1.0) - kS;
        // Multiply kD by the inverse metalness so that only non-metals have diffuse lighting
        kD *= 1.0 - metallic;

        // Scale light by NdotL
        float NdotL = max(dot(N, L), EPSILON);

        // Add to outgoing radiance Lo
        // Already multiplied the BRDF by kS, so don't need to multiply again
        Lo += (kD * albedo / PI + specular) * radiance * NdotL;
    }

    // Spot lights
    for (int i = 0; i < NUM_SPOT_LIGHTS; ++i) {
        if (spotLights[i].enabled) {
            // Check if fragment is in shadow
            float spotShadow = spotShadowCalculation(FragPosSpotlightSpace, i);
//            TEST_FRAG_COLOR = vec4(spotShadow, spotShadow, spotShadow, 1.0);

            if (spotShadow == 0.001) {  // Avoid compiler optimisation
                continue;
            }

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
            vec3 F = fresnelSchlick(clamp(dot(H, V), EPSILON, 1.0), F0);

            vec3 numerator = NDF * G * F;
            float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + EPSILON;
            vec3 specular = numerator / denominator;

            // kS is equal to Fresnel
            vec3 kS = F;
            // kD is equal to 1 - kS
            vec3 kD = vec3(1.0) - kS;
            // Multiply kD by the inverse metalness so that only non-metals have diffuse lighting
            kD *= 1.0 - metallic;

            // Scale light by NdotL
            float NdotL = max(dot(N, L), EPSILON);

            // Add to outgoing radiance Lo
            // Already multiplied the BRDF by kS, so don't need to multiply again
            Lo += (kD * albedo / PI + specular) * radiance * NdotL;
        }
    }

    // Directional lighting
    for (int i = 0; i < 1; i++) {
        // Check if fragment is in shadow
        float shadow = orthoShadowCalculation(FragPosLightSpace);
//        TEST_FRAG_COLOR = vec4(shadow, shadow, shadow, 1.0);

        if (shadow > 0.0) {
            continue;
        }

        vec3 L = normalize(dirLight.direction);
        vec3 H = normalize(V + L);

        float NDF = DistributionGGX(N, H, roughness);
        float G = GeometrySmith(N, V, L, roughness);
        vec3 F = fresnelSchlick(clamp(dot(H, V), EPSILON, 1.0), F0);

        vec3 numerator = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + EPSILON;
        vec3 specular = numerator / denominator;

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;

        float NdotL = max(dot(N, L), EPSILON);

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

    // Temporary fix for infinitely bright/dark fragments causing bloom to break
    if (isinf(FragColor.x)) FragColor.x = 100.0;
    if (isinf(FragColor.y)) FragColor.y = 100.0;
    if (isinf(FragColor.z)) FragColor.z = 100.0;

    if (isnan(FragColor.x)) FragColor.x = EPSILON;
    if (isnan(FragColor.y)) FragColor.y = EPSILON;
    if (isnan(FragColor.z)) FragColor.z = EPSILON;

//    if (TEST_FRAG_COLOR.x != 0.01234) {
//        FragColor = TEST_FRAG_COLOR;
//    } else {
//        FragColor = vec4(color, 1.0);
//    }

//    if (emissionStrength > 0.0) {
//        FragColor = vec4(emissionStrength, emissionStrength, emissionStrength, 1.0);
//    }
}
