#version 330 core

#define NR_POINT_LIGHTS 1
#define NR_SPOT_LIGHTS 1

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in vec3 aTangent;

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

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform vec3 viewPos;

uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLights[NR_SPOT_LIGHTS];

out vec3 FragPos;

out vec3 TangentFragPos;
out vec3 TangentViewPos;
out vec2 TexCoords;

out vec3[] TangentDirLightPos;
out vec3[NR_POINT_LIGHTS] TangentPointLightPos;
out vec3[NR_SPOT_LIGHTS] TangentSpotLightPos;

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    TexCoords = aTexCoords;
    FragPos = vec3(model * vec4(aPos, 1.0));

    mat3 normalMatrix = transpose(inverse(mat3(model)));
    vec3 T = normalize(normalMatrix * aTangent);
    vec3 N = normalize(normalMatrix * aNormal);
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);

    mat3 TBN = transpose(mat3(T, B, N));

    TangentViewPos = TBN * viewPos;
    TangentFragPos = TBN * FragPos;

    // Direction light
    TangentDirLightPos[0] = TBN * vec3(dirLight.direction);

    // Point lights
    for (int i = 0; i < NR_POINT_LIGHTS; i++) {
        TangentPointLightPos[i] = TBN * pointLights[i].position;
    }

    // Spot lights
    for (int i = 0; i < NR_SPOT_LIGHTS; i++) {
        TangentSpotLightPos[i] = TBN * spotLights[i].position;
    }
}
