public class Material {

    private Texture diffuse;
    private Texture specular;
    private float shininess;
    private Texture normalMap;

    public Material(Texture diffuse, Texture specular, float shininess, Texture normalMap) throws Exception {
        this.diffuse = diffuse != null ? diffuse : new Texture("src/main/resources/textures/default_diffuse.png", Texture.Format.SRGBA);
        this.specular = specular != null ? specular : new Texture("src/main/resources/textures/default_specular.png", Texture.Format.RGBA);
        this.shininess = shininess;
        this.normalMap = normalMap != null ? normalMap : new Texture("src/main/resources/textures/default_normal.png", Texture.Format.RGBA);
    }

    public Texture getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Texture diffuse) {
        this.diffuse = diffuse;
    }

    public Texture getSpecular() {
        return specular;
    }

    public void setSpecular(Texture specular) {
        this.specular = specular;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(Texture normalMap) {
        this.normalMap = normalMap;
    }
}
