import static org.lwjgl.opengl.GL20.*;

class ShaderProgram {

    private final int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    public ShaderProgram() {
        programID = glCreateProgram();
        if (programID == 0) {
            System.out.println("Could not create shader");
        }
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