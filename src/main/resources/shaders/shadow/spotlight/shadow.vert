#version 460 core

#extension GL_ARB_shader_viewport_layer_array : enable
#extension GL_AMD_vertex_shader_layer : enable
#extension GL_NV_viewport_array2 : enable
#define HAS_VERTEX_LAYERED_RENDERING (GL_ARB_shader_viewport_layer_array || GL_AMD_vertex_shader_layer || GL_NV_viewport_array2)

layout (location = 0) in vec3 aPos;

uniform mat4 lightSpaceMatrix;
uniform int textureLayer;

//out vec4 FragPos;

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
    #if HAS_VERTEX_LAYERED_RENDERING
        gl_Layer = textureLayer;
    #endif
    ModelMeshInstance modelMeshInstance = modelMeshInstanceBuffer.Instances[gl_BaseInstance + gl_InstanceID];
    vec3 worldPos = (modelMeshInstance.World * vec4(aPos, 1.0)).xyz;
    gl_Position = lightSpaceMatrix * vec4(worldPos, 1.0);
//    FragPos = vec4(worldPos, 1.0);
}
