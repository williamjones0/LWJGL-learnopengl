package io.william;

import io.william.io.ModelLoader;
import io.william.renderer.*;
import io.william.io.Input;
import io.william.io.Window;
import io.william.renderer.primitive.Cube;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import io.william.renderer.shadow.SpotlightShadowRenderer;
import io.william.util.Maths;
import org.joml.Vector3f;
import org.lwjgl.*;
import io.william.renderer.primitive.Cylinder;
import io.william.renderer.primitive.Quad;
import io.william.renderer.primitive.UVSphere;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class Main {

    private Window window;
    private Renderer renderer;
    private List<Mesh> meshes;
    private Camera camera;
    private List<SpotLight> spotLights;
    private Scene scene;
    private ShadowRenderer shadowRenderer;
    private OmnidirectionalShadowRenderer omnidirectionalShadowRenderer;
    private SpotlightShadowRenderer spotlightShadowRenderer;
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
        window = new Window(1920, 1080, "LearnOpenGL", 0, false);
        window.create();

        renderer = new Renderer();

        meshes = new ArrayList<>();

        camera = new Camera(new Vector3f(0, 0, 15), 0, 0);

        shadowRenderer = new ShadowRenderer();

        omnidirectionalShadowRenderer = new OmnidirectionalShadowRenderer();

        spotlightShadowRenderer = new SpotlightShadowRenderer();

        gui = new GUI();

        masterRenderer = new MasterRenderer();
        masterRenderer.init(window, renderer, camera, shadowRenderer, omnidirectionalShadowRenderer, spotlightShadowRenderer, gui);

//        PBRMaterial rustedIron = new PBRMaterial(
//            new Texture("src/main/resources/textures/PBR/rusted_iron/basecolor.png", GL_SRGB_ALPHA),
//            new Texture("src/main/resources/textures/PBR/rusted_iron/normal.png", GL_RGBA),
//            new Texture("src/main/resources/textures/PBR/rusted_iron/metallic.png", GL_RGBA),
//            new Texture("src/main/resources/textures/PBR/rusted_iron/roughness.png", GL_RGBA),
//            null,
//            null,
//            null
//        );
//
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
            new Texture("src/main/resources/textures/PBR/brushed_metal/albedo.png", GL_SRGB_ALPHA),
            new Texture("src/main/resources/textures/PBR/brushed_metal/normal.png", GL_RGBA),
            new Texture("src/main/resources/textures/PBR/brushed_metal/metallic.png", GL_RGBA),
            new Texture("src/main/resources/textures/PBR/brushed_metal/roughness.png", GL_RGBA),
            null,
            new Texture("src/main/resources/textures/PBR/brushed_metal/ao.png", GL_RGBA),
            null
        );

        UVSphere uvSphere = new UVSphere(1f, 128, 128);

        Mesh sphereMesh = new Mesh(
            uvSphere.getPositions(),
            uvSphere.getNormals(),
            uvSphere.getTexCoords(),
            uvSphere.getIndices()
        );

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
        List<Entity> entities = new ArrayList<>();

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

        Quad quad = new Quad();
        Mesh planeMesh = new Mesh(
            quad.getPositions(),
            quad.getNormals(),
            quad.getTexCoords(),
            quad.getIndices()
        );
        MaterialMesh planeMaterialMesh = new MaterialMesh(planeMesh, new PBRMaterial());

        meshes.add(planeMesh);

        entities.add(new Entity(planeMaterialMesh, new Vector3f(0, 0, -10), new Vector3f(0, 0, 0), 20));

        Cube cube = new Cube();
        Mesh cubeMesh = new Mesh(
            cube.getPositions(),
            cube.getNormals(),
            cube.getTexCoords(),
            cube.getIndices()
        );
        MaterialMesh cubeMaterialMesh = new MaterialMesh(cubeMesh, new PBRMaterial());

        meshes.add(cubeMesh);

//        entities.add(new Entity(cubeMaterialMesh, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 2));

        MaterialMesh sphereMaterialMesh = new MaterialMesh(sphereMesh, brushedMetal);
        Entity sphere = new Entity(sphereMaterialMesh, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1);
        entities.add(sphere);

//        // Sponza
//        MaterialMesh[] sponzaMaterialMeshes = ModelLoader.load("C:/Users/wmjon/Downloads/KhronosGroup glTF-Sample-Models master 2.0-Sponza_glTF/sponza.gltf", "C:/Users/wmjon/Downloads/KhronosGroup glTF-Sample-Models master 2.0-Sponza_glTF");
//        for (MaterialMesh mesh : sponzaMaterialMeshes) {
//            meshes.add(mesh.getMesh());
//        }
//        Entity sponza = new Entity(sponzaMaterialMeshes, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 10f, "Sponza");
//        entities.add(sponza);

