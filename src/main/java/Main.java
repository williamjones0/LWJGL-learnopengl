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