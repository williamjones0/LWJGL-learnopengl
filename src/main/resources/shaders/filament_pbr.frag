//#version 330 core
//
//#define PI 3.1415926535897932384626433832795
//#define MEDIUMP_FLT_MAX 65504.0
//#define saturateMediump(x) min(x, MEDIUMP_FLT_MAX)
//
//struct PBRMaterial {
//    sampler2D albedo;
//    sampler2D normal;
//    sampler2D metallic;
//    sampler2D roughness;
//    sampler2D ao;
//};
//
//struct DirLight {
//    vec3 direction;
//
//    vec3 ambient;
//    vec3 diffuse;
//    vec3 specular;
//};
//
//struct PointLight {
//    vec3 position;
//
//    vec3 color;
//    float intensity;
//    float invRadius;
//};
//
//in vec3 FragPos;
//
//uniform vec3 viewPos;
//uniform PBRMaterial material;
//
//float D_GGX(float roughness, float NoH, const vec3 n, const vec3 h);
//float V_SmithGGXCorrelatedFast(float NoV, float NoL, float roughness);
//vec3 F_Schlick(float u, vec3 f0);
//
//float D_GGX(float roughness, float NoH, const vec3 n, const vec3 h) {
//    vec3 NxH = cross(n, h);
//    float a = NoH * roughness;
//    float k = roughness / (dot(NxH, NxH) + a * a);
//    float d = k * k * (1.0 / PI);
//    return saturateMediump(d);
//}
//
//vec3 F_Schlick(float u, vec3 f0) {
//    float f = pow(1.0 - u, 5.0);
//    return f + f0 * (1.0 - f);
//}
//
//float V_SmithGGXCorrelatedFast(float NoV, float NoL, float roughness) {
//    float a = roughness;
//    float GGXV = NoL * (NoV * (1.0 - a) + a);
//    float GGXL = NoV * (NoL * (1.0 - a) + a);
//    return 0.5 / (GGXV + GGXL);
//}
//
//float Fd_Lambert() {
//    return 1.0 / PI;
//}
//
//void BRDF(vec3 v, vec3 l) {
//    vec3 h = normalize(v + l);
//
//    float NoV = abs(dot(n, v)) + 1e-5;
//    float NoL = clamp(dot(n, l), 0.0, 1.0);
//    float NoH = clamp(dot(n, h), 0.0, 1.0);
//    float LoH = clamp(dot(l, h), 0.0, 1.0);
//
//    // perceptually linear roughness to roughness
//    float roughness = perceptualRoughness * perceptualRoughness;
//
//    float D = D_GGX(NoH, a);
//    vec3  F = F_Schlick(LoH, f0);
//    float V = V_SmithGGXCorrelated(NoV, NoL, roughness);
//
//    // specular BRDF
//    vec3 Fr = (D * V) * F;
//
//    // diffuse BRDF
//    vec3 Fd = diffuseColor * Fd_Lambert();
//
//    // apply lighting...
//}
//
//float getSquareFalloffAttenuation(vec3 posToLight, float invRadius) {
//    float distanceSquare = dot(posToLight, posToLight);
//    float factor = distanceSquare * invRadius * invRadius;
//    float smoothFactor = max(1.0 - factor * factor, 0.0);
//    return (smoothFactor * smoothFactor) / max(distanceSquare, 1e-4);
//}
//
//float getSpotAngleAttenuation(vec3 l, vec3 lightDir, float innerAngle, float outerAngle) {
//    // the scale and offset computations can be done CPU-side
//    float cosOuter = cos(outerAngle);
//    float spotScale = 1.0 / max(cos(innerAngle) - cosOuter, 1e-4);
//    float spotOffset = -cosOuter * spotScale;
//
//    float cd = dot(normalize(-lightDir), l);
//    float attenuation = clamp(cd * spotScale + spotOffset, 0.0, 1.0);
//    return attenuation * attenuation;
//}
//
//vec3 evaluatePunctualLight(PointLight light, vec3 posToLight, float NoL) {
//    vec3 l = normalize(posToLight);
//    float NoL = clamp(dot(n, l), 0.0, 1.0);
//
//    float attenuation;
//    attenuation  = getSquareFalloffAttenuation(posToLight, lightInvRadius);
////    attenuation *= getSpotAngleAttenuation(l, lightDir, innerAngle, outerAngle);
//
//    vec3 luminance = (BRDF(v, l) * light.intensity * attenuation * NoL) * light.color;
//    return luminance;
//}
//
//void main() {
//    vec3 v = normalize(viewPos - FragPos);
//    vec3 n = texture(material.normal, TexCoords).rgb * 2.0 - 1.0;
//    vec3 h = normalize(l + v);
//
//    vec3 diffuseColor = (1.0 - material.metallic) * material.albedo.rgb;
//
//    float reflectance = 0.5;
//    vec3 f0 = 0.16 * reflectance * reflectance * (1.0 - metallic) + baseColor * metallic;
//
////    // Directional lights
////    for (int i = 0; i < NUM_DIR_LIGHTS; i++) {
////        DirLight light = dirLights[i];
////        vec3 l = normalize(light.direction);
////        float NoL = clamp(dot(n, l), 0.0, 1.0);
////
////        float illuminance = light.intensity * NoL;
////        vec3 luminance = BSDF(v, l) * illuminance;
////    }
//
//    // Point lights
//    for (int i = 0; i < NUM_POINT_LIGHTS; i++) {
//        PointLight light = pointLights[i];
//        vec3 posToLight = light.position - viewPos;
//        float NoL = clamp(dot(n, l), 0.0, 1.0);
//
//        evaluatePunctualLight(light, posToLight, NoL);
//    }
//
//    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
//}
