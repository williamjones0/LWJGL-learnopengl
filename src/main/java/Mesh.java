import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private int VAO;
    private int positionsVBO;
    private int normalsVBO;
    private int texCoordsVBO;
    private int indicesVBO;
    private int vertexCount;
    private final Material material;
    private final PBRMaterial pbrMaterial;

    public Mesh(float[] positions, float[] normals, float[] texCoords, int[] indices, Material material) {
        this.material = material;
        this.pbrMaterial = null;

        init(positions, normals, texCoords, indices);
    }

    public Mesh(float[] positions, float[] normals, float[] texCoords, int[] indices, PBRMaterial material) {
        this.material = null;
        this.pbrMaterial = material;

        init(positions, normals, texCoords, indices);
    }

    private void init(float[] positions, float[] normals, float[] texCoords, int[] indices) {
        vertexCount = indices.length;

        // Create VAO and bind it
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        // Position VBO
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
        verticesBuffer.put(positions).flip();
        positionsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, positionsVBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // Normals VBO
        FloatBuffer normalsBuffer = MemoryUtil.memAllocFloat(normals.length);
        normalsBuffer.put(normals).flip();
        normalsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBO);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        // Texture coordinates VBO
        FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        texCoordsBuffer.put(texCoords).flip();
        texCoordsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texCoordsVBO);
        glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);

        // Indices VBO
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        indicesVBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // Free the off-heap memory allocated by the FloatBuffer / IntBuffer
        MemoryUtil.memFree(verticesBuffer);
        MemoryUtil.memFree(normalsBuffer);
        MemoryUtil.memFree(texCoordsBuffer);
        MemoryUtil.memFree(indicesBuffer);
    }

    public void render() {
        // Activate and bind textures
        if (pbrMaterial != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getAlbedo().getID());
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getNormal().getID());
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getMetallic().getID());
            glActiveTexture(GL_TEXTURE3);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getRoughness().getID());
            glActiveTexture(GL_TEXTURE4);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getMetallicRoughness().getID());
            glActiveTexture(GL_TEXTURE5);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getAo().getID());
            glActiveTexture(GL_TEXTURE6);
            glBindTexture(GL_TEXTURE_2D, pbrMaterial.getEmissive().getID());
        }

        glBindVertexArray(VAO);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(positionsVBO);
        glDeleteBuffers(normalsVBO);
        glDeleteBuffers(texCoordsVBO);
        glDeleteBuffers(indicesVBO);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(0);
    }

//    public Material getMaterial() {
//        return material;
//    }

    public PBRMaterial getPbrMaterial() {
        return pbrMaterial;
    }
}
