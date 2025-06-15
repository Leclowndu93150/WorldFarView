#version 150

#moj_import <fog.glsl>

in float vertexDistance;
in vec4 vertexColor;
in vec4 normal;

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec4 ColorModulator;

uniform vec3 SunDirection;

out vec4 fragColor;

void main() {
    vec3 norm = normalize(normal.xyz);

    // --- Lighting calculations ---
    float sunDot = dot(norm, normalize(SunDirection));
    float sunlight = max(sunDot, 0.0); // only if sun is above the horizon

    // Sun below horizon → sunlight = 0
    if (SunDirection.y <= 0.0) {
        sunlight = 0.0;
    }

    // Moonlight from opposite direction (faint blue light)
    vec3 moonDir = normalize(-SunDirection);
    float moonDot = dot(norm, moonDir);
    float moonlight = max(moonDot, 0.0) * 0.2; // dimmer and cooler

    // Ambient term
    float ambient = 0.2;

    // Total light
    float totalLight = clamp(ambient + sunlight + moonlight, 0.0, 1.0);

    // Tint moonlight slightly blue
    vec3 moonTint = vec3(0.6, 0.7, 1.0);
    vec3 baseColor = vertexColor.rgb * totalLight;
    baseColor += moonTint * moonlight;

    vec4 color = vec4(baseColor, 1.0) * ColorModulator;

    if (vertexColor.a == 0.0) {
        discard;
    }

    fragColor = linear_fog(color, min(vertexDistance, FogEnd * 4.5), FogStart, FogEnd * 5.0, FogColor);
}