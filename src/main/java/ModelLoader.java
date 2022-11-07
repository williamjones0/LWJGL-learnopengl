import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static Utils.Utils.floatListToArray;
import static Utils.Utils.intListToArray;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;

public class ModelLoader {

    public static Mesh[] load(String modelPath, String texturesPath) throws Exception {
        return load(modelPath, texturesPath, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
            | aiProcess_Triangulate | aiProcess_FixInfacingNormals
            | aiProcess_PreTransformVertices | aiProcess_CalcTangentSpace | aiProcess_FlipUVs);
    }

    public static Mesh[] load(String modelPath, String texturesPath, int flags) throws Exception {
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
//            processMaterial(aiMaterial, materials, texturesPath);
            processPBRMaterial(aiMaterial, pbrMaterials, texturesPath);
        }

        // If none of the materials have any textures, use the default material
        if (materials.size() == 0) {
            Material material = new Material(null, null, 32.0f, null);
            materials.add(material);
        }

        // Process meshes
        int numMeshes = aiScene.mNumMeshes();
        System.out.println("Number of meshes: " + numMeshes);
        PointerBuffer aiMeshes = aiScene.mMeshes();
        Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
//            meshes[i] = processMesh(aiMesh, materials);
            meshes[i] = processMesh(aiMesh, pbrMaterials);
        }

        return meshes;
    }

    private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) throws Exception {
        // Check if the material has any textures
        boolean hasDiffuseMap = aiGetMaterialTextureCount(aiMaterial, aiTextureType_DIFFUSE) > 0;
        boolean hasSpecularMap = aiGetMaterialTextureCount(aiMaterial, aiTextureType_SPECULAR) > 0;
        boolean hasNormalMap = aiGetMaterialTextureCount(aiMaterial, aiTextureType_NORMALS) > 0;

        // Diffuse map
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture diffuseTexture = null;
        System.out.println(texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            diffuseTexture = new Texture(texturesDir + "/" + textPath, GL_SRGB_ALPHA);
        }

        // Specular map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_SPECULAR, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture specularTexture = null;
        System.out.println(texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            specularTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        // Normal map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture normalTexture = null;
        System.out.println(texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            normalTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        if (hasDiffuseMap) {
            Material material = new Material(diffuseTexture, specularTexture, 32.0f, normalTexture);
            materials.add(material);
        }
    }

    private static void processPBRMaterial(AIMaterial aiMaterial, List<PBRMaterial> materials, String texturesDir) throws Exception {
        // Albedo map
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture albedoTexture = null;
        System.out.println("Albedo map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            albedoTexture = new Texture(texturesDir + "/" + textPath, GL_SRGB_ALPHA);
        }

        // Normal map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture normalTexture = null;
        System.out.println("Normal map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            normalTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        // Metallic map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_METALNESS, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture metallicTexture = null;
        System.out.println("Metallic map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            metallicTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        // Roughness map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_SHININESS, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture roughnessTexture = null;
        System.out.println("Roughness map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            roughnessTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        // Metallic roughness map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_UNKNOWN, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture metallicRoughnessTexture = null;
        System.out.println("Metallic roughness map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            metallicRoughnessTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        // AO map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_LIGHTMAP, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture aoTexture = null;
        System.out.println("AO map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            aoTexture = new Texture(texturesDir + "/" + textPath, GL_RGBA);
        }

        // Emissive map
        path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_EMISSIVE, 0, path, (IntBuffer) null, null, null, null, null, null);
        textPath = path.dataString();
        Texture emissiveTexture = null;
        System.out.println("Emissive map: " + texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            emissiveTexture = new Texture(texturesDir + "/" + textPath, GL_SRGB_ALPHA);
        }

        for (int i = 0; i < 21; i++) {
            path = AIString.calloc();
            Assimp.aiGetMaterialTexture(aiMaterial, i, 0, path, (IntBuffer) null, null, null, null, null, null);
            System.out.println(i + ": " + path.dataString());
        }

        PBRMaterial material = new PBRMaterial(
            albedoTexture,
            normalTexture,
            metallicTexture,
            roughnessTexture,
            metallicRoughnessTexture,
            aoTexture,
            emissiveTexture);
        materials.add(material);
    }

    private static Mesh processMesh(AIMesh aiMesh, List<PBRMaterial> pbrMaterials) throws Exception {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> tangents = new ArrayList<>();
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

        return new Mesh(
            floatListToArray(vertices),
            floatListToArray(normals),
            floatListToArray(texCoords),
            intListToArray(indices),
//            new PBRMaterial(
//                new Texture("src/main/resources/models/helmet/Default_albedo.jpg", GL_SRGB_ALPHA),
//                new Texture("src/main/resources/models/helmet/Default_normal.jpg", GL_RGBA),
//                new Texture("src/main/resources/textures/PBR/default_metallic.png", GL_RGBA),
//                new Texture("src/main/resources/models/helmet/Default_metalRoughness.jpg", GL_RGBA),
//                new Texture("src/main/resources/models/helmet/Default_AO.jpg", GL_RGBA),
//                new Texture("src/main/resources/models/helmet/Default_emissive.jpg", GL_SRGB_ALPHA)
//            )
            pbrMaterials.get(0)
        );

    }
}
