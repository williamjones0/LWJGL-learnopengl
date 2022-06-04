import org.joml.Vector3f;
import org.lwjgl.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

    private Window window;
    private Renderer renderer;
    private Mesh mesh;
    private Entity[] entities;
    private Camera camera;

    private float deltaTime = 0.0f;
    private float lastFrame = 0.0f;

    private double lastX, lastY = 0;

    public void run() throws Exception {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        renderer.cleanup();
        mesh.cleanup();
        window.destroy();
    }

    private void init() throws Exception {
        window = new Window(800, 600, "LearnOpenGL", false, 0.2f, 0.3f, 0.3f);
        window.create();

        renderer = new Renderer();
        renderer.init(window);

        camera = new Camera(new Vector3f(0, 0, -5), 0, 0);

        float[] vertices = {
            // VO
           -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
            // V1
           -0.5f, -0.5f,  0.5f,  0.0f, 0.5f,
            // V2
            0.5f, -0.5f,  0.5f,  0.5f, 0.5f,
            // V3
            0.5f,  0.5f,  0.5f,  0.5f, 0.0f,
            // V4
           -0.5f,  0.5f, -0.5f,  0.5f, 0.0f,
            // V5
            0.5f,  0.5f, -0.5f,  0.0f, 0.0f,
            // V6
           -0.5f, -0.5f, -0.5f,  0.5f, 0.5f,
            // V7
            0.5f, -0.5f, -0.5f,  0.0f, 0.5f,

            // Top face
            // V8: V4 repeated
           -0.5f,  0.5f, -0.5f,  0.0f, 0.5f,
            // V9: V5 repeated
            0.5f,  0.5f, -0.5f,  0.5f, 0.5f,
            // V10: V0 repeated
           -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
            // V11: V3 repeated
            0.5f,  0.5f,  0.5f,  0.5f, 1.0f,

            // Right face
            // V12: V3 repeated
            0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
            // V13: V2 repeated
            0.5f, -0.5f,  0.5f,  0.0f, 0.5f,
            // V14: V5 repeated
            0.5f,  0.5f, -0.5f,  0.5f, 0.0f,
            // V15: V7 repeated
            0.5f, -0.5f, -0.5f,  0.5f, 0.5f,

            // Left face
            // V16: V0 repeated
           -0.5f,  0.5f,  0.5f,  0.5f, 0.0f,
            // V17: V1 repeated
           -0.5f, -0.5f,  0.5f,  0.5f, 0.5f,
            // V18: V4 repeated
           -0.5f,  0.5f, -0.5f,  0.0f, 0.0f,
            // V19: V6 repeated
           -0.5f, -0.5f, -0.5f,  0.0f, 0.5f,

            // Bottom face
            // V20: V6 repeated
           -0.5f, -0.5f, -0.5f,  0.5f, 0.0f,
            // V21: V7 repeated
            0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
            // V22: V1 repeated
           -0.5f, -0.5f,  0.5f,  0.5f, 0.5f,
            // V23: V2 repeated
            0.5f, -0.5f,  0.5f,  1.0f, 0.5f
        };

        int[] indices = {
            // Front face
            0, 1, 3, 3, 1, 2,
            // Top face
            8, 10, 11, 9, 8, 11,
            // Right face
            12, 13, 14, 14, 13, 15,
            // Left face
            18, 19, 16, 16, 19, 17,
            // Bottom face
            22, 20, 23, 23, 20, 21,
            // Back face
            5, 7, 4, 4, 7, 6
        };

        Texture texture = new Texture("src/main/resources/grassblock.png");

        mesh = new Mesh(vertices, indices, texture);
        Entity entity1 = new Entity(mesh);
        Entity entity2 = new Entity(mesh);
        entity2.setPosition(2, 1, -1);

        entities = new Entity[] {
            entity1,
            entity2
        };
    }

    private void loop() {
        while ( !window.shouldClose() && !Input.isKeyDown(GLFW_KEY_ESCAPE)) {
            update();
            render();
        }
    }

    private void update() {
        window.update();

        // Calculate delta time
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        processInput();
    }

    private void processInput() {
        // Keyboard
        if (Input.isKeyDown(GLFW_KEY_W))
            camera.processKeyboard(Camera.Movement.FORWARD, deltaTime);
        if (Input.isKeyDown(GLFW_KEY_S))
            camera.processKeyboard(Camera.Movement.BACKWARD, deltaTime);
        if (Input.isKeyDown(GLFW_KEY_A))
            camera.processKeyboard(Camera.Movement.LEFT, deltaTime);
        if (Input.isKeyDown(GLFW_KEY_D))
            camera.processKeyboard(Camera.Movement.RIGHT, deltaTime);

        // Mouse
        double xpos = Input.getMouseX();
        double ypos = Input.getMouseY();

        double xoffset = xpos - lastX;
        double yoffset = lastY - ypos;  // reversed since y-coordinates go from bottom to top

        lastX = xpos;
        lastY = ypos;

        camera.processMouse((float) xoffset, (float) yoffset);
    }

    private void render() {
        renderer.render(camera, entities);
        window.swapBuffers();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}
