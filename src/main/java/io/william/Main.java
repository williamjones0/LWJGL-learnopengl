package io.william;

import io.william.io.ModelLoader;
import io.william.io.SceneExporter;
import io.william.renderer.*;
import io.william.io.Input;
import io.william.io.Window;
import io.william.renderer.primitive.Cube;
import io.william.renderer.primitive.Quad;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import io.william.renderer.shadow.SpotlightShadowRenderer;
import io.william.renderer.sky.Sky;
import io.william.util.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.*;
import io.william.renderer.primitive.UVSphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class Main {

    private Window window;
    private Renderer renderer;
    private List<Mesh> meshes;
    private Camera camera;
    private Scene scene;
    private GUI gui;
    private MasterRenderer masterRenderer;

    private float deltaTime = 0.0f;
    private float lastFrame = 0.0f;

    private boolean firstMouse = true;
    private double lastX, lastY, lastScrollX, lastScrollY = 0;
    private final List<Integer> lastFrameKeys = new ArrayList<>();
    private final List<Integer> lastFrameButtons = new ArrayList<>();

    private boolean cursorEnabled = false;

    public void run() throws Exception {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        renderer.cleanup();
        for (Mesh mesh : meshes) {
            mesh.cleanup();
        }
        window.destroy();
    }

    private void init() throws Exception {
        window = new Window(1920, 1080, "Renderer", 1, false);
        window.create();

        renderer = new Renderer();

        meshes = new ArrayList<>();

        camera = new Camera(new Vector3f(0, 0, 15), 0, 0);

        ShadowRenderer shadowRenderer = new ShadowRenderer();

        OmnidirectionalShadowRenderer omnidirectionalShadowRenderer = new OmnidirectionalShadowRenderer();

        SpotlightShadowRenderer spotlightShadowRenderer = new SpotlightShadowRenderer();

        gui = new GUI();

        masterRenderer = new MasterRenderer();

        scene = new Scene();

        PBRMaterial defaultMaterial = new PBRMaterial("Default");
        scene.addPBRMaterial(defaultMaterial);

        PBRMaterial rustedIron = new PBRMaterial(
            "Rusted Iron",
            false,
            new Texture("src/main/resources/textures/PBR/rusted_iron/basecolor.png", GL_SRGB_ALPHA),
            new Texture("src/main/resources/textures/PBR/rusted_iron/normal.png", GL_RGBA),
            new Texture("src/main/resources/textures/PBR/rusted_iron/metallic.png", GL_RGBA),
            new Texture("src/main/resources/textures/PBR/rusted_iron/roughness.png", GL_RGBA),
            null,
            null,
            null
        );

//        PBRMaterial red = new PBRMaterial(
//            null,
//            null,
//            null,
//            null,
//            null,
//            null,
//            null
//        );

//        PBRMaterial blackTile = new PBRMaterial(
//            new Texture("src/main/resources/textures/PBR/black_tile/albedo.png", GL_SRGB_ALPHA),
//            new Texture("src/main/resources/textures/PBR/black_tile/normal.png", GL_RGBA),
//            new Texture("src/main/resources/textures/PBR/black_tile/metallic.png", GL_RGBA),
//            new Texture("src/main/resources/textures/PBR/black_tile/roughness.png", GL_RGBA),
//            null,
//            new Texture("src/main/resources/textures/PBR/black_tile/ao.png", GL_RGBA),
//            null
//        );

//        PBRMaterial vintageTile = new PBRMaterial(
//            new Texture("src/main/resources/textures/PBR/vintage_tile/albedo.png", GL_SRGB_ALPHA),
//            new Texture("src/main/resources/textures/PBR/vintage_tile/normal.png", GL_RGBA),
//            new Texture("src/main/resources/textures/PBR/vintage_tile/metallic.png", GL_RGBA),
//            new Texture("src/main/resources/textures/PBR/vintage_tile/roughness.png", GL_RGBA),
//            null,
//            new Texture("src/main/resources/textures/PBR/vintage_tile/ao.png", GL_RGBA),
//            null
//        );

        PBRMaterial brushedMetal = new PBRMaterial(
            "Brushed Metal",
            false,
            new Texture("src/main/resources/textures/PBR/brushed_metal/albedo.png", GL_SRGB_ALPHA),
            new Texture("src/main/resources/textures/PBR/brushed_metal/normal.png", GL_RGBA),
            new Texture("src/main/resources/textures/PBR/brushed_metal/metallic.png", GL_RGBA),
            new Texture("src/main/resources/textures/PBR/brushed_metal/roughness.png", GL_RGBA),
            null,
            new Texture("src/main/resources/textures/PBR/brushed_metal/ao.png", GL_RGBA),
            null
        );

        scene.addPBRMaterial(rustedIron);
        scene.addPBRMaterial(brushedMetal);

        UVSphere uvSphere = new UVSphere(1f, 128, 128);

        Mesh sphereMesh = new Mesh(new MeshData(
            uvSphere.getPositions(),
            uvSphere.getNormals(),
            uvSphere.getTexCoords(),
            new float[]{},
            new float[]{},
            uvSphere.getIndices()
        ));

        meshes.add(sphereMesh);

//        Cylinder cylinder = new Cylinder(1f, 1f, 2f, 16);
//
//        Mesh cylinderMesh = new Mesh(
//            cylinder.getPositions(),
//            cylinder.getNormals(),
//            cylinder.getTexCoords(),
//            cylinder.getIndices()
//        );
//        MaterialMesh cylinderMaterialMesh = new MaterialMesh(cylinderMesh, new PBRMaterial());

//        entities.add(new Entity(cylinderMaterialMesh, new Vector3f(0, -8, -5), new Vector3f(90, 0, 0), 1f));

//        meshes.add(backpackMesh[0]);
//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/backpack/backpack.obj", "src/main/resources/models/backpack");
//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/backpack_original/scene.gltf", "src/main/resources/models/backpack_original");
//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/backpack_fbx/source/Survival_BackPack_2/Survival_BackPack_2.fbx", "src/main/resources/models/backpack_fbx/textures");

//        Entity backpack = new Entity(backpackMesh[0], new Vector3f(5, 0, 5), new Vector3f(), 0.01f);

//        int numRows = 7;
//        int numColumns = 7;
//        float spacing = 2.5f;
//

//        Entity cubeParent = new Entity(new Vector3f(0, 0, 0), "Cube Parent");
//        entities.add(cubeParent);
//
//        Entity sphereParent = new Entity(new Vector3f(0, 0, 0), "Spheres", cubeParent);
//        entities.add(sphereParent);
//
//        for (int row = 0; row < numRows; row++) {
//            for (int column = 0; column < numColumns; column++) {
//                PBRMaterial pbrMaterial = new PBRMaterial(
//                    new Vector3f(1.0f, row * 1.0f, column * 1.0f).normalize(),
//                    (float) row / 7,
//                    Maths.clamp((float) column / (float) 7, 0.05f, 1.0f),
//                    new Vector3f(0.0f, 0.0f, 0.0f)
//                );
//
//                MaterialMesh sphereMaterialMesh = new MaterialMesh(sphereMesh, pbrMaterial);
//                Entity sphere = new Entity(sphereMaterialMesh, new Vector3f((column - (float) (numColumns / 2)) * spacing, (row - (float) (numRows / 2)) * spacing, 0), new Vector3f(0, 90, 0), 1, sphereParent);
//                entities.add(sphere);
//            }
//        }

//        Movement movement = Movement.orbit(Movement.Mode.CONSTANT, new Vector3f(-8, 8, 8), new Vector3f(0, 1, 0), 5, (float) Math.toRadians(90.0f));
//        helmet.setMovement(movement);
//
//        RotationController rotationController = new RotationController();
//        helmet.setRotationController(rotationController);
//
//        entities.add(backpack);
//

//        // Shaderball
//        Model shaderballModel = ModelLoader.load(scene, "src/main/resources/models/shaderball/shaderball.obj", "", null);
//        // Set all meshes to use the brushed metal material
//        for (int i = 0; i < shaderballModel.getMeshDatas().size(); i++) {
//            MeshData meshData = shaderballModel.getMeshDatas().get(i);
//            meshData.setMaterialID(brushedMetal.getID());
//            shaderballModel.getModelMetadata().getMeshDataMaterialIDs().put(i, brushedMetal.getID());
//        }
//
//        Entity shaderball = new Entity(new Vector3f(0, 5, 0), new Vector3f(0, 0, 0), 1f, "Shaderball");
//        shaderballModel.addEntity(shaderball);
//        scene.addEntity(shaderball);
//        scene.addModel(shaderballModel);
//        shaderball.setModelID(shaderballModel.getID());

//        // Sponza
//        Model sponzaModel = ModelLoader.load(scene, "C:/Users/wmjon/Downloads/KhronosGroup glTF-Sample-Models master 2.0-Sponza_glTF/Sponza.gltf", "C:/Users/wmjon/Downloads/KhronosGroup glTF-Sample-Models master 2.0-Sponza_glTF", null);
//        scene.addModel(sponzaModel);
//
//        for (int x = 0; x < 1; x++) {
//            for (int z = 0; z < 1; z++) {
//                Entity sponza = new Entity(new Vector3f(x * 400, 0, z * 200), new Vector3f(0, 0, 0), 10f, "Sponza");
//                sponzaModel.addEntity(sponza);
//                scene.addEntity(sponza);
//                sponza.setModelID(sponzaModel.getID());
//            }
//        }
//
//        Entity sponza = new Entity(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 10f, "Sponza");
//        sponzaModel.addEntity(sponza);
//        scene.addEntity(sponza);
//        sponza.setModelID(sponzaModel.getID());

        // Helmet
        Model helmetModel = ModelLoader.load(scene, "src/main/resources/models/helmet/DamagedHelmet.gltf", "src/main/resources/models/helmet", null);
        Entity helmetEntity = new Entity(new Vector3f(0, 5, 0), new Vector3f(0, 0, 0), 1f, "Helmet");
        helmetModel.addEntity(helmetEntity);
        scene.addEntity(helmetEntity);
        scene.addModel(helmetModel);
        helmetEntity.setModelID(helmetModel.getID());

//        // Cylinder
//        Cylinder newCylinder = new Cylinder(
//                1f,
//                1f,
//                1f,
//                32
//        );
//
//        Model cylinderModel = new Model(new MeshData(
//                newCylinder.getPositions(),
//                newCylinder.getNormals(),
//                new float[]{},
//                new float[]{},
//                newCylinder.getTexCoords(),
//                newCylinder.getIndices()
//        ));
//
//        cylinderModel.getMeshDatas().get(0).setMaterialID(rustedIron.getID());
//        System.out.println("Cylinder Material ID: " + cylinderModel.getMeshDatas().get(0).getMaterialID());
//
//        Entity cylinderEntity = new Entity(new Vector3f(0, 15, 0), new Vector3f(0, 0, 0), 1f, "Cylinder");
//        cylinderModel.addEntity(cylinderEntity);
//        scene.addEntity(cylinderEntity);
//        scene.addModel(cylinderModel);
//        cylinderEntity.setModelID(cylinderModel.getID());
//
//        System.out.println("Scene cylinder material ID: " + scene.getModels().get(1).getMeshDatas().get(0).getMaterialID());

//        // Backpack
//        Model backpackModel = ModelLoader.load(scene, "src/main/resources/models/backpack_original/scene.gltf", "src/main/resources/models/backpack_original");
//        Entity backpack = new Entity(
//            new Vector3f(0, 0, 10),
//            new Vector3f(0, 0, 0),
//            0.01f,
//            "Backpack"
//        );
//        backpackModel.addEntity(backpack);
//        scene.addEntity(backpack);
//        scene.addModel(backpackModel);

//        // Helmet
//        Model helmetModel = ModelLoader.load(scene, "src/main/resources/models/helmet/DamagedHelmet.gltf", "src/main/resources/models/helmet", null);
//
//        // Add 1000 helmets in a cube
//        int numDirection = 10;
//        float spacingDirection = 2.5f;
//        for (int x = 0; x < numDirection; x++) {
//            for (int y = 0; y < numDirection; y++) {
//                for (int z = 0; z < numDirection; z++) {
//                    Entity entity = new Entity(
//                        new Vector3f((x - (float) (numDirection / 2)) * spacingDirection, (y - (float) (numDirection / 2)) * spacingDirection, (z - (float) (numDirection / 2)) * spacingDirection),
//                        new Vector3f(0, 0, 0),
//                        1f,
//                        "Entity" + x + y + z
//                    );
//                    helmetModel.addEntity(entity);
//                    scene.addEntity(entity);
//                }
//            }
//        }

//        // Add 80 * 80 helmets in a square
//        int numDirection = 80;
//        float spacing = 2.5f;
//        for (int row = 0; row < numDirection; row++) {
//            for (int column = 0; column < numDirection; column++) {
//                Entity helmet = new Entity(
//                        new Vector3f((column - (float) (numDirection / 2)) * spacing, (row - (float) (numDirection / 2)) * spacing, 0),
//                        new Vector3f(0, 0, 0),
//                        1f,
//                        "Damaged Helmet" + row + column
//                );
//                helmetModel.addEntity(helmet);
//                scene.addEntity(helmet);
//            }
//        }
//        scene.addModel(helmetModel);

//        // Quad
//        Map<Integer, Integer> meshDataMaterialIDs = new HashMap<>();
//        meshDataMaterialIDs.put(0, 0);
//
//        Quad quad = new Quad();
//        Model quadModel = new Model(
//            new MeshData(
//                quad.getPositions(),
//                quad.getNormals(),
//                new float[]{},
//                new float[]{},
//                quad.getTexCoords(),
//                quad.getIndices()
//            ),
//            new ModelMetadata(
//                quad, meshDataMaterialIDs
//            ),
//            "Quad"
//        );
//
//        quadModel.getMeshDatas().get(0).setMaterialID(0);
//
//        Entity quadEntity = new Entity(new Vector3f(0, 0, -20), new Vector3f(0, 0, 0), 30f, "Quad");
//        quadModel.addEntity(quadEntity);
//        scene.addEntity(quadEntity);
//        scene.addModel(quadModel);
//        quadEntity.setModelID(quadModel.getID());

//        // Add 20 * 20 * 20 cubes
//        Cube cube = new Cube();
//        MeshData cubeMeshData = new MeshData(
//            cube.getPositions(),
//            cube.getNormals(),
//            new float[]{},
//            new float[]{},
//            cube.getTexCoords(),
//            cube.getIndices()
//        );
//
//        Map<Integer, Integer> meshDataMaterialIDs = new HashMap<>();
//        Model cubeModel = new Model(
//            cubeMeshData,
//            new ModelMetadata(
//                cube, meshDataMaterialIDs
//            ),
//            "Cube"
//        );
//
//        int numDirection = 40;
//        float spacing = 2.5f;
//        for (int x = 0; x < numDirection; x++) {
//            for (int y = 0; y < numDirection; y++) {
//                for (int z = 0; z < numDirection; z++) {
//                    Entity cubeEntity = new Entity(
//                        new Vector3f((x - (float) (numDirection / 2)) * spacing, (y - (float) (numDirection / 2)) * spacing, (z - (float) (numDirection / 2)) * spacing),
//                        new Vector3f(0, 0, 0),
//                        1f,
//                        "Cube" + x + y + z
//                    );
//                    cubeModel.addEntity(cubeEntity);
//                    scene.addEntity(cubeEntity);
//                }
//            }
//        }
//
//        scene.addModel(cubeModel);

        DirLight dirLight = new DirLight(
            new Vector3f(2f, 5f, 2f).normalize(),
            new Vector3f(1f, 1f, 1f)
        );

        scene.setDirLight(dirLight);

        PointLight pointLight1 = new PointLight(
            new Vector3f(-50.0f, 10.0f, 30.0f),
            new Vector3f(1.0f, 1.0f, 1.0f),
            500.0f
        );

        scene.addPointLight(pointLight1);

//        PointLight pointLight2 = new PointLight(
//            new Vector3f(50.0f, 10.0f, 30.0f),
//            new Vector3f(1.0f, 1.0f, 1.0f),
//            500.0f
//        );
//
//        scene.addPointLight(pointLight2);

        SpotLight spotLight = new SpotLight(
            new Vector3f(0.0f, 5.0f, 0.0f),
            new Vector3f(0.0f, -1.0f, 0.0f),

            12.5f,
            15.0f,

            new Vector3f(1.0f, 1.0f, 1.0f),

            50.0f
        );

//        SpotLight spotLight2 = new SpotLight(
//            new Vector3f(0, 0, 25),
//            new Vector3f(0, 0, -1),
//
//            15.0f,
//            25.0f,
//
//            new Vector3f(1.0f, 1.0f, 1.0f),
//            5000
//        );

        scene.addSpotLight(spotLight);
//        scene.addSpotLight(spotLight2);

        Texture backgroundTexture = new Texture(
            "src/main/resources/skybox/HDR/Newport_Loft.hdr",
            org.lwjgl.opengl.GL30.GL_RGB16F,
            GL_RGBA,
            org.lwjgl.opengl.GL30.GL_FLOAT,
            true
        );

        EquirectangularMap equirectangularMap = new EquirectangularMap(
            backgroundTexture
        );

        scene.setEquirectangularMap(equirectangularMap);

        Sky sky = new Sky();

        scene.setSky(sky);

        masterRenderer.init(window, renderer, scene, camera, shadowRenderer, omnidirectionalShadowRenderer, spotlightShadowRenderer, gui);
    }

    private void loop() throws Exception {
        while (!window.shouldClose() && !Input.isKeyDown(GLFW_KEY_ESCAPE)) {
            update();
            render();
        }
    }

    private void update() {
        // Calculate delta time
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        scene.getSpotLights().get(0).setPosition(camera.getPosition());
        scene.getSpotLights().get(0).setDirection(camera.getFront());

        for (int i = 0; i < scene.getEntities().size(); i++) {
            Entity entity = scene.getEntities().get(i);
            // Update names
            if (entity.getName() == null) {
                entity.setName("Entity " + i);
            }

            // Update children
            if (entity.getParent() != null && !entity.getParent().getChildren().contains(entity)) {
                entity.getParent().addChild(entity);
            }

            // Update transformations
            entity.update(deltaTime);
        }

        // Update light positions
        for (int i = 0; i < scene.getPointLights().size(); i++) {
            PointLight pointLight = scene.getPointLights().get(i);
            pointLight.update(deltaTime);
        }

        window.update();
        camera.update(deltaTime);

        updateEntities();

        processInput();
    }

    private void updateEntities() {
        int firstEntityIndex = -1;
        int lastEntityIndex = -1;

        int firstModelMeshInstanceIndex = -1;
        int lastModelMeshInstanceIndex = -1;

        List<Entity> entities = scene.getEntities().stream().filter(entity -> entity.getModelID() != -1).toList();

        // Determine first and last updated entity index
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);

            if (entity.isUpdated()) {
                firstEntityIndex = i;
                break;
            }
        }

        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity entity = entities.get(i);

            if (entity.isUpdated()) {
                lastEntityIndex = i;
                break;
            }
        }

        if (firstEntityIndex == -1 || lastEntityIndex == -1) {
            return;
        } else {
            masterRenderer.setSceneUpdated(true);
        }

        int count = 0;
//        for (Entity entity : scene.getEntities()) {
//            if (entity.isUpdated()) {
//                firstModelMeshInstanceIndex = count;
//                break;
//            }
//            count += scene.getModelByID(entity.getModelID()).getMeshDatas().size();
//        }
//
//        count = -1;
//        for (Entity entity : scene.getEntities()) {
//            Model model = scene.getModelByID(entity.getModelID());
//            int increment = model.getMeshDatas().size();
//            count += increment;
//
//            if (entity.isUpdated()) {
//                lastModelMeshInstanceIndex = count;
//            }
//        }

        List<Model> models = scene.getModels().stream().filter(model -> model.getEntities().size() > 0).toList();

        // Determine first and last updated model mesh instance index and map to mesh data indexes
        Map<Integer, Integer> modelMeshInstanceIndexToMeshDataIndexes = new HashMap<>();

        outerloop:
        for (Model model : models) {
            List<Entity> modelEntities = model.getEntities();
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                for (Entity entity : modelEntities) {
                    if (entity.isUpdated()) {
                        firstModelMeshInstanceIndex = count;
                        break outerloop;
                    }
                    count++;
                }
            }
        }

        count = 0;
        for (Model model : models) {
            List<Entity> modelEntities = model.getEntities();
            int meshDataIndex = 0;
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                for (Entity entity : modelEntities) {
                    if (entity.isUpdated()) {
                        lastModelMeshInstanceIndex = count;
                    }

                    modelMeshInstanceIndexToMeshDataIndexes.put(count, meshDataIndex);

                    count++;
                }
                meshDataIndex++;
            }
        }

        System.out.println("Updating " + (lastEntityIndex - firstEntityIndex + 1) + " entities");
        System.out.println("First entity index: " + firstEntityIndex);
        System.out.println("Last entity index: " + lastEntityIndex);
        System.out.println("First model mesh instance index: " + firstModelMeshInstanceIndex);
        System.out.println("Last model mesh instance index: " + lastModelMeshInstanceIndex);

        Matrix4f[] worldMatrices = new Matrix4f[lastModelMeshInstanceIndex - firstModelMeshInstanceIndex + 1];
        int[] materialIDs = new int[lastModelMeshInstanceIndex - firstModelMeshInstanceIndex + 1];
        float[] emissionStrengths = new float[lastModelMeshInstanceIndex - firstModelMeshInstanceIndex + 1];

//        for (int i = firstEntityIndex; i <= lastEntityIndex; i++) {
//            System.out.println("Updating entity " + i);
//            Entity entity = scene.getEntities().get(i);
//
//            // Update all model mesh instances
//            for (int j = firstModelMeshInstanceIndex; j <= lastModelMeshInstanceIndex; j++) {
//                System.out.println("Updating model mesh instance " + (j - firstModelMeshInstanceIndex));
//                worldMatrices[j - firstModelMeshInstanceIndex] = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
//                int materialID = scene.getModelByID(entity.getModelID()).getMeshDatas().get((j - firstModelMeshInstanceIndex) % scene.getModelByID(entity.getModelID()).getMeshDatas().size()).getMaterialID();
////                int materialID = scene.getModelByID(entity.getModelID()).getMeshDatas().get(j - firstModelMeshInstanceIndex).getMaterialID();
//                materialIDs[j - firstModelMeshInstanceIndex] = materialID;
//            }
//
//            entity.setUpdated(false);
//        }
        for (int i = firstModelMeshInstanceIndex; i <= lastModelMeshInstanceIndex; i++) {
            System.out.println("Updating model mesh instance " + i);
            // Calculate entity index from model mesh instance index
            int entityIndex = calculateEntityIndexFromModelMeshInstanceIndex(i);

            Entity entity = scene.getEntities().get(entityIndex);

            worldMatrices[i - firstModelMeshInstanceIndex] = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());

