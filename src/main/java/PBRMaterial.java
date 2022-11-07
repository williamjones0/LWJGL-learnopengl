import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class PBRMaterial {

    private Texture albedo;
    private Texture normal;
    private Texture metallic;
    private Texture roughness;
    private Texture metallicRoughness;
    private Texture ao;
    private Texture emissive;

    private final boolean combinedMetallicRoughness;

    public PBRMaterial(Texture albedo, Texture normal, Texture metallic, Texture roughness, Texture metallicRoughness, Texture ao, Texture emissive) throws Exception {
        this.albedo = albedo != null ? albedo : new Texture("src/main/resources/textures/PBR/default_albedo.png", GL_SRGB_ALPHA);
        this.normal = normal != null ? normal : new Texture("src/main/resources/textures/PBR/default_normal.png", GL_RGBA);
        this.metallic = metallic != null ? metallic : new Texture("src/main/resources/textures/PBR/default_metallic.png", GL_RGBA);
        this.roughness = roughness != null ? roughness : new Texture("src/main/resources/textures/PBR/default_roughness.png", GL_RGBA);
        this.metallicRoughness = metallicRoughness != null ? metallicRoughness : new Texture("src/main/resources/textures/PBR/default_metallicRoughness.png", GL_RGBA);
        this.ao = ao != null ? ao : new Texture("src/main/resources/textures/PBR/default_ao.png", GL_RGBA);
        this.emissive = emissive != null ? emissive : new Texture("src/main/resources/textures/PBR/default_emissive.png", GL_RGBA);

        this.combinedMetallicRoughness = metallicRoughness != null;
    }

    public Texture getAlbedo() {
        return albedo;
    }

    public void setAlbedo(Texture albedo) {
        this.albedo = albedo;
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

    public Texture getRoughness() {
        return roughness;
    }

    public void setRoughness(Texture roughness) {
        this.roughness = roughness;
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

    public boolean isCombinedMetallicRoughness() {
        return combinedMetallicRoughness;
    }
}