//        // Helmet
//        MaterialMesh[] helmetMaterialMeshes = ModelLoader.load("src/main/resources/models/helmet/DamagedHelmet.gltf", "src/main/resources/models/helmet");
//        for (MaterialMesh mesh : helmetMaterialMeshes) {
//            meshes.add(mesh.getMesh());
//        }
//        Entity helmet = new Entity(helmetMaterialMeshes, new Vector3f(0, 4, 12), new Vector3f(0, 0, 0), 1f, "Damaged Helmet");
//        entities.add(helmet);

//        entities.add(new Entity(planeMaterialMesh, new Vector3f(0, -10, 0), new Vector3f(-90, 0, 0), 10, cubeParent));
//        entities.add(new Entity(planeMaterialMesh, new Vector3f(10, 0, 0), new Vector3f(0f, -90, 0), 10, cubeParent));
//        entities.add(new Entity(planeMaterialMesh, new Vector3f(-10, 0, 0), new Vector3f(0, 90, 0), 10, cubeParent));
//        entities.add(new Entity(planeMaterialMesh, new Vector3f(0, 0, -10), new Vector3f(0, 0, 90), 10, cubeParent));
//        entities.add(new Entity(planeMaterialMesh, new Vector3f(0, 10, 0), new Vector3f(90, 0, 0), 10, cubeParent));

//        DirLight dirLight = new DirLight(
//            new Vector3f(-0.2f, -1.0f, -0.3f),
//            new Vector3f(0.05f, 0.05f, 0.05f),
//            new Vector3f(0.4f, 0.4f, 0.4f),
//            new Vector3f(0.5f, 0.5f, 0.5f)
//        );

        DirLight dirLight = new DirLight(
            new Vector3f(2f, 5f, 2f).normalize(),
            new Vector3f(1f, 1f, 1f)
        );

        PointLight pointLight1 = new PointLight(
            new Vector3f(0.0f, 50.0f, 8.0f),
            new Vector3f(1.0f, 1.0f, 1.0f),
            500.0f
        );

        PointLight pointLight2 = new PointLight(
            new Vector3f(8.0f, 8.0f, 8.0f),
            new Vector3f(1.0f, 1.0f, 1.0f),
            150.0f
        );

        PointLight pointLight3 = new PointLight(
            new Vector3f(-8.0f, -8.0f, 8.0f),
            new Vector3f(1.0f, 1.0f, 1.0f),
            150.0f
        );

        PointLight pointLight4 = new PointLight(
            new Vector3f(8.0f, -8.0f, 8.0f),
            new Vector3f(1.0f, 0.0f, 0.0f),
            150.0f
        );

        List<PointLight> pointLights = new ArrayList<>();
        pointLights.add(pointLight1);
//        pointLights.add(pointLight2);
//        pointLights.add(pointLight3);
//        pointLights.add(pointLight4);

        SpotLight spotLight = new SpotLight(
            sphereMesh,

            new Vector3f(0.0f, 5.0f, 0.0f),
            new Vector3f(0.0f, -1.0f, 0.0f),

            12.5f,
            15.0f,

            new Vector3f(1.0f, 1.0f, 1.0f)
        );

//        SpotLight spotLight2 = new SpotLight(
//            new Vector3f(0, 0, 12),
//            new Vector3f(0, 0, -1),
//
//            12.5f,
//            15.0f,
//
//            new Vector3f(1.0f, 1.0f, 1.0f),
//            500
//        );

        spotLights = new ArrayList<>();
        spotLights.add(spotLight);
//        spotLights.add(spotLight2);

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

        scene = new Scene(entities, dirLight, pointLights, spotLights, equirectangularMap);
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

        spotLights.get(0).setPosition(camera.getPosition());
        spotLights.get(0).setDirection(camera.getFront());

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

        window.update();
        camera.update(deltaTime);

        processInput();
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

            if (Input.isKeyDown(GLFW_KEY_Q) && renderer.getExposure() > 0.0f)
                renderer.setExposure(renderer.getExposure() - 0.01f);
            if (Input.isKeyDown(GLFW_KEY_E))
                renderer.setExposure(renderer.getExposure() + 0.01f);

            if (Input.isKeyDown(GLFW_KEY_F) && !lastFrameKeys.contains(GLFW_KEY_F)) {  // If F pressed (and wasn't pressed last frame)
                spotLights.get(0).setEnabled(!spotLights.get(0).isEnabled());
            }

            if (Input.isKeyDown(GLFW_KEY_T) && !lastFrameKeys.contains(GLFW_KEY_T)) {  // If T pressed (and wasn't pressed last frame)
                renderer.setWireframe(!renderer.isWireframe());
            }

            if (Input.isKeyDown(GLFW_KEY_B) && !lastFrameKeys.contains(GLFW_KEY_B)) {  // If B pressed (and wasn't pressed last frame)
                renderer.setNormalMapping(!renderer.isNormalMapping());
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
