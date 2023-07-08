package io.william.renderer;

import io.william.io.Window;
import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import io.william.renderer.shadow.SpotlightShadowRenderer;
import io.william.util.Maths;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL43.GL_BUFFER;

public class MasterRenderer {

    private Renderer renderer;
    private ShadowRenderer shadowRenderer;
    private OmnidirectionalShadowRenderer omnidirectionalShadowRenderer;
    private SpotlightShadowRenderer spotlightShadowRenderer;
    private GUI gui;

    private SceneMesh sceneMesh;
    private int indirectBuffer;
    private int drawCount;
    private int modelMeshInstanceBuffer;
    private int materialBuffer;

    private boolean firstRender = true;
    private boolean sceneUpdated;

    private boolean showGUI = true;

    public void init(Window window, Renderer renderer, Scene scene, Camera camera, ShadowRenderer shadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer, SpotlightShadowRenderer spotlightShadowRenderer, GUI gui) throws Exception {
        this.renderer = renderer;
        this.shadowRenderer = shadowRenderer;
        this.omnidirectionalShadowRenderer = omnidirectionalShadowRenderer;
        this.spotlightShadowRenderer = spotlightShadowRenderer;
        this.gui = gui;

        sceneMesh = new SceneMesh();

        setupBuffers(scene);

        renderer.init(window, camera);
        gui.init(window.getWindowHandle());
    }

    public void render(Camera camera, Scene scene, Window window) throws Exception {
        // Only update shadow maps if entities have been updated
        if (sceneUpdated || firstRender) {
            shadowRenderer.render(scene, sceneMesh, indirectBuffer, drawCount);
            omnidirectionalShadowRenderer.render(scene, sceneMesh, indirectBuffer, drawCount);
            spotlightShadowRenderer.render(scene, sceneMesh, indirectBuffer, drawCount);
        }

        firstRender = false;
        sceneUpdated = false;

        renderer.render(camera, scene, sceneMesh, indirectBuffer, drawCount, shadowRenderer, spotlightShadowRenderer, omnidirectionalShadowRenderer, window);
        if (showGUI) gui.render(scene, camera, this, renderer, shadowRenderer, omnidirectionalShadowRenderer, window);
    }

    public void setupBuffers(Scene scene) {
        System.out.println(scene.getPBRMaterials());

        sceneMesh.loadModels(scene);

        setupIndirectBuffer(scene);

        // SSBO
        modelMeshInstanceBuffer = glGenBuffers();
        materialBuffer = glGenBuffers();

        // ModelMeshInstanceBuffer
        List<Model> models = scene.getModels().stream().filter(model -> model.getEntities().size() > 0).toList();

        // Calculate buffer capacity - 20 * number of entities
        int capacity = 0;
        for (Model model : models) {
            List<Entity> entities = model.getEntities();
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                capacity += (16 + 4) * entities.size();
            }
        }

