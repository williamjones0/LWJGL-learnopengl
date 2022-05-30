import org.lwjgl.*;

public class Main {

    public Window window;
    public Renderer renderer;
    private Mesh mesh;

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

        float[] vertices = {
             // VO
            -0.5f,  0.5f,  0.5f,  1.0f, 0.0f, 0.0f,
             // V1
            -0.5f, -0.5f,  0.5f,  0.0f, 1.0f, 0.0f,
             // V2
             0.5f, -0.5f,  0.5f,  0.0f, 0.0f, 1.0f,
             // V3
             0.5f,  0.5f,  0.5f,  1.0f, 0.0f, 0.0f,
             // V4
            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 0.0f,
             // V5
             0.5f,  0.5f, -0.5f,  0.0f, 0.0f, 1.0f,
             // V6
            -0.5f, -0.5f, -0.5f,  1.0f, 0.0f, 0.0f,
             // V7
             0.5f, -0.5f, -0.5f,  0.0f, 1.0f, 0.0f
        };

        int[] indices = {
            // Front face
            0, 1, 3, 3, 1, 2,
            // Top Face
            4, 0, 3, 5, 4, 3,
            // Right face
            3, 2, 7, 5, 3, 7,
            // Left face
            6, 1, 0, 6, 0, 4,
            // Bottom face
            2, 1, 6, 2, 6, 7,
            // Back face
            7, 6, 4, 7, 4, 5
        };

        mesh = new Mesh(vertices, indices);
    }

    private void loop() {
        while ( !window.shouldClose() ) {
            update();
            render();
        }
    }

    private void update() {
        window.update();
    }

    private void render() {
        renderer.render(mesh);
        window.swapBuffers();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}