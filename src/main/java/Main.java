import org.joml.Vector3f;
import org.lwjgl.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

    private Window window;
    private Renderer renderer;
    private Mesh mesh;
    private Entity[] entities;
    private Camera camera;
    private PointLight pointLight;
    private Material material;

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
            // positions         // normals           // texture coords
            // Front face
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
            0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,

            // Back face
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,

            // Left face
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

            // Right face
            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

            // Bottom face
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,

            // Top face
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
        };

        Texture materialDiffuse = new Texture("src/main/resources/container.png");
        Texture materialSpecular = new Texture("src/main/resources/container_specular.png");

        mesh = new Mesh(vertices, materialDiffuse, materialSpecular);
        Entity entity1 = new Entity(mesh, new Vector3f(), new Vector3f(), 2);
        Entity entity2 = new Entity(mesh, new Vector3f(4, 2, -2), new Vector3f(), 2);

        entities = new Entity[] {
            entity1,
            entity2
        };

        Vector3f lightPos = new Vector3f(1.2f, 2.0f, 4.0f);
        Vector3f lightAmbient = new Vector3f(0.2f, 0.2f, 0.2f);
        Vector3f lightDiffuse = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f lightSpecular = new Vector3f(1.0f, 1.0f, 1.0f);
        float constant = 1.0f;
        float linear = 0.09f;
        float quadratic = 0.032f;
        pointLight = new PointLight(mesh, lightPos, lightAmbient, lightDiffuse, lightSpecular, constant, linear, quadratic);

        float materialShininess = 64.0f;
        material = new Material(materialDiffuse, materialSpecular, materialShininess);
    }

    private void loop() {
        while ( !window.shouldClose() && !Input.isKeyDown(GLFW_KEY_ESCAPE)) {
            update();
            render();
        }
    }

    private void update() {
        window.update();

        pointLight.setPosition(pointLight.getPosition().x + 0.001f, pointLight.getPosition().y, pointLight.getPosition().z);

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
        if (Input.isKeyDown(GLFW_KEY_SPACE))
            camera.processKeyboard(Camera.Movement.UP, deltaTime);
        if (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL))
            camera.processKeyboard(Camera.Movement.DOWN, deltaTime);

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
        renderer.render(camera, entities, pointLight, material);
        window.swapBuffers();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}
