import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class PBRMaterial {

    // Textures or Vector3fs/floats for each of the PBR material properties
    private Texture albedo;
    private Vector3f albedoColor = new Vector3f(1.0f, 1.0f, 1.0f);

    private Texture normal;

    private Texture metallic;
    private float metallicFactor = 0.0f;

    private Texture roughness;
    private float roughnessFactor = 0.0f;

    private Texture metallicRoughness;

    private Texture ao;

    private Texture emissive;
    private Vector3f emissiveColor = new Vector3f(0, 0, 0);

    private Map<String, Boolean> usesTextures = new HashMap<>();

    public PBRMaterial(Texture albedo, Texture normal, Texture metallic, Texture roughness, Texture metallicRoughness, Texture ao, Texture emissive) throws Exception {
        this.albedo = albedo != null ? albedo : new Texture("src/main/resources/textures/PBR/default_albedo.png", GL_SRGB_ALPHA);
        this.normal = normal != null ? normal : new Texture("src/main/resources/textures/PBR/default_normal.png", GL_RGBA);
        this.metallic = metallic != null ? metallic : new Texture("src/main/resources/textures/PBR/default_metallic.png", GL_RGBA);
        this.roughness = roughness != null ? roughness : new Texture("src/main/resources/textures/PBR/default_roughness.png", GL_RGBA);
        this.metallicRoughness = metallicRoughness != null ? metallicRoughness : new Texture("src/main/resources/textures/PBR/default_metallicRoughness.png", GL_RGBA);
        this.ao = ao != null ? ao : new Texture("src/main/resources/textures/PBR/default_ao.png", GL_RGBA);
        this.emissive = emissive != null ? emissive : new Texture("src/main/resources/textures/PBR/default_emissive.png", GL_RGBA);

        this.usesTextures.put("albedo", albedo != null);
        this.usesTextures.put("normal", normal != null);
        this.usesTextures.put("metallic", metallic != null);
        this.usesTextures.put("roughness", roughness != null);
        this.usesTextures.put("metallicRoughness", metallicRoughness != null);
        this.usesTextures.put("ao", ao != null);
        this.usesTextures.put("emissive", emissive != null);
    }

    // PBR material without textures
    public PBRMaterial(Vector3f albedo, float metallic, float roughness, Vector3f emissive) {
        this.albedoColor = albedo;
        this.metallicFactor = metallic;
        this.roughnessFactor = roughness;
        this.emissiveColor = emissive;

        this.usesTextures.put("albedo", false);
        this.usesTextures.put("normal", false);
        this.usesTextures.put("metallic", false);
        this.usesTextures.put("roughness", false);
        this.usesTextures.put("metallicRoughness", false);
        this.usesTextures.put("ao", false);
        this.usesTextures.put("emissive", false);
    }

    public PBRMaterial() {
        this(new Vector3f(1, 1, 1), 0, 1, new Vector3f(0, 0, 0));
    }

    public Texture getAlbedo() {
        return albedo;
    }

    public void setAlbedo(Texture albedo) {
        this.albedo = albedo;
    }

    public Vector3f getAlbedoColor() {
        return albedoColor;
    }

    public void setAlbedoColor(Vector3f albedoColor) {
        this.albedoColor = albedoColor;
    }

    public Texture getNormal() {
        return normal;
    }

    public void setNormal(Texture normal) {
        this.normal = normal;
    }

    public Texture getMetallic() {
        return metallic;
    }

    public void setMetallic(Texture metallic) {
        this.metallic = metallic;
    }

    public float getMetallicFactor() {
        return metallicFactor;
    }

    public void setMetallicFactor(float metallicFactor) {
        this.metallicFactor = metallicFactor;
    }

    public Texture getRoughness() {
        return roughness;
    }

    public void setRoughness(Texture roughness) {
        this.roughness = roughness;
    }

    public float getRoughnessFactor() {
        return roughnessFactor;
    }

    public void setRoughnessFactor(float roughnessFactor) {
        this.roughnessFactor = roughnessFactor;
    }

    public Texture getMetallicRoughness() {
        return metallicRoughness;
    }

    public void setMetallicRoughness(Texture metallicRoughness) {
        this.metallicRoughness = metallicRoughness;
    }

    public Texture getAo() {
        return ao;
    }

    public void setAo(Texture ao) {
        this.ao = ao;
    }

    public Texture getEmissive() {
        return emissive;
    }

    public void setEmissive(Texture emissive) {
        this.emissive = emissive;
    }

    public Vector3f getEmissiveColor() {
        return emissiveColor;
    }

    public void setEmissiveColor(Vector3f emissiveColor) {
        this.emissiveColor = emissiveColor;
    }

    public Map<String, Boolean> getUsesTextures() {
        return usesTextures;
    }

    public void setUseTexture(String texture, boolean use) {
        this.usesTextures.put(texture, use);
    }
}
