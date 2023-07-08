package io.william.io;

import io.william.game.component.MovementController;
import io.william.game.component.RotationController;
import io.william.renderer.*;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import org.joml.Vector3f;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SceneExporter {

    public static void export(Scene scene,
                              Camera camera,
                              Renderer renderer,
                              ShadowRenderer shadowRenderer,
                              OmnidirectionalShadowRenderer omnidirectionalShadowRenderer) {
        export(System.getProperty("user.dir") + "/src/main/resources/scene_" + System.currentTimeMillis() + ".json",
                scene,
                camera,
                renderer,
                shadowRenderer,
                omnidirectionalShadowRenderer);
    }

    public static void export(String path,
                              Scene scene,
                              Camera camera,
                              Renderer renderer,
                              ShadowRenderer shadowRenderer,
                              OmnidirectionalShadowRenderer omnidirectionalShadowRenderer) {

        JSONObject jsonObject = new JSONObject();

        // Scene
        jsonObject.put("scene", sceneToJSONObject(scene));

        // Camera
        jsonObject.put("camera", cameraToJSONObject(camera));

        // Renderer
        jsonObject.put("renderer", rendererToJSONObject(renderer));

        // ShadowRenderer
        jsonObject.put("shadowRenderer", shadowRendererToJSONObject(shadowRenderer));

        // OmnidirectionalShadowRenderer
        jsonObject.put("omnidirectionalShadowRenderer", omnidirectionalShadowRendererToJSONObject(omnidirectionalShadowRenderer));

        try (FileWriter file = new FileWriter(path)) {
            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject sceneToJSONObject(Scene scene) {
        JSONObject sceneObject = new JSONObject();

        // Models
        List<Model> models = scene.getModels();
        JSONObject modelsObject = modelsToJSONObject(models);
        sceneObject.put("models", modelsObject);

        // Materials
        List<PBRMaterial> materials = scene.getPBRMaterials();
        JSONObject materialsObject = materialsToJSONObject(materials);
        sceneObject.put("materials", materialsObject);

        // Entities
        List<Entity> entities = scene.getEntities();
        JSONObject entitiesObject = entitiesToJSONObject(entities);
        sceneObject.put("entities", entitiesObject);

        // Directional light
        DirLight dirLight = scene.getDirLight();
        JSONObject dirLightObject = dirLightToJSONObject(dirLight);
        sceneObject.put("dirLight", dirLightObject);

        // Point lights
        List<PointLight> pointLights = scene.getPointLights();
        JSONObject pointLightsObject = pointLightsToJSONObject(pointLights);
        sceneObject.put("pointLights", pointLightsObject);

        // Spot lights
        List<SpotLight> spotLights = scene.getSpotLights();
        JSONObject spotLightsObject = spotLightsToJSONObject(spotLights);
        sceneObject.put("spotLights", spotLightsObject);

        // Equirectangular map
        EquirectangularMap equirectangularMap = scene.getEquirectangularMap();
        JSONObject equirectangularMapObject = equirectangularMapToJSONObject(equirectangularMap);
        sceneObject.put("equirectangularMap", equirectangularMapObject);

        return sceneObject;
    }

    private static JSONObject modelsToJSONObject(List<Model> models) {
        JSONObject modelsObject = new JSONObject();
        for (Model model : models) {
            JSONObject modelJSON = new JSONObject();

            modelJSON.put("name", model.getName());

            // Entities list
            JSONArray entitiesArray = new JSONArray();
            for (Entity entity : model.getEntities()) {
                entitiesArray.add(entity.getID());
            }
            modelJSON.put("entities", entitiesArray);

            // Model metadata
            modelJSON.put("modelMetadata", modelMetadataToJSONObject(model.getModelMetadata()));

            modelsObject.put(model.getID(), modelJSON);
        }

        return modelsObject;
    }

    private static JSONObject modelMetadataToJSONObject(ModelMetadata modelMetadata) {
        JSONObject o = new JSONObject();
        switch (modelMetadata.getType().toString()) {
            case "QUAD" -> o.put("type", "quad");
            case "CUBE" -> o.put("type", "cube");
            case "CYLINDER" -> {
                o.put("type", "cylinder");
                o.put("topRadius", modelMetadata.getTopRadius());
                o.put("bottomRadius", modelMetadata.getBottomRadius());
                o.put("height", modelMetadata.getHeight());
                o.put("sectors", modelMetadata.getSectors());
            }
            case "SPHERE" -> {
                o.put("type", "sphere");
                o.put("radius", modelMetadata.getRadius());
                o.put("sectors", modelMetadata.getSectors());
                o.put("stacks", modelMetadata.getStacks());
            }
            case "ASSIMP" -> {
                o.put("type", "assimp");
                o.put("modelPath", modelMetadata.getModelPath());
                o.put("texturesPath", modelMetadata.getTexturesPath());
            }
        }

        JSONObject meshDataMaterialIDsObject = new JSONObject();
        meshDataMaterialIDsObject.putAll(modelMetadata.getMeshDataMaterialIDs());

        o.put("meshDataMaterialIDs", meshDataMaterialIDsObject);

        return o;
    }

    private static JSONObject materialsToJSONObject(List<PBRMaterial> materials) {
        JSONObject materialsObject = new JSONObject();
        for (PBRMaterial material : materials) {
            // Don't export materials that have been loaded from a model, because they will be loaded from the model when the scene is loaded
            if (material.isLoadedFromModel()) continue;

            JSONObject o = new JSONObject();
            o.put("name", material.getName());
            o.put("albedo", material.hasTexture("albedo") ? material.getAlbedo().getPath() : null);
            o.put("normal", material.hasTexture("normal") ? material.getNormal().getPath() : null);
            o.put("metallic", material.hasTexture("metallic") ? material.getMetallic().getPath() : null);
            o.put("roughness", material.hasTexture("roughness") ? material.getRoughness().getPath() : null);
            o.put("metallicRoughness", material.hasTexture("metallicRoughness") ? material.getMetallicRoughness().getPath() : null);
            o.put("ao", material.hasTexture("ao") ? material.getAo().getPath() : null);
            o.put("emissive", material.hasTexture("emissive") ? material.getEmissive().getPath() : null);
            o.put("albedoColor", vector3fToJSONObject(material.getAlbedoColor()));
            o.put("metallicFactor", material.getMetallicFactor());
            o.put("roughnessFactor", material.getRoughnessFactor());
            o.put("emissiveColor", vector3fToJSONObject(material.getEmissiveColor()));

            materialsObject.put(material.getID(), o);
        }

        return materialsObject;
    }

    private static JSONObject entitiesToJSONObject(List<Entity> entities) {
        JSONObject entitiesObject = new JSONObject();
        for (Entity entity : entities) {
            JSONObject o = new JSONObject();
            o.put("id", entity.getID());
            o.put("name", entity.getName());
            o.put("modelID", entity.getModelID());
            o.put("position", vector3fToJSONObject(entity.getPosition()));
            o.put("rotation", vector3fToJSONObject(entity.getRotation()));
            o.put("scale", entity.getScale());
            o.put("parent", entity.getParent() != null ? entity.getParent().getID() : null);
            o.put("children", entityChildrenToJSONArray(entity.getChildren()));
            o.put("movementController", entity.getMovementController() != null ? entityMovementToJSONObject(entity.getMovementController()) : null);
            o.put("rotationController", entity.getRotationController() != null ? entityRotationToJSONObject(entity.getRotationController()) : null);

            entitiesObject.put(entity.getID(), o);
        }

        return entitiesObject;
    }

    private static JSONObject entityToJSONObject(Entity entity) {
        JSONObject o = new JSONObject();
        o.put("name", entity.getName());
        o.put("modelID", entity.getModelID());
        o.put("position", vector3fToJSONObject(entity.getPosition()));
        o.put("rotation", vector3fToJSONObject(entity.getRotation()));
        o.put("scale", entity.getScale());
        o.put("parent", entity.getParent().getID());
        o.put("children", entityChildrenToJSONArray(entity.getChildren()));
        o.put("movementController", entityMovementToJSONObject(entity.getMovementController()));
        o.put("rotationController", entityRotationToJSONObject(entity.getRotationController()));
        return o;
    }

    private static JSONArray entityChildrenToJSONArray(List<Entity> children) {
        JSONArray jsonArray = new JSONArray();
        for (Entity child : children) {
            jsonArray.add(child.getID());
        }
        return jsonArray;
    }

    private static JSONObject entityMovementToJSONObject(MovementController movement) {
        JSONObject o = new JSONObject();
        o.put("type", movement.getType().toString());
        o.put("mode", movement.getMode().toString());
        o.put("speed", movement.getSpeed());

        // Orbit
        o.put("center", vector3fToJSONObject(movement.getCenter()));
        o.put("axis", vector3fToJSONObject(movement.getAxis()));
        o.put("radius", movement.getRadius());
        o.put("anglePerSecond", movement.getAnglePerSecond());
        o.put("pointTowardsCenter", movement.isPointTowardsCenter());

        // Direction
        o.put("origin", vector3fToJSONObject(movement.getOrigin()));
        o.put("direction", vector3fToJSONObject(movement.getDirection()));
        o.put("acceleration", movement.getAcceleration());
        o.put("distance", movement.getDistance());
        o.put("noMaxDistance", movement.isNoMaxDistance());
        o.put("stopAtZeroSpeed", movement.isStopAtZeroSpeed());

        // Path
        o.put("path", vector3fListToJSONArray(movement.getPath()));

        return o;
    }

    private static JSONObject entityRotationToJSONObject(RotationController rotation) {
        JSONObject o = new JSONObject();
        o.put("mode", rotation.getMode().toString());
        o.put("speed", vector3fToJSONObject(rotation.getSpeed()));
        o.put("acceleration", vector3fToJSONObject(rotation.getAcceleration()));
        o.put("stopAtZeroSpeed", rotation.isStopAtZeroSpeed());
        return o;
    }

    private static JSONObject pbrMaterialToJSONObject(PBRMaterial pbrMaterial) {
        JSONObject o = new JSONObject();
        o.put("albedo", pbrMaterial.getAlbedo().getPath());
        o.put("normal", pbrMaterial.getNormal().getPath());
        o.put("metallic", pbrMaterial.getMetallic().getPath());
        o.put("roughness", pbrMaterial.getRoughness().getPath());
        o.put("metallicRoughness", pbrMaterial.getMetallicRoughness().getPath());
        o.put("ao", pbrMaterial.getAo().getPath());
        o.put("emissive", pbrMaterial.getEmissive().getPath());
        o.put("albedoColor", vector3fToJSONObject(pbrMaterial.getAlbedoColor()));
        o.put("metallicFactor", pbrMaterial.getMetallicFactor());
        o.put("roughnessFactor", pbrMaterial.getRoughnessFactor());
        o.put("emissiveColor", vector3fToJSONObject(pbrMaterial.getEmissiveColor()));
        return o;
    }

    private static JSONObject dirLightToJSONObject(DirLight dirLight) {
        JSONObject o = new JSONObject();
        o.put("direction", vector3fToJSONObject(dirLight.getDirection()));
        o.put("color", vector3fToJSONObject(dirLight.getColor()));
        return o;
    }

    private static JSONObject pointLightsToJSONObject(List<PointLight> pointLights) {
        JSONObject pointLightsObject = new JSONObject();
        for (PointLight pointLight : pointLights) {
            pointLightsObject.put(pointLights.indexOf(pointLight), pointLightToJSONObject(pointLight));
        }
        return pointLightsObject;
    }

    private static JSONObject pointLightToJSONObject(PointLight pointLight) {
        JSONObject o = new JSONObject();
        o.put("position", vector3fToJSONObject(pointLight.getPosition()));
        o.put("color", vector3fToJSONObject(pointLight.getColor()));
        o.put("intensity", pointLight.getIntensity());
        o.put("enabled", pointLight.isEnabled());
        return o;
    }

    private static JSONObject spotLightsToJSONObject(List<SpotLight> spotLights) {
        JSONObject spotLightsObject = new JSONObject();
        for (SpotLight spotLight : spotLights) {
            spotLightsObject.put(spotLights.indexOf(spotLight), spotLightToJSONObject(spotLight));
        }
        return spotLightsObject;
    }

    private static JSONObject spotLightToJSONObject(SpotLight spotLight) {
        JSONObject o = new JSONObject();
        o.put("position", vector3fToJSONObject(spotLight.getPosition()));
        o.put("direction", vector3fToJSONObject(spotLight.getDirection()));
        o.put("color", vector3fToJSONObject(spotLight.getColor()));
        o.put("intensity", spotLight.getIntensity());
        o.put("cutoff", spotLight.getCutoff());
        o.put("outerCutoff", spotLight.getOuterCutoff());
        o.put("enabled", spotLight.isEnabled());
        return o;
    }

    private static JSONObject equirectangularMapToJSONObject(EquirectangularMap equirectangularMap) {
        JSONObject o = new JSONObject();
        o.put("path", equirectangularMap.getPath());
        return o;
    }

    private static JSONArray vector3fListToJSONArray(List<Vector3f> list) {
        if (list == null) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        for (Vector3f v : list) {
            jsonArray.add(vector3fToJSONObject(v));
        }
        return jsonArray;
    }

    private static JSONObject cameraToJSONObject(Camera camera) {
        JSONObject o = new JSONObject();
        o.put("position", vector3fToJSONObject(camera.getPosition()));
        o.put("yaw", camera.getYaw());
        o.put("pitch", camera.getPitch());
        o.put("fov", camera.getFOV());
        o.put("speed", camera.getMovementSpeed());
        o.put("sensitivity", camera.getMouseSensitivity());
        o.put("deceleration", camera.getDeceleration());
        return o;
    }

    private static JSONObject rendererToJSONObject(Renderer renderer) {
        JSONObject o = new JSONObject();
        o.put("zNear", renderer.getzNear());
        o.put("zFar", renderer.getzFar());
        o.put("shaderSettings", shaderSettingsToJSONObject(renderer.getShaderSettings()));
        return o;
    }

    private static JSONObject shaderSettingsToJSONObject(ShaderSettings settings) {
        JSONObject o = new JSONObject();
        o.put("exposure", settings.getExposure());
        o.put("specularOcclusion", settings.isSpecularOcclusion());
        o.put("horizonSpecularOcclusion", settings.isHorizonSpecularOcclusion());
        o.put("shadowMinBias", settings.getShadowMinBias());
        o.put("shadowMaxBias", settings.getShadowMaxBias());
        o.put("pointShadows", settings.isPointShadows());
        o.put("pointShadowBias", settings.getPointShadowBias());
        return o;
    }

    private static JSONObject shadowRendererToJSONObject(ShadowRenderer renderer) {
        JSONObject o = new JSONObject();
        o.put("resolution" , renderer.getResolution());
        o.put("nearPlane", renderer.getNearPlane());
        o.put("farPlane", renderer.getFarPlane());
        o.put("size", renderer.getSize());
        return o;
    }

    private static JSONObject omnidirectionalShadowRendererToJSONObject(OmnidirectionalShadowRenderer renderer) {
        JSONObject o = new JSONObject();
        o.put("resolution" , renderer.getResolution());
        o.put("nearPlane", renderer.getNearPlane());
        o.put("farPlane", renderer.getFarPlane());
        return o;
    }

    private static JSONObject vector3fToJSONObject(Vector3f v) {
        if (v == null) return null;
        JSONObject o = new JSONObject();
        o.put("x", v.x);
        o.put("y", v.y);
        o.put("z", v.z);
        return o;
    }

}
