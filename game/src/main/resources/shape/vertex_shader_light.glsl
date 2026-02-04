#version 320 es
precision highp float;
precision highp int;

// количество источников света каждого типа
#define  snumber 1
#define  dnumber 1
#define  pnumber 1

// ===== ВХОДНЫЕ АТРИБУТЫ ВЕРШИНЫ =====

// позиция вершины в object space
layout (location = 0) in vec3 aPos;

// UV координаты вершины
layout (location = 1) in vec2 aTexCoord;

// нормаль вершины (per-vertex normal, из меша)
layout (location = 2) in vec3 normalVec;

// tangent вершины (перпендикулярен нормали, направлен вдоль U)
layout (location = 3) in vec3 aT;

// bitangent вершины (перпендикулярен нормали и tangent)
layout (location = 4) in vec3 aB;

// ===== СТРУКТУРА ДАННЫХ ДЛЯ ФРАГМЕНТНОГО ШЕЙДЕРА =====
out struct Data {
    mat4 model2;              // модельная матрица (не используется, но передаётся)
    vec3 normal;              // нормаль в world space (для совместимости)
    vec3 FragPos;             // позиция фрагмента в world space
    vec2 TexCoord;            // UV координаты
    vec3 TangentViewPos;      // позиция камеры в tangent space
    vec3 TangentFragPos;      // позиция фрагмента в tangent space
} data;

// позиции источников света, уже переведённые в tangent space
out vec3 pLightPos[pnumber];
out vec3 dLightDir[dnumber];
out vec3 sLightDir[snumber];
out vec3 sLightPos[snumber];

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
uniform DirectedLight dLights[dnumber];
uniform PointLight pLights[pnumber];
uniform AmibentLight aLight;

uniform int pLightNum;
uniform int sLightNum;
uniform int dLightNum;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform vec3 viewPos; // позиция камеры в world space

void main()
{
    // стандартное преобразование вершины в clip space
    gl_Position = projection * view * model * vec4(aPos, 1.0);

    // вычисляем позицию вершины (будущего фрагмента) в world space
    data.FragPos = vec3(model * vec4(aPos, 1.0));

    // передаём UV координаты напрямую
    data.TexCoord = aTexCoord;

    // сохраняем модельную матрицу (не используется напрямую)
    data.model2 = model;

    // ===== КОРРЕКТНАЯ МАТРИЦА ДЛЯ НОРМАЛЕЙ =====
    // inverse-transpose необходима, если в model есть scale / non-uniform scale
    mat3 normalMatrix = transpose(inverse(mat3(model)));

    // ===== ПЕРЕВОД ВЕКТОРОВ В WORLD SPACE =====

    // нормаль вершины в world space
    vec3 N = normalize(normalMatrix * normalVec);

    // tangent вершины в world space
    vec3 T = normalize(normalMatrix * aT);

    // ===== ОРТОГОНАЛИЗАЦИЯ T К N =====
    // гарантируем, что T строго перпендикулярен N
    T = normalize(T - dot(T, N) * N);

    // ===== BITANGENT =====
    // восстанавливаем битангенту как cross(N, T)
    // предполагается правосторонняя система координат
    vec3 B = normalize(cross(N, T));

    // ===== TBN МАТРИЦА =====
    // матрица преобразования из world space → tangent space
    // transpose нужен, потому что хотим инверсию базиса
    mat3 TBN = transpose(mat3(T, B, N));

    // сохраняем нормаль в world space (не используется в lighting с normal map)
    data.normal = N;

    // ===== ПЕРЕВОД ИСТОЧНИКОВ СВЕТА В TANGENT SPACE =====

    // point lights: позиция относительно фрагмента
    for (int i = 0; i < pLightNum; i++) {
        pLightPos[i] = TBN * (pLights[i].position - data.FragPos);
    }

    // directional lights: только направление
    for (int i = 0; i < dLightNum; i++) {
        dLightDir[i] = TBN * (-dLights[i].direction);
    }

    // spot lights: позиция и направление
    for (int i = 0; i < sLightNum; i++) {
        sLightPos[i] = TBN * (sLights[i].position - data.FragPos);
        sLightDir[i] = TBN * (-sLights[i].direction);
    }

    // ===== КАМЕРА В TANGENT SPACE =====
    // позиция камеры относительно фрагмента
    data.TangentViewPos = TBN * (viewPos - data.FragPos);

    // позиция фрагмента в tangent space всегда (0,0,0)
    data.TangentFragPos = vec3(0.0);
}
