#version 320 es

precision highp float;
precision highp int;

// max number of lights
#define  snumber 1
#define  dnumber 1
#define  pnumber 1

out vec4 FragColor;

uniform sampler2D textureSamp;
uniform sampler2D normalMap;

struct PointLight {
    vec3 position;
    vec3 color;
    float constant;
    float linear;
    float quadratic;
    float diffuse;
    float specular;
};

struct AmibentLight {
    vec3 color;
};

struct DirectedLight {
    vec3 color;
    vec3 direction;
    float diffuse;
    float specular;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    vec3 color;
    float cutOff;
    float outerCutOff;
    float constant;
    float linear;
    float quadratic;
    float ambient;
    float diffuse;
    float specular;
};

uniform SpotLight sLights[snumber];
uniform PointLight pLights[pnumber];
uniform DirectedLight dLights[dnumber];
uniform AmibentLight aLight;

uniform int pLightNum;
uniform int sLightNum;
uniform int dLightNum;

uniform struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
} material;

in struct Data {
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
} data;

in vec3 pLightPos[pnumber];
in vec3 dLightDir[dnumber];
in vec3 sLightDir[snumber];
in vec3 sLightPos[snumber];

uniform int normalMapEnable;

// -------------------------------------------------------------

vec3 applyAmbient(vec3 color) {
    return color * aLight.color;
}

// -------------------------------------------------------------
// Blinn–Phong + suppression fixes

vec3 applyDirectedLight(vec3 color, vec3 normal, vec3 viewDir, int index)
{
    vec3 lightDir = normalize(dLightDir[index]);

    float diff = max(dot(normal, lightDir), 0.0);

    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    // CHANGE: suppress specular at grazing view angles
    float NdotV = max(dot(normal, viewDir), 0.0);
    spec *= smoothstep(0.0, 0.2, NdotV);

    // CHANGE: suppress specular when light is almost parallel
    spec *= smoothstep(0.0, 0.2, diff);

    vec3 diffuse  = dLights[index].diffuse  * diff * material.diffuse;
    vec3 specular = dLights[index].specular * spec * material.specular;

    return color * dLights[index].color * (diffuse + specular);
}

// -------------------------------------------------------------

vec3 applyPointLight(vec3 color, int index, vec3 fragPos, vec3 normal, vec3 viewDir)
{
    vec3 lightDir = normalize(pLightPos[index]);

    float diff = max(dot(normal, lightDir), 0.0);

    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    float NdotV = max(dot(normal, viewDir), 0.0);
    spec *= smoothstep(0.0, 0.2, NdotV);
    spec *= smoothstep(0.0, 0.2, diff); // CHANGE

    float distance = length(pLightPos[index]);
    float attenuation =
        1.0 / (pLights[index].constant +
               pLights[index].linear * distance +
               pLights[index].quadratic * distance * distance);

    vec3 diffuse  = pLights[index].diffuse  * diff * material.diffuse;
    vec3 specular = pLights[index].specular * spec * material.specular;

    return color * pLights[index].color * (diffuse + specular) * attenuation;
}

// -------------------------------------------------------------

vec3 CalcSpotLight(vec3 color, SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, int i)
{
    vec3 lightDir = normalize(sLightPos[i]);

    float diff = max(dot(normal, lightDir), 0.0);

    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    float NdotV = max(dot(normal, viewDir), 0.0);
    spec *= smoothstep(0.0, 0.2, NdotV);
    spec *= smoothstep(0.0, 0.2, diff); // CHANGE

    float distance = length(sLightPos[i]);
    float attenuation =
        1.0 / (light.constant +
               light.linear * distance +
               light.quadratic * distance * distance);

    float theta = dot(lightDir, normalize(-sLightDir[i]));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);

    vec3 ambient  = light.ambient  * material.diffuse;
    vec3 diffuse  = light.diffuse  * diff * material.diffuse;
    vec3 specular = light.specular * spec * material.specular;

    return (ambient + diffuse + specular) *
           color * light.color * attenuation * intensity;
}

// -------------------------------------------------------------

void main()
{
    vec3 color = texture(textureSamp, data.TexCoord).rgb;

    // view direction in tangent space
    vec3 viewDir = normalize(data.TangentViewPos);

    vec3 norm;
    if (normalMapEnable == 1) {
        norm = texture(normalMap, data.TexCoord).rgb * 2.0 - 1.0;

        // CHANGE: reconstruct Z to fix edge artifacts
        norm.z = sqrt(max(0.0, 1.0 - dot(norm.xy, norm.xy)));

        // CHANGE: always renormalize (important with mipmaps)
        norm = normalize(norm);

        // if DirectX normal map:
        // norm.y = -norm.y;
    } else {
        norm = vec3(0.0, 0.0, 1.0);
    }

    vec3 result = applyAmbient(color);

    for (int i = 0; i < dLightNum; i++)
        result += applyDirectedLight(color, norm, viewDir, i);

    for (int i = 0; i < pLightNum; i++)
        result += applyPointLight(color, i, vec3(0.0), norm, viewDir);

    for (int i = 0; i < sLightNum; i++)
        result += CalcSpotLight(color, sLights[i], norm, vec3(0.0), viewDir, i);

    FragColor = vec4(result, 1.0);
}
