import org.lwjgl.*;

public class Main {

    public Window window;
    public Renderer renderer;

    public void run() throws Exception {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        renderer.cleanup();
        window.destroy();
    }

    private void init() throws Exception {
        window = new Window(800, 600, "LearnOpenGL", false, 0.2f, 0.3f, 0.3f);
        window.create();

        renderer = new Renderer();
        renderer.init();
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
        renderer.render();
        window.swapBuffers();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}