package io.william.renderer;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL43.GL_BUFFER;
import static org.lwjgl.opengl.GL43.glObjectLabel;

public class SceneMesh {

    public record MeshDrawData(int sizeBytes, int materialID, int offset, int vertices) {}

    private int VAO;
    private final List<Integer> VBOs;

    public SceneMesh() {
        VBOs = new ArrayList<>();
    }

    public void loadModels(Scene scene) {
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        int positionsCount = 0;
        int normalsCount = 0;
        int texCoordsCount = 0;
        int indicesCount = 0;
        int offset = 0;

        List<Model> models = scene.getModels().stream().filter(model -> model.getEntities().size() > 0).toList();
        System.out.println("Loading " + models.size() + " models");
        for (Model model : models) {
            List<MeshDrawData> meshDrawDatas = model.getMeshDrawDatas();
            meshDrawDatas.clear();

//            boolean loaded = meshDrawDatas.size() > 0;

            for (MeshData meshData : model.getMeshDatas()) {
                positionsCount += meshData.getPositions().length;
                normalsCount += meshData.getNormals().length;
                texCoordsCount += meshData.getTexCoords().length;
                indicesCount += meshData.getIndices().length;

//                if (loaded) {
//                    offset = positionsCount / 3;
//                    continue;
//                }

                int meshSizeBytes = (meshData.getPositions().length + meshData.getNormals().length + meshData.getTexCoords().length) * Float.BYTES;
                meshDrawDatas.add(new MeshDrawData(
                    meshSizeBytes,
                    meshData.getMaterialID(),
                    offset,
                    meshData.getIndices().length
                ));

                offset = positionsCount / 3;
            }
        }

        int VBO = glGenBuffers();
        VBOs.add(VBO);
        FloatBuffer meshesBuffer = MemoryUtil.memAllocFloat(positionsCount + normalsCount + texCoordsCount);
        for (Model model : models) {
            for (MeshData meshData : model.getMeshDatas()) {
                float[] positions = meshData.getPositions();
                float[] normals = meshData.getNormals();
                float[] texCoords = meshData.getTexCoords();

                int rows = positions.length / 3;
                for (int i = 0; i < rows; i++) {
                    meshesBuffer.put(positions[i * 3]);
                    meshesBuffer.put(positions[i * 3 + 1]);
                    meshesBuffer.put(positions[i * 3 + 2]);
                    meshesBuffer.put(normals[i * 3]);
                    meshesBuffer.put(normals[i * 3 + 1]);
                    meshesBuffer.put(normals[i * 3 + 2]);
                    meshesBuffer.put(texCoords[i * 2]);
                    meshesBuffer.put(texCoords[i * 2 + 1]);
                }
            }
        }
        meshesBuffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, meshesBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(meshesBuffer);

        // Vertex attributes
        int stride = 8 * Float.BYTES;
        int pointer = 0;

        // Positions
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, pointer);
        pointer += 3 * Float.BYTES;

        // Normals
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, pointer);
        pointer += 3 * Float.BYTES;

        // Texture coordinates
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, pointer);

        // Indices
        VBO = glGenBuffers();
        VBOs.add(VBO);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indicesCount);
        System.out.println("Loading " + indicesCount + " indices");
        for (Model model : models) {
            for (MeshData meshData : model.getMeshDatas()) {
                indicesBuffer.put(meshData.getIndices());
            }
        }
        indicesBuffer.flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBO);
        glObjectLabel(GL_BUFFER, VBO, "SceneMeshIndicesBuffer");
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteVertexArrays(VAO);
        for (int VBO : VBOs) {
            glDeleteBuffers(VBO);
        }
    }

    public int getVAO() {
        return VAO;
    }

}
