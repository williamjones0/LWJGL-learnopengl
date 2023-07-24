package io.william.renderer.terrain;

import io.william.renderer.Texture;
import io.william.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.lwjgl.opengl.GL11.*;

public class TerrainConfig {

    private float scaleY;
    private float scaleXZ;

    private Texture heightMap;
    private Texture normalMap;

    private int tessellationFactor;
    private float tessellationSlope;
    private float tessellationShift;

    private int[] lod_range = new int[8];
    private int[] lod_morphing_area = new int[8];

    public void loadFile(String file) {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {

                String[] tokens = line.split(" ");
                tokens = Utils.removeEmptyStrings(tokens);

                if (tokens.length == 0)
                    continue;

                if (tokens[0].equals("scaleY")) {
                    setScaleY(Float.valueOf(tokens[1]));
                }

                if (tokens[0].equals("scaleXZ")) {
                    setScaleXZ(Float.valueOf(tokens[1]));
                }

                if (tokens[0].equals("heightMap")) {
                    heightMap = new Texture(tokens[1], GL_RGBA);
                    heightMap.bind();
                    heightMap.bilinearFilter();

                    NormalMapRenderer normalMapRenderer = new NormalMapRenderer(heightMap.getWidth());
                    normalMapRenderer.setStrength(8.0f);
                    normalMapRenderer.render(heightMap);
                    setNormalMap(normalMapRenderer.getNormalMap());
                }

                if (tokens[0].equals("tessellationFactor")) {
                    setTessellationFactor(Integer.valueOf(tokens[1]));
                }

                if (tokens[0].equals("tessellationSlope")) {
                    setTessellationSlope(Float.valueOf(tokens[1]));
                }

                if (tokens[0].equals("tessellationShift")) {
                    setTessellationShift(Float.valueOf(tokens[1]));
                }

                if (tokens[0].equals("#lod_ranges")) {
                    for (int i = 0; i < 8; i++) {
                        line = reader.readLine();
                        tokens = line.split(" ");
                        tokens = Utils.removeEmptyStrings(tokens);
                        if (tokens[0].equals("lod" + (i+1) + "_range")) {
                            if (Integer.valueOf(tokens[1]) == 0) {
                                lod_range[i] = 0;
                                lod_morphing_area[i] = 0;
                            } else {
                                setLodRange(i, Integer.valueOf(tokens[1]));
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private int updateMorphingArea(int lod) {
        return (int) ((scaleXZ / TerrainQuadtree.getRootNodes()) / (Math.pow(2, lod)));
    }

    private void setLodRange(int index, int lod_range) {
        this.lod_range[index] = lod_range;
        lod_morphing_area[index] = lod_range - updateMorphingArea(index + 1);
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getScaleXZ() {
        return scaleXZ;
    }

    public void setScaleXZ(float scaleXZ) {
        this.scaleXZ = scaleXZ;
    }

    public int[] getLod_range() {
        return lod_range;
    }

    public void setLod_range(int[] lod_range) {
        this.lod_range = lod_range;
    }

    public int[] getLod_morphing_area() {
        return lod_morphing_area;
    }

    public void setLod_morphing_area(int[] lod_morphing_area) {
        this.lod_morphing_area = lod_morphing_area;
    }

    public int getTessellationFactor() {
        return tessellationFactor;
    }

    public void setTessellationFactor(int tessellationFactor) {
        this.tessellationFactor = tessellationFactor;
    }

    public float getTessellationSlope() {
        return tessellationSlope;
    }

    public void setTessellationSlope(float tessellationSlope) {
        this.tessellationSlope = tessellationSlope;
    }

    public float getTessellationShift() {
        return tessellationShift;
    }

    public void setTessellationShift(float tessellationShift) {
        this.tessellationShift = tessellationShift;
    }

    public Texture getHeightMap() {
        return heightMap;
    }

    public void setHeightMap(Texture heightMap) {
        this.heightMap = heightMap;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(Texture normalMap) {
        this.normalMap = normalMap;
    }
}
