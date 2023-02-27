package io.william.io;

import io.william.renderer.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static io.william.util.Utils.floatListToArray;
import static io.william.util.Utils.intListToArray;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class ModelLoader {

    public static Model load(Scene scene, String modelPath, String texturesPath) throws Exception {
        return load(scene, modelPath, texturesPath, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
            | aiProcess_Triangulate | aiProcess_FixInfacingNormals
            | aiProcess_PreTransformVertices | aiProcess_CalcTangentSpace | aiProcess_FlipUVs);
    }

    public static Model load(Scene scene, String modelPath, String texturesPath, int flags) throws Exception {
        AIScene aiScene = aiImportFile(modelPath, flags);
        if (aiScene == null) {
            throw new RuntimeException("Failed to load model: " + modelPath);
        }

        // Process materials
        int numMaterials = aiScene.mNumMaterials();
        System.out.println("Number of materials: " + numMaterials);
        PointerBuffer aiMaterials = aiScene.mMaterials();
        List<Material> materials = new ArrayList<>();
        List<PBRMaterial> pbrMaterials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            PBRMaterial material = processPBRMaterial(aiMaterial, texturesPath);
            if (material == null) {
                System.out.println("Material " + i + " is null");
                continue;
            }
            if (material.isEmpty()) {
                System.out.println("Material " + i + " is empty");
                continue;
            }
            pbrMaterials.add(material);
            scene.addPBRMaterial(material);
        }

        if (materials.size() == 0) {
            Material material = new Material(null, null, 32.0f, null);
            materials.add(material);
        }

        // Process meshes
        int numMeshes = aiScene.mNumMeshes();
        System.out.println("Number of meshes: " + numMeshes);
        PointerBuffer aiMeshes = aiScene.mMeshes();
        List<MeshData> meshDatas = new ArrayList<>();

        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            MeshData meshData = processMesh(aiMesh);

            if (pbrMaterials.size() == 0) {
                meshData.setMaterialID(0);
                meshDatas.add(meshData);
                continue;
            }

            int materialIndex = aiMesh.mMaterialIndex();
            meshData.setMaterialID(pbrMaterials.get(materialIndex).getID());
            System.out.println("Material ID: " + pbrMaterials.get(materialIndex).getID());

            meshDatas.add(meshData);
        }

        return new Model(meshDatas);
    }

    private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) throws Exception {
        // Check if the material has any textures
        boolean hasDiffuseMap = aiGetMaterialTextureCount(aiMaterial, aiTextureType_DIFFUSE) > 0;
        boolean hasSpecularMap = aiGetMaterialTextureCount(aiMaterial, aiTextureType_SPECULAR) > 0;
        boolean hasNormalMap = aiGetMaterialTextureCount(aiMaterial, aiTextureType_NORMALS) > 0;

        // Diffuse map
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String texturePath = path.dataString();
        Texture diffuseTexture = null;
        System.out.println(texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            diffuseTexture = new Texture(texturesDir + "/" + texturePath, GL_SRGB_ALPHA);
        }

        // Specular map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_SPECULAR, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture specularTexture = null;
        System.out.println(texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            specularTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        // Normal map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture normalTexture = null;
        System.out.println(texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            normalTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        if (hasDiffuseMap) {
            Material material = new Material(diffuseTexture, specularTexture, 32.0f, normalTexture);
            materials.add(material);
        }
    }

    private static PBRMaterial processPBRMaterial(AIMaterial aiMaterial, String texturesDir) throws Exception {
        if (texturesDir == null || texturesDir.length() == 0) {
            System.out.println("textures dir is null or length is zero");
            return null;
        }

        // Albedo map
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String texturePath = path.dataString();
        Texture albedoTexture = null;
        System.out.println("Albedo map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            albedoTexture = new Texture(texturesDir + "/" + texturePath, GL_SRGB_ALPHA);
        }

        // Normal map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture normalTexture = null;
        System.out.println("Normal map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            normalTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        // Metallic map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_METALNESS, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture metallicTexture = null;
        System.out.println("Metallic map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            metallicTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        // Roughness map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_SHININESS, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture roughnessTexture = null;
        System.out.println("Roughness map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            roughnessTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        // Metallic roughness map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_UNKNOWN, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture metallicRoughnessTexture = null;
        System.out.println("Metallic roughness map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            metallicRoughnessTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        // AO map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_LIGHTMAP, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture aoTexture = null;
        System.out.println("AO map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            aoTexture = new Texture(texturesDir + "/" + texturePath, GL_RGBA);
        }

        // Emissive map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_EMISSIVE, 0, path, (IntBuffer) null, null, null, null, null, null);
        texturePath = path.dataString();
        Texture emissiveTexture = null;
        System.out.println("Emissive map: " + texturesDir + "/" + texturePath);
        if (texturePath.length() > 0) {
            emissiveTexture = new Texture(texturesDir + "/" + texturePath, GL_SRGB_ALPHA);
        }

        for (int i = 0; i < 21; i++) {
            path = AIString.calloc();
            Assimp.aiGetMaterialTexture(aiMaterial, i, 0, path, (IntBuffer) null, null, null, null, null, null);
            System.out.println(i + ": " + path.dataString());
        }

        return new PBRMaterial(
            null,
            albedoTexture,
            normalTexture,
            metallicTexture,
            roughnessTexture,
            metallicRoughnessTexture,
            aoTexture,
            emissiveTexture
        );
    }

    private static MeshData processMesh(AIMesh aiMesh) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> tangents = new ArrayList<>();
        List<Float> bitangents = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // Process vertices
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.hasRemaining()) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }

        // Process normals
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        while (aiNormals.hasRemaining()) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }

        // Process tangents
        AIVector3D.Buffer aiTangents = aiMesh.mTangents();
        while (aiTangents.hasRemaining()) {
            AIVector3D aiTangent = aiTangents.get();
            tangents.add(aiTangent.x());
            tangents.add(aiTangent.y());
            tangents.add(aiTangent.z());
        }

        // Process bitangents
        AIVector3D.Buffer aiBitangents = aiMesh.mBitangents();
        while (aiBitangents.hasRemaining()) {
            AIVector3D aiBitangent = aiBitangents.get();
            bitangents.add(aiBitangent.x());
            bitangents.add(aiBitangent.y());
            bitangents.add(aiBitangent.z());
        }

        // Process texture coordinates
        AIVector3D.Buffer aiTexCoords = aiMesh.mTextureCoords(0);
        while (aiTexCoords.hasRemaining()) {
            AIVector3D aiTexCoord = aiTexCoords.get();
            texCoords.add(aiTexCoord.x());
            texCoords.add(aiTexCoord.y());
        }

        // Process indices
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        int numFaces = aiMesh.mNumFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get();
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.hasRemaining()) {
                indices.add(buffer.get());
            }
        }

        // If there are no texture coordinates, generate them
        if (texCoords.size() == 0) {
            int numElements = (vertices.size() / 3) * 2;
            for (int i = 0; i < numElements; i++) {
                texCoords.add(0.0f);
            }
        }

        return new MeshData(
            floatListToArray(vertices),
            floatListToArray(normals),
            floatListToArray(tangents),
            floatListToArray(bitangents),
            floatListToArray(texCoords),
            intListToArray(indices)
        );

    }
}
