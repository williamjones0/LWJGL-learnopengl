#version 460 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec3 WorldPos;
out vec3 Normal;
out vec2 TexCoords;
out flat uint ModelMeshMaterialID;
out vec4 FragPosLightSpace;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 lightSpaceMatrix;

struct ModelMeshInstance {
    mat4 World;
    uint MaterialID;
    uint _pad0;
    uint _pad1;
    uint _pad2;
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

    ModelMeshMaterialID = modelMeshInstance.MaterialID.x;

    gl_Position = projection * view * vec4(WorldPos, 1.0);
}
