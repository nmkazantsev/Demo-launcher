#version 320 es
precision highp float;
precision highp int;

// количество источников света
#define  snumber 1
#define  dnumber 1
#define  pnumber 1

out vec4 FragColor;

// ===== ТЕКСТУРЫ =====
uniform sampler2D textureSamp; // diffuse / albedo
uniform sampler2D normalMap;   // normal map (tangent space)

// ===== СТРУКТУРЫ СВЕТА =====
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

// ===== UNIFORMS =====
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

// ===== ВХОДНЫЕ ИНТЕРПОЛИРОВАННЫЕ ДАННЫЕ =====
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

// ===== AMBIENT =====
vec3 applyAmbient(vec3 color) {
    // ambient освещение не зависит от направления
    return color * aLight.color;
}

// ===== DIRECTED LIGHT =====
vec3 applyDirectedLight(vec3 color, vec3 normal, vec3 viewDir, int index)
{
    // направление света в tangent space
    vec3 lightDir = normalize(dLightDir[index]);

    // diffuse = cos(theta)
    float diff = max(dot(normal, lightDir), 0.0);

    // Blinn–Phong: half vector
    vec3 halfDir = normalize(lightDir + viewDir);

    // specular компонент
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    // подавляем specular под острым углом к камере
    float NdotV = max(dot(normal, viewDir), 0.0);
    spec *= smoothstep(0.0, 0.2, NdotV);

    // подавляем specular, если свет почти параллелен поверхности
    spec *= smoothstep(0.0, 0.2, diff);

    vec3 diffuse  = dLights[index].diffuse  * diff * material.diffuse;
    vec3 specular = dLights[index].specular * spec * material.specular;

    return color * dLights[index].color * (diffuse + specular);
}

// ===== POINT LIGHT =====
vec3 applyPointLight(vec3 color, int index, vec3 fragPos, vec3 normal, vec3 viewDir)
{
    vec3 lightDir = normalize(pLightPos[index]);

    float diff = max(dot(normal, lightDir), 0.0);

    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    float NdotV = max(dot(normal, viewDir), 0.0);
    spec *= smoothstep(0.0, 0.2, NdotV);
    spec *= smoothstep(0.0, 0.2, diff);

    float distance = length(pLightPos[index]);

    float attenuation =
        1.0 / (pLights[index].constant +
               pLights[index].linear * distance +
               pLights[index].quadratic * distance * distance);

    vec3 diffuse  = pLights[index].diffuse  * diff * material.diffuse;
    vec3 specular = pLights[index].specular * spec * material.specular;

    return color * pLights[index].color * (diffuse + specular) * attenuation;
}

// ===== SPOT LIGHT =====
vec3 CalcSpotLight(vec3 color, SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, int i)
{
    vec3 lightDir = normalize(sLightPos[i]);

    float diff = max(dot(normal, lightDir), 0.0);

    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    float NdotV = max(dot(normal, viewDir), 0.0);
    spec *= smoothstep(0.0, 0.2, NdotV);
    spec *= smoothstep(0.0, 0.2, diff);

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

// ===== MAIN =====
void main()
{
    // base color из diffuse текстуры
    vec3 color = texture(textureSamp, data.TexCoord).rgb;

    // направление взгляда в tangent space
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);

    vec3 norm;
    if (normalMapEnable == 1) {

        // читаем нормаль из normal map (0..1 → -1..1)
        norm = texture(normalMap, data.TexCoord).rgb * 2.0 - 1.0;

        // восстанавливаем Z, чтобы нормаль была единичной
        norm.z = sqrt(max(0.0, 1.0 - dot(norm.xy, norm.xy)));

        // финальная нормализация
        norm = normalize(norm);

        // для DirectX normal maps:
        // norm.y = -norm.y;
    } else {
        // fallback нормаль — строго вверх в tangent space
        norm = vec3(0.0, 0.0, 1.0);
    }

    // начинаем с ambient освещения
    vec3 result = applyAmbient(color);

    // добавляем directed lights
    for (int i = 0; i < dLightNum; i++)
        result += applyDirectedLight(color, norm, viewDir, i);

    // добавляем point lights
    for (int i = 0; i < pLightNum; i++)
        result += applyPointLight(color, i, vec3(0.0), norm, viewDir);

    // добавляем spot lights
    for (int i = 0; i < sLightNum; i++)
        result += CalcSpotLight(color, sLights[i], norm, vec3(0.0), viewDir, i);

    FragColor = vec4(result, 1.0);
}
