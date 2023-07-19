package io.william.renderer;

public class ShaderSettings {

    private float exposure = 1.0f;
    private float bloomStrength = 0.04f;

    private float pointLightMeshRadius = 0.1f;

    private boolean specularOcclusion = true;
    private boolean horizonSpecularOcclusion = true;

    // IBL
    private boolean fastIrradiance = true;
    private float cubemapCamFOV = (float) Math.toDegrees(Math.PI/3.5);

    // Shadows

    // Directional shadows
    private float shadowMinBias = 0.005f;
    private float shadowMaxBias = 0.05f;

    // Point shadows
    private boolean pointShadows = true;
    private float pointShadowBias = 0.05f;

    public float getExposure() {
        return exposure;
    }

    public void setExposure(float exposure) {
        this.exposure = exposure;
    }

    public float getBloomStrength() {
        return bloomStrength;
    }

    public void setBloomStrength(float bloomStrength) {
        this.bloomStrength = bloomStrength;
    }

    public float getPointLightMeshRadius() {
        return pointLightMeshRadius;
    }

    public void setPointLightMeshRadius(float pointLightMeshRadius) {
        this.pointLightMeshRadius = pointLightMeshRadius;
    }

    public boolean isSpecularOcclusion() {
        return specularOcclusion;
    }

    public void setSpecularOcclusion(boolean specularOcclusion) {
        this.specularOcclusion = specularOcclusion;
    }

    public boolean isHorizonSpecularOcclusion() {
        return horizonSpecularOcclusion;
    }

    public void setHorizonSpecularOcclusion(boolean horizonSpecularOcclusion) {
        this.horizonSpecularOcclusion = horizonSpecularOcclusion;
    }

    public boolean isFastIrradiance() {
        return fastIrradiance;
    }

    public void setFastIrradiance(boolean fastIrradiance) {
        this.fastIrradiance = fastIrradiance;
    }

    public float getCubemapCamFOV() {
        return cubemapCamFOV;
    }

    public void setCubemapCamFOV(float cubemapCamFOV) {
        this.cubemapCamFOV = cubemapCamFOV;
    }

    public float getShadowMinBias() {
        return shadowMinBias;
    }

    public void setShadowMinBias(float shadowMinBias) {
        this.shadowMinBias = shadowMinBias;
    }

    public float getShadowMaxBias() {
        return shadowMaxBias;
    }

    public void setShadowMaxBias(float shadowMaxBias) {
        this.shadowMaxBias = shadowMaxBias;
    }

    public boolean isPointShadows() {
        return pointShadows;
    }

    public void setPointShadows(boolean pointShadows) {
        this.pointShadows = pointShadows;
    }

    public float getPointShadowBias() {
        return pointShadowBias;
    }

    public void setPointShadowBias(float pointShadowBias) {
        this.pointShadowBias = pointShadowBias;
    }
}
