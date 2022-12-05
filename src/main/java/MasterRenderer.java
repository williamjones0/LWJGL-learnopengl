public class MasterRenderer {

    private Renderer renderer;
    private GUI gui;

    public void init(Window window, Renderer renderer, GUI gui) throws Exception {
        this.renderer = renderer;
        this.gui = gui;

        renderer.init(window);
        gui.init(window.getWindowHandle());
    }

    public void render(Camera camera, Scene scene, Window window) throws Exception {
        renderer.render(camera, scene, window);
        gui.render(scene, camera, renderer, window);
    }

}
