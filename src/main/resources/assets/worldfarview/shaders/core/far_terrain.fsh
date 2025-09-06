#version 150

#moj_import <fog.glsl>

in float vertexDistance;
in vec2 uv;
in vec4 vertexColor;
in vec4 normal;
in vec3 worldPos;

uniform sampler2D Sampler0;

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec4 ColorModulator;

uniform vec3 SunDirection;

uniform float CustomFogStart;
uniform float CustomFogEnd;
uniform float EnableCustomFog;

uniform float EnableCrossfade;
uniform float CrossfadeStart;
uniform float CrossfadeEnd;

out vec4 fragColor;

float calculateCrossfade(float distance) {
    if (EnableCrossfade < 0.5) {
        return 1.0;
    }

    return smoothstep(CrossfadeStart, CrossfadeEnd, distance);
}

vec4 applyCustomFog(vec4 color, float distance) {
    if (EnableCustomFog < 0.5) {
        return linear_fog(color, distance, FogStart, FogEnd, FogColor);
    }

    float fogFactor = smoothstep(CustomFogStart, CustomFogEnd, distance);
    return mix(color, FogColor, fogFactor);
}

void main() {
    vec4 texColor = texture(Sampler0, uv);
    vec3 norm = normalize(normal.xyz);

    float sunDot = dot(norm, normalize(SunDirection));
    float sunlight = max(sunDot, 0.0);

    if (SunDirection.y <= 0.0) {
        sunlight *= max(0.0, 1.0 + SunDirection.y * 2.0);
    }

    vec3 moonDir = normalize(-SunDirection);
    float moonDot = dot(norm, moonDir);
    float moonlight = max(moonDot, 0.0) * 0.15;

    float ambient = 0.35;

    float skyLight = max(0.0, norm.y) * 0.2;

    float totalLight = clamp(ambient + sunlight * 0.8 + skyLight, 0.0, 1.0);

    vec3 moonTint = vec3(0.7, 0.8, 1.0);
    vec3 baseColor = (texColor.rgb * vertexColor.rgb) * totalLight;
    baseColor = mix(baseColor, baseColor * moonTint, moonlight * 0.5);

    vec4 color = vec4(baseColor, texColor.a * vertexColor.a) * ColorModulator;

    if (texColor.a == 0.0) {
        discard;
    }

    float crossfadeAlpha = calculateCrossfade(vertexDistance);
    color.a *= crossfadeAlpha;

    fragColor = applyCustomFog(color, vertexDistance);
}