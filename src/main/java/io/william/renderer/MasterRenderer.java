package io.william.renderer;

import io.william.io.Window;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import io.william.renderer.shadow.SpotlightShadowRenderer;

public class MasterRenderer {

    private Renderer renderer;
    private ShadowRenderer shadowRenderer;
    private OmnidirectionalShadowRenderer omnidirectionalShadowRenderer;
    private SpotlightShadowRenderer spotlightShadowRenderer;
    private GUI gui;

    public void init(Window window, Renderer renderer, Camera camera, ShadowRenderer shadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer, SpotlightShadowRenderer spotlightShadowRenderer, GUI gui) throws Exception {
        this.renderer = renderer;
        this.shadowRenderer = shadowRenderer;
        this.omnidirectionalShadowRenderer = omnidirectionalShadowRenderer;
        this.spotlightShadowRenderer = spotlightShadowRenderer;
        this.gui = gui;

        renderer.init(window, camera);
        gui.init(window.getWindowHandle());
    }

    public void render(Camera camera, Scene scene, Window window) throws Exception {
        shadowRenderer.render(scene);
        omnidirectionalShadowRenderer.render(scene);
        spotlightShadowRenderer.render(scene);
        renderer.render(camera, scene, shadowRenderer, omnidirectionalShadowRenderer, window);
        gui.render(scene, camera, renderer, shadowRenderer, omnidirectionalShadowRenderer, window);
    }

}
