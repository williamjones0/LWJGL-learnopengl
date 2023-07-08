#version 460 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec3 WorldPos;
out vec3 Normal;
out vec2 TexCoords;
out flat uint ModelMeshMaterialID;
out vec4 FragPosLightSpace;
out vec4 FragPosSpotlightSpace;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 lightSpaceMatrix;
uniform mat4 spotlightSpaceMatrix;

struct ModelMeshInstance {
    mat4 World;                 // 64 bytes

    uint MaterialID;            // 4 bytes
    uint _pad0;                 // 4 bytes
    uint _pad1;                 // 4 bytes
    uint _pad2;                 // 4 bytes
};

layout (binding = 0, std430) buffer ModelMeshInstanceBuffer {
    ModelMeshInstance Instances[];
} modelMeshInstanceBuffer;

void main() {
    ModelMeshInstance modelMeshInstance = modelMeshInstanceBuffer.Instances[gl_BaseInstance + gl_InstanceID];
    WorldPos = (modelMeshInstance.World * vec4(aPos, 1.0)).xyz;
    Normal = normalize(inverse(transpose(mat3(modelMeshInstance.World))) * aNormal);
    TexCoords = aTexCoords;
    FragPosLightSpace = lightSpaceMatrix * vec4(WorldPos, 1.0);
    FragPosSpotlightSpace = spotlightSpaceMatrix * vec4(WorldPos, 1.0);

    ModelMeshMaterialID = modelMeshInstance.MaterialID.x;

    gl_Position = projection * view * vec4(WorldPos, 1.0);
}
