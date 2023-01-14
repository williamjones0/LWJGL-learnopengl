package io.william.renderer;

import io.william.game.component.Movement;
import io.william.game.component.RotationController;
import io.william.io.ModelLoader;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import io.william.util.Utils;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.william.io.Window;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import io.william.renderer.primitive.Cube;
import io.william.renderer.primitive.Cylinder;
import io.william.renderer.primitive.Quad;
import io.william.renderer.primitive.UVSphere;

import java.nio.ByteBuffer;
import java.util.*;

import static imgui.flag.ImGuiConfigFlags.NoMouse;
import static imgui.flag.ImGuiConfigFlags.None;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class GUI {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private final Map<String, Integer> selected = new HashMap<>();
    private Entity selectedEntity;
    private boolean showMaterialWindow = false;
    private boolean showEntityWindow = false;
    private String newEntityType = "";
    private boolean showPointLightWindow = false;
    private boolean showSpotLightWindow = false;
    private boolean showSettingsWindow = false;

    private ImInt debugTextureID = new ImInt(1);

    private PointLight newPointLight = new PointLight(
        new Vector3f(0, 0, 0),
        new Vector3f(1, 1, 1),
        1
    );

    private SpotLight newSpotLight = new SpotLight(
        new Vector3f(0, 0, 0),
        new Vector3f(1, 0, 0),
        (float) Math.cos(Math.toRadians(12.5f)),
        (float) Math.cos(Math.toRadians(15.0f)),
        new Vector3f(1, 1, 1),
        1
    );

    private Entity newEntity = new Entity(
        new Vector3f(0, 0, 0),
        new Vector3f(0, 0, 0),
        1,
        ""
    );

    private Cylinder newCylinder = new Cylinder(
        1f,
        1f,
        1f,
        32
    );

    private UVSphere newSphere = new UVSphere(
        1f,
        32,
        32
    );

    // GUI configuration
    private final float PADDING = 20;
    private final float HIERARCHY_WIDTH = 200;
    private final float HIERARCHY_HEIGHT = 800;
    private final float INSPECTOR_WIDTH = 400;
    private final float INSPECTOR_HEIGHT = 600;
    private final float MATERIAL_WIDTH = 480;
    private final float MATERIAL_HEIGHT = 400;

    public void init(long windowHandle) {
        ImGui.createContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330");

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.setConfigFlags(NoMouse);

        selectedEntity = null;

        selected.put("entity", -1);
        selected.put("pointLight", -1);
        selected.put("spotLight", -1);
        selected.put("dirLight", -1);
        selected.put("camera", -1);
        selected.put("skybox", -1);
    }

    public void render(Scene scene, Camera camera, Renderer renderer, ShadowRenderer shadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer, Window window) throws Exception {
        List<Entity> entities = scene.getEntities();
        DirLight dirLight = scene.getDirLight();
        List<PointLight> pointLights = scene.getPointLights();
        List<SpotLight> spotLights = scene.getSpotLights();
        EquirectangularMap equirectangularMap = scene.getEquirectangularMap();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

//        ImGui.showDemoWindow();

        // Main menu bar
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySize().x, 0);
        ImGui.beginMainMenuBar();

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("New", "Ctrl+N")) {

            }
            if (ImGui.menuItem("Open", "Ctrl+O")) {

            }
            if (ImGui.menuItem("Save")) {

            }
            if (ImGui.menuItem("Save As")) {

            }
            if (ImGui.menuItem("Exit", "Ctrl+Q")) {
                System.exit(0);
            }
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Add")) {
            if (ImGui.menuItem("Empty")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Entity")) {
                        count++;
                    }
                }
                entities.add(new Entity(new Vector3f(0, 0, 0), "Entity " + count));
            }

            if (ImGui.beginMenu("2D Primitives")) {
                if (ImGui.menuItem("Quad")) {
                    Quad quad = new Quad();
                    Mesh quadMesh = new Mesh(
                        quad.getPositions(),
                        quad.getNormals(),
                        quad.getTexCoords(),
                        quad.getIndices()
                    );

                    entities.add(new Entity(new MaterialMesh(quadMesh, null)));
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("3D Primitives")) {
                if (ImGui.menuItem("Cube")) {
                    showEntityWindow = true;
                    newEntityType = "Cube";
                }
                if (ImGui.menuItem("Cylinder")) {
                    showEntityWindow = true;
                    newEntityType = "Cylinder";
                }
                if (ImGui.menuItem("Sphere")) {
                    showEntityWindow = true;
                    newEntityType = "Sphere";
                }
                ImGui.endMenu();
            }

            if (ImGui.menuItem("Import Model")) {
                String modelPath = openSingle("obj,fbx,gltf,glb");
                String texturesPath = openFolder();

                if (modelPath != null && texturesPath != null) {
                    MaterialMesh[] meshes = ModelLoader.load(modelPath, texturesPath);
                    entities.add(new Entity(meshes));
                }
            }

            if (ImGui.beginMenu("Lights")) {
                if (pointLights.size() < 8) {
                    if (ImGui.menuItem("Point Light")) {
                        showPointLightWindow = true;
                    }
                } else {
                    ImGui.menuItem("Point Light", null, false, false);
                }

                if (spotLights.size() < 4) {
                    if (ImGui.menuItem("Spot Light")) {
                        showSpotLightWindow = true;
                    }
                } else {
                    ImGui.menuItem("Spot Light", null, false, false);
                }

                ImGui.endMenu();
            }

            ImGui.endMenu();
        }

        if (ImGui.menuItem("Settings")) {
            showSettingsWindow = true;
        }

        ImGui.sameLine(ImGui.getWindowSize().x - 70);
        ImGui.text("FPS: " + window.getFPS());

        ImGui.endMainMenuBar();

        if (showEntityWindow) {
            ImGui.begin("New Entity", ImGuiWindowFlags.AlwaysAutoResize);

            float[] position = Utils.vector3fToArray(newEntity.getPosition());
            float[] rotation = Utils.vector3fToArray(newEntity.getRotation());
            ImFloat scale = new ImFloat(newEntity.getScale());

            // Generate default name
            if (Objects.equals(newEntityType, "Cube")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Cube") && entity.getMaterialMeshes() != null) {
                        count++;
                    }
                }
                newEntity.setName("Cube " + count);
            }
            if (Objects.equals(newEntityType, "Cylinder")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Cylinder") && entity.getMaterialMeshes() != null) {
                        count++;
                    }
                }
                newEntity.setName("Cylinder " + count);
            }
            if (Objects.equals(newEntityType, "Sphere")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Sphere") && entity.getMaterialMeshes() != null) {
                        count++;
                    }
                }
                newEntity.setName("Sphere " + count);
            }

            ImString name = new ImString(newEntity.getName());

            ImGui.inputText("Name", name);
            ImGui.inputFloat3("Position", position);
            ImGui.inputFloat3("Rotation", rotation);
            ImGui.inputFloat("Scale", scale);

            newEntity.setPosition(Utils.arrayToVector3f(position));
            newEntity.setRotation(Utils.arrayToVector3f(rotation));
            newEntity.setScale(scale.get());
            newEntity.setName(name.get());

            if (Objects.equals(newEntityType, "Cylinder")) {
                ImFloat topRadius = new ImFloat(newCylinder.getTopRadius());
                ImFloat bottomRadius = new ImFloat(newCylinder.getBottomRadius());
                ImFloat height = new ImFloat(newCylinder.getHeight());
                ImInt sectors = new ImInt(newCylinder.getSectors());

                ImGui.separator();
                ImGui.inputFloat("Top Radius", topRadius);
                ImGui.inputFloat("Bottom Radius", bottomRadius);
                ImGui.inputFloat("Height", height);
                ImGui.inputInt("Sectors", sectors);

                newCylinder.setTopRadius(topRadius.get());
                newCylinder.setBottomRadius(bottomRadius.get());
                newCylinder.setHeight(height.get());
                newCylinder.setSectors(sectors.get());
            } else if (Objects.equals(newEntityType, "Sphere")) {
                ImFloat radius = new ImFloat(newSphere.getRadius());
                ImInt sectors = new ImInt(newSphere.getSectors());
                ImInt stacks = new ImInt(newSphere.getStacks());

                ImGui.separator();
                ImGui.inputFloat("Radius", radius);
                ImGui.inputInt("Sectors", sectors);
                ImGui.inputInt("Stacks", stacks);

                newSphere.setRadius(radius.get());
                newSphere.setSectors(sectors.get());
                newSphere.setStacks(stacks.get());
            }

            if (ImGui.button("Add", 120, 0)) {
                if (Objects.equals(newEntityType, "Cube")) {
                    Cube cube = new Cube();
                    Mesh cubeMesh = new Mesh(
                        cube.getPositions(),
                        cube.getNormals(),
                        cube.getTexCoords(),
                        cube.getIndices()
                    );

                    entities.add(new Entity(
                        new MaterialMesh(cubeMesh, null),
                        Utils.arrayToVector3f(position),
                        Utils.arrayToVector3f(rotation),
                        scale.get(),
                        name.get()
                    ));
                } else if (Objects.equals(newEntityType, "Cylinder")) {
                    newCylinder.update();
                    Mesh cylinderMesh = new Mesh(
                        newCylinder.getPositions(),
                        newCylinder.getNormals(),
                        newCylinder.getTexCoords(),
                        newCylinder.getIndices()
                    );

                    entities.add(new Entity(
                        new MaterialMesh(cylinderMesh, null),
                        Utils.arrayToVector3f(position),
                        Utils.arrayToVector3f(rotation),
                        scale.get(),
                        name.get()
                    ));
                } else if (Objects.equals(newEntityType, "Sphere")) {
                    newSphere.update();
                    Mesh sphereMesh = new Mesh(
                        newSphere.getPositions(),
                        newSphere.getNormals(),
                        newSphere.getTexCoords(),
                        newSphere.getIndices()
                    );

                    entities.add(new Entity(
                        new MaterialMesh(sphereMesh, null),
                        Utils.arrayToVector3f(position),
                        Utils.arrayToVector3f(rotation),
                        scale.get(),
                        name.get()
                    ));
                }

                showEntityWindow = false;

                newEntity = new Entity(
                    new Vector3f(0, 0, 0),
                    new Vector3f(0, 0, 0),
                    1,
                    ""
                );
                newCylinder = new Cylinder(1, 1, 1, 32);
                newSphere = new UVSphere(1, 32, 32);
            }

            if (ImGui.button("Cancel", 120, 0)) {
                showEntityWindow = false;
                newEntity = new Entity(
                    new Vector3f(0, 0, 0),
                    new Vector3f(0, 0, 0),
                    1,
                    ""
                );
                newCylinder = new Cylinder(1, 1, 1, 32);
                newSphere = new UVSphere(1, 32, 32);
            }

            ImGui.end();
        }

        if (showPointLightWindow) {
            ImGui.begin("Point Light", ImGuiWindowFlags.AlwaysAutoResize);

            float[] position = Utils.vector3fToArray(newPointLight.getPosition());
            float[] color = Utils.vector3fToArray(newPointLight.getColor());
            float[] intensity = new float[] { newPointLight.getIntensity() };

            if (ImGui.dragFloat3("Position", position, 0.1f)) newPointLight.setPosition(Utils.arrayToVector3f(position));
            if (ImGui.colorPicker3("Color: ", color)) newPointLight.setColor(Utils.arrayToVector3f(color));
            if (ImGui.dragFloat("Intensity: ", intensity, 0.1f, 0f, Float.MAX_VALUE)) newPointLight.setIntensity(intensity[0]);

            if (ImGui.button("Add", 120, 0)) {
                pointLights.add(new PointLight(newPointLight.getPosition(), newPointLight.getColor(), newPointLight.getIntensity()));
                showPointLightWindow = false;
                newPointLight = new PointLight(
                    new Vector3f(0, 0, 0),
                    new Vector3f(1, 1, 1),
                    1
                );
            }

            if (ImGui.button("Cancel", 120, 0)) {
                showPointLightWindow = false;
                newPointLight = new PointLight(
                    new Vector3f(0, 0, 0),
                    new Vector3f(1, 1, 1),
                    1
                );
            }

            ImGui.end();
        }

        if (showSpotLightWindow) {
            ImGui.begin("Spot Light", ImGuiWindowFlags.AlwaysAutoResize);

            float[] position = Utils.vector3fToArray(newSpotLight.getPosition());
            float[] direction = Utils.vector3fToArray(newSpotLight.getDirection());
            float[] cutoff = new float[] { newSpotLight.getCutoff() };
            float[] outerCutoff = new float[] { newSpotLight.getOuterCutoff() };
            float[] color = Utils.vector3fToArray(newSpotLight.getColor());
            float[] intensity = new float[] { newSpotLight.getIntensity() };

            if (ImGui.dragFloat3("Position", position, 0.1f)) newSpotLight.setPosition(Utils.arrayToVector3f(position));
            if (ImGui.dragFloat3("Direction", direction, 0.1f, -1, 1)) newSpotLight.setDirection(Utils.arrayToVector3f(direction));
            if (ImGui.colorPicker3("Color: ", color)) newSpotLight.setColor(Utils.arrayToVector3f(color));
            if (ImGui.dragFloat("Intensity: ", intensity, 0.1f, 0f, Float.MAX_VALUE)) newSpotLight.setIntensity(intensity[0]);
            if (ImGui.dragFloat("Cutoff: ", cutoff, 0.1f, 0f, 180f)) newSpotLight.setCutoff(cutoff[0]);
            if (ImGui.dragFloat("Outer Cutoff: ", outerCutoff, 0.1f, 0f, 180f)) newSpotLight.setOuterCutoff(outerCutoff[0]);

            if (ImGui.button("Add", 120, 0)) {
                spotLights.add(new SpotLight(newSpotLight.getPosition(), newSpotLight.getDirection(), newSpotLight.getCutoff(), newSpotLight.getOuterCutoff(), newSpotLight.getColor(), newSpotLight.getIntensity()));
                showSpotLightWindow = false;
                newSpotLight = new SpotLight(
                    new Vector3f(0, 0, 0),
                    new Vector3f(1, 0, 0),
                    (float) Math.cos(Math.toRadians(12.5f)),
                    (float) Math.cos(Math.toRadians(15.0f)),
                    new Vector3f(1, 1, 1),
                    1
                );
            }

            if (ImGui.button("Cancel", 120, 0)) {
                showSpotLightWindow = false;
                newSpotLight = new SpotLight(
                    new Vector3f(0, 0, 0),
                    new Vector3f(1, 0, 0),
                    (float) Math.cos(Math.toRadians(12.5f)),
                    (float) Math.cos(Math.toRadians(15.0f)),
                    new Vector3f(1, 1, 1),
                    1
                );
            }

            ImGui.end();
        }

        if (showSettingsWindow) {
            ImGui.begin("Settings", ImGuiWindowFlags.AlwaysAutoResize);

            if (ImGui.beginTabBar("SettingsTabBar")) {
                if (ImGui.beginTabItem("General")) {
                    ImGui.text("General settings");
                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem("Graphics")) {
                    ImGui.text("Shaders");

                    // Shader selection
                    String[] shaders = { "LearnOpenGL", "Filament", "Frostbite" };
                    int[] currentShader = new int[] { renderer.getCurrentShader().ordinal() };
                    if (ImGui.beginCombo("Shader", shaders[currentShader[0]])) {
                        for (int i = 0; i < shaders.length; i++) {
                            boolean isSelected = currentShader[0] == i;
                            if (ImGui.selectable(shaders[i], isSelected)) {
                                currentShader[0] = i;
                                renderer.setCurrentShader(Renderer.Shader.values()[i]);
                            }

                            if (isSelected) ImGui.setItemDefaultFocus();
                        }
                        ImGui.endCombo();
                    }

                    // Settings
                    ImGui.text("Settings");
                    ShaderSettings settings = renderer.getShaderSettings();

                    // Occlusion
                    ImBoolean specularOcclusion = new ImBoolean(settings.isSpecularOcclusion());
                    if (ImGui.checkbox("Specular Occlusion", specularOcclusion)) {
                        settings.setSpecularOcclusion(specularOcclusion.get());
                    }

                    ImBoolean horizonSpecularOcclusion = new ImBoolean(settings.isHorizonSpecularOcclusion());
                    if (ImGui.checkbox("Horizon Specular Occlusion", horizonSpecularOcclusion)) {
                        settings.setHorizonSpecularOcclusion(horizonSpecularOcclusion.get());
                    }

                    // Shadow mapping
                    ImGui.separator();
                    ImGui.text("Directional shadow mapping");
                    float[] nearPlane = new float[] {shadowRenderer.getNearPlane()};
                    float[] farPlane = new float[] {shadowRenderer.getFarPlane()};
                    float[] size = new float[] {shadowRenderer.getSize()};
                    float[] shadowMinBias = new float[] {settings.getShadowMinBias()};
                    float[] shadowMaxBias = new float[] {settings.getShadowMaxBias()};

                    if (ImGui.dragFloat("Near plane##Directional", nearPlane, 0.1f, 0f, Float.MAX_VALUE)) {
                        shadowRenderer.setNearPlane(nearPlane[0]);
                    }
                    if (ImGui.dragFloat("Far plane##Directional", farPlane, 0.1f, 0f, Float.MAX_VALUE)) {
                        shadowRenderer.setFarPlane(farPlane[0]);
                    }
                    if (ImGui.dragFloat("Size", size, 0.1f, 0f, Float.MAX_VALUE)) {
                        shadowRenderer.setSize(size[0]);
                    }
                    if (ImGui.dragFloat("Minimum shadow bias", shadowMinBias, 0.00001f)) {
                        settings.setShadowMinBias(shadowMinBias[0]);
                    }
                    if (ImGui.dragFloat("Maximum shadow bias", shadowMaxBias, 0.0001f)) {
                        settings.setShadowMaxBias(shadowMaxBias[0]);
                    }

                    ImGui.separator();
                    ImGui.text("Point shadow mapping");
                    ImBoolean pointShadows = new ImBoolean(settings.isPointShadows());
                    float[] pointNearPlane = new float[] {omnidirectionalShadowRenderer.getNearPlane()};
                    float[] pointFarPlane = new float[] {omnidirectionalShadowRenderer.getFarPlane()};
                    float[] pointShadowBias = new float[] {settings.getPointShadowBias()};

                    if (ImGui.checkbox("Point shadows", pointShadows)) {
                        settings.setPointShadows(pointShadows.get());
                    }
                    if (ImGui.dragFloat("Near plane##Point", pointNearPlane, 0.1f, 0f, Float.MAX_VALUE)) {
                        omnidirectionalShadowRenderer.setNearPlane(pointNearPlane[0]);
                    }
                    if (ImGui.dragFloat("Far plane##Point", pointFarPlane, 0.1f, 0f, Float.MAX_VALUE)) {
                        omnidirectionalShadowRenderer.setFarPlane(pointFarPlane[0]);
                    }
                    if (ImGui.dragFloat("Point shadow bias", pointShadowBias, 0.001f, 0f, Float.MAX_VALUE)) {
                        settings.setPointShadowBias(pointShadowBias[0]);
                    }

                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }

            if (ImGui.button("Close", 120, 0)) {
                showSettingsWindow = false;
            }

            ImGui.end();
        }

        // Hierarchy window
        ImGui.setNextWindowPos(PADDING, 2 * PADDING, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(HIERARCHY_WIDTH, HIERARCHY_HEIGHT, ImGuiCond.FirstUseEver);
        ImGui.begin("Hierarchy");

        if (ImGui.treeNode("Entities")) {
            for (Entity entity : entities) {
                if (entity.getParent() == null) {
                    drawTree(entity);
                }
            }
            ImGui.treePop();
        }

        if (ImGui.treeNode("Lights")) {
            for (int i = 0; i < pointLights.size(); i++) {
                if (ImGui.selectable("Point Light " + (i + 1), selected.get("pointLight") == i)) setSelected("pointLight", i);
            }
            for (int i = 0; i < spotLights.size(); i++) {
                if (ImGui.selectable("Spot Light " + (i + 1), selected.get("spotLight") == i)) setSelected("spotLight", i);
            }
            if (ImGui.selectable("Directional Light", selected.get("dirLight") == 0)) setSelected("dirLight", 0);
            ImGui.treePop();
        }

        if (ImGui.selectable("Camera", selected.get("camera") == 0)) setSelected("camera", 0);

        if (ImGui.selectable("Skybox", selected.get("skybox") == 0)) setSelected("skybox", 0);

        ImGui.end();

        // Inspector window
        ImGui.setNextWindowPos(window.getWidth() - INSPECTOR_WIDTH - PADDING, 2 * PADDING, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(INSPECTOR_WIDTH, INSPECTOR_HEIGHT, ImGuiCond.FirstUseEver);
        ImGui.begin("Inspector", ImGuiWindowFlags.AlwaysAutoResize);

        if (selectedEntity != null && selected.get("entity") != -1) {
            Entity entity = selectedEntity;

            if (entity.getMaterialMeshes() == null) {
                // Placeholder parent entity
                float[] position = Utils.vector3fToArray(entity.getPosition());
                ImString name = new ImString(entity.getName(), 128);

                if (entity.getName() != null && !Objects.equals(entity.getName(), "null")) {
                    ImGui.text(entity.getName());
                } else {
                    ImGui.text("Entity " + (selected.get("entity") + 1));
                }

                ImGui.separator();
                if (ImGui.dragFloat3("Position", position, 0.1f)) entity.setPosition(new Vector3f(position[0], position[1], position[2]));
                if (ImGui.inputText("Name", name)) entity.setName(name.get());
            } else {
                float[] position = Utils.vector3fToArray(entity.getPosition());
                float[] rotation = Utils.vector3fToArray(entity.getRotation());
                float[] scale = new float[] {entity.getScale()};
                ImString name = new ImString(entity.getName(), 128);
                ImBoolean focused = new ImBoolean(camera.getFocus() == entity);

                PBRMaterial pbrMaterial = entity.getMaterialMeshes()[0].getPbrMaterial();

                if (entity.getName() != null && !Objects.equals(entity.getName(), "null")) {
                    ImGui.text(entity.getName());
                } else {
                    ImGui.text("Entity " + (selected.get("entity") + 1));
                }

                ImGui.separator();
                if (ImGui.dragFloat3("Position: ", position, 0.1f)) entity.setPosition(new Vector3f(position[0], position[1], position[2]));
                ImGui.sameLine();
                if (ImGui.button("Reset##Position")) entity.setPosition(new Vector3f(0, 0, 0));
                if (ImGui.dragFloat3("Rotation: ", rotation, 0.1f)) entity.setRotation(new Vector3f(rotation[0], rotation[1], rotation[2]));
                ImGui.sameLine();
                if (ImGui.button("Reset##Rotation")) entity.setRotation(new Vector3f(0, 0, 0));
                if (ImGui.dragFloat("Scale: ", scale, 0.1f)) entity.setScale(scale[0]);
                if (ImGui.inputText("Name: ", name)) entity.setName(name.get());
                if (ImGui.checkbox("Focused: ", focused)) camera.setFocus(focused.get() ? entity : null);

                if (pbrMaterial != null) {
                    if (ImGui.button("Change material")) {
                        showMaterialWindow = true;
                    }
                }
            }

            // Components
            ImGui.separator();
            ImGui.text("Components");
            ImGui.separator();

            Movement movement = entity.getMovement();
            if (movement != null) {
                ImGui.text("Movement");

                ImInt currentItem = new ImInt(entity.getMovement().getType().ordinal());
                String[] items = { "None", "Orbit", "Direction", "Points", "Keyboard" };
                if (ImGui.beginCombo("Movement type", items[currentItem.get()])) {
                    for (int i = 0; i < items.length; i++) {
                        boolean isSelected = currentItem.get() == i;
                        if (ImGui.selectable(items[i], isSelected)) {
                            currentItem.set(i);
                            switch (i) {
                                case 0 -> movement.setType(Movement.Type.NONE);  // Preserve the movement attributes
                                case 1 -> {
                                    movement.setType(Movement.Type.ORBIT);
                                    if (movement.getCenter() == null) movement.setCenter(new Vector3f(0, 0, 0));
                                    if (movement.getAxis() == null) movement.setAxis(new Vector3f(0, 1, 0));
                                    if (movement.getRadius() == 0) movement.setRadius(1);
                                    if (movement.getAnglePerSecond() == 0) movement.setAnglePerSecond((float) Math.toRadians(90.0f));
                                }
                                case 2 -> {
                                    movement.setType(Movement.Type.DIRECTION);
                                    if (movement.getSpeed() == 0) movement.setSpeed(1);
                                    if (movement.getOrigin() == null) movement.setOrigin(new Vector3f(0, 0, 0));
                                    if (movement.getDirection() == null) movement.setDirection(new Vector3f(0, 0, 1));
                                    if (movement.getDistance() == 0) movement.setDistance(0);
                                }
                                case 3 -> {
                                    movement.setType(Movement.Type.POINTS);
                                    if (movement.getSpeed() == 0) movement.setSpeed(1);
                                    if (movement.getPoints() == null) movement.setPoints(new ArrayList<>() {});
                                }
                                case 4 -> {
                                    movement.setType(Movement.Type.KEYBOARD);
                                    if (movement.getSpeed() == 0) movement.setSpeed(1);
                                    if (movement.getDeceleration() == 0) movement.setDeceleration(0.95f);
                                }
                            }
                        }
                        if (isSelected) {
                            ImGui.setScrollHereY();
                        }
                    }
                    ImGui.endCombo();
                }

                if (movement.getType() == Movement.Type.ORBIT) {
                    float[] center = Utils.vector3fToArray(movement.getCenter());
                    float[] axis = Utils.vector3fToArray(movement.getAxis());
                    float[] radius = new float[] { movement.getRadius() };
                    float[] speed = new float[] { movement.getSpeed() };
                    float[] anglePerSecond = new float[] { (float) Math.toDegrees(movement.getAnglePerSecond()) };
                    ImBoolean pointTowardsCenter = new ImBoolean(movement.isPointTowardsCenter());

                    if (ImGui.dragFloat3("Center: ", center, 0.1f)) movement.setCenter(new Vector3f(center[0], center[1], center[2]));
                    if (ImGui.dragFloat3("Axis: ", axis, 0.1f)) movement.setAxis(new Vector3f(axis[0], axis[1], axis[2]));
                    if (ImGui.dragFloat("Radius: ", radius, 0.1f, 0.01f, Float.MAX_VALUE)) movement.setRadius(radius[0]);
                    if (ImGui.dragFloat("Speed: ", speed, 0.1f, 0.01f, Float.MAX_VALUE)) movement.setSpeed(speed[0]);
                    if (ImGui.dragFloat("Angle per second: ", anglePerSecond, 0.1f)) movement.setAnglePerSecond((float) Math.toRadians(anglePerSecond[0]));
                    if (ImGui.checkbox("Point towards center: ", pointTowardsCenter)) movement.setPointTowardsCenter(pointTowardsCenter.get());
                } else if (movement.getType() == Movement.Type.DIRECTION) {
                    ImInt currentMode = new ImInt(entity.getMovement().getMode().ordinal());
                    String[] modes = { "Constant", "Acceleration", "Deceleration" };
                    if (ImGui.beginCombo("Movement mode", modes[currentMode.get()])) {
                        for (int i = 0; i < modes.length; i++) {
                            boolean isSelected = currentMode.get() == i;
                            if (ImGui.selectable(modes[i], isSelected)) {
                                currentMode.set(i);
                                switch (i) {
                                    case 0 -> movement.setMode(Movement.Mode.CONSTANT);
                                    case 1 -> movement.setMode(Movement.Mode.ACCELERATION);
                                    case 2 -> movement.setMode(Movement.Mode.DECELERATION);
                                }
                            }
                            if (isSelected) {
                                ImGui.setScrollHereY();
                            }
                        }
                        ImGui.endCombo();
                    }

                    float[] origin = Utils.vector3fToArray(movement.getOrigin());
                    float[] direction = Utils.vector3fToArray(movement.getDirection());
                    float[] speed = new float[] { movement.getSpeed() };
                    float[] acceleration = new float[] { movement.getAcceleration() };
                    float[] distance = new float[] { movement.getDistance() };
                    ImBoolean noMaxDistance = new ImBoolean(movement.isNoMaxDistance());
                    ImBoolean stopAtZeroSpeed = new ImBoolean(movement.isStopAtZeroSpeed());

                    if (ImGui.dragFloat3("Origin: ", origin, 0.1f)) movement.setOrigin(new Vector3f(origin[0], origin[1], origin[2]));
                    if (ImGui.dragFloat3("Direction: ", direction, 0.1f)) movement.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movement.setSpeed(speed[0]);
                    if (movement.getMode() == Movement.Mode.ACCELERATION) {
                        if (ImGui.dragFloat("Acceleration: ", acceleration, 0.1f)) movement.setAcceleration(acceleration[0]);
                    } else if (movement.getMode() == Movement.Mode.DECELERATION) {
                        if (ImGui.dragFloat("Deceleration: ", acceleration, 0.1f)) movement.setAcceleration(acceleration[0]);
                    }
                    if (movement.getMode() != Movement.Mode.CONSTANT) {
                        if (ImGui.checkbox("Stop at zero speed: ", stopAtZeroSpeed)) movement.setStopAtZeroSpeed(stopAtZeroSpeed.get());
                    }
                    if (!movement.isNoMaxDistance()) {
                        if (ImGui.dragFloat("Distance: ", distance, 0.1f)) movement.setDistance(distance[0]);
                    }
                    if (ImGui.checkbox("No max distance: ", noMaxDistance)) movement.setNoMaxDistance(noMaxDistance.get());
                    if (ImGui.button("Reset##Origin", 60, 0)) entity.setPosition(new Vector3f(movement.getOrigin()));
                } else if (movement.getType() == Movement.Type.POINTS) {
                    float[] speed = new float[] { movement.getSpeed() };
                    List<Vector3f> points = movement.getPoints();

                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movement.setSpeed(speed[0]);

                    ImGui.text("Points");
                    for (int i = 0; i < points.size(); i++) {
                        float[] point = Utils.vector3fToArray(points.get(i));
                        if (ImGui.dragFloat3("Point " + (i + 1), point, 0.1f)) movement.setPoint(i, new Vector3f(point[0], point[1], point[2]));
                        ImGui.sameLine();
                        if (ImGui.button("Remove")) movement.removePoint(i);
                    }

                    if (ImGui.button("Add point")) {
                        movement.addPoint(new Vector3f(0, 0, 0));
                    }
                } else if (movement.getType() == Movement.Type.KEYBOARD) {
                    float[] speed = new float[] { movement.getSpeed() };
                    float[] deceleration = new float[] { movement.getDeceleration() };

                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movement.setSpeed(speed[0]);
                    if (ImGui.dragFloat("Deceleration: ", deceleration, 0.1f)) movement.setDeceleration(deceleration[0]);
                }
            }

            RotationController rotationController = entity.getRotationController();
            if (rotationController != null) {
                ImGui.separator();
                ImGui.text("Rotation");
                ImGui.separator();

                ImInt currentMode = new ImInt(rotationController.getMode().ordinal());
                String[] modes = { "None", "Constant", "Acceleration", "Deceleration" };
                if (ImGui.beginCombo("Rotation mode", modes[currentMode.get()])) {
                    for (int i = 0; i < modes.length; i++) {
                        boolean isSelected = currentMode.get() == i;
                        if (ImGui.selectable(modes[i], isSelected)) {
                            currentMode.set(i);
                            switch (i) {
                                case 0 -> rotationController.setMode(RotationController.Mode.NONE);
                                case 1 -> rotationController.setMode(RotationController.Mode.CONSTANT);
                                case 2 -> rotationController.setMode(RotationController.Mode.ACCELERATION);
                                case 3 -> rotationController.setMode(RotationController.Mode.DECELERATION);
                            }
                        }
                        if (isSelected) {
                            ImGui.setScrollHereY();
                        }
                    }
                    ImGui.endCombo();
                }

                if (rotationController.getMode() == RotationController.Mode.CONSTANT) {
                    float[] speed = Utils.vector3fToArray(rotationController.getSpeed());
                    if (ImGui.dragFloat3("Speed: ", speed, 0.1f)) rotationController.setSpeed(new Vector3f(speed[0], speed[1], speed[2]));
                } else if (rotationController.getMode() == RotationController.Mode.ACCELERATION) {
                    float[] speed = Utils.vector3fToArray(rotationController.getSpeed());
                    float[] acceleration = Utils.vector3fToArray(rotationController.getAcceleration());
                    ImBoolean stopAtZeroSpeed = new ImBoolean(rotationController.isStopAtZeroSpeed());
                    if (ImGui.dragFloat3("Speed: ", speed, 0.1f)) rotationController.setSpeed(new Vector3f(speed[0], speed[1], speed[2]));
                    if (ImGui.dragFloat3("Acceleration: ", acceleration, 0.1f)) rotationController.setAcceleration(new Vector3f(acceleration[0], acceleration[1], acceleration[2]));
                    if (ImGui.checkbox("Stop at zero speed: ", stopAtZeroSpeed)) rotationController.setStopAtZeroSpeed(stopAtZeroSpeed.get());
                } else if (rotationController.getMode() == RotationController.Mode.DECELERATION) {
                    float[] speed = Utils.vector3fToArray(rotationController.getSpeed());
                    float[] acceleration = Utils.vector3fToArray(rotationController.getAcceleration());
                    ImBoolean stopAtZeroSpeed = new ImBoolean(rotationController.isStopAtZeroSpeed());
                    if (ImGui.dragFloat3("Speed: ", speed, 0.1f)) rotationController.setSpeed(new Vector3f(speed[0], speed[1], speed[2]));
                    if (ImGui.dragFloat3("Deceleration: ", acceleration, 0.1f)) rotationController.setAcceleration(new Vector3f(acceleration[0], acceleration[1], acceleration[2]));
                    if (ImGui.checkbox("Stop at zero speed: ", stopAtZeroSpeed)) rotationController.setStopAtZeroSpeed(stopAtZeroSpeed.get());
                }
            }

            ImGui.separator();
            if (ImGui.button("Add component")) {
                ImGui.openPopup("Add component");
            }

            if (ImGui.beginPopup("Add component")) {
                if (ImGui.menuItem("Movement")) {
                    entity.setMovement(Movement.none());
                }
                if (ImGui.menuItem("Rotation")) {
                    entity.setRotationController(new RotationController());
                }
                ImGui.endPopup();
            }

            if (ImGui.button("Remove")) {
                entities.remove(entity);
                selected.put("entity", -1);
            }
        }

        if (selected.get("pointLight") != -1) {
            PointLight light = pointLights.get(selected.get("pointLight"));

            float[] position = Utils.vector3fToArray(light.getPosition());
            float[] color = Utils.vector3fToArray(light.getColor());
            float[] intensity = new float[] { light.getIntensity() };

            ImGui.text("Point Light " + (selected.get("pointLight") + 1));
            if (ImGui.dragFloat3("Position: ", position, 0.1f)) light.setPosition(new Vector3f(position[0], position[1], position[2]));
            if (ImGui.button("Set to camera position")) light.setPosition(camera.getPosition());
            if (ImGui.colorPicker3("Color: ", color)) light.setColor(new Vector3f(color[0], color[1], color[2]));
            if (ImGui.dragFloat("Intensity: ", intensity, 0.1f, 0f, Float.MAX_VALUE)) light.setIntensity(intensity[0]);

            if (ImGui.button("Remove")) {
                pointLights.remove(light);
                selected.put("pointLight", -1);
            }
        }

        if (selected.get("spotLight") != -1) {
            SpotLight light = spotLights.get(selected.get("spotLight"));

            float[] position = Utils.vector3fToArray(light.getPosition());
            float[] direction = Utils.vector3fToArray(light.getDirection());
            float[] azimuth = new float[] { light.getAzimuth() };
            float[] elevation = new float[] { light.getElevation() };
            float[] color = Utils.vector3fToArray(light.getColor());
            float[] intensity = new float[] { light.getIntensity() };
            float[] cutoff = new float[] { light.getCutoff() };
            float[] outerCutoff = new float[] { light.getOuterCutoff() };
            ImBoolean enabled = new ImBoolean(light.isEnabled());

            ImGui.text("Spot Light " + (selected.get("spotLight") + 1));
            if (ImGui.dragFloat3("Position: ", position, 0.1f)) light.setPosition(new Vector3f(position[0], position[1], position[2]));
            if (ImGui.button("Set to camera position")) light.setPosition(camera.getPosition());
            if (ImGui.dragFloat3("Direction: ", direction, 0.01f, -1f, 1f))  light.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
            if (ImGui.button("Set to camera direction")) light.setDirection(camera.getFront());
            if (ImGui.dragFloat("Azimuth: ", azimuth, 0.2f, 0f, 360f)) light.setAzimuth(azimuth[0]);
            if (ImGui.dragFloat("Elevation: ", elevation, 0.1f, -90f, 90f)) light.setElevation(elevation[0]);
            if (ImGui.colorPicker3("Color: ", color)) light.setColor(new Vector3f(color[0], color[1], color[2]));
            if (ImGui.dragFloat("Intensity: ", intensity, 0.1f, 0f, Float.MAX_VALUE)) light.setIntensity(intensity[0]);
            if (ImGui.dragFloat("Cutoff: ", cutoff, 0.1f, 0f, 180f)) light.setCutoff(cutoff[0]);
            if (ImGui.dragFloat("Outer Cutoff: ", outerCutoff, 0.1f, 0f, 180f)) light.setOuterCutoff(outerCutoff[0]);
            if (ImGui.checkbox("Enabled: ", enabled)) light.setEnabled(enabled.get());

            if (ImGui.button("Remove")) {
                // If index is not zero
                if (selected.get("spotLight") != 0) {
                    spotLights.remove(light);
                    selected.put("spotLight", -1);
                } else {
                    // If index is zero, disable the light instead
                    light.setEnabled(false);
                }
            }
        }

        if (selected.get("dirLight") != -1) {
            float[] direction = Utils.vector3fToArray(dirLight.getDirection());
            float[] color = Utils.vector3fToArray(dirLight.getColor());

            ImGui.text("Directional Light");
            if (ImGui.dragFloat3("Direction: ", direction, 0.1f)) dirLight.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
            if (ImGui.colorPicker3("Color: ", color)) dirLight.setColor(new Vector3f(color[0], color[1], color[2]));
        }

        if (selected.get("camera") != -1) {
            float[] position = Utils.vector3fToArray(camera.getPosition());
            float[] FOV = new float[] {(float) Math.toDegrees(camera.getFOV())};
            float[] exposure = new float[] { renderer.getExposure() };
            float[] yaw = new float[] { camera.getYaw() };
            float[] pitch = new float[] { camera.getPitch() };
            float[] movementSpeed = new float[] { camera.getMovementSpeed() };
            float[] mouseSensitivity = new float[] { camera.getMouseSensitivity() };
            float[] deceleration = new float[] { camera.getDeceleration() };
            float[] zNear = new float[] { renderer.getzNear() };
            float[] zFar = new float[] { renderer.getzFar() };
            ImBoolean wireframe = new ImBoolean(renderer.isWireframe());
            ImBoolean toneMapping = new ImBoolean(renderer.isToneMapping());

            ImGui.text("Camera");
            if (ImGui.dragFloat3("Position: ", position, 0.1f)) camera.setPosition(new Vector3f(position[0], position[1], position[2]));
            if (ImGui.dragFloat("FOV: ", FOV, 0.1f, 10.0f, 180f)) camera.setFOV((float) Math.toRadians(FOV[0]));
            if (ImGui.dragFloat("Exposure: ", exposure, 0.01f, 0.0f, 10.0f)) renderer.setExposure(exposure[0]);
            if (ImGui.dragFloat("Yaw: ", yaw, 0.1f)) camera.setYaw(yaw[0]);
            if (ImGui.dragFloat("Pitch: ", pitch, 0.1f)) camera.setPitch(pitch[0]);
            if (ImGui.dragFloat("Movement Speed: ", movementSpeed, 0.01f, 0.0f, 10.0f)) camera.setMovementSpeed(movementSpeed[0]);
            if (ImGui.dragFloat("Mouse Sensitivity: ", mouseSensitivity, 0.0001f, 0.0f, 0.1f)) camera.setMouseSensitivity(mouseSensitivity[0]);
            if (ImGui.dragFloat("Deceleration: ", deceleration, 0.001f, 0.0f, 1.0f)) camera.setDeceleration(deceleration[0]);
            if (ImGui.radioButton("Constant Movement: ", camera.getMovementMode() == Camera.MovementMode.CONSTANT)) camera.setMovementMode(Camera.MovementMode.CONSTANT);
            if (ImGui.radioButton("Smooth Movement: ", camera.getMovementMode() == Camera.MovementMode.SMOOTH)) camera.setMovementMode(Camera.MovementMode.SMOOTH);
            if (ImGui.dragFloat("zNear: ", zNear, 0.1f, 0.1f, 1000f)) renderer.setzNear(zNear[0]);
            if (ImGui.dragFloat("zFar: ", zFar, 10f, 0.1f, 100000f)) renderer.setzFar(zFar[0]);
            if (ImGui.checkbox("Wireframe: ", wireframe)) renderer.setWireframe(wireframe.get());
            if (ImGui.checkbox("Tone mapping: ", toneMapping)) renderer.setToneMapping(toneMapping.get());
        }

        if (selected.get("skybox") != -1) {
            ImGui.image(equirectangularMap.getEnvironmentCubemap(), 256, 256);
            if (ImGui.button("Change environment map")) {
                String albedoPath = openSingle("hdr");
                if (albedoPath != null) {
                    Texture backgroundTexture = new Texture(
                        albedoPath,
                        org.lwjgl.opengl.GL30.GL_RGB16F,
                        org.lwjgl.opengl.GL30.GL_RGBA,
                        org.lwjgl.opengl.GL30.GL_FLOAT,
                        true
                    );
                    equirectangularMap.updateTexture(backgroundTexture);
                }
            }

        }

        ImGui.end();

        // Material editor window
        if (showMaterialWindow) {
            ImGui.setNextWindowPos(1920 - MATERIAL_WIDTH - PADDING, window.getHeight() - MATERIAL_HEIGHT - PADDING, ImGuiCond.FirstUseEver);
            ImGui.setNextWindowSize(MATERIAL_WIDTH, MATERIAL_HEIGHT, ImGuiCond.FirstUseEver);
            ImGui.begin("Material Editor");

            if (selectedEntity != null) {
                Entity entity = selectedEntity;
                PBRMaterial pbrMaterial = entity.getMaterialMeshes()[0].getPbrMaterial();
                ImBoolean useAlbedoTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("albedo"));
                ImBoolean useNormalTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("normal"));
                ImBoolean useMetallicTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("metallic"));
                ImBoolean useRoughnessTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("roughness"));
                ImBoolean useMetallicRoughnessTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("metallicRoughness"));
                ImBoolean useAoTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("ao"));
                ImBoolean useEmissiveTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("emissive"));

                // Albedo
                ImGui.text("Albedo");
                if (pbrMaterial.getAlbedo() != null)
                    ImGui.image(pbrMaterial.getAlbedo().getID(), 128, 128);
                if (ImGui.button("Change texture##Albedo")) {
                    String albedoPath = openSingle("png,jpg,jpeg");
                    if (albedoPath != null) {
                        pbrMaterial.setAlbedo(new Texture(albedoPath, GL_SRGB_ALPHA, true));
                    }
                }

                float[] albedo = new float[] { pbrMaterial.getAlbedoColor().x, pbrMaterial.getAlbedoColor().y, pbrMaterial.getAlbedoColor().z };
                ImGui.colorPicker3("Albedo color: ", albedo);
                pbrMaterial.setAlbedoColor(new Vector3f(albedo[0], albedo[1], albedo[2]));

                if (ImGui.checkbox("Use texture##Albedo", useAlbedoTexture) && pbrMaterial.getAlbedo() != null) {
                    pbrMaterial.setUseTexture("albedo", useAlbedoTexture.get());
                }

                ImGui.newLine();

                // Normal
                ImGui.text("Normal");
                if (pbrMaterial.getNormal() != null)
                    ImGui.image(pbrMaterial.getNormal().getID(), 128, 128);
                if (ImGui.button("Change texture##Normal")) {
                    String normalPath = openSingle("png,jpg,jpeg");
                    if (normalPath != null) {
                        pbrMaterial.setNormal(new Texture(normalPath, GL_RGB, true));
                    }
                }

                if (ImGui.checkbox("Use texture##Normal", useNormalTexture) && pbrMaterial.getNormal() != null) {
                    pbrMaterial.setUseTexture("normal", useNormalTexture.get());
                }
                ImGui.newLine();

                // Metallic
                ImGui.text("Metallic");
                if (pbrMaterial.getMetallic() != null)
                    ImGui.image(pbrMaterial.getMetallic().getID(), 128, 128);
                if (ImGui.button("Change texture##Metallic")) {
                    String metallicPath = openSingle("png,jpg,jpeg");
                    if (metallicPath != null) {
                        pbrMaterial.setMetallic(new Texture(metallicPath, GL_RED, true));
                    }
                }
                float[] metallic = new float[] { pbrMaterial.getMetallicFactor() };
                if (ImGui.dragFloat("Metallic factor: ", metallic, 0.001f, 0.0f, 1.0f)) pbrMaterial.setMetallicFactor(metallic[0]);

                if (ImGui.checkbox("Use texture##Metallic", useMetallicTexture) && pbrMaterial.getMetallic() != null) pbrMaterial.setUseTexture("metallic", useMetallicTexture.get());
                ImGui.newLine();

                // Roughness
                ImGui.text("Roughness");
                if (pbrMaterial.getRoughness() != null)
                    ImGui.image(pbrMaterial.getRoughness().getID(), 128, 128);
                if (ImGui.button("Change texture##Roughness")) {
                    String roughnessPath = openSingle("png,jpg,jpeg");
                    if (roughnessPath != null) {
                        pbrMaterial.setRoughness(new Texture(roughnessPath, GL_RED, true));
                    }
                }
                float[] roughness = new float[] { pbrMaterial.getRoughnessFactor() };
                if (ImGui.dragFloat("Roughness factor: ", roughness, 0.001f, 0.0f, 1.0f)) pbrMaterial.setRoughnessFactor(roughness[0]);

                if (ImGui.checkbox("Use texture##Roughness", useRoughnessTexture) && pbrMaterial.getRoughness() != null) {
                    pbrMaterial.setUseTexture("roughness", useRoughnessTexture.get());
                }
                ImGui.newLine();

                // Metallic roughness
                ImGui.text("Metallic Roughness");
                if (pbrMaterial.getMetallicRoughness() != null)
                    ImGui.image(pbrMaterial.getMetallicRoughness().getID(), 128, 128);
                if (ImGui.button("Change texture##MetallicRoughness")) {
                    String metallicRoughnessPath = openSingle("png,jpg,jpeg");
                    if (metallicRoughnessPath != null) {
                        pbrMaterial.setMetallicRoughness(new Texture(metallicRoughnessPath, GL_RGBA, true));
                    }
                }
                if (ImGui.checkbox("Use texture##MetallicRoughness", useMetallicRoughnessTexture) && pbrMaterial.getMetallicRoughness() != null) {
                    pbrMaterial.setUseTexture("metallicRoughness", useMetallicRoughnessTexture.get());
                }
                ImGui.newLine();

                // Ambient occlusion
                ImGui.text("Ambient Occlusion");
                if (pbrMaterial.getAo() != null)
                    ImGui.image(pbrMaterial.getAo().getID(), 128, 128);
                if (ImGui.button("Change texture##AmbientOcclusion")) {
                    String aoPath = openSingle("png,jpg,jpeg");
                    if (aoPath != null) {
                        pbrMaterial.setAo(new Texture(aoPath, GL_RED, true));
                    }
                }
                if (ImGui.checkbox("Use texture##AmbientOcclusion", useAoTexture)) {
                    pbrMaterial.setUseTexture("ao", useAoTexture.get());
                }
                ImGui.newLine();

                // Emissive
                ImGui.text("Emissive");
                if (pbrMaterial.getEmissive() != null)
                    ImGui.image(pbrMaterial.getEmissive().getID(), 128, 128);
                if (ImGui.button("Change texture##Emissive")) {
                    String emissivePath = openSingle("png,jpg,jpeg");
                    if (emissivePath != null) {
                        pbrMaterial.setEmissive(new Texture(emissivePath, GL_RGB, true));
                    }
                }
                float[] emissive = new float[] { pbrMaterial.getEmissiveColor().x, pbrMaterial.getEmissiveColor().y, pbrMaterial.getEmissiveColor().z };
                if (ImGui.checkbox("Use texture##Emissive", useEmissiveTexture) && pbrMaterial.getEmissive() != null) {
                    pbrMaterial.setUseTexture("emissive", useEmissiveTexture.get());
                } else {
                    pbrMaterial.setEmissiveColor(new Vector3f(emissive[0], emissive[1], emissive[2]));
                }
            }

            ImGui.end();
        }

        // Debug window
        ImGui.setNextWindowPos(window.getWidth() - 256 - PADDING, window.getHeight() - 300 - PADDING, ImGuiCond.FirstUseEver);
        ImGui.begin("Debug");

        ImGui.inputInt("ID", debugTextureID);
        ImGui.image(debugTextureID.get(), 256, 256, 0, 1, 1, 0);
        ImGui.end();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void drawTree(Entity root) {
        int flags = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;

        String label = root.getName();

        if (root == selectedEntity) {
            flags |= ImGuiTreeNodeFlags.Selected;
        }

        if (root.getChildren().size() > 0) {
            boolean nodeOpen = ImGui.treeNodeEx(label, flags);
            if (ImGui.isItemClicked()) {
                selectedEntity = root;
                setSelected("entity", 0);
            }
            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload("entity", root);
                ImGui.text(root.getName());
                ImGui.endDragDropSource();
            }
            if (ImGui.beginDragDropTarget()) {
                Entity payload = ImGui.acceptDragDropPayload("entity", Entity.class);
                if (payload != null) {
                    if (payload.getParent() != null) {
                        payload.getParent().removeChild(payload);
                    }
                    payload.setParent(root);
                }
            }

            if (nodeOpen) {
                for (int j = 0; j < root.getChildren().size(); j++) {
                    drawTree(root.getChildren().get(j));
                }
                ImGui.treePop();
            }
        } else {
            // Need to use a default label string if the label is null, as if it is null the program will crash
            ImGui.treeNodeEx(label != null ? label : "default", flags | ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen);
            if (ImGui.isItemClicked()) {
                selectedEntity = root;
                setSelected("entity", 0);
            }
            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload("entity", root);
                ImGui.text(root.getName());
                ImGui.endDragDropSource();
            }
            if (ImGui.beginDragDropTarget()) {
                Entity payload = ImGui.acceptDragDropPayload("entity", Entity.class);
                if (payload != null) {
                    if (payload.getParent() != null) {
                        payload.getParent().removeChild(payload);
                    }
                    payload.setParent(root);
                }
            }
        }
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

    private String openSingle(String filterList) {
        PointerBuffer outPath = MemoryUtil.memAllocPointer(1);
        try {
            if (checkResult(
                NFD_OpenDialog(filterList, null, outPath),
                outPath
            )) {
                return outPath.getStringUTF8();
            }
        } finally {
            MemoryUtil.memFree(outPath);
        }

        return null;
    }

    private String openFolder() {
        PointerBuffer outPath = MemoryUtil.memAllocPointer(1);
        try {
            if (checkResult(
                NFD_PickFolder((ByteBuffer) null, outPath),
                outPath
            )) {
                return outPath.getStringUTF8();
            }
        } finally {
            MemoryUtil.memFree(outPath);
        }

        return null;
    }

    private boolean checkResult(int result, PointerBuffer path) {
        switch (result) {
            case NFD_OKAY -> {
                System.out.println("Success!");
                System.out.println(path.getStringUTF8(0));
                nNFD_Free(path.get(0));
                return true;
            }
            case NFD_CANCEL -> {
                System.out.println("User pressed cancel.");
                return false;
            }
            default -> { // NFD_ERROR
                System.err.format("Error: %s\n", NFD_GetError());
                return false;
            }
        }
    }

}
