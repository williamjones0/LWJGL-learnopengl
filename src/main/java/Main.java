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
        renderer.init();

        float[] vertices = {
                -0.5f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f,  0.5f, 0.0f,
        };

        int[] indices = {
                0, 1, 3, 3, 1, 2,
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