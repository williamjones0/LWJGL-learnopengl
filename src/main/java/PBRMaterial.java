public class PBRMaterial {

    private Texture albedo;
    private Texture normal;
    private Texture metallic;
    private Texture roughness;
    private Texture ao;

    public PBRMaterial(Texture albedo, Texture normal, Texture metallic, Texture roughness, Texture ao) throws Exception {
        this.albedo = albedo != null ? albedo : new Texture("src/main/resources/textures/PBR/default_albedo.png", Texture.Format.SRGBA);
        this.normal = normal != null ? normal : new Texture("src/main/resources/textures/PBR/default_normal.png", Texture.Format.RGBA);
        this.metallic = metallic != null ? metallic : new Texture("src/main/resources/textures/PBR/default_metallic.png", Texture.Format.RGBA);
        this.roughness = roughness != null ? roughness : new Texture("src/main/resources/textures/PBR/default_roughness.png", Texture.Format.RGBA);
        this.ao = ao != null ? ao : new Texture("src/main/resources/textures/PBR/default_ao.png", Texture.Format.RGBA);
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

    public Texture getAo() {
        return ao;
    }

    public void setAo(Texture ao) {
        this.ao = ao;
    }
}
