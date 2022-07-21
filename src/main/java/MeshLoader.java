import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static Utils.Utils.floatListToArray;
import static Utils.Utils.intListToArray;
import static org.lwjgl.assimp.Assimp.*;

public class MeshLoader {

    public static Mesh[] load(String modelPath, String texturesPath) throws Exception {
        return load(modelPath, texturesPath, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
            | aiProcess_Triangulate | aiProcess_FixInfacingNormals
            | aiProcess_PreTransformVertices);
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
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesPath);
        }

        // Process meshes
        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            meshes[i] = processMesh(aiMesh, materials);
        }

        return meshes;
    }

    private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) throws Exception {
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture texture;
        System.out.println(texturesDir + "/" + textPath);
        if (textPath.length() > 0) {
            texture = new Texture(texturesDir + "/" + textPath);
            Material material = new Material(texture, texture, 64.0f);
            materials.add(material);
        }
    }

    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
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
            materials.get(0)
        );

    }
}
