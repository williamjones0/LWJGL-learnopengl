package io.william.renderer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.*;

public class MaterialMesh {

    private final Mesh mesh;
    private final PBRMaterial pbrMaterial;

    public MaterialMesh(Mesh mesh, PBRMaterial pbrMaterial) {
        this.mesh = mesh;
        this.pbrMaterial = pbrMaterial == null ? new PBRMaterial(null) : pbrMaterial;
    }

    public void render() {
        // Activate and bind textures
        if (pbrMaterial.getAlbedo() != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getAlbedo().getID());
        }
        if (pbrMaterial.getNormal() != null) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getNormal().getID());
        }
        if (pbrMaterial.getMetallic() != null) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getMetallic().getID());
        }
        if (pbrMaterial.getRoughness() != null) {
            glActiveTexture(GL_TEXTURE3);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getRoughness().getID());
        }
        if (pbrMaterial.getMetallicRoughness() != null) {
            glActiveTexture(GL_TEXTURE4);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getMetallicRoughness().getID());
        }
        if (pbrMaterial.getAo() != null) {
            glActiveTexture(GL_TEXTURE5);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getAo().getID());
        }
        if (pbrMaterial.getEmissive() != null) {
            glActiveTexture(GL_TEXTURE6);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getEmissive().getID());
        }

        mesh.render();
    }

    public Mesh getMesh() {
        return mesh;
    }

    public PBRMaterial getPbrMaterial() {
        return pbrMaterial;
    }

}
