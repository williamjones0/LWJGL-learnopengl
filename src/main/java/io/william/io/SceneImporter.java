package io.william.io;

import io.william.game.component.MovementController;
import io.william.game.component.RotationController;
import io.william.renderer.*;
import io.william.renderer.primitive.Cube;
import io.william.renderer.primitive.Cylinder;
import io.william.renderer.primitive.Quad;
import io.william.renderer.primitive.UVSphere;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import org.joml.Vector3f;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class SceneImporter {

    public static void importScene(String path, Window window, Scene scene, Camera camera, MasterRenderer masterRenderer, Renderer renderer, ShadowRenderer shadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer) {
        // Load JSON file
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(path)) {
            // Read JSON file
            Object obj = parser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);

            parseJson(jsonObject, scene, camera, renderer, shadowRenderer, omnidirectionalShadowRenderer);

            renderer.init(window, camera);
            masterRenderer.setSceneUpdated(true);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void parseJson(JSONObject jsonObject, Scene scene, Camera camera, Renderer renderer, ShadowRenderer shadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer) throws Exception {
        // Scene
        JSONObject sceneJson = (JSONObject) jsonObject.get("scene");
        parseScene(sceneJson, scene);

        // Camera
        JSONObject cameraJson = (JSONObject) jsonObject.get("camera");
        parseCamera(cameraJson, camera);

        // Renderer
        JSONObject rendererJson = (JSONObject) jsonObject.get("renderer");
        parseRenderer(rendererJson, renderer);

        // ShadowRenderer
        JSONObject shadowRendererJson = (JSONObject) jsonObject.get("shadowRenderer");
        parseShadowRenderer(shadowRendererJson, shadowRenderer);

        // OmnidirectionalShadowRenderer
        JSONObject omnidirectionalShadowRendererJson = (JSONObject) jsonObject.get("omnidirectionalShadowRenderer");
        parseOmnidirectionalShadowRenderer(omnidirectionalShadowRendererJson, omnidirectionalShadowRenderer);
    }

    public static void parseScene(JSONObject sceneJson, Scene scene) throws Exception {
        scene.clear();

        // Entities
        JSONObject entitiesJson = (JSONObject) sceneJson.get("entities");
        parseEntities(entitiesJson, scene);

        // Models
        JSONObject modelsJson = (JSONObject) sceneJson.get("models");
        parseModels(modelsJson, scene);

        // Materials
        JSONObject materialsJson = (JSONObject) sceneJson.get("materials");
        parseMaterials(materialsJson, scene);

        // Directional light
        JSONObject dirLightJson = (JSONObject) sceneJson.get("dirLight");
        parseDirLight(dirLightJson, scene);

        // Point lights
        JSONObject pointLightsJson = (JSONObject) sceneJson.get("pointLights");
        parsePointLights(pointLightsJson, scene);

        // Spot lights
        JSONObject spotLightsJson = (JSONObject) sceneJson.get("spotLights");
        parseSpotLights(spotLightsJson, scene);

        // Equirectangular map
        JSONObject equirectangularMapJson = (JSONObject) sceneJson.get("equirectangularMap");
        parseEquirectangularMap(equirectangularMapJson, scene);
    }

    public static void parseModels(JSONObject modelsJson, Scene scene) {
        modelsJson.forEach((key, value) -> {
            JSONObject modelJson = (JSONObject) value;
            try {
                parseModel(modelJson, scene);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void parseModel(JSONObject modelJson, Scene scene) throws Exception {
        System.out.println(modelJson);

        JSONObject modelMetadataJson = (JSONObject) modelJson.get("modelMetadata");
        System.out.println(modelMetadataJson.get("type").toString());

        JSONObject meshDataMaterialIDsJson = (JSONObject) modelMetadataJson.get("meshDataMaterialIDs");
        // Map mesh data indexes to material IDs
        Map<Integer, Integer> meshDataMaterialIDs = new HashMap<>();
        meshDataMaterialIDsJson.forEach((key, value) -> {
            meshDataMaterialIDs.put(Integer.parseInt(key.toString()), Integer.parseInt(value.toString()));
        });

        Model model;
        switch (modelMetadataJson.get("type").toString()) {
            case "quad" -> {
                Quad quad = new Quad();

                model = new Model(
                    new MeshData(
                        quad.getPositions(),
                        quad.getNormals(),
                        new float[] {},
                        new float[] {},
                        quad.getTexCoords(),
                        quad.getIndices()
                    ),
                    new ModelMetadata(
                        ModelMetadata.Type.QUAD,
                        meshDataMaterialIDs
                    ),
                    modelJson.get("name").toString()
                );
            }
            case "cube" -> {
                Cube cube = new Cube();

                model = new Model(
                    new MeshData(
                        cube.getPositions(),
                        cube.getNormals(),
                        new float[] {},
                        new float[] {},
                        cube.getTexCoords(),
                        cube.getIndices()
                    ),
                    new ModelMetadata(
                        ModelMetadata.Type.CUBE,
                        meshDataMaterialIDs
                    ),
                    modelJson.get("name").toString()
                );
            }
            case "cylinder" -> {
                Cylinder cylinder = new Cylinder(
                    Float.parseFloat(modelMetadataJson.get("topRadius").toString()),
                    Float.parseFloat(modelMetadataJson.get("bottomRadius").toString()),
                    Float.parseFloat(modelMetadataJson.get("height").toString()),
                    Integer.parseInt(modelMetadataJson.get("sectors").toString())
                );

                model = new Model(
                    new MeshData(
                        cylinder.getPositions(),
                        cylinder.getNormals(),
                        new float[] {},
                        new float[] {},
                        cylinder.getTexCoords(),
                        cylinder.getIndices()
                    ),
                    new ModelMetadata(
                        cylinder,
                        meshDataMaterialIDs
                    ),
                    modelJson.get("name").toString()
                );
            }
            case "sphere" -> {
                UVSphere sphere = new UVSphere(
                    Float.parseFloat(modelMetadataJson.get("radius").toString()),
                    Integer.parseInt(modelMetadataJson.get("sectors").toString()),
                    Integer.parseInt(modelMetadataJson.get("stacks").toString())
                );

                model = new Model(
                    new MeshData(
                        sphere.getPositions(),
                        sphere.getNormals(),
                        new float[] {},
                        new float[] {},
                        sphere.getTexCoords(),
                        sphere.getIndices()
                    ),
                    new ModelMetadata(
                        ModelMetadata.Type.SPHERE,
                        meshDataMaterialIDs
                    ),
                    modelJson.get("name").toString()
                );
            }
            case "assimp" -> {
                model = ModelLoader.load(scene, modelMetadataJson.get("modelPath").toString(), modelMetadataJson.get("texturesPath").toString(), meshDataMaterialIDs);
            }
            default -> throw new Exception("Unknown model type");
        }

        // Update model mesh data material IDs
        meshDataMaterialIDs.forEach((meshDataIndex, materialID) -> {
            model.getMeshDatas().get(meshDataIndex).setMaterialID(materialID);
        });

        JSONArray entitiesJson = (JSONArray) modelJson.get("entities");
        entitiesJson.forEach(entityID -> {
            Entity entity = scene.getEntityByID(Integer.parseInt(entityID.toString()));
            model.addEntity(entity);
        });

        scene.addModel(model);
    }

    public static void parseMaterials(JSONObject materialsJson, Scene scene) {
        materialsJson.forEach((key, value) -> {
            JSONObject materialJson = (JSONObject) value;
            try {
                parseMaterial(materialJson, (String) key, scene);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void parseMaterial(JSONObject materialJson, String id, Scene scene) throws Exception {
        PBRMaterial material;
        if (materialJson.get("albedo") == null) {
            // Material does not use textures
            JSONObject albedoColorJson = (JSONObject) materialJson.get("albedoColor");
            JSONObject emissiveColorJson = (JSONObject) materialJson.get("emissiveColor");

            material = new PBRMaterial(
                materialJson.get("name").toString(),
                false,
                new Vector3f(
                    Float.parseFloat(albedoColorJson.get("x").toString()),
                    Float.parseFloat(albedoColorJson.get("y").toString()),
                    Float.parseFloat(albedoColorJson.get("z").toString())
                ),
                Float.parseFloat(materialJson.get("metallicFactor").toString()),
                Float.parseFloat(materialJson.get("roughnessFactor").toString()),
                new Vector3f(
                    Float.parseFloat(emissiveColorJson.get("x").toString()),
                    Float.parseFloat(emissiveColorJson.get("y").toString()),
                    Float.parseFloat(emissiveColorJson.get("z").toString())
                )
            );
        } else {
            // Material does use textures
            material = new PBRMaterial(
                materialJson.get("name").toString(),
                false,
                materialJson.get("albedo") != null ? new Texture(materialJson.get("albedo").toString(), GL_SRGB_ALPHA) : null,
                materialJson.get("normal") != null ? new Texture(materialJson.get("normal").toString(), GL_RGBA) : null,
                materialJson.get("metallic") != null ? new Texture(materialJson.get("metallic").toString(), GL_RGBA) : null,
                materialJson.get("roughness") != null ? new Texture(materialJson.get("roughness").toString(), GL_RGBA) : null,
                materialJson.get("metallicRoughness") != null ? new Texture(materialJson.get("metallicRoughness").toString(), GL_RGBA) : null,
                materialJson.get("ao") != null ? new Texture(materialJson.get("ao").toString(), GL_RGBA) : null,
                materialJson.get("emissive") != null ? new Texture(materialJson.get("emissive").toString(), GL_SRGB_ALPHA) : null
            );
        }

        scene.addPBRMaterialByID(material, Integer.parseInt(id));
    }

    public static void parseEntities(JSONObject entitiesJson, Scene scene) {
        entitiesJson.forEach((key, value) -> {
            JSONObject entityJson = (JSONObject) value;
            try {
                parseEntity(entityJson, scene);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Set parents and children once all entities have been parsed
        entitiesJson.forEach((key, value) -> {
            JSONObject entityJson = (JSONObject) value;
            try {
                Entity entity = scene.getEntityByID(Integer.parseInt(entityJson.get("id").toString()));
                if (entityJson.get("parent") != null) {
                    entity.setParent(scene.getEntityByID(Integer.parseInt(entityJson.get("parent").toString())));
                }
                if (entityJson.get("children") != null) {
                    JSONArray childrenJson = (JSONArray) entityJson.get("children");
                    for (Object child : childrenJson) {
                        entity.addChild(scene.getEntityByID(Integer.parseInt(child.toString())));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void parseEntity(JSONObject entityJson, Scene scene) {
        JSONObject positionJson = (JSONObject) entityJson.get("position");
        JSONObject rotationJson = (JSONObject) entityJson.get("rotation");
        JSONObject movementControllerJson = (JSONObject) entityJson.get("movementController");
        JSONObject rotationControllerJson = (JSONObject) entityJson.get("rotationController");

        Entity entity = new Entity(
            new Vector3f(
                Float.parseFloat(positionJson.get("x").toString()),
                Float.parseFloat(positionJson.get("y").toString()),
                Float.parseFloat(positionJson.get("z").toString())
            ),
            new Vector3f(
                Float.parseFloat(rotationJson.get("x").toString()),
                Float.parseFloat(rotationJson.get("y").toString()),
                Float.parseFloat(rotationJson.get("z").toString())
            ),
            Float.parseFloat(entityJson.get("scale").toString()),
            entityJson.get("name").toString()
        );

        entity.setID(Integer.parseInt(entityJson.get("id").toString()));

        entity.setModelID(Integer.parseInt(entityJson.get("modelID").toString()));

        if (movementControllerJson != null) {
            JSONObject centerJson = (JSONObject) movementControllerJson.get("center");
            JSONObject axisJson = (JSONObject) movementControllerJson.get("axis");
            JSONObject originJson = (JSONObject) movementControllerJson.get("origin");
            JSONObject directionJson = (JSONObject) movementControllerJson.get("direction");

            switch (movementControllerJson.get("type").toString()) {
                case "NONE" -> entity.setMovementController(MovementController.none(scene));
                case "ORBIT" -> entity.setMovementController(MovementController.orbit(
                    scene,
                    switch (movementControllerJson.get("mode").toString()) {
                        case "CONSTANT" -> MovementController.Mode.CONSTANT;
                        case "ACCELERATION" -> MovementController.Mode.ACCELERATION;
                        case "DECELERATION" -> MovementController.Mode.DECELERATION;
                        default -> throw new IllegalStateException("Unexpected value: " + movementControllerJson.get("orbitType").toString());
                    },
                    Float.parseFloat(movementControllerJson.get("speed").toString()),
                    new Vector3f(
                        Float.parseFloat(centerJson.get("x").toString()),
                        Float.parseFloat(centerJson.get("y").toString()),
                        Float.parseFloat(centerJson.get("z").toString())
                    ),
                    new Vector3f(
                        Float.parseFloat(axisJson.get("x").toString()),
                        Float.parseFloat(axisJson.get("y").toString()),
                        Float.parseFloat(axisJson.get("z").toString())
                    ),
                    Float.parseFloat(movementControllerJson.get("radius").toString())
                ));
                case "DIRECTION" -> entity.setMovementController(MovementController.direction(
                    scene,
                    switch (movementControllerJson.get("mode").toString()) {
                        case "CONSTANT" -> MovementController.Mode.CONSTANT;
                        case "ACCELERATION" -> MovementController.Mode.ACCELERATION;
                        case "DECELERATION" -> MovementController.Mode.DECELERATION;
                        default -> throw new IllegalStateException("Unexpected value: " + movementControllerJson.get("orbitType").toString());
                    },
                    Float.parseFloat(movementControllerJson.get("speed").toString()),
                    new Vector3f(
                        Float.parseFloat(originJson.get("x").toString()),
                        Float.parseFloat(originJson.get("y").toString()),
                        Float.parseFloat(originJson.get("z").toString())
                    ),
                    new Vector3f(
                        Float.parseFloat(directionJson.get("x").toString()),
                        Float.parseFloat(directionJson.get("y").toString()),
                        Float.parseFloat(directionJson.get("z").toString())
                    ),
                    Float.parseFloat(movementControllerJson.get("acceleration").toString()),
                    Float.parseFloat(movementControllerJson.get("distance").toString())
                ));
                case "PATH" -> {
                    List<Vector3f> path = new ArrayList<>();
                    JSONArray pathJson = (JSONArray) movementControllerJson.get("path");
                    pathJson.forEach((pathPoint) -> {
                        JSONObject pathPointJson = (JSONObject) pathPoint;
                        path.add(new Vector3f(
                            Float.parseFloat(pathPointJson.get("x").toString()),
                            Float.parseFloat(pathPointJson.get("y").toString()),
                            Float.parseFloat(pathPointJson.get("z").toString())
                        ));
                    });

                    entity.setMovementController(MovementController.path(
                        scene,
                        switch (movementControllerJson.get("mode").toString()) {
                            case "CONSTANT" -> MovementController.Mode.CONSTANT;
                            case "ACCELERATION" -> MovementController.Mode.ACCELERATION;
                            case "DECELERATION" -> MovementController.Mode.DECELERATION;
                            default -> throw new IllegalStateException("Unexpected value: " + movementControllerJson.get("orbitType").toString());
                        },
                        Float.parseFloat(movementControllerJson.get("speed").toString()),
                        path
                    ));
                }
            }
        }

        if (rotationControllerJson != null) {
            JSONObject speedJson = (JSONObject) rotationControllerJson.get("speed");
            JSONObject accelerationJson = (JSONObject) rotationControllerJson.get("acceleration");

            RotationController rotationController = new RotationController(
                new Vector3f(
                    Float.parseFloat(speedJson.get("x").toString()),
                    Float.parseFloat(speedJson.get("y").toString()),
                    Float.parseFloat(speedJson.get("z").toString())
                ),
                new Vector3f(
                    Float.parseFloat(accelerationJson.get("x").toString()),
                    Float.parseFloat(accelerationJson.get("y").toString()),
                    Float.parseFloat(accelerationJson.get("z").toString())
                )
            );

            rotationController.setMode(switch (rotationControllerJson.get("mode").toString()) {
                case "NONE" -> RotationController.Mode.NONE;
                case "CONSTANT" -> RotationController.Mode.CONSTANT;
                case "ACCELERATION" -> RotationController.Mode.ACCELERATION;
                case "DECELERATION" -> RotationController.Mode.DECELERATION;
                default -> throw new IllegalStateException("Unexpected value: " + rotationControllerJson.get("mode").toString());
            });

            rotationController.setStopAtZeroSpeed(Boolean.parseBoolean(rotationControllerJson.get("stopAtZeroSpeed").toString()));

            entity.setRotationController(rotationController);
        }

        scene.addEntity(entity);
    }

    public static void parseDirLight(JSONObject dirLightJson, Scene scene) {
        JSONObject directionJson = (JSONObject) dirLightJson.get("direction");
        JSONObject colorJson = (JSONObject) dirLightJson.get("color");

        Vector3f direction = new Vector3f(
            Float.parseFloat(directionJson.get("x").toString()),
            Float.parseFloat(directionJson.get("y").toString()),
            Float.parseFloat(directionJson.get("z").toString())
        );

        DirLight directionalLight = new DirLight(
            direction,
            new Vector3f(
                Float.parseFloat(colorJson.get("x").toString()),
                Float.parseFloat(colorJson.get("y").toString()),
                Float.parseFloat(colorJson.get("z").toString())
            )
        );

        scene.setDirLight(directionalLight);
        scene.getDirLight().setDirection(direction);  // hacky but stops the azimuth/elevation resetting the direction vector
    }

    public static void parsePointLights(JSONObject pointLightsJson, Scene scene) {
        pointLightsJson.forEach((key, value) -> {
            JSONObject pointLightJson = (JSONObject) value;
            try {
                parsePointLight(pointLightJson, scene);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void parsePointLight(JSONObject pointLightJson, Scene scene) {
        JSONObject positionJson = (JSONObject) pointLightJson.get("position");
        JSONObject colorJson = (JSONObject) pointLightJson.get("color");

        PointLight pointLight = new PointLight(
            new Vector3f(
                Float.parseFloat(positionJson.get("x").toString()),
                Float.parseFloat(positionJson.get("y").toString()),
                Float.parseFloat(positionJson.get("z").toString())
            ),
            new Vector3f(
                Float.parseFloat(colorJson.get("x").toString()),
                Float.parseFloat(colorJson.get("y").toString()),
                Float.parseFloat(colorJson.get("z").toString())
            ),
            Float.parseFloat(pointLightJson.get("intensity").toString())
        );

        pointLight.setEnabled(Boolean.parseBoolean(pointLightJson.get("enabled").toString()));

        scene.addPointLight(pointLight);
    }

    public static void parseSpotLights(JSONObject spotLightsJson, Scene scene) {
        spotLightsJson.forEach((key, value) -> {
            JSONObject spotLightJson = (JSONObject) value;
            try {
                parseSpotLight(spotLightJson, scene);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void parseSpotLight(JSONObject spotLightJson, Scene scene) {
        JSONObject positionJson = (JSONObject) spotLightJson.get("position");
        JSONObject directionJson = (JSONObject) spotLightJson.get("direction");
        JSONObject colorJson = (JSONObject) spotLightJson.get("color");

        SpotLight spotLight = new SpotLight(
            new Vector3f(
                Float.parseFloat(positionJson.get("x").toString()),
                Float.parseFloat(positionJson.get("y").toString()),
                Float.parseFloat(positionJson.get("z").toString())
            ),
            new Vector3f(
                Float.parseFloat(directionJson.get("x").toString()),
                Float.parseFloat(directionJson.get("y").toString()),
                Float.parseFloat(directionJson.get("z").toString())
            ),
            Float.parseFloat(spotLightJson.get("cutoff").toString()),
            Float.parseFloat(spotLightJson.get("outerCutoff").toString()),
            new Vector3f(
                Float.parseFloat(colorJson.get("x").toString()),
                Float.parseFloat(colorJson.get("y").toString()),
                Float.parseFloat(colorJson.get("z").toString())
            ),
            Float.parseFloat(spotLightJson.get("intensity").toString())
        );

        spotLight.setEnabled(Boolean.parseBoolean(spotLightJson.get("enabled").toString()));

        scene.addSpotLight(spotLight);
    }

    public static void parseEquirectangularMap(JSONObject equirectangularMapJson, Scene scene) throws Exception {
        String path = equirectangularMapJson.get("path").toString();
        scene.setEquirectangularMap(new EquirectangularMap(path));
    }

    public static void parseCamera(JSONObject cameraJson, Camera camera) {
        JSONObject positionJson = (JSONObject) cameraJson.get("position");
        camera.setPosition(
            new Vector3f(
                Float.parseFloat(positionJson.get("x").toString()),
                Float.parseFloat(positionJson.get("y").toString()),
                Float.parseFloat(positionJson.get("z").toString())
            )
        );
        camera.setYaw(Float.parseFloat(cameraJson.get("yaw").toString()));
        camera.setPitch(Float.parseFloat(cameraJson.get("pitch").toString()));
        camera.setFOV(Float.parseFloat(cameraJson.get("fov").toString()));
        camera.setMovementSpeed(Float.parseFloat(cameraJson.get("speed").toString()));
        camera.setMouseSensitivity(Float.parseFloat(cameraJson.get("sensitivity").toString()));
        camera.setDeceleration(Float.parseFloat(cameraJson.get("deceleration").toString()));
    }

    public static void parseRenderer(JSONObject rendererJSON, Renderer renderer) {
        renderer.setzNear(Float.parseFloat(rendererJSON.get("zNear").toString()));
        renderer.setzFar(Float.parseFloat(rendererJSON.get("zFar").toString()));
        parseShaderSettings((JSONObject) rendererJSON.get("shaderSettings"), renderer);
    }

    public static void parseShaderSettings(JSONObject shaderSettingsJson, Renderer renderer) {
        ShaderSettings shaderSettings = new ShaderSettings();
        shaderSettings.setExposure(Float.parseFloat(shaderSettingsJson.get("exposure").toString()));
        shaderSettings.setSpecularOcclusion(Boolean.parseBoolean(shaderSettingsJson.get("specularOcclusion").toString()));
        shaderSettings.setHorizonSpecularOcclusion(Boolean.parseBoolean(shaderSettingsJson.get("horizonSpecularOcclusion").toString()));
        shaderSettings.setShadowMinBias(Float.parseFloat(shaderSettingsJson.get("shadowMinBias").toString()));
        shaderSettings.setShadowMaxBias(Float.parseFloat(shaderSettingsJson.get("shadowMaxBias").toString()));
        shaderSettings.setPointShadows(Boolean.parseBoolean(shaderSettingsJson.get("pointShadows").toString()));
        shaderSettings.setPointShadowBias(Float.parseFloat(shaderSettingsJson.get("pointShadowBias").toString()));

        renderer.setShaderSettings(shaderSettings);
    }

    public static void parseShadowRenderer(JSONObject shadowRendererJson, ShadowRenderer shadowRenderer) {
        shadowRenderer.setNearPlane(Float.parseFloat(shadowRendererJson.get("nearPlane").toString()));
        shadowRenderer.setFarPlane(Float.parseFloat(shadowRendererJson.get("farPlane").toString()));
        shadowRenderer.setSize(Float.parseFloat(shadowRendererJson.get("size").toString()));
    }

    public static void parseOmnidirectionalShadowRenderer(JSONObject omnidirectionalShadowRendererJson, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer) {
        omnidirectionalShadowRenderer.setNearPlane(Float.parseFloat(omnidirectionalShadowRendererJson.get("nearPlane").toString()));
        omnidirectionalShadowRenderer.setFarPlane(Float.parseFloat(omnidirectionalShadowRendererJson.get("farPlane").toString()));
    }

}
