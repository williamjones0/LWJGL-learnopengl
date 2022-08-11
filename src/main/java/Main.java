import org.joml.Vector3f;
import org.lwjgl.*;
import primitives.UVSphere;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

    private Window window;
    private Renderer renderer;
    private List<Mesh> meshes;
    private Camera camera;
    private SpotLight[] spotLights;
    private Scene scene;

    private float deltaTime = 0.0f;
    private float lastFrame = 0.0f;

    private double lastX, lastY = 0;
    private List<Integer> lastFrameKeys = new ArrayList<>();

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
        window = new Window(1280, 720, "LearnOpenGL", false, 0.1f, 0.1f, 0.1f);
        window.create();

        renderer = new Renderer();
        renderer.init(window);

        meshes = new ArrayList<>();

        camera = new Camera(new Vector3f(0, 0, 3), -90f, 0);

        float[] skyboxVertices = {
            // positions
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            -1.0f,  1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f
        };

        float[] planePositions = {
            // Positions
           -1.0f,  1.0f, 0f,  // Top left
           -1.0f, -1.0f, 0f,  // Bottom left
            1.0f, -1.0f, 0f,  // Bottom right
            1.0f,  1.0f, 0f,  // Top right
        };

        float[] planeNormals = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
        };

        float[] planeTangents = {
            2.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f
        };

        float[] planeTexCoords = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        };

        int[] planeIndices = {
            0, 1, 3,
            3, 1, 2
        };

//        Texture planeDiffuse = new Texture("src/main/resources/textures/brickwall.jpg", Texture.Format.SRGBA);
//        Texture planeNormal = new Texture("src/main/resources/textures/brickwall_normal.jpg", Texture.Format.RGBA);
//        Material planeMaterial = new Material(planeDiffuse, null, 32, planeNormal);
//
//        Mesh planeMesh = new Mesh(
//            planePositions,
//            planeNormals,
//            planeTangents,
//            planeTexCoords,
//            planeIndices,
//            planeMaterial
//        );
//
//        meshes.add(planeMesh);
//
//        Entity plane = new Entity(planeMesh, new Vector3f(0, 0, 0), new Vector3f(), 1);

        Texture materialDiffuse = new Texture("src/main/resources/textures/container.png", Texture.Format.SRGBA);
        Texture materialSpecular = new Texture("src/main/resources/textures/container_specular.png", Texture.Format.RGBA);
        float materialShininess = 256.0f;
        Material material = new Material(materialDiffuse, materialSpecular, materialShininess, null);

        PBRMaterial pbrMaterial = new PBRMaterial(
            new Texture("src/main/resources/textures/PBR/rusted_iron/basecolor.png", Texture.Format.SRGBA),
            new Texture("src/main/resources/textures/PBR/rusted_iron/normal.png", Texture.Format.RGBA),
            new Texture("src/main/resources/textures/PBR/rusted_iron/metallic.png", Texture.Format.RGBA),
            new Texture("src/main/resources/textures/PBR/rusted_iron/roughness.png", Texture.Format.RGBA),
            null
        );

        UVSphere uvSphere = new UVSphere(1f, 36, 18);

        float[] sphereNormals = {
            2.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f,
            2.0f, 0.0f, 0.0f,
        };

        Mesh sphereMesh = new Mesh(
            uvSphere.getPositions(),
            uvSphere.getNormals(),
            sphereNormals,
            uvSphere.getTexCoords(),
            uvSphere.getIndices(),
            pbrMaterial
        );
        meshes.add(sphereMesh);

//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/helmet/DamagedHelmet.gltf", "src/main/resources/models/helmet");
//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/backpack/backpack.obj", "src/main/resources/models/backpack");
//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/backpack_original/scene.gltf", "src/main/resources/models/backpack_original");
//        Mesh[] backpackMesh = ModelLoader.load("src/main/resources/models/backpack_fbx/source/Survival_BackPack_2/Survival_BackPack_2.fbx", "src/main/resources/models/backpack_fbx/textures");
//        meshes.addAll(Arrays.asList(backpackMesh));

//        Entity backpack = new Entity(backpackMesh[0], new Vector3f(0, 0, 5), new Vector3f(), 1);

        int numRows = 7;
        int numColumns = 7;
        float spacing = 2.5f;

        Entity[] entities = new Entity[numRows * numColumns];

        for (int row = 0; row < numRows; row++) {
            for (int column = 0; column < numColumns; column++) {
                Entity sphere = new Entity(sphereMesh, new Vector3f((column - (numColumns / 2)) * spacing, (row - (numRows / 2)) * spacing, 0), new Vector3f(0, 90, 0), 1);
                entities[row * numColumns + column] = sphere;
            }
        }

