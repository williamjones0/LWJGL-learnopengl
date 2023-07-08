#version 460 core

layout (location = 0) in vec3 aPos;

struct ModelMeshInstance {
    mat4 World;
    uint materialID;
    uint _pad0;
    uint _pad1;
    uint _pad2;
};

layout (binding = 0, std430) buffer ModelMeshInstanceBuffer {
    ModelMeshInstance Instances[];
} modelMeshInstanceBuffer;

void main() {
    ModelMeshInstance modelMeshInstance = modelMeshInstanceBuffer.Instances[gl_BaseInstance + gl_InstanceID];
    vec3 worldPos = (modelMeshInstance.World * vec4(aPos, 1.0)).xyz;
    gl_Position = vec4(worldPos, 1.0);
}
