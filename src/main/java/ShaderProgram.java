import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

class ShaderProgram {

    private final int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private final Map<String, Integer> uniforms;

    public ShaderProgram() {
        programID = glCreateProgram();
        if (programID == 0) {
            System.out.println("Could not create shader");
        }

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
    }

    public void createDirLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
    }

    public void createPointLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
    }

    public void createSpotLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createSpotLightUniform(uniformName + "[" + i + "]");
        }
    }

    public void createSpotLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".cutoff");
        createUniform(uniformName + ".outerCutoff");
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
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

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String source) {
        vertexShaderID = createShader(source, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String source) {
        fragmentShaderID = createShader(source, GL_FRAGMENT_SHADER);
    }

    private int createShader(String source, int shaderType) {
        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) {
            System.out.println("Error creating shader: " + shaderType);
        }

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