//        DirLight dirLight = new DirLight(
//            new Vector3f(-0.2f, -1.0f, -0.3f),
//            new Vector3f(0.05f, 0.05f, 0.05f),
//            new Vector3f(0.4f, 0.4f, 0.4f),
//            new Vector3f(0.5f, 0.5f, 0.5f)
//        );

        DirLight dirLight = new DirLight(
            new Vector3f(-0.2f, -1.0f, -0.3f),
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(0.0f, 0.0f, 0.0f)
        );

        PointLight pointLight1 = new PointLight(
            sphereMesh,
            new Vector3f(-10.0f,  10.0f, 10.0f),
            new Vector3f(150.0f, 150.0f, 150.0f)
        );

        PointLight pointLight2 = new PointLight(
            sphereMesh,
            new Vector3f(10.0f,  10.0f, 10.0f),
            new Vector3f(150.0f, 150.0f, 150.0f)
        );

        PointLight pointLight3 = new PointLight(
            sphereMesh,
            new Vector3f(-10.0f,  -10.0f, 10.0f),
            new Vector3f(150.0f, 150.0f, 150.0f)
        );

        PointLight pointLight4 = new PointLight(
            sphereMesh,
            new Vector3f(10.0f,  -10.0f, 10.0f),
            new Vector3f(150.0f, 150.0f, 150.0f)
        );

        PointLight[] pointLights = new PointLight[]{
            pointLight1,
            pointLight2,
            pointLight3,
            pointLight4
        };

        SpotLight spotLight = new SpotLight(
            sphereMesh,

            new Vector3f(0.0f, 5.0f, 0.0f),
            new Vector3f(0.0f, -1.0f, 0.0f),

            (float) Math.cos(Math.toRadians(12.5f)),
            (float) Math.cos(Math.toRadians(15.0f)),

            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(1.0f, 1.0f, 1.0f),
            new Vector3f(1.0f, 1.0f, 1.0f)
        );

        spotLights = new SpotLight[] {
            spotLight
        };

        String[] faces = {
            "src/main/resources/skybox/right.jpg",
            "src/main/resources/skybox/left.jpg",
            "src/main/resources/skybox/top.jpg",
            "src/main/resources/skybox/bottom.jpg",
            "src/main/resources/skybox/front.jpg",
            "src/main/resources/skybox/back.jpg"
        };

        Skybox skybox = new Skybox(faces, skyboxVertices);

        scene = new Scene(entities, dirLight, pointLights, spotLights, skybox);
    }

    private void loop() {
        while ( !window.shouldClose() && !Input.isKeyDown(GLFW_KEY_ESCAPE)) {
            update();
            render();
        }
    }

    private void update() {
        window.update();

        // Calculate delta time
        float currentFrame = (float) glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        spotLights[0].setPosition(camera.getPosition());
        spotLights[0].setDirection(camera.getFront());

//        scene.getEntities()[1].setRotation(scene.getEntities()[1].getRotation().add(new Vector3f(0, 0, 0.1f)));

        processInput();
    }

    private void processInput() {
        // Keyboard
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

        if (Input.isKeyDown(GLFW_KEY_I))
            scene.getPointLights()[0].setPosition(scene.getPointLights()[0].getPosition().add(new Vector3f(0, 0.01f, 0)));
        if (Input.isKeyDown(GLFW_KEY_K))
            scene.getPointLights()[0].setPosition(scene.getPointLights()[0].getPosition().add(new Vector3f(0, -0.01f, 0)));
        if (Input.isKeyDown(GLFW_KEY_J))
            scene.getPointLights()[0].setPosition(scene.getPointLights()[0].getPosition().add(new Vector3f(-0.01f, 0, 0)));
        if (Input.isKeyDown(GLFW_KEY_L))
            scene.getPointLights()[0].setPosition(scene.getPointLights()[0].getPosition().add(new Vector3f(0.01f, 0, 0)));

        if (Input.isKeyDown(GLFW_KEY_Q) && renderer.getExposure() > 0.0f)
            renderer.setExposure(renderer.getExposure() - 0.01f);
        if (Input.isKeyDown(GLFW_KEY_E))
            renderer.setExposure(renderer.getExposure() + 0.01f);

        if (Input.isKeyDown(GLFW_KEY_C)) {
            renderer.setFOV((float) Math.toRadians(30.0));
        } else {
            renderer.setFOV((float) Math.toRadians(60.0));
        }

        if (Input.isKeyDown(GLFW_KEY_F) && !lastFrameKeys.contains(GLFW_KEY_F)) {  // If F pressed (and wasn't pressed last frame)
            spotLights[0].setEnabled(!spotLights[0].isEnabled());
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

        // Mouse
        double xpos = Input.getMouseX();
        double ypos = Input.getMouseY();

        double xoffset = xpos - lastX;
        double yoffset = lastY - ypos;  // reversed since y-coordinates go from bottom to top

        lastX = xpos;
        lastY = ypos;

        camera.processMouse((float) xoffset, (float) yoffset);
    }

    private void render() {
        renderer.render(camera, scene);
        window.swapBuffers();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

}
