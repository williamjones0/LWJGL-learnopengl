package io.william.renderer;

import io.william.game.component.MovementController;
import io.william.game.component.RotationController;
import io.william.io.ModelLoader;
import io.william.io.SceneExporter;
import io.william.io.SceneImporter;
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

    private final ImInt debugTextureID = new ImInt(1);

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

    private PBRMaterial newPbrMaterial = new PBRMaterial(
        "New Material",
        false,
        new Vector3f(1.0f, 1.0f, 1.0f),
        0.0f,
        1.0f,
        new Vector3f(0.0f, 0.0f, 0.0f)
    );

    // GUI configuration
    private final float PADDING = 20;
    private final float HIERARCHY_WIDTH = 200;
    private final float HIERARCHY_HEIGHT = 600;
    private final float INSPECTOR_WIDTH = 400;
    private final float INSPECTOR_HEIGHT = 600;

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
        selected.put("model", -1);
        selected.put("material", -1);
    }

    public void render(Scene scene, Camera camera, MasterRenderer masterRenderer, Renderer renderer, ShadowRenderer shadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer, Window window) throws Exception {
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
                String sceneJsonPath = openSingle("json");
                if (sceneJsonPath != null) {
                    SceneImporter.importScene(sceneJsonPath, window, scene, camera, masterRenderer, renderer, shadowRenderer, omnidirectionalShadowRenderer);
                    masterRenderer.setupBuffers(scene);
                }
            }
            if (ImGui.menuItem("Save")) {
                SceneExporter.export(scene, camera, renderer, masterRenderer.getShadowRenderer(), masterRenderer.getOmnidirectionalShadowRenderer());
            }
            if (ImGui.menuItem("Save As")) {
                String sceneJsonPath = save("json");
                if (sceneJsonPath != null) {
                    SceneExporter.export(sceneJsonPath, scene, camera, renderer, masterRenderer.getShadowRenderer(), masterRenderer.getOmnidirectionalShadowRenderer());
                }
            }
            if (ImGui.menuItem("Exit", "Ctrl+Q")) {
                System.exit(0);
            }
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Add")) {
            ImGui.menuItem("Entity", null, false, false);

            if (ImGui.menuItem("Empty")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Entity")) {
                        count++;
                    }
                }
                Entity entity = new Entity(new Vector3f(0, 0, 0), "Entity " + count);
                scene.addEntity(entity);
                masterRenderer.setupBuffers(scene);
            }

            if (ImGui.beginMenu("From Model...")) {
                for (Model model : scene.getModels()) {
                    if (ImGui.menuItem(model.getName() + " (ID: " + model.getID() + ")" + " (" + model.getEntities().size() + " usages)")) {
                        Entity entity = new Entity(new Vector3f(0, 0, 0), model.getName());
                        model.addEntity(entity);
                        scene.addEntity(entity);
                        entity.setModelID(model.getID());
                        masterRenderer.setupBuffers(scene);
                    }
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("2D Primitives")) {
                Map<Integer, Integer> meshDataMaterialIDs = new HashMap<>();
                meshDataMaterialIDs.put(0, 0);

                if (ImGui.menuItem("Quad")) {
                    Quad quad = new Quad();
                    Model quadModel = new Model(
                        new MeshData(
                            quad.getPositions(),
                            quad.getNormals(),
                            new float[]{},
                            new float[]{},
                            quad.getTexCoords(),
                            quad.getIndices()
                        ),
                        new ModelMetadata(
                            quad, meshDataMaterialIDs
                        ),
                        "Quad"
                    );

                    quadModel.getMeshDatas().get(0).setMaterialID(0);

                    Entity quadEntity = new Entity(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1f, "Quad");
                    quadModel.addEntity(quadEntity);
                    scene.addEntity(quadEntity);
                    scene.addModel(quadModel);
                    quadEntity.setModelID(quadModel.getID());

                    masterRenderer.setupBuffers(scene);
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
                    Model model = ModelLoader.load(scene, modelPath, texturesPath, null);
                    Entity entity = new Entity(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1f, "3D Model");
                    model.addEntity(entity);
                    scene.addEntity(entity);
                    scene.addModel(model);
                    entity.setModelID(model.getID());

                    masterRenderer.setupBuffers(scene);
                }
            }

            ImGui.separator();
            ImGui.menuItem("Light", null, false, false);

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

            ImGui.separator();
            ImGui.menuItem("Material", null, false, false);

            if (ImGui.menuItem("PBR Material")) {
                showMaterialWindow = true;
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
                    if (entity.getName().startsWith("Cube") && entity.getModelID() != -1) {
                        count++;
                    }
                }
                newEntity.setName("Cube " + count);
            }
            if (Objects.equals(newEntityType, "Cylinder")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Cylinder") && entity.getModelID() != -1) {
                        count++;
                    }
                }
                newEntity.setName("Cylinder " + count);
            }
            if (Objects.equals(newEntityType, "Sphere")) {
                int count = 0;
                for (Entity entity : entities) {
                    if (entity.getName().startsWith("Sphere") && entity.getModelID() != -1) {
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
                Map<Integer, Integer> meshDataMaterialIDs = new HashMap<>();
                meshDataMaterialIDs.put(0, 0);

                if (Objects.equals(newEntityType, "Cube")) {
                    Cube cube = new Cube();
                    Model cubeModel = new Model(
                        new MeshData(
                            cube.getPositions(),
                            cube.getNormals(),
                            new float[]{},
                            new float[]{},
                            cube.getTexCoords(),
                            cube.getIndices()
                        ),
                        new ModelMetadata(
                            cube, meshDataMaterialIDs
                        ),
                        "Cube"
                    );

                    cubeModel.getMeshDatas().get(0).setMaterialID(0);

                    Entity cubeEntity = new Entity(Utils.arrayToVector3f(position), Utils.arrayToVector3f(rotation), scale.get(), name.get());
                    cubeModel.addEntity(cubeEntity);
                    scene.addEntity(cubeEntity);
                    scene.addModel(cubeModel);
                    cubeEntity.setModelID(cubeModel.getID());

                    masterRenderer.setupBuffers(scene);
                } else if (Objects.equals(newEntityType, "Cylinder")) {
                    newCylinder.update();
                    Model cylinderModel = new Model(
                        new MeshData(
                            newCylinder.getPositions(),
                            newCylinder.getNormals(),
                            new float[]{},
                            new float[]{},
                            newCylinder.getTexCoords(),
                            newCylinder.getIndices()
                        ),
                        new ModelMetadata(
                            newCylinder, meshDataMaterialIDs
                        ),
                        "Cylinder"
                    );

                    cylinderModel.getMeshDatas().get(0).setMaterialID(0);

                    Entity cylinderEntity = new Entity(Utils.arrayToVector3f(position), Utils.arrayToVector3f(rotation), scale.get(), name.get());
                    cylinderModel.addEntity(cylinderEntity);
                    scene.addEntity(cylinderEntity);
                    scene.addModel(cylinderModel);
                    cylinderEntity.setModelID(cylinderModel.getID());

                    masterRenderer.setupBuffers(scene);
                } else if (Objects.equals(newEntityType, "Sphere")) {
                    newSphere.update();
                    Model sphereModel = new Model(
                        new MeshData(
                            newSphere.getPositions(),
                            newSphere.getNormals(),
                            new float[]{},
                            new float[]{},
                            newSphere.getTexCoords(),
                            newSphere.getIndices()
                        ),
                        new ModelMetadata(
                            newSphere, meshDataMaterialIDs
                        ),
                        "Sphere"
                    );

                    sphereModel.getMeshDatas().get(0).setMaterialID(0);

                    Entity sphereEntity = new Entity(Utils.arrayToVector3f(position), Utils.arrayToVector3f(rotation), scale.get(), name.get());
                    sphereModel.addEntity(sphereEntity);
                    scene.addEntity(sphereEntity);
                    scene.addModel(sphereModel);
                    sphereEntity.setModelID(sphereModel.getID());

                    masterRenderer.setupBuffers(scene);
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

            ShaderSettings settings = renderer.getShaderSettings();

            if (ImGui.beginTabBar("SettingsTabBar")) {
                if (ImGui.beginTabItem("General")) {
                    ImGui.text("General settings");
                    // Point light mesh radius
                    float[] pointLightMeshRadius = new float[] { settings.getPointLightMeshRadius() };
                    if (ImGui.dragFloat("Point light mesh radius", pointLightMeshRadius, 0.001f, 0f, Float.MAX_VALUE)) {
                        settings.setPointLightMeshRadius(pointLightMeshRadius[0]);
                        for (PointLight pointLight : pointLights) {
                            pointLight.updateRadius(settings.getPointLightMeshRadius());
                        }
                    }
                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem("Graphics")) {
                    ImGui.text("Shaders");

                    // Shader selection
                    String[] shaders = { "Default", "Filament", "Frostbite" };
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

                    // Bloom
                    float[] bloomStrength = new float[] { settings.getBloomStrength() };
                    if (ImGui.dragFloat("Bloom strength", bloomStrength, 0.001f, 0f, Float.MAX_VALUE)) {
                        settings.setBloomStrength(bloomStrength[0]);
                    }

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

                    float initialNearPlane = shadowRenderer.getNearPlane();
                    float initialFarPlane = shadowRenderer.getFarPlane();
                    float initialSize = shadowRenderer.getSize();

                    if (ImGui.dragFloat("Near plane##Directional", nearPlane, 0.1f, 0f, -1f)) {
                        shadowRenderer.setNearPlane(nearPlane[0]);
                    }
                    if (ImGui.dragFloat("Far plane##Directional", farPlane, 0.1f, 0f, -1f)) {
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

                    float initialPointNearPlane = omnidirectionalShadowRenderer.getNearPlane();
                    float initialPointFarPlane = omnidirectionalShadowRenderer.getFarPlane();

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

                    if (initialNearPlane != nearPlane[0] || initialFarPlane != farPlane[0] || initialSize != size[0] || initialPointNearPlane != pointNearPlane[0] || initialPointFarPlane != pointFarPlane[0]) {
                        masterRenderer.setSceneUpdated(true);
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
        ImGui.setNextWindowSizeConstraints(INSPECTOR_WIDTH, 400, INSPECTOR_WIDTH, 700);
        ImGui.begin("Inspector");

        if (selectedEntity != null && selected.get("entity") != -1) {
            Entity entity = selectedEntity;

            if (entity.getModelID() == -1) {
                // Placeholder parent entity
                float[] position = Utils.vector3fToArray(entity.getPosition());
                ImString name = new ImString(entity.getName(), 128);

                Vector3f initialPosition = new Vector3f(entity.getPosition());

                if (entity.getName() != null && !Objects.equals(entity.getName(), "null")) {
                    ImGui.text(entity.getName());
                } else {
                    ImGui.text("Entity " + (selected.get("entity") + 1));
                }

                ImGui.text("ID: " + entity.getID());

                ImGui.separator();
                if (ImGui.dragFloat3("Position", position, 0.1f)) {
                    entity.setPosition(new Vector3f(position[0], position[1], position[2]));
                }
                if (ImGui.inputText("Name", name)) entity.setName(name.get());

                if (!entity.getChildren().isEmpty() && !initialPosition.equals(entity.getPosition())) {
                    entity.setUpdated(true);
                }
            } else {
                float[] position = Utils.vector3fToArray(entity.getPosition());
                float[] rotation = Utils.vector3fToArray(entity.getRotation());
                float[] scale = new float[] {entity.getScale()};
                ImString name = new ImString(entity.getName(), 128);
                ImBoolean focused = new ImBoolean(camera.getFocus() == entity);

                Vector3f initialPosition = new Vector3f(entity.getPosition());
                Vector3f initialRotation = new Vector3f(entity.getRotation());
                float initialScale = entity.getScale();

                List<PBRMaterial> pbrMaterials = new ArrayList<>();
                if (entity.getModelID() != -1) {
                    for (MeshData meshData : scene.getModelByID(entity.getModelID()).getMeshDatas()) {
                        pbrMaterials.add(scene.getPBRMaterialByID(meshData.getMaterialID()));
                    }
                }

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

                ImGui.separator();
                if (entity.getModelID() != -1) {
                    ImGui.text("Model: " + scene.getModelByID(entity.getModelID()).getName() + " (ID: " + entity.getModelID() + ")");
                }

                ImGui.separator();
                ImGui.text("Materials (" + pbrMaterials.size() + ")");
                for (PBRMaterial pbrMaterial : pbrMaterials) {
                    ImGui.text(pbrMaterial.getName() + " (ID: " + pbrMaterial.getID() + ")");

                    ImGui.sameLine();
                    if (ImGui.button("Edit##" + pbrMaterial.getID())) {
                        setSelected("material", pbrMaterial.getID());
                    }

                    ImGui.sameLine();

                    if (ImGui.button("Change##" + pbrMaterial.getID())) {
                        ImGui.openPopup("Change##" + pbrMaterial.getID());
                    }

                    if (ImGui.beginPopup("Change##" + pbrMaterial.getID())) {
                        for (int i = 0; i < scene.getPBRMaterials().size(); i++) {
                            PBRMaterial material = scene.getPBRMaterials().get(i);
                            if (ImGui.menuItem(material.getName() + " (ID: " + material.getID() + ")")) {
                                // Set the corresponding mesh data to use the new material
                                scene.getModelByID(entity.getModelID()).getMeshDatas().get(pbrMaterials.indexOf(pbrMaterial)).setMaterialID(material.getID());
                                entity.setUpdated(true);
                            }
                        }
                        ImGui.endPopup();
                    }

                    float[] emissionStrength = new float[] {scene.getModelByID(entity.getModelID()).getMeshDatas().get(pbrMaterials.indexOf(pbrMaterial)).getEmissionStrength()};
                    if (ImGui.dragFloat("Emission Strength##" + pbrMaterial.getID(), emissionStrength, 0.01f, 0.0f, Float.MAX_VALUE)) {
                        scene.getModelByID(entity.getModelID()).getMeshDatas().get(pbrMaterials.indexOf(pbrMaterial)).setEmissionStrength(emissionStrength[0]);
                        masterRenderer.setupBuffers(scene);
                        entity.setUpdated(true);
                    }

//                    List<String> names = new ArrayList<>();
//                    for (PBRMaterial material : scene.getPBRMaterials()) {
//                        names.add(material.getName() + " (ID: " + material.getID() + ")");
//                    }
//
//                    System.out.println("Names: " + names);
//
//                    int[] currentItem = new int[] {pbrMaterials.indexOf(pbrMaterial)};
//                    if (ImGui.beginCombo("Material " + (pbrMaterials.indexOf(pbrMaterial) + 1), names.get(currentItem[0]))) {
//                        for (int i = 0; i < names.size(); i++) {
//                            boolean isSelected = currentItem[0] == i;
//                            if (ImGui.selectable(names.get(i), isSelected)) {
//                                currentItem[0] = i;
//                                scene.getModels().get(entity.getModelID()).getMeshDatas().get(pbrMaterials.indexOf(pbrMaterial)).setMaterialID(scene.getPBRMaterials().get(i).getID());
//                                entity.setUpdated(true);
//                            }
//                        }
//                        ImGui.endCombo();
//                    }
                }

                if (!initialPosition.equals(entity.getPosition()) || !initialRotation.equals(entity.getRotation()) || initialScale != entity.getScale()) {
                    entity.setUpdated(true);
                }
            }

            // Components
            ImGui.separator();
            ImGui.text("Components");
            ImGui.separator();

            MovementController movementController = entity.getMovementController();
            if (movementController != null) {
                ImGui.text("Movement");

                ImInt currentItem = new ImInt(entity.getMovementController().getType().ordinal());
                String[] items = { "None", "Orbit", "Direction", "Points", "Keyboard" };
                if (ImGui.beginCombo("Movement type", items[currentItem.get()])) {
                    for (int i = 0; i < items.length; i++) {
                        boolean isSelected = currentItem.get() == i;
                        if (ImGui.selectable(items[i], isSelected)) {
                            currentItem.set(i);
                            switch (i) {
                                case 0 -> movementController.setType(MovementController.Type.NONE);  // Preserve the movement attributes
                                case 1 -> {
                                    movementController.setType(MovementController.Type.ORBIT);
                                    if (movementController.getCenter() == null) movementController.setCenter(new Vector3f(0, 0, 0));
                                    if (movementController.getAxis() == null) movementController.setAxis(new Vector3f(0, 1, 0));
                                    if (movementController.getRadius() == 0) movementController.setRadius(1);
                                    if (movementController.getAnglePerSecond() == 0) movementController.setAnglePerSecond((float) Math.toRadians(90.0f));
                                }
                                case 2 -> {
                                    movementController.setType(MovementController.Type.DIRECTION);
                                    if (movementController.getSpeed() == 0) movementController.setSpeed(1);
                                    if (movementController.getOrigin() == null) movementController.setOrigin(new Vector3f(0, 0, 0));
                                    if (movementController.getDirection() == null) movementController.setDirection(new Vector3f(0, 0, 1));
                                    if (movementController.getDistance() == 0) movementController.setDistance(0);
                                }
                                case 3 -> {
                                    movementController.setType(MovementController.Type.PATH);
                                    if (movementController.getSpeed() == 0) movementController.setSpeed(1);
                                    if (movementController.getPath() == null) movementController.setPath(new ArrayList<>() {});
                                }
                                case 4 -> {
                                    movementController.setType(MovementController.Type.KEYBOARD);
                                    if (movementController.getSpeed() == 0) movementController.setSpeed(1);
                                    if (movementController.getDeceleration() == 0) movementController.setDeceleration(0.95f);
                                }
                            }
                        }
                        if (isSelected) {
                            ImGui.setScrollHereY();
                        }
                    }
                    ImGui.endCombo();
                }

                if (movementController.getType() == MovementController.Type.ORBIT) {
                    float[] center = Utils.vector3fToArray(movementController.getCenter());
                    float[] axis = Utils.vector3fToArray(movementController.getAxis());
                    float[] radius = new float[] { movementController.getRadius() };
                    float[] speed = new float[] { movementController.getSpeed() };
                    float[] anglePerSecond = new float[] { (float) Math.toDegrees(movementController.getAnglePerSecond()) };
                    ImBoolean pointTowardsCenter = new ImBoolean(movementController.isPointTowardsCenter());

                    if (ImGui.dragFloat3("Center: ", center, 0.1f)) movementController.setCenter(new Vector3f(center[0], center[1], center[2]));
                    if (ImGui.dragFloat3("Axis: ", axis, 0.1f)) movementController.setAxis(new Vector3f(axis[0], axis[1], axis[2]));
                    if (ImGui.dragFloat("Radius: ", radius, 0.1f, 0.01f, Float.MAX_VALUE)) movementController.setRadius(radius[0]);
                    if (ImGui.dragFloat("Speed: ", speed, 0.1f, 0.01f, Float.MAX_VALUE)) movementController.setSpeed(speed[0]);
                    if (ImGui.dragFloat("Angle per second: ", anglePerSecond, 0.1f)) movementController.setAnglePerSecond((float) Math.toRadians(anglePerSecond[0]));
                    if (ImGui.checkbox("Point towards center: ", pointTowardsCenter)) movementController.setPointTowardsCenter(pointTowardsCenter.get());
                } else if (movementController.getType() == MovementController.Type.DIRECTION) {
                    ImInt currentMode = new ImInt(entity.getMovementController().getMode().ordinal());
                    String[] modes = { "Constant", "Acceleration", "Deceleration" };
                    if (ImGui.beginCombo("Movement mode", modes[currentMode.get()])) {
                        for (int i = 0; i < modes.length; i++) {
                            boolean isSelected = currentMode.get() == i;
                            if (ImGui.selectable(modes[i], isSelected)) {
                                currentMode.set(i);
                                switch (i) {
                                    case 0 -> movementController.setMode(MovementController.Mode.CONSTANT);
                                    case 1 -> movementController.setMode(MovementController.Mode.ACCELERATION);
                                    case 2 -> movementController.setMode(MovementController.Mode.DECELERATION);
                                }
                            }
                            if (isSelected) {
                                ImGui.setScrollHereY();
                            }
                        }
                        ImGui.endCombo();
                    }

                    float[] origin = Utils.vector3fToArray(movementController.getOrigin());
                    float[] direction = Utils.vector3fToArray(movementController.getDirection());
                    float[] speed = new float[] { movementController.getSpeed() };
                    float[] acceleration = new float[] { movementController.getAcceleration() };
                    float[] distance = new float[] { movementController.getDistance() };
                    ImBoolean noMaxDistance = new ImBoolean(movementController.isNoMaxDistance());
                    ImBoolean stopAtZeroSpeed = new ImBoolean(movementController.isStopAtZeroSpeed());

                    if (ImGui.dragFloat3("Origin: ", origin, 0.1f)) movementController.setOrigin(new Vector3f(origin[0], origin[1], origin[2]));
                    if (ImGui.dragFloat3("Direction: ", direction, 0.1f)) movementController.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movementController.setSpeed(speed[0]);
                    if (movementController.getMode() == MovementController.Mode.ACCELERATION) {
                        if (ImGui.dragFloat("Acceleration: ", acceleration, 0.1f)) movementController.setAcceleration(acceleration[0]);
                    } else if (movementController.getMode() == MovementController.Mode.DECELERATION) {
                        if (ImGui.dragFloat("Deceleration: ", acceleration, 0.1f)) movementController.setAcceleration(acceleration[0]);
                    }
                    if (movementController.getMode() != MovementController.Mode.CONSTANT) {
                        if (ImGui.checkbox("Stop at zero speed: ", stopAtZeroSpeed)) movementController.setStopAtZeroSpeed(stopAtZeroSpeed.get());
                    }
                    if (!movementController.isNoMaxDistance()) {
                        if (ImGui.dragFloat("Distance: ", distance, 0.1f)) movementController.setDistance(distance[0]);
                    }
                    if (ImGui.checkbox("No max distance: ", noMaxDistance)) movementController.setNoMaxDistance(noMaxDistance.get());
                    if (ImGui.button("Reset##Origin", 60, 0)) entity.setPosition(new Vector3f(movementController.getOrigin()));
                } else if (movementController.getType() == MovementController.Type.PATH) {
                    float[] speed = new float[] { movementController.getSpeed() };
                    List<Vector3f> points = movementController.getPath();

                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movementController.setSpeed(speed[0]);

                    ImGui.text("Points");
                    for (int i = 0; i < points.size(); i++) {
                        float[] point = Utils.vector3fToArray(points.get(i));
                        if (ImGui.dragFloat3("Point " + (i + 1), point, 0.1f)) movementController.setPoint(i, new Vector3f(point[0], point[1], point[2]));
                        ImGui.sameLine();
                        if (ImGui.button("Remove")) movementController.removePoint(i);
                    }

                    if (ImGui.button("Add point")) {
                        movementController.addPoint(new Vector3f(0, 0, 0), scene);
                    }
                } else if (movementController.getType() == MovementController.Type.KEYBOARD) {
                    float[] speed = new float[] { movementController.getSpeed() };
                    float[] deceleration = new float[] { movementController.getDeceleration() };

                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movementController.setSpeed(speed[0]);
                    if (ImGui.dragFloat("Deceleration: ", deceleration, 0.1f)) movementController.setDeceleration(deceleration[0]);
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
                    entity.setMovementController(MovementController.none(scene));
                }
                if (ImGui.menuItem("Rotation")) {
                    entity.setRotationController(new RotationController());
                }
                ImGui.endPopup();
            }

            if (ImGui.button("Remove")) {
                scene.getModelByID(entity.getModelID()).removeEntity(entity);
                scene.removeEntity(entity);
                masterRenderer.setupBuffers(scene);
                selected.put("entity", -1);
            }
        }

        if (selected.get("pointLight") != -1) {
            PointLight light = pointLights.get(selected.get("pointLight"));

            float[] position = Utils.vector3fToArray(light.getPosition());
            float[] color = Utils.vector3fToArray(light.getColor());
            float[] intensity = new float[] { light.getIntensity() };

            Vector3f initialPosition = new Vector3f(light.getPosition());

            ImGui.text("Point Light " + (selected.get("pointLight") + 1));
            if (ImGui.dragFloat3("Position: ", position, 0.1f)) light.setPosition(new Vector3f(position[0], position[1], position[2]));
            if (ImGui.button("Set to camera position")) light.setPosition(new Vector3f(camera.getPosition()));
            if (ImGui.colorPicker3("Color: ", color)) light.setColor(new Vector3f(color[0], color[1], color[2]));
            if (ImGui.dragFloat("Intensity: ", intensity, 0.1f, 0f, Float.MAX_VALUE)) light.setIntensity(intensity[0]);

            // Components
            ImGui.separator();
            ImGui.text("Components");
            ImGui.separator();

            MovementController movementController = light.getMovementController();
            if (movementController != null) {
                ImGui.text("Movement");

                ImInt currentItem = new ImInt(light.getMovementController().getType().ordinal());
                String[] items = { "None", "Orbit", "Direction", "Points", "Keyboard" };
                if (ImGui.beginCombo("Movement type", items[currentItem.get()])) {
                    for (int i = 0; i < items.length; i++) {
                        boolean isSelected = currentItem.get() == i;
                        if (ImGui.selectable(items[i], isSelected)) {
                            currentItem.set(i);
                            switch (i) {
                                case 0 -> movementController.setType(MovementController.Type.NONE);  // Preserve the movement attributes
                                case 1 -> {
                                    movementController.setType(MovementController.Type.ORBIT);
                                    if (movementController.getCenter() == null) movementController.setCenter(new Vector3f(0, 0, 0));
                                    if (movementController.getAxis() == null) movementController.setAxis(new Vector3f(0, 1, 0));
                                    if (movementController.getRadius() == 0) movementController.setRadius(1);
                                    if (movementController.getAnglePerSecond() == 0) movementController.setAnglePerSecond((float) Math.toRadians(90.0f));
                                }
                                case 2 -> {
                                    movementController.setType(MovementController.Type.DIRECTION);
                                    if (movementController.getSpeed() == 0) movementController.setSpeed(1);
                                    if (movementController.getOrigin() == null) movementController.setOrigin(new Vector3f(0, 0, 0));
                                    if (movementController.getDirection() == null) movementController.setDirection(new Vector3f(0, 0, 1));
                                    if (movementController.getDistance() == 0) movementController.setDistance(0);
                                }
                                case 3 -> {
                                    movementController.setType(MovementController.Type.PATH);
                                    if (movementController.getSpeed() == 0) movementController.setSpeed(1);
                                    if (movementController.getPath() == null) movementController.setPath(new ArrayList<>() {});
                                }
                                case 4 -> {
                                    movementController.setType(MovementController.Type.KEYBOARD);
                                    if (movementController.getSpeed() == 0) movementController.setSpeed(1);
                                    if (movementController.getDeceleration() == 0) movementController.setDeceleration(0.95f);
                                }
                            }
                        }
                        if (isSelected) {
                            ImGui.setScrollHereY();
                        }
                    }
                    ImGui.endCombo();
                }

                if (movementController.getType() == MovementController.Type.ORBIT) {
                    float[] center = Utils.vector3fToArray(movementController.getCenter());
                    float[] axis = Utils.vector3fToArray(movementController.getAxis());
                    float[] radius = new float[] { movementController.getRadius() };
                    float[] speed = new float[] { movementController.getSpeed() };
                    float[] anglePerSecond = new float[] { (float) Math.toDegrees(movementController.getAnglePerSecond()) };
                    ImBoolean pointTowardsCenter = new ImBoolean(movementController.isPointTowardsCenter());

                    if (ImGui.dragFloat3("Center: ", center, 0.1f)) movementController.setCenter(new Vector3f(center[0], center[1], center[2]));
                    if (ImGui.dragFloat3("Axis: ", axis, 0.1f)) movementController.setAxis(new Vector3f(axis[0], axis[1], axis[2]));
                    if (ImGui.dragFloat("Radius: ", radius, 0.1f, 0.01f, Float.MAX_VALUE)) movementController.setRadius(radius[0]);
                    if (ImGui.dragFloat("Speed: ", speed, 0.1f, 0.01f, Float.MAX_VALUE)) movementController.setSpeed(speed[0]);
                    if (ImGui.dragFloat("Angle per second: ", anglePerSecond, 0.1f)) movementController.setAnglePerSecond((float) Math.toRadians(anglePerSecond[0]));
                    if (ImGui.checkbox("Point towards center: ", pointTowardsCenter)) movementController.setPointTowardsCenter(pointTowardsCenter.get());
                } else if (movementController.getType() == MovementController.Type.DIRECTION) {
                    ImInt currentMode = new ImInt(light.getMovementController().getMode().ordinal());
                    String[] modes = { "Constant", "Acceleration", "Deceleration" };
                    if (ImGui.beginCombo("Movement mode", modes[currentMode.get()])) {
                        for (int i = 0; i < modes.length; i++) {
                            boolean isSelected = currentMode.get() == i;
                            if (ImGui.selectable(modes[i], isSelected)) {
                                currentMode.set(i);
                                switch (i) {
                                    case 0 -> movementController.setMode(MovementController.Mode.CONSTANT);
                                    case 1 -> movementController.setMode(MovementController.Mode.ACCELERATION);
                                    case 2 -> movementController.setMode(MovementController.Mode.DECELERATION);
                                }
                            }
                            if (isSelected) {
                                ImGui.setScrollHereY();
                            }
                        }
                        ImGui.endCombo();
                    }

                    float[] origin = Utils.vector3fToArray(movementController.getOrigin());
                    float[] direction = Utils.vector3fToArray(movementController.getDirection());
                    float[] speed = new float[] { movementController.getSpeed() };
                    float[] acceleration = new float[] { movementController.getAcceleration() };
                    float[] distance = new float[] { movementController.getDistance() };
                    ImBoolean noMaxDistance = new ImBoolean(movementController.isNoMaxDistance());
                    ImBoolean stopAtZeroSpeed = new ImBoolean(movementController.isStopAtZeroSpeed());

                    if (ImGui.dragFloat3("Origin: ", origin, 0.1f)) movementController.setOrigin(new Vector3f(origin[0], origin[1], origin[2]));
                    if (ImGui.dragFloat3("Direction: ", direction, 0.1f)) movementController.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movementController.setSpeed(speed[0]);
                    if (movementController.getMode() == MovementController.Mode.ACCELERATION) {
                        if (ImGui.dragFloat("Acceleration: ", acceleration, 0.1f)) movementController.setAcceleration(acceleration[0]);
                    } else if (movementController.getMode() == MovementController.Mode.DECELERATION) {
                        if (ImGui.dragFloat("Deceleration: ", acceleration, 0.1f)) movementController.setAcceleration(acceleration[0]);
                    }
                    if (movementController.getMode() != MovementController.Mode.CONSTANT) {
                        if (ImGui.checkbox("Stop at zero speed: ", stopAtZeroSpeed)) movementController.setStopAtZeroSpeed(stopAtZeroSpeed.get());
                    }
                    if (!movementController.isNoMaxDistance()) {
                        if (ImGui.dragFloat("Distance: ", distance, 0.1f)) movementController.setDistance(distance[0]);
                    }
                    if (ImGui.checkbox("No max distance: ", noMaxDistance)) movementController.setNoMaxDistance(noMaxDistance.get());
                    if (ImGui.button("Reset##Origin", 60, 0)) light.setPosition(new Vector3f(movementController.getOrigin()));
                } else if (movementController.getType() == MovementController.Type.PATH) {
                    float[] speed = new float[] { movementController.getSpeed() };
                    List<Vector3f> points = movementController.getPath();

                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movementController.setSpeed(speed[0]);

                    ImGui.text("Points");
                    for (int i = 0; i < points.size(); i++) {
                        float[] point = Utils.vector3fToArray(points.get(i));
                        if (ImGui.dragFloat3("Point " + (i + 1), point, 0.1f)) movementController.setPoint(i, new Vector3f(point[0], point[1], point[2]));
                        ImGui.sameLine();
                        if (ImGui.button("Remove")) movementController.removePoint(i);
                    }

                    if (ImGui.button("Add point")) {
                        movementController.addPoint(new Vector3f(0, 0, 0), scene);
                    }
                } else if (movementController.getType() == MovementController.Type.KEYBOARD) {
                    float[] speed = new float[] { movementController.getSpeed() };
                    float[] deceleration = new float[] { movementController.getDeceleration() };

                    if (ImGui.dragFloat("Speed: ", speed, 0.1f)) movementController.setSpeed(speed[0]);
                    if (ImGui.dragFloat("Deceleration: ", deceleration, 0.1f)) movementController.setDeceleration(deceleration[0]);
                }
            }

            ImGui.separator();
            if (ImGui.button("Add component")) {
                ImGui.openPopup("Add component");
            }

            if (ImGui.beginPopup("Add component")) {
                if (ImGui.menuItem("Movement")) {
                    light.setMovementController(MovementController.none(scene));
                }
                ImGui.endPopup();
            }

            if (ImGui.button("Remove")) {
                pointLights.remove(light);
                selected.put("pointLight", -1);
            }

            if (!initialPosition.equals(light.getPosition())) {
                masterRenderer.setSceneUpdated(true);
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

            Vector3f initialPosition = new Vector3f(light.getPosition());
            Vector3f initialDirection = new Vector3f(light.getDirection());

            ImGui.text("Spot Light " + (selected.get("spotLight") + 1));
            if (ImGui.dragFloat3("Position: ", position, 0.1f)) light.setPosition(new Vector3f(position[0], position[1], position[2]));
            if (ImGui.button("Set to camera position")) light.setPosition(new Vector3f(camera.getPosition()));
            if (ImGui.dragFloat3("Direction: ", direction, 0.01f, -1f, 1f))  light.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
            if (ImGui.button("Set to camera direction")) light.setDirection(new Vector3f(camera.getFront()));
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

            if (!initialPosition.equals(light.getPosition()) || !initialDirection.equals(light.getDirection())) {
                masterRenderer.setSceneUpdated(true);
            }
        }

        if (selected.get("dirLight") != -1) {
            float[] direction = Utils.vector3fToArray(dirLight.getDirection());
            float[] azimuth = new float[] { dirLight.getAzimuth() };
            float[] elevation = new float[] { dirLight.getElevation() };
            float[] color = Utils.vector3fToArray(dirLight.getColor());

            Vector3f initialDirection = new Vector3f(dirLight.getDirection());

            ImGui.text("Directional Light");
            if (ImGui.dragFloat3("Direction: ", direction, 0.1f)) dirLight.setDirection(new Vector3f(direction[0], direction[1], direction[2]));
            if (ImGui.button("Set to camera direction")) dirLight.setDirection(new Vector3f(camera.getFront()));
            if (ImGui.dragFloat("Azimuth: ", azimuth, 0.2f, 0f, 360f)) dirLight.setAzimuth(azimuth[0]);
            if (ImGui.dragFloat("Elevation: ", elevation, 0.1f, -90f, 90f)) dirLight.setElevation(elevation[0]);
            if (ImGui.colorPicker3("Color: ", color)) dirLight.setColor(new Vector3f(color[0], color[1], color[2]));

            if (!initialDirection.equals(dirLight.getDirection())) {
                masterRenderer.setSceneUpdated(true);
            }
        }

        if (selected.get("camera") != -1) {
            float[] position = Utils.vector3fToArray(camera.getPosition());
            float[] FOV = new float[] {(float) Math.toDegrees(camera.getFOV())};
            float[] exposure = new float[] { renderer.getShaderSettings().getExposure() };
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
            if (ImGui.dragFloat("Exposure: ", exposure, 0.01f, 0.0f, 10.0f)) renderer.getShaderSettings().setExposure(exposure[0]);
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

        if (selected.get("model") != -1) {
            Model model = scene.getModels().get(selected.get("model"));

            ImGui.text("Model " + model.getID());

            ImGui.text("Material ID: " + model.getMeshDatas().get(0).getMaterialID());

            ImString name = new ImString(model.getName(), 128);
            if (ImGui.inputText("Name: ", name)) model.setName(name.get());

            ImGui.separator();

            ImGui.text("Entities:");
            for (Entity entity : model.getEntities()) {
                ImGui.text(entity.getName() + " (ID: " + entity.getID() + ")");
            }
        }

        if (selected.get("material") != -1) {
            PBRMaterial pbrMaterial = scene.getPBRMaterials().get(selected.get("material"));

            ImBoolean useAlbedoTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("albedo"));
            ImBoolean useNormalTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("normal"));
            ImBoolean useMetallicTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("metallic"));
            ImBoolean useRoughnessTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("roughness"));
            ImBoolean useMetallicRoughnessTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("metallicRoughness"));
            ImBoolean useAoTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("ao"));
            ImBoolean useEmissiveTexture = new ImBoolean(pbrMaterial.getUsesTextures().get("emissive"));

            ImString name = new ImString(pbrMaterial.getName(), 128);
            if (ImGui.inputText("Name: ", name)) pbrMaterial.setName(name.get());

            ImGui.separator();

            // Albedo
            ImGui.text("Albedo");
            if (pbrMaterial.getAlbedo() != null)
                ImGui.image(pbrMaterial.getAlbedo().getID(), 128, 128);
            if (ImGui.button("Change texture##Albedo")) {
                String albedoPath = openSingle("png,jpg,jpeg");
                if (albedoPath != null) {
                    Texture texture = new Texture(albedoPath, GL_SRGB_ALPHA, true);
                    texture.generateHandle();
                    pbrMaterial.setAlbedo(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }

            float[] albedo = new float[] { pbrMaterial.getAlbedoColor().x, pbrMaterial.getAlbedoColor().y, pbrMaterial.getAlbedoColor().z };
            if (ImGui.colorPicker3("Albedo color: ", albedo)) {
                pbrMaterial.setAlbedoColor(new Vector3f(albedo[0], albedo[1], albedo[2]));
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }

            if (ImGui.checkbox("Use texture##Albedo", useAlbedoTexture) && pbrMaterial.getAlbedo() != null) {
                pbrMaterial.setUseTexture("albedo", useAlbedoTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }

            ImGui.newLine();

            // Normal
            ImGui.text("Normal");
            if (pbrMaterial.getNormal() != null)
                ImGui.image(pbrMaterial.getNormal().getID(), 128, 128);
            if (ImGui.button("Change texture##Normal")) {
                String normalPath = openSingle("png,jpg,jpeg");
                if (normalPath != null) {
                    Texture texture = new Texture(normalPath, GL_RGB, true);
                    texture.generateHandle();
                    pbrMaterial.setNormal(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }

            if (ImGui.checkbox("Use texture##Normal", useNormalTexture) && pbrMaterial.getNormal() != null) {
                pbrMaterial.setUseTexture("normal", useNormalTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }
            ImGui.newLine();

            // Metallic
            ImGui.text("Metallic");
            if (pbrMaterial.getMetallic() != null)
                ImGui.image(pbrMaterial.getMetallic().getID(), 128, 128);
            if (ImGui.button("Change texture##Metallic")) {
                String metallicPath = openSingle("png,jpg,jpeg");
                if (metallicPath != null) {
                    Texture texture = new Texture(metallicPath, GL_RED, true);
                    texture.generateHandle();
                    pbrMaterial.setMetallic(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }
            float[] metallic = new float[] { pbrMaterial.getMetallicFactor() };
            if (ImGui.dragFloat("Metallic factor: ", metallic, 0.001f, 0.0f, 1.0f)) {
                pbrMaterial.setMetallicFactor(metallic[0]);
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }

            if (ImGui.checkbox("Use texture##Metallic", useMetallicTexture) && pbrMaterial.getMetallic() != null) {
                pbrMaterial.setUseTexture("metallic", useMetallicTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }
            ImGui.newLine();

            // Roughness
            ImGui.text("Roughness");
            if (pbrMaterial.getRoughness() != null)
                ImGui.image(pbrMaterial.getRoughness().getID(), 128, 128);
            if (ImGui.button("Change texture##Roughness")) {
                String roughnessPath = openSingle("png,jpg,jpeg");
                if (roughnessPath != null) {
                    Texture texture = new Texture(roughnessPath, GL_RED, true);
                    texture.generateHandle();
                    pbrMaterial.setRoughness(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }
            float[] roughness = new float[] { pbrMaterial.getRoughnessFactor() };
            if (ImGui.dragFloat("Roughness factor: ", roughness, 0.001f, 0.0f, 1.0f)) {
                pbrMaterial.setRoughnessFactor(roughness[0]);
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }

            if (ImGui.checkbox("Use texture##Roughness", useRoughnessTexture) && pbrMaterial.getRoughness() != null) {
                pbrMaterial.setUseTexture("roughness", useRoughnessTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }
            ImGui.newLine();

            // Metallic roughness
            ImGui.text("Metallic Roughness");
            if (pbrMaterial.getMetallicRoughness() != null)
                ImGui.image(pbrMaterial.getMetallicRoughness().getID(), 128, 128);
            if (ImGui.button("Change texture##MetallicRoughness")) {
                String metallicRoughnessPath = openSingle("png,jpg,jpeg");
                if (metallicRoughnessPath != null) {
                    Texture texture = new Texture(metallicRoughnessPath, GL_RGBA, true);
                    texture.generateHandle();
                    pbrMaterial.setMetallicRoughness(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }
            if (ImGui.checkbox("Use texture##MetallicRoughness", useMetallicRoughnessTexture) && pbrMaterial.getMetallicRoughness() != null) {
                pbrMaterial.setUseTexture("metallicRoughness", useMetallicRoughnessTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }
            ImGui.newLine();

            // Ambient occlusion
            ImGui.text("Ambient Occlusion");
            if (pbrMaterial.getAo() != null)
                ImGui.image(pbrMaterial.getAo().getID(), 128, 128);
            if (ImGui.button("Change texture##AmbientOcclusion")) {
                String aoPath = openSingle("png,jpg,jpeg");
                if (aoPath != null) {
                    Texture texture = new Texture(aoPath, GL_RED, true);
                    texture.generateHandle();
                    pbrMaterial.setAo(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }
            if (ImGui.checkbox("Use texture##AmbientOcclusion", useAoTexture)) {
                pbrMaterial.setUseTexture("ao", useAoTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }
            ImGui.newLine();

            // Emissive
            ImGui.text("Emissive");
            if (pbrMaterial.getEmissive() != null)
                ImGui.image(pbrMaterial.getEmissive().getID(), 128, 128);
            if (ImGui.button("Change texture##Emissive")) {
                String emissivePath = openSingle("png,jpg,jpeg");
                if (emissivePath != null) {
                    Texture texture = new Texture(emissivePath, GL_RGB, true);
                    texture.generateHandle();
                    pbrMaterial.setEmissive(texture);
                    masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
                }
            }

            float[] emissive = new float[] { pbrMaterial.getEmissiveColor().x, pbrMaterial.getEmissiveColor().y, pbrMaterial.getEmissiveColor().z };
            if (ImGui.colorEdit3("Emissive color: ", emissive)) {
                pbrMaterial.setEmissiveColor(new Vector3f(emissive[0], emissive[1], emissive[2]));
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }

            if (ImGui.checkbox("Use texture##Emissive", useEmissiveTexture) && pbrMaterial.getEmissive() != null) {
                pbrMaterial.setUseTexture("emissive", useEmissiveTexture.get());
                masterRenderer.updateMaterial(pbrMaterial.getID(), pbrMaterial);
            }
        }

        ImGui.end();

        // Scene stuff lists window
        ImGui.setNextWindowPos(PADDING, 1080 - PADDING - 300, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(200, 300, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSizeConstraints(200, 100, 200, 500);
        ImGui.begin("Scene Stuff Lists");
        if (ImGui.beginTabBar("Scene Stuff Lists")) {
            if (ImGui.beginTabItem("Models")) {
                for (int i = 0; i < scene.getModels().size(); i++) {
                    Model model = scene.getModels().get(i);
                    if (ImGui.selectable(model.getName() + " (ID: " + model.getID() + ")", selected.get("model") == i)) {
                        setSelected("model", i);
                    }
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Materials")) {
                for (int i = 0; i < scene.getPBRMaterials().size(); i++) {
                    PBRMaterial pbrMaterial = scene.getPBRMaterials().get(i);
                    if (ImGui.selectable(pbrMaterial.getName() + " (ID: " + pbrMaterial.getID() + ")", selected.get("material") == i)) {
                        setSelected("material", i);
                    }
                }
                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }
        ImGui.end();

        // Material editor window
        if (showMaterialWindow) {
            ImGui.begin("New Material", ImGuiWindowFlags.AlwaysAutoResize);

            ImBoolean useAlbedoTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("albedo"));
            ImBoolean useNormalTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("normal"));
            ImBoolean useMetallicTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("metallic"));
            ImBoolean useRoughnessTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("roughness"));
            ImBoolean useMetallicRoughnessTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("metallicRoughness"));
            ImBoolean useAoTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("ao"));
            ImBoolean useEmissiveTexture = new ImBoolean(newPbrMaterial.getUsesTextures().get("emissive"));

            ImString name = new ImString(newPbrMaterial.getName(), 128);
            if (ImGui.inputText("Name: ", name)) newPbrMaterial.setName(name.get());

            ImGui.separator();

            // Albedo
            ImGui.text("Albedo");
            if (newPbrMaterial.getAlbedo() != null)
                ImGui.image(newPbrMaterial.getAlbedo().getID(), 128, 128);
            if (ImGui.button("Change texture##Albedo")) {
                String albedoPath = openSingle("png,jpg,jpeg");
                if (albedoPath != null) {
                    Texture texture = new Texture(albedoPath, GL_SRGB_ALPHA, true);
                    texture.generateHandle();
                    newPbrMaterial.setAlbedo(texture);
                }
            }

            float[] albedo = new float[] { newPbrMaterial.getAlbedoColor().x, newPbrMaterial.getAlbedoColor().y, newPbrMaterial.getAlbedoColor().z };
            if (ImGui.colorPicker3("Albedo color: ", albedo)) {
                newPbrMaterial.setAlbedoColor(new Vector3f(albedo[0], albedo[1], albedo[2]));
            }

            if (ImGui.checkbox("Use texture##Albedo", useAlbedoTexture) && newPbrMaterial.getAlbedo() != null) {
                newPbrMaterial.setUseTexture("albedo", useAlbedoTexture.get());
            }

            ImGui.newLine();

            // Normal
            ImGui.text("Normal");
            if (newPbrMaterial.getNormal() != null)
                ImGui.image(newPbrMaterial.getNormal().getID(), 128, 128);
            if (ImGui.button("Change texture##Normal")) {
                String normalPath = openSingle("png,jpg,jpeg");
                if (normalPath != null) {
                    Texture texture = new Texture(normalPath, GL_RGB, true);
                    texture.generateHandle();
                    newPbrMaterial.setNormal(texture);
                }
            }

            if (ImGui.checkbox("Use texture##Normal", useNormalTexture) && newPbrMaterial.getNormal() != null) {
                newPbrMaterial.setUseTexture("normal", useNormalTexture.get());
            }
            ImGui.newLine();

            // Metallic
            ImGui.text("Metallic");
            if (newPbrMaterial.getMetallic() != null)
                ImGui.image(newPbrMaterial.getMetallic().getID(), 128, 128);
            if (ImGui.button("Change texture##Metallic")) {
                String metallicPath = openSingle("png,jpg,jpeg");
                if (metallicPath != null) {
                    Texture texture = new Texture(metallicPath, GL_RED, true);
                    texture.generateHandle();
                    newPbrMaterial.setMetallic(texture);
                }
            }
            float[] metallic = new float[] { newPbrMaterial.getMetallicFactor() };
            if (ImGui.dragFloat("Metallic factor: ", metallic, 0.001f, 0.0f, 1.0f)) {
                newPbrMaterial.setMetallicFactor(metallic[0]);
            }

            if (ImGui.checkbox("Use texture##Metallic", useMetallicTexture) && newPbrMaterial.getMetallic() != null) {
                newPbrMaterial.setUseTexture("metallic", useMetallicTexture.get());
            }
            ImGui.newLine();

            // Roughness
            ImGui.text("Roughness");
            if (newPbrMaterial.getRoughness() != null)
                ImGui.image(newPbrMaterial.getRoughness().getID(), 128, 128);
            if (ImGui.button("Change texture##Roughness")) {
                String roughnessPath = openSingle("png,jpg,jpeg");
                if (roughnessPath != null) {
                    Texture texture = new Texture(roughnessPath, GL_RED, true);
                    texture.generateHandle();
                    newPbrMaterial.setRoughness(texture);
                }
            }
            float[] roughness = new float[] { newPbrMaterial.getRoughnessFactor() };
            if (ImGui.dragFloat("Roughness factor: ", roughness, 0.001f, 0.0f, 1.0f)) {
                newPbrMaterial.setRoughnessFactor(roughness[0]);
            }

            if (ImGui.checkbox("Use texture##Roughness", useRoughnessTexture) && newPbrMaterial.getRoughness() != null) {
                newPbrMaterial.setUseTexture("roughness", useRoughnessTexture.get());
            }
            ImGui.newLine();

            // Metallic roughness
            ImGui.text("Metallic Roughness");
            if (newPbrMaterial.getMetallicRoughness() != null)
                ImGui.image(newPbrMaterial.getMetallicRoughness().getID(), 128, 128);
            if (ImGui.button("Change texture##MetallicRoughness")) {
                String metallicRoughnessPath = openSingle("png,jpg,jpeg");
                if (metallicRoughnessPath != null) {
                    Texture texture = new Texture(metallicRoughnessPath, GL_RGBA, true);
                    texture.generateHandle();
                    newPbrMaterial.setMetallicRoughness(texture);
                }
            }
            if (ImGui.checkbox("Use texture##MetallicRoughness", useMetallicRoughnessTexture) && newPbrMaterial.getMetallicRoughness() != null) {
                newPbrMaterial.setUseTexture("metallicRoughness", useMetallicRoughnessTexture.get());
            }
            ImGui.newLine();

            // Ambient occlusion
            ImGui.text("Ambient Occlusion");
            if (newPbrMaterial.getAo() != null)
                ImGui.image(newPbrMaterial.getAo().getID(), 128, 128);
            if (ImGui.button("Change texture##AmbientOcclusion")) {
                String aoPath = openSingle("png,jpg,jpeg");
                if (aoPath != null) {
                    Texture texture = new Texture(aoPath, GL_RED, true);
                    texture.generateHandle();
                    newPbrMaterial.setAo(texture);
                }
            }
            if (ImGui.checkbox("Use texture##AmbientOcclusion", useAoTexture)) {
                newPbrMaterial.setUseTexture("ao", useAoTexture.get());
            }
            ImGui.newLine();

            // Emissive
            ImGui.text("Emissive");
            if (newPbrMaterial.getEmissive() != null)
                ImGui.image(newPbrMaterial.getEmissive().getID(), 128, 128);
            if (ImGui.button("Change texture##Emissive")) {
                String emissivePath = openSingle("png,jpg,jpeg");
                if (emissivePath != null) {
                    Texture texture = new Texture(emissivePath, GL_RGB, true);
                    texture.generateHandle();
                    newPbrMaterial.setEmissive(texture);
                }
            }
            float[] emissive = new float[] { newPbrMaterial.getEmissiveColor().x, newPbrMaterial.getEmissiveColor().y, newPbrMaterial.getEmissiveColor().z };
            if (ImGui.colorEdit3("Emissive color: ", emissive)) {
                newPbrMaterial.setEmissiveColor(new Vector3f(emissive[0], emissive[1], emissive[2]));
            }

            if (ImGui.checkbox("Use texture##Emissive", useEmissiveTexture) && newPbrMaterial.getEmissive() != null) {
                newPbrMaterial.setUseTexture("emissive", useEmissiveTexture.get());
            }

            if (ImGui.button("Add", 120, 0)) {
                scene.addPBRMaterial(newPbrMaterial);
                masterRenderer.setupBuffers(scene);
                showMaterialWindow = false;
            }

            if (ImGui.button("Cancel", 120, 0)) {
                showMaterialWindow = false;
                newPbrMaterial = new PBRMaterial(
                    "New Material",
                    false,
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    0.0f,
                    1.0f,
                    new Vector3f(0.0f, 0.0f, 0.0f)
                );
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

    private String save(String filterList) {
        PointerBuffer outPath = MemoryUtil.memAllocPointer(1);
        try {
            if (checkResult(
                NFD_SaveDialog(filterList, null, outPath),
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
