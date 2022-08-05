import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private final int VAO;
    private final int positionsVBO;
    private final int normalsVBO;
    private final int texCoordsVBO;
    private final int tangentsVBO;
    private final int bitangentsVBO;
    private final int indicesVBO;
    private final int vertexCount;
    private final Material material;

    public Mesh(float[] positions, float[] normals, float[] texCoords, int[] indices, Material material) {
        this.material = material;

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

        // Normal mapping
        if (material.getNormalMap() != null) {
            Vector3f edge1 = new Vector3f(positions[4], positions[5], positions[6]).sub(new Vector3f(positions[0], positions[1], positions[2]));
            Vector3f edge2 = new Vector3f(positions[7], positions[8], positions[9]).sub(new Vector3f(positions[0], positions[1], positions[2]));
            Vector2f deltaUV1 = new Vector2f(texCoords[2], texCoords[3]).sub(new Vector2f(texCoords[0], texCoords[1]));
            Vector2f deltaUV2 = new Vector2f(texCoords[4], texCoords[5]).sub(new Vector2f(texCoords[0], texCoords[1]));

            float f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);

            Vector3f tangent1 = new Vector3f(
                f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x),
                f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y),
                f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z)
            );

            Vector3f bitangent1 = new Vector3f(
                f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x),
                f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y),
                f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z)
            );

            edge1 = new Vector3f(positions[6], positions[7], positions[8]).sub(new Vector3f(positions[0], positions[1], positions[2]));
            edge2 = new Vector3f(positions[9], positions[10], positions[11]).sub(new Vector3f(positions[0], positions[1], positions[2]));
            deltaUV1 = new Vector2f(texCoords[4], texCoords[5]).sub(new Vector2f(texCoords[0], texCoords[1]));
            deltaUV2 = new Vector2f(texCoords[6], texCoords[7]).sub(new Vector2f(texCoords[0], texCoords[1]));

            f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);

            Vector3f tangent2 = new Vector3f(
                f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x),
                f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y),
                f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z)
            );

            Vector3f bitangent2 = new Vector3f(
                f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x),
                f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y),
                f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z)
            );

            System.out.println(tangent1);
            System.out.println(tangent2);

            tangent1 = new Vector3f(2, 0, 0);
            tangent2 = new Vector3f(2, 0, 0);

            bitangent1 = new Vector3f(0, 2, 0);
            bitangent2 = new Vector3f(0, 2, 0);

            // Tangents
            float[] tangents = {
                tangent1.x, tangent1.y, tangent1.z,
                tangent1.x, tangent1.y, tangent1.z,
                tangent2.x, tangent2.y, tangent2.z,
                tangent2.x, tangent2.y, tangent2.z
            };

            FloatBuffer tangentsBuffer = MemoryUtil.memAllocFloat(tangents.length);
            tangentsBuffer.put(tangents).flip();
            tangentsVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, tangentsVBO);
            glBufferData(GL_ARRAY_BUFFER, tangentsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(3);

            // Bitangent
            float[] bitangents = {
                bitangent1.x, bitangent1.y, bitangent1.z,
                bitangent1.x, bitangent1.y, bitangent1.z,
                bitangent2.x, bitangent2.y, bitangent2.z,
                bitangent2.x, bitangent2.y, bitangent2.z
            };

            FloatBuffer bitangentsBuffer = MemoryUtil.memAllocFloat(bitangents.length);
            bitangentsBuffer.put(bitangents).flip();
            bitangentsVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, bitangentsVBO);
            glBufferData(GL_ARRAY_BUFFER, bitangentsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(4);
        } else {
            tangentsVBO = 0;
            bitangentsVBO = 0;
        }

        // Indices VBO
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        this.indicesVBO = glGenBuffers();
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
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, material.getDiffuse().getID());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, material.getSpecular().getID());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, material.getNormalMap().getID());

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

    public Material getMaterial() {
        return material;
    }
}
