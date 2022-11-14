import Utils.Utils;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static imgui.flag.ImGuiConfigFlags.NoMouse;
import static imgui.flag.ImGuiConfigFlags.None;

public class GUI {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private Map<String, Integer> selected = new HashMap<>();

    public void init(long windowHandle) {
        ImGui.createContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330");

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.setConfigFlags(NoMouse);

        selected.put("entity", -1);
        selected.put("pointLight", -1);
        selected.put("spotLight", -1);
        selected.put("dirLight", -1);
        selected.put("camera", -1);
    }

    public void render(Scene scene, Camera camera, Renderer renderer) {
        List<Entity> entities = scene.getEntities();
        DirLight dirLight = scene.getDirLight();
        PointLight[] pointLights = scene.getPointLights();
        SpotLight[] spotLights = scene.getSpotLights();
        EquirectangularMap equirectangularMap = scene.getEquirectangularMap();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

//        ImGui.showDemoWindow();

        ImGui.setNextWindowPos(20, 20, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(200, 800, ImGuiCond.FirstUseEver);
        ImGui.begin("Hierarchy");

        if (ImGui.treeNode("Entities")) {
            for (int i = 0; i < entities.size(); i++) {
                if (ImGui.selectable("Entity " + (i + 1), selected.get("entity") == i)) {
                    setSelected("entity", i);
                }
            }
            ImGui.treePop();
        }

        if (ImGui.treeNode("Lights")) {
            for (int i = 0; i < pointLights.length; i++) {
                if (ImGui.selectable("Point Light " + (i + 1), selected.get("pointLight") == i)) {
                    setSelected("pointLight", i);
                }
            }
            for (int i = 0; i < spotLights.length; i++) {
                if (ImGui.selectable("Spot Light " + (i + 1), selected.get("spotLight") == i)) {
                    setSelected("spotLight", i);
                }
            }
            if (ImGui.selectable("Directional Light", selected.get("dirLight") == 0)) {
                setSelected("dirLight", 0);
            }
            ImGui.treePop();
        }

        if (ImGui.selectable("Camera", selected.get("camera") == 0)) {
            setSelected("camera", 0);
        }

        ImGui.end();

        ImGui.setNextWindowPos(1920 - 320, 20, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(300, 600, ImGuiCond.FirstUseEver);
        ImGui.begin("Inspector");

        if (selected.get("entity") != -1) {
            Entity entity = entities.get(selected.get("entity"));

            float[] position = Utils.vector3fToArray(entity.getPosition());
            float[] rotation = Utils.vector3fToArray(entity.getRotation());
            float[] scale = new float[] { entity.getScale() };

            ImGui.text("Entity " + (selected.get("entity") + 1));
            ImGui.dragFloat3("Position: ", position, 0.1f);
            ImGui.dragFloat3("Rotation: ", rotation, 0.1f);
            ImGui.dragFloat("Scale: ", scale, 0.1f);

            entity.setPosition(new Vector3f(position[0], position[1], position[2]));
            entity.setRotation(new Vector3f(rotation[0], rotation[1], rotation[2]));
            entity.setScale(scale[0]);
        }

        if (selected.get("pointLight") != -1) {
            PointLight light = pointLights[selected.get("pointLight")];

            float[] position = Utils.vector3fToArray(light.getPosition());
            float[] color = Utils.vector3fToArray(light.getColor());

            ImGui.text("Point Light " + (selected.get("pointLight") + 1));
            ImGui.dragFloat3("Position: ", position, 0.1f);
            ImGui.colorPicker3("Color: ", color);

            light.setPosition(new Vector3f(position[0], position[1], position[2]));
            light.setColor(new Vector3f(color[0], color[1], color[2]));
        }

        if (selected.get("spotLight") != -1) {
            SpotLight light = spotLights[selected.get("spotLight")];

            float[] position = Utils.vector3fToArray(light.getPosition());
            float[] direction = Utils.vector3fToArray(light.getDirection());
            float[] color = Utils.vector3fToArray(light.getColor());
            float[] cutoff = new float[] { light.getCutoff() };
            float[] outerCutoff = new float[] { light.getOuterCutoff() };
            boolean enabled = light.isEnabled();

            ImGui.text("Spot Light " + (selected.get("spotLight") + 1));
            ImGui.dragFloat3("Position: ", position, 0.1f);
            ImGui.dragFloat3("Direction: ", direction, 0.1f);
            ImGui.colorPicker3("Color: ", color);
            ImGui.dragFloat("Cutoff: ", cutoff, 0.1f);
            ImGui.dragFloat("Outer Cutoff: ", outerCutoff, 0.1f);
            ImGui.checkbox("Enabled: ", enabled);

            light.setPosition(new Vector3f(position[0], position[1], position[2]));
            light.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
            light.setColor(new Vector3f(color[0], color[1], color[2]));
            light.setCutoff(cutoff[0]);
            light.setOuterCutoff(outerCutoff[0]);
            light.setEnabled(enabled);
        }

        if (selected.get("dirLight") != -1) {
            float[] direction = Utils.vector3fToArray(dirLight.getDirection());
            float[] color = Utils.vector3fToArray(dirLight.getColor());

            ImGui.text("Directional Light");
            ImGui.dragFloat3("Direction: ", direction, 0.1f);
            ImGui.colorPicker3("Color: ", color);

            dirLight.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
            dirLight.setColor(new Vector3f(color[0], color[1], color[2]));
        }

        if (selected.get("camera") != -1) {
            // Initialise attribute arrays
            float[] position = Utils.vector3fToArray(camera.getPosition());
            float[] FOV = new float[] {(float) Math.toDegrees(renderer.getFOV())};
            float[] exposure = new float[] { renderer.getExposure() };

            ImGui.text("Camera");
            ImGui.dragFloat3("Position: ", position, 0.1f);
            ImGui.dragFloat("FOV: ", FOV, 0.1f, 45.0f, 180f);
            ImGui.dragFloat("Exposure: ", exposure, 0.1f, 0.0f, 10.0f);

            camera.setPosition(new Vector3f(position[0], position[1], position[2]));
            renderer.setFOV((float) Math.toRadians(FOV[0]));
            renderer.setExposure(exposure[0]);
        }

        ImGui.end();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void setCursorEnabled(boolean enabled) {
        ImGuiIO io = ImGui.getIO();
        if (!enabled)
            io.setConfigFlags(NoMouse);
        else
            io.setConfigFlags(None);
    }

    private void setSelected(String key, int value) {
        System.out.println("Selected " + key + " " + value);
        for (String k : selected.keySet()) {
            if (k.equals(key))
                selected.put(k, value);
            else
                selected.put(k, -1);
        }
    }

}
