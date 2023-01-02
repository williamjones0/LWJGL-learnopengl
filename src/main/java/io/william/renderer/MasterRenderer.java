package io.william.renderer;

import io.william.io.Window;
import io.william.renderer.shadow.ShadowRenderer;

public class MasterRenderer {

    private Renderer renderer;
    private ShadowRenderer shadowRenderer;
    private GUI gui;

    public void init(Window window, Renderer renderer, Camera camera, ShadowRenderer shadowRenderer, GUI gui) throws Exception {
        this.renderer = renderer;
        this.shadowRenderer = shadowRenderer;
        this.gui = gui;

        renderer.init(window, camera);
        gui.init(window.getWindowHandle());
    }

    public void render(Camera camera, Scene scene, Window window) throws Exception {
        shadowRenderer.render(scene);
        renderer.render(camera, scene, shadowRenderer, window);
        gui.render(scene, camera, renderer, shadowRenderer, window);
    }

}
