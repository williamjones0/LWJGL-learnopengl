#version 330 core

out vec4 FragColor;

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
    sampler2D normalMap;
};

struct DirLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    float cutoff;
    float outerCutoff;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;

    int enabled;
};

#define NR_POINT_LIGHTS 1
#define NR_SPOT_LIGHTS 1

in vec3 FragPos;

in vec3 TangentFragPos;
in vec3 TangentViewPos;
in vec2 TexCoords;

in vec3[] TangentDirLightPos;
in vec3[NR_POINT_LIGHTS] TangentPointLightPos;
in vec3[NR_SPOT_LIGHTS] TangentSpotLightPos;

uniform vec3 viewPos;
uniform Material material;
uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLights[NR_SPOT_LIGHTS];

uniform int isNormalMapping;

vec3 calculateDirLight(DirLight light, vec3 tangentDirLightPos, vec3 normal, vec3 viewDir);
vec3 calculatePointLight(PointLight light, vec3 tangentPointLightPos, vec3 normal, vec3 fragPos, vec3 viewDir);
vec3 calculateSpotLight(SpotLight light, vec3 tangentSpotLightPos, vec3 normal, vec3 fragPos, vec3 viewDir);

void main() {
    vec3 norm = texture(material.normalMap, TexCoords).rgb;
    norm = normalize(norm * 2.0 - 1.0);

    if (isNormalMapping > 0) {
        norm = vec3(0.0, 0.0, 1.0);
    }

    vec3 viewDir = normalize(TangentViewPos - TangentFragPos);

    vec3 result = calculateDirLight(dirLight, TangentDirLightPos[0], norm, viewDir);

    for (int i = 0; i < NR_POINT_LIGHTS; i++)
        result += calculatePointLight(pointLights[i], TangentPointLightPos[i], norm, FragPos, viewDir);

    for (int i = 0; i < NR_SPOT_LIGHTS; i++)
        if (spotLights[i].enabled == 1)
            result += calculateSpotLight(spotLights[i], TangentPointLightPos[i], norm, FragPos, viewDir);

    FragColor = vec4(result, 1.0);

    // Gamma correction
    float gamma = 2.2;
    FragColor.rgb = pow(FragColor.rgb, vec3(1.0 / gamma));
}

vec3 calculateDirLight(DirLight light, vec3 tangentLightPos, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(tangentLightPos - TangentFragPos);

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);

    // Specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess);

    // Calculate result
    vec3 ambient  = light.ambient  * texture(material.diffuse, TexCoords).rgb;
    vec3 diffuse  = light.diffuse  * diff * texture(material.diffuse, TexCoords).rgb;
    vec3 specular = light.specular * spec * texture(material.specular, TexCoords).rgb;

    return ambient + diffuse + specular;
}

vec3 calculatePointLight(PointLight light, vec3 tangentLightPos, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir = normalize(tangentLightPos - TangentFragPos);

    // Get diffuse color
    vec3 color = texture(material.diffuse, TexCoords).rgb;

    // Diffuse
    float diff = max(dot(lightDir, normal), 0.0);

    // Specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess);

//    // Attenuation
//    float distance = length(light.position - fragPos);
//    float attenuation = 1.0 / distance;

    // Calculate result
    vec3 ambient  = light.ambient * color;
    vec3 diffuse  = light.diffuse * diff * color;
    vec3 specular = light.specular * spec * texture(material.specular, TexCoords).rgb;

    return (ambient + diffuse + specular);
}

vec3 calculateSpotLight(SpotLight light, vec3 tangentLightPos, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir = normalize(tangentLightPos - TangentFragPos);

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);

    // Specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), material.shininess);

    // Attenuation
    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / distance;

    // Intensity
    float theta     = dot(lightDir, normalize(-light.direction));
    float epsilon   = light.cutoff - light.outerCutoff;
    float intensity = clamp((theta - light.outerCutoff) / epsilon, 0.0, 1.0);

    // Calculate result
    vec3 ambient  = light.ambient  * texture(material.diffuse, TexCoords).rgb;
    vec3 diffuse  = light.diffuse  * diff * texture(material.diffuse, TexCoords).rgb;
    vec3 specular = light.specular * spec * texture(material.specular, TexCoords).rgb;

    ambient  *= attenuation;
    diffuse  *= attenuation * intensity;
    specular *= attenuation * intensity;

    return (ambient + diffuse + specular);
}