        ByteBuffer mmib = MemoryUtil.memAlloc(capacity * 4);
        for (Model model : models) {
            List<Entity> entities = model.getEntities();
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                for (Entity entity : entities) {
                    Matrix4f m = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
                    putMatrix4f(mmib, m);
                    mmib.putInt(meshDrawData.materialID());
                    mmib.putFloat(meshDrawData.emissionStrength());
                    mmib.putInt(0);
                    mmib.putInt(0);
                }
            }
        }
        mmib.flip();

        // Bind SSBO
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, modelMeshInstanceBuffer);
        glObjectLabel(GL_BUFFER, modelMeshInstanceBuffer, "ModelMeshInstanceBuffer");
        glBufferData(GL_SHADER_STORAGE_BUFFER, mmib, GL_STATIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, modelMeshInstanceBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        MemoryUtil.memFree(mmib);

        System.out.println("ModelMeshInstanceBuffer size: " + capacity * 8 + " bytes");

        // MaterialBuffer (ByteBuffer)
        capacity = 0;
        for (int i = 0; i < scene.getPBRMaterials().size(); i++) {
            capacity += (4 * 10) + (8 * 7) + (4 * 8);
        }

        System.out.println("Number of materials: " + scene.getPBRMaterials().size());
        System.out.println("MaterialBuffer size: " + capacity + " bytes");

        ByteBuffer mb = MemoryUtil.memAlloc(capacity);
        for (PBRMaterial material : scene.getPBRMaterials()) {
            putPBRMaterial(mb, material);
        }
        mb.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, materialBuffer);
        glObjectLabel(GL_BUFFER, materialBuffer, "MaterialBuffer");
        glBufferData(GL_SHADER_STORAGE_BUFFER, mb, GL_STATIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, materialBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        MemoryUtil.memFree(mb);

        System.out.println("MaterialBuffer size: " + capacity + " bytes");
    }

    public void setupIndirectBuffer(Scene scene) {
        List<Model> models = scene.getModels().stream().filter(model -> model.getEntities().size() > 0).toList();

        int numCommands = 0;
        for (Model model : models) {
            numCommands += model.getMeshDrawDatas().size();
        }

        int firstIndex = 0;
        int baseInstance = 0;
        System.out.println("Num commands: " + numCommands);
        ByteBuffer indirectBuffer = MemoryUtil.memAlloc(numCommands * 5 * 4);
        for (Model model : models) {
            List<Entity> entities = model.getEntities();
            int numEntities = entities.size();

            System.out.println("mesh draw datas size: " + model.getMeshDrawDatas().size());
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                // Count
                indirectBuffer.putInt(meshDrawData.vertices());

                // Instance count
                indirectBuffer.putInt(numEntities);

                // First index
                indirectBuffer.putInt(firstIndex);

                // Base vertex
                indirectBuffer.putInt(meshDrawData.offset());

                // Base instance
                indirectBuffer.putInt(baseInstance);

                firstIndex += meshDrawData.vertices();
                baseInstance += numEntities;
            }
        }

        indirectBuffer.flip();

        drawCount = indirectBuffer.remaining() / (5 * 4);
        System.out.println("Draw count: " + drawCount);

        this.indirectBuffer = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.indirectBuffer);
        glObjectLabel(GL_BUFFER, this.indirectBuffer, "IndirectBuffer");
        glBufferData(GL_DRAW_INDIRECT_BUFFER, indirectBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        MemoryUtil.memFree(indirectBuffer);
    }

    // Buffers:
    //  - Indirect buffer (draw commands)
    //  - SceneMeshIndicesBuffer (indices)
    //  - ModelMeshInstanceBuffer (world matrix, material id)
    //  - MaterialBuffer (material data)

    // Types of buffer updates:
    //  - Entity added/removed (ModelMeshInstanceBuffer)
    //  - Entity changed (ModelMeshInstanceBuffer)
    //  - Material added/removed (MaterialBuffer)
    //  - Material changed (MaterialBuffer)
    //  - Model added/removed (ModelMeshInstanceBuffer, SceneMeshIndicesBuffer)
    //  - Model changed (ModelMeshInstanceBuffer, SceneMeshIndicesBuffer)

    // Entity added/removed
    public void recreateModelMeshInstanceBuffer(List<Model> models) {
        // Calculate buffer capacity - 20 * number of entities
        int capacity = 0;
        for (Model model : models) {
            List<Entity> entities = model.getEntities();
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                capacity += (16 + 4) * entities.size();
            }
        }

        ByteBuffer mmib = MemoryUtil.memAlloc(capacity * 4);
        for (Model model : models) {
            List<Entity> entities = model.getEntities();
            for (SceneMesh.MeshDrawData meshDrawData : model.getMeshDrawDatas()) {
                for (Entity entity : entities) {
                    Matrix4f m = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
                    putMatrix4f(mmib, m);
                    mmib.putInt(meshDrawData.materialID());
                    mmib.putFloat(meshDrawData.emissionStrength());
                    mmib.putInt(0);
                    mmib.putInt(0);
                }
            }
        }
        mmib.flip();

        // Bind SSBO
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, modelMeshInstanceBuffer);
        glObjectLabel(GL_BUFFER, modelMeshInstanceBuffer, "ModelMeshInstanceBuffer");
        glBufferData(GL_SHADER_STORAGE_BUFFER, mmib, GL_STATIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, modelMeshInstanceBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        MemoryUtil.memFree(mmib);
    }

    public void updateModelMeshInstances(int firstIndex, int lastIndex, Matrix4f[] worlds, int[] materialIDs, float[] emissionStrengths) {
        ByteBuffer buffer = MemoryUtil.memAlloc((lastIndex - firstIndex + 1) * 20 * 4);
        for (int i = 0; i < worlds.length; i++) {
            buffer.putFloat(worlds[i].m00());
            buffer.putFloat(worlds[i].m01());
            buffer.putFloat(worlds[i].m02());
            buffer.putFloat(worlds[i].m03());
            buffer.putFloat(worlds[i].m10());
            buffer.putFloat(worlds[i].m11());
            buffer.putFloat(worlds[i].m12());
            buffer.putFloat(worlds[i].m13());
            buffer.putFloat(worlds[i].m20());
            buffer.putFloat(worlds[i].m21());
            buffer.putFloat(worlds[i].m22());
            buffer.putFloat(worlds[i].m23());
            buffer.putFloat(worlds[i].m30());
            buffer.putFloat(worlds[i].m31());
            buffer.putFloat(worlds[i].m32());
            buffer.putFloat(worlds[i].m33());

            // Material ID
            buffer.putInt(materialIDs[i]);
            buffer.putFloat(emissionStrengths[i]);
            buffer.putInt(0);
            buffer.putInt(0);
        }
        buffer.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, modelMeshInstanceBuffer);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, firstIndex * 80L, buffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        MemoryUtil.memFree(buffer);
    }

    // Material added/removed
    public void recreateMaterialBuffer(List<PBRMaterial> materials) {
        int capacity = 0;
        for (int i = 0; i < materials.size(); i++) {
            capacity += (4 * 10) + (8 * 7) + (4 * 8);
        }

        ByteBuffer mb = MemoryUtil.memAlloc(capacity);
        for (PBRMaterial material : materials) {
            putPBRMaterial(mb, material);
        }
        mb.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, materialBuffer);
        glObjectLabel(GL_BUFFER, materialBuffer, "MaterialBuffer");
        glBufferData(GL_SHADER_STORAGE_BUFFER, mb, GL_STATIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, materialBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        MemoryUtil.memFree(mb);
    }

    // Material changed
    public void updateMaterial(int index, PBRMaterial material) {
        ByteBuffer buffer = MemoryUtil.memAlloc((4 * 10) + (8 * 7) + (4 * 8));
        putPBRMaterial(buffer, material);

        buffer.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, materialBuffer);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, index * ((4 * 10) + (8 * 7) + (4 * 8)), buffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        MemoryUtil.memFree(buffer);
    }

    private void putPBRMaterial(ByteBuffer buffer, PBRMaterial pbrMaterial) {
        Map<String, Boolean> usesTextures = pbrMaterial.getUsesTextures();
        
        putVector3f(buffer, pbrMaterial.getAlbedoColor());
        putVector3f(buffer, pbrMaterial.getEmissiveColor());
        
        buffer.putLong(usesTextures.get("albedo") ? pbrMaterial.getAlbedo().getHandle() : 0);
        buffer.putLong(usesTextures.get("normal") ? pbrMaterial.getNormal().getHandle() : 0);

        buffer.putLong(usesTextures.get("metallic") ? pbrMaterial.getMetallic().getHandle() : 0);
        buffer.putLong(usesTextures.get("roughness") ? pbrMaterial.getRoughness().getHandle() : 0);

        buffer.putLong(usesTextures.get("metallicRoughness") ? pbrMaterial.getMetallicRoughness().getHandle() : 0);
        buffer.putLong(usesTextures.get("ao") ? pbrMaterial.getAo().getHandle() : 0);

        buffer.putLong(usesTextures.get("emissive") ? pbrMaterial.getEmissive().getHandle() : 0);
        buffer.putFloat(pbrMaterial.getMetallicFactor());
        buffer.putFloat(pbrMaterial.getRoughnessFactor());

        buffer.putInt(usesTextures.get("albedo") ? 1 : 0);
        buffer.putInt(usesTextures.get("normal") ? 1 : 0);
        buffer.putInt(usesTextures.get("metallic") ? 1 : 0);
        buffer.putInt(usesTextures.get("roughness") ? 1 : 0);

        buffer.putInt(usesTextures.get("metallicRoughness") ? 1 : 0);
        buffer.putInt(usesTextures.get("ao") ? 1 : 0);
        buffer.putInt(usesTextures.get("emissive") ? 1 : 0);
        buffer.putInt(0);
    }

    private void putVector2f(ByteBuffer buffer, Vector2f v) {
        buffer.putFloat(v.x);
        buffer.putFloat(v.y);
    }

    private void putVector3f(ByteBuffer buffer, Vector3f v) {
        buffer.putFloat(v.x);
        buffer.putFloat(v.y);
        buffer.putFloat(v.z);
        buffer.putInt(0);
    }

    private void putMatrix4f(ByteBuffer buffer, Matrix4f m) {
        buffer.putFloat(m.m00());
        buffer.putFloat(m.m01());
        buffer.putFloat(m.m02());
        buffer.putFloat(m.m03());
        buffer.putFloat(m.m10());
        buffer.putFloat(m.m11());
        buffer.putFloat(m.m12());
        buffer.putFloat(m.m13());
        buffer.putFloat(m.m20());
        buffer.putFloat(m.m21());
        buffer.putFloat(m.m22());
        buffer.putFloat(m.m23());
        buffer.putFloat(m.m30());
        buffer.putFloat(m.m31());
        buffer.putFloat(m.m32());
        buffer.putFloat(m.m33());
    }

    public boolean isSceneUpdated() {
        return sceneUpdated;
    }

    public void setSceneUpdated(boolean sceneUpdated) {
        this.sceneUpdated = sceneUpdated;
    }

    public ShadowRenderer getShadowRenderer() {
        return shadowRenderer;
    }

    public OmnidirectionalShadowRenderer getOmnidirectionalShadowRenderer() {
        return omnidirectionalShadowRenderer;
    }

    public boolean isShowGUI() {
        return showGUI;
    }

    public void setShowGUI(boolean showGUI) {
        this.showGUI = showGUI;
    }
}