//            int meshDataIndex = (i - firstModelMeshInstanceIndex) % scene.getModelByID(entity.getModelID()).getMeshDatas().size();
//            meshDataIndex /= scene.getModelByID(entity.getModelID()).getEntities().size();
//            int meshDataIndex = (i - (firstModelMeshInstanceIndex - firstModelMeshInstanceIndexRelativeToModel)) / scene.getModelByID(entity.getModelID()).getEntities().size();

            int meshDataIndex = modelMeshInstanceIndexToMeshDataIndexes.get(i);

            int materialID = scene.getModelByID(entity.getModelID()).getMeshDatas().get(meshDataIndex).getMaterialID();
            materialIDs[i - firstModelMeshInstanceIndex] = materialID;

            float emissionStrength = scene.getModelByID(entity.getModelID()).getMeshDatas().get(meshDataIndex).getEmissionStrength();
            emissionStrengths[i - firstModelMeshInstanceIndex] = emissionStrength;

            entity.setUpdated(false);
        }

        masterRenderer.updateModelMeshInstances(firstModelMeshInstanceIndex, lastModelMeshInstanceIndex, worldMatrices, materialIDs, emissionStrengths);
    }

    private int calculateEntityIndexFromModelMeshInstanceIndex(int index) {
        List<Model> models = scene.getModels().stream().filter(model -> model.getEntities().size() > 0).toList();
        int count = 0;
        for (Model model : models) {
            List<Entity> entities = model.getEntities();
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                for (Entity entity : entities) {
                    if (count == index) {
                        return scene.getEntities().indexOf(entity);
                    }
                    count++;
                }
            }
        }

        return -1;
    }

    private void processInput() {
        // Keyboard
        if (!cursorEnabled) {
            if (Input.isKeyDown(GLFW_KEY_W))
                camera.processKeyboard(Camera.Movement.FORWARD, deltaTime);
            if (Input.isKeyDown(GLFW_KEY_S))
                camera.processKeyboard(Camera.Movement.BACKWARD, deltaTime);
            if (Input.isKeyDown(GLFW_KEY_A))
                camera.processKeyboard(Camera.Movement.LEFT, deltaTime);
            if (Input.isKeyDown(GLFW_KEY_D))
                camera.processKeyboard(Camera.Movement.RIGHT, deltaTime);
            if (Input.isKeyDown(GLFW_KEY_SPACE))
                camera.processKeyboard(Camera.Movement.UP, deltaTime);
            if (Input.isKeyDown(GLFW_KEY_LEFT_SHIFT))
                camera.processKeyboard(Camera.Movement.DOWN, deltaTime);

            if (Input.isKeyDown(GLFW_KEY_Q) && renderer.getShaderSettings().getExposure() > 0.0f)
                renderer.getShaderSettings().setExposure(renderer.getShaderSettings().getExposure() - 0.01f);
            if (Input.isKeyDown(GLFW_KEY_E))
                renderer.getShaderSettings().setExposure(renderer.getShaderSettings().getExposure() + 0.01f);

            if (Input.isKeyDown(GLFW_KEY_F) && !lastFrameKeys.contains(GLFW_KEY_F)) {  // If F pressed (and wasn't pressed last frame)
                scene.getSpotLights().get(0).setEnabled(!scene.getSpotLights().get(0).isEnabled());
            }

            if (Input.isKeyDown(GLFW_KEY_T) && !lastFrameKeys.contains(GLFW_KEY_T)) {  // If T pressed (and wasn't pressed last frame)
                renderer.setWireframe(!renderer.isWireframe());
            }

            if (Input.isKeyDown(GLFW_KEY_B) && !lastFrameKeys.contains(GLFW_KEY_B)) {  // If B pressed (and wasn't pressed last frame)
                renderer.setNormalMapping(!renderer.isNormalMapping());
            }

            if (Input.isKeyDown(GLFW_KEY_H)) {
                if (!lastFrameKeys.contains(GLFW_KEY_H)) {
                    masterRenderer.setShowGUI(!masterRenderer.isShowGUI());
                }
                lastFrameKeys.add(GLFW_KEY_H);
            } else {
                lastFrameKeys.remove(Integer.valueOf(GLFW_KEY_H));
            }

            // Update lastFrameKeys
            if (Input.isKeyDown(GLFW_KEY_F) && !lastFrameKeys.contains(GLFW_KEY_F))
                lastFrameKeys.add(GLFW_KEY_F);
            else if (!Input.isKeyDown(GLFW_KEY_F))
                lastFrameKeys.remove(Integer.valueOf(GLFW_KEY_F));

            if (Input.isKeyDown(GLFW_KEY_T) && !lastFrameKeys.contains(GLFW_KEY_T))
                lastFrameKeys.add(GLFW_KEY_T);
            else if (!Input.isKeyDown(GLFW_KEY_T))
                lastFrameKeys.remove(Integer.valueOf(GLFW_KEY_T));

            if (Input.isKeyDown(GLFW_KEY_B) && !lastFrameKeys.contains(GLFW_KEY_B))
                lastFrameKeys.add(GLFW_KEY_B);
            else if (!Input.isKeyDown(GLFW_KEY_B))
                lastFrameKeys.remove(Integer.valueOf(GLFW_KEY_B));
        }

        if (Input.isKeyDown(GLFW_KEY_F12) && !lastFrameKeys.contains(GLFW_KEY_F12)) {
            // Screenshot
            lastFrameKeys.add(GLFW_KEY_F12);
            renderer.screenshot();
        } else if (!Input.isKeyDown(GLFW_KEY_F12)) {
            lastFrameKeys.remove(Integer.valueOf(GLFW_KEY_F12));
        }

        // Mouse

        // Position
        double xpos = Input.getMouseX();
        double ypos = Input.getMouseY();

        if (firstMouse) {
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
        }

        double xoffset = xpos - lastX;
        double yoffset = lastY - ypos;  // reversed since y-coordinates go from bottom to top

        lastX = xpos;
        lastY = ypos;

        // Scroll
        double scrollxpos = Input.getScrollX();
        double scrollypos = Input.getScrollY();

        double scrollxoffset = scrollxpos - lastScrollX;
        double scrollyoffset = scrollypos - lastScrollY;

        lastScrollX = scrollxpos;
        lastScrollY = scrollypos;

        if (!cursorEnabled)
            camera.processMouse((float) xoffset, (float) yoffset, (float) scrollyoffset);

        if (Input.isButtonDown(GLFW_MOUSE_BUTTON_2) && !lastFrameButtons.contains(GLFW_MOUSE_BUTTON_2)) {
            cursorEnabled = !cursorEnabled;
            window.setCursorEnabled(cursorEnabled);
            gui.setCursorEnabled(cursorEnabled);
        }

        // Update lastFrameButtons
        if (Input.isButtonDown(GLFW_MOUSE_BUTTON_2) && !lastFrameButtons.contains(GLFW_MOUSE_BUTTON_2))
            lastFrameButtons.add(GLFW_MOUSE_BUTTON_2);
        else if (!Input.isButtonDown(GLFW_MOUSE_BUTTON_2))
            lastFrameButtons.remove(Integer.valueOf(GLFW_MOUSE_BUTTON_2));
    }

    private void render() throws Exception {
        masterRenderer.render(camera, scene, window);
        window.swapBuffers();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}
