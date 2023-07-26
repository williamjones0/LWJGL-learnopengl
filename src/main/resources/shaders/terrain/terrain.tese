#version 430 core

layout (quads, fractional_odd_spacing, cw) in;

in vec2 mapCoord_TE[];

out vec2 mapCoord_GS;

out vec3 WorldPos_GS;
out vec4 FragPosLightSpace_GS;
out vec4 FragPosSpotlightSpace_GS;

uniform sampler2D heightMap;
uniform float scaleY;

uniform mat4 lightSpaceMatrix;
uniform mat4 spotlightSpaceMatrix;

void main() {
    float u = gl_TessCoord.x;
    float v = gl_TessCoord.y;

    // world position
    vec4 position =
    ((1 - u) * (1 - v) * gl_in[12].gl_Position +
    u * (1 - v) * gl_in[0].gl_Position +
    u * v * gl_in[3].gl_Position +
    (1 - u) * v * gl_in[15].gl_Position);

    vec2 mapCoord =
    ((1 - u) * (1 - v) * mapCoord_TE[12] +
    u * (1 - v) * mapCoord_TE[0] +
    u * v * mapCoord_TE[3] +
    (1 - u) * v * mapCoord_TE[15]);

    float height = texture(heightMap, mapCoord).r;
    height *= scaleY;

    position.y = height;

    mapCoord_GS = mapCoord;

    WorldPos_GS = position.xyz;

    FragPosLightSpace_GS = lightSpaceMatrix * vec4(WorldPos_GS, 1.0);
    FragPosSpotlightSpace_GS = spotlightSpaceMatrix * vec4(WorldPos_GS, 1.0);

    gl_Position = position;
}
