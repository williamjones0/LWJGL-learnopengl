package io.william.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.*;

public class ShaderProgram {

    private final int programID;
    private int vertexShaderID;
    private int tesselationControlShaderID;
    private int tesselationEvaluationShaderID;
    private int geometryShaderID;
    private int fragmentShaderID;
    private final Map<String, Integer> uniforms;

    private final String label;

    public ShaderProgram(String label) {
        programID = glCreateProgram();
        if (programID == 0) {
            System.out.println("Could not create shader");
        }

        this.label = label;
        glObjectLabel(GL_PROGRAM, programID, label);

        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(programID, uniformName);
        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void createMaterialUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".shininess");
        createUniform(uniformName + ".normalMap");
    }

    public void createPBRMaterialUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".albedo");
        createUniform(uniformName + ".normal");
        createUniform(uniformName + ".metallic");
        createUniform(uniformName + ".roughness");
        createUniform(uniformName + ".metallicRoughness");
        createUniform(uniformName + ".ao");
        createUniform(uniformName + ".emissive");

        createUniform(uniformName + ".albedoColor");
        createUniform(uniformName + ".metallicFactor");
        createUniform(uniformName + ".roughnessFactor");
        createUniform(uniformName + ".emissiveColor");

        createUniform(uniformName + ".uses_albedo_map");
        createUniform(uniformName + ".uses_normal_map");
        createUniform(uniformName + ".uses_metallic_map");
        createUniform(uniformName + ".uses_roughness_map");
        createUniform(uniformName + ".uses_metallicRoughness_map");
        createUniform(uniformName + ".uses_ao_map");
        createUniform(uniformName + ".uses_emissive_map");
    }

    public void createDirLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".color");
    }

    public void createPointLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".enabled");
    }

    public void createSpotLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createSpotLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createSpotLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".cutoff");
        createUniform(uniformName + ".outerCutoff");
        createUniform(uniformName + ".enabled");
    }

    public void createSettingsUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".specularOcclusion");
        createUniform(uniformName + ".horizonSpecularOcclusion");
        createUniform(uniformName + ".pointShadows");
        createUniform(uniformName + ".pointShadowBias");
        createUniform(uniformName + ".shadowMinBias");
        createUniform(uniformName + ".shadowMaxBias");
    }

    public void setUniform(String uniformName, Vector2f value) {
        // Dump the vector into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(2);
            value.get(fb);
            glUniform2fv(uniforms.get(uniformName), fb);
        }
    }

    public void setUniform(String uniformName, Vector3f value) {
        // Dump the vector into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(3);
            value.get(fb);
            glUniform3fv(uniforms.get(uniformName), fb);
        }
    }

    public void setUniform(String uniformName, Vector4f value) {
        // Dump the vector into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(4);
            value.get(fb);
            glUniform4fv(uniforms.get(uniformName), fb);
        }
    }

    public void setUniform(String uniformName, Matrix4f value) {
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(String uniformName, boolean value) {
        glUniform1i(uniforms.get(uniformName), value ? 1 : 0);
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String path) throws IOException {
        String source = Files.readString(new File(path).toPath(), StandardCharsets.US_ASCII);
        vertexShaderID = createShader(source, GL_VERTEX_SHADER);
    }

    public void createTesselationControlShader(String path) throws IOException {
        String source = Files.readString(new File(path).toPath(), StandardCharsets.US_ASCII);
        tesselationControlShaderID = createShader(source, GL_TESS_CONTROL_SHADER);
    }

    public void createTesselationEvaluationShader(String path) throws IOException {
        String source = Files.readString(new File(path).toPath(), StandardCharsets.US_ASCII);
        tesselationEvaluationShaderID = createShader(source, GL_TESS_EVALUATION_SHADER);
    }

    public void createGeometryShader(String path) throws IOException {
        String source = Files.readString(new File(path).toPath(), StandardCharsets.US_ASCII);
        geometryShaderID = createShader(source, GL_GEOMETRY_SHADER);
    }

    public void createFragmentShader(String path) throws IOException {
        String source = Files.readString(new File(path).toPath(), StandardCharsets.US_ASCII);
        fragmentShaderID = createShader(source, GL_FRAGMENT_SHADER);
    }

    private int createShader(String source, int shaderType) {
        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) {
            System.out.println("Error creating shader: " + shaderType);
        }

        String shaderTypeLabel = switch (shaderType) {
            case GL_VERTEX_SHADER -> "Vertex";
            case GL_TESS_CONTROL_SHADER -> "Tesselation Control";
            case GL_TESS_EVALUATION_SHADER -> "Tesselation Evaluation";
            case GL_GEOMETRY_SHADER -> "Geometry";
            case GL_FRAGMENT_SHADER -> "Fragment";
            default -> "Unknown";
        };

        glObjectLabel(GL_SHADER, shaderID, label + " " + shaderTypeLabel);

        glShaderSource(shaderID, source);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            System.out.println("Error compiling shader: " + glGetShaderInfoLog(shaderID, 1024));
        }

        glAttachShader(programID, shaderID);

        return shaderID;
    }

    public void link() {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            System.out.println("Error linking Shader code: " + glGetProgramInfoLog(programID, 1024));
        }

        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        if (tesselationControlShaderID != 0) glDetachShader(programID, tesselationControlShaderID);
        if (tesselationEvaluationShaderID != 0) glDetachShader(programID, tesselationEvaluationShaderID);
        if (geometryShaderID != 0) glDetachShader(programID, geometryShaderID);

        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programID, 1024));
        }
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        glDeleteProgram(programID);
    }
}
