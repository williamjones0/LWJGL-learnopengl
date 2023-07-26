#version 460 core

layout (location = 0) in vec2 position0;

out vec2 mapCoord_TC;

uniform mat4 localMatrix;
uniform mat4 worldMatrix;
uniform vec3 camPos;
uniform float scaleY;
uniform int lod;
uniform vec2 index;
uniform float gap;
uniform vec2 location;
uniform int lod_morph_area[8];
uniform sampler2D heightMap;

float morphLatitude(vec2 position) {
    vec2 frac = position - location;

    if (index == vec2(0, 0)) {
        float morph = frac.x - frac.y;
        if (morph > 0) {
            return morph;
        }
    }

    if (index == vec2(1, 0)) {
        float morph = gap - frac.x - frac.y;
        if (morph > 0) {
            return morph;
        }
    }

    if (index == vec2(0, 1)) {
        float morph = frac.x + frac.y - gap;
        if (morph > 0) {
            return -morph;
        }
    }

    if (index == vec2(1, 1)) {
        float morph = frac.y - frac.x;
        if (morph > 0) {
            return -morph;
        }
    }

    return 0;
}

float morphLongitude(vec2 position) {
    vec2 frac = position - location;

    if (index == vec2(0, 0)) {
        float morph = frac.y - frac.x;
        if (morph > 0) {
            return -morph;
        }
    }

    if (index == vec2(1, 0)) {
        float morph = frac.y - (gap - frac.x);
        if (morph > 0) {
            return morph;
        }
    }

    if (index == vec2(0, 1)) {
        float morph = gap - frac.y - frac.x;
        if (morph > 0) {
            return -morph;
        }
    }

    if (index == vec2(1, 1)) {
        float morph = frac.x - frac.y;
        if (morph > 0) {
            return morph;
        }
    }

    return 0;
}

vec2 morph(vec2 localPosition, int morph_area) {
    vec2 morphing = vec2(0, 0);

    vec2 fixPointLatitude = vec2(0, 0);
    vec2 fixPointLongitude = vec2(0, 0);
    float distLatitude;
    float distLongitude;

    if (index == vec2(0, 0)) {
        fixPointLatitude = location + vec2(gap, 0);
        fixPointLongitude = location + vec2(0, gap);
    } else if (index == vec2(1, 0)) {
        fixPointLatitude = location;
        fixPointLongitude = location + vec2(gap, gap);
    } else if (index == vec2(0, 1)) {
        fixPointLatitude = location + vec2(gap, gap);
        fixPointLongitude = location;
    } else if (index == vec2(1, 1)) {
        fixPointLatitude = location + vec2(0, gap);
        fixPointLongitude = location + vec2(gap, 0);
    }

    float planarFactor = 0;
    if (camPos.y > abs(scaleY)) {
        planarFactor = 1;
    } else {
        planarFactor = camPos.y / abs(scaleY);
    }

    distLatitude = length(camPos - (worldMatrix * vec4(fixPointLatitude.x, planarFactor, fixPointLatitude.y, 1)).xyz);
    distLongitude = length(camPos - (worldMatrix * vec4(fixPointLongitude.x, planarFactor, fixPointLongitude.y, 1)).xyz);

    if (distLatitude > morph_area) {
        morphing.x = morphLatitude(localPosition.xy);
    }

    if (distLongitude > morph_area) {
        morphing.y = morphLongitude(localPosition.xy);
    }

    return morphing;
}

void main() {
    vec2 localPosition = (localMatrix * vec4(position0.x, 0, position0.y, 1)).xz;

    if (lod > 0) {
        localPosition += morph(localPosition, lod_morph_area[lod - 1]);
    }

    float height = texture(heightMap, localPosition).r;

    mapCoord_TC = localPosition;
    gl_Position = worldMatrix * vec4(localPosition.x, height, localPosition.y, 1);
}
