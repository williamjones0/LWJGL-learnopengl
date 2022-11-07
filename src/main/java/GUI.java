import Utils.Utils;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector3f;

import java.util.List;

import static imgui.flag.ImGuiConfigFlags.NoMouse;
import static imgui.flag.ImGuiConfigFlags.None;

public class GUI {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private int entitySelected = -1;
    private int pointLightSelected = -1;
    private int spotLightSelected = -1;
    private int cameraSelected = -1;

    public void init(long windowHandle) {
        ImGui.createContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330");

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.setConfigFlags(NoMouse);
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
                if (ImGui.selectable("Entity " + (i + 1), entitySelected == i)) {
                    entitySelected = i;
                    pointLightSelected = -1;
                    spotLightSelected = -1;
                    cameraSelected = -1;
                }
            }
            ImGui.treePop();
        }

        if (ImGui.treeNode("Lights")) {
            for (int i = 0; i < pointLights.length; i++) {
                if (ImGui.selectable("Point Light " + (i + 1), pointLightSelected == i)) {
                    pointLightSelected = i;
                    entitySelected = -1;
                    spotLightSelected = -1;
                    cameraSelected = -1;
                }
            }
            for (int i = 0; i < spotLights.length; i++) {
                if (ImGui.selectable("Spot Light " + (i + 1), spotLightSelected == i)) {
                    spotLightSelected = i;
                    entitySelected = -1;
                    pointLightSelected = -1;
                    cameraSelected = -1;
                }
            }
            ImGui.treePop();
        }

        if (ImGui.selectable("Camera", cameraSelected == 0)) {
            spotLightSelected = -1;
            entitySelected = -1;
            pointLightSelected = -1;
            cameraSelected = 0;
        }

        ImGui.end();

        ImGui.setNextWindowPos(1920 - 320, 20, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(300, 600, ImGuiCond.FirstUseEver);
        ImGui.begin("Inspector");

        if (entitySelected != -1) {
            Entity entity = entities.get(entitySelected);

            float[] position = Utils.vector3fToArray(entity.getPosition());
            float[] rotation = Utils.vector3fToArray(entity.getRotation());
            float[] scale = new float[] { entity.getScale() };

            ImGui.text("Entity " + (entitySelected + 1));
            ImGui.dragFloat3("Position: ", position, 0.1f);
            ImGui.dragFloat3("Rotation: ", rotation, 0.1f);
            ImGui.dragFloat("Scale: ", scale, 0.1f);

            entity.setPosition(new Vector3f(position[0], position[1], position[2]));
            entity.setRotation(new Vector3f(rotation[0], rotation[1], rotation[2]));
            entity.setScale(scale[0]);
        }

        if (pointLightSelected != -1) {
            PointLight light = pointLights[pointLightSelected];

            float[] position = Utils.vector3fToArray(light.getPosition());
            float[] color = Utils.vector3fToArray(light.getColor());

            ImGui.text("Point Light " + (pointLightSelected + 1));
            ImGui.dragFloat3("Position: ", position, 0.1f);
            ImGui.colorPicker3("Color: ", color);

            light.setPosition(new Vector3f(position[0], position[1], position[2]));
            light.setColor(new Vector3f(color[0], color[1], color[2]));
        }

        if (cameraSelected != -1) {
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

}
