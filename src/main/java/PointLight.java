import org.joml.Vector3f;

public class PointLight {

    private final Mesh mesh;

    private Vector3f position;

    private Vector3f ambient;
    private Vector3f diffuse;
    private Vector3f specular;

    private float constant;
    private float linear;
    private float quadratic;

    public PointLight(Mesh mesh, Vector3f position, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic) {
        this.mesh = mesh;

        this.position = position;

        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;

        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setPosition(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
    }

    public Vector3f getAmbient() {
        return ambient;
    }

    public void setAmbient(Vector3f ambient) {
        this.ambient = ambient;
    }

    public Vector3f getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
    }

    public Vector3f getSpecular() {
        return specular;
    }

    public void setSpecular(Vector3f specular) {
        this.specular = specular;
    }

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getQuadratic() {
        return quadratic;
    }

    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }
}
