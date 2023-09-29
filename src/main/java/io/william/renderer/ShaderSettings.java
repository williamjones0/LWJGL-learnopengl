package io.william.renderer;

import org.joml.Vector3f;

public class ShaderSettings {

    private float exposure = 1.0f;
    private float bloomStrength = 0.04f;

    private float pointLightMeshRadius = 0.1f;

    private boolean specularOcclusion = true;
    private boolean horizonSpecularOcclusion = true;

    // IBL
    private boolean fastIrradiance = true;
    private float cubemapCamFOV = (float) Math.toDegrees(Math.PI/3.5);

    // Probes
    private boolean useProbes = false;
    private boolean probeDebug = true;
    private float probeDebugRadius = 2.0f;
    private float probeDebugMetallic = 0.1f;
    private float probeDebugRoughness = 0.0f;
    private Vector3f probeInnerRange = new Vector3f(0);
    private Vector3f probeOuterRange = new Vector3f(60);
    private float probeInnerRadius = 0.0f;
    private float probeOuterRadius = 0.0f;

    // Shadows

    // Directional shadows
    private float shadowMinBias = 0.005f;
    private float shadowMaxBias = 0.05f;

    // Point shadows
    private boolean pointShadows = true;
    private float pointShadowBias = 0.05f;

    // Sun
    private boolean updateDirLight = false;

    // Post-processing

    // Tone mapping
    private int toneMappingType = 0;

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

    public boolean isUseProbes() {
        return useProbes;
    }

    public void setUseProbes(boolean useProbes) {
        this.useProbes = useProbes;
    }

    public boolean isProbeDebug() {
        return probeDebug;
    }

    public void setProbeDebug(boolean probeDebug) {
        this.probeDebug = probeDebug;
    }

    public float getProbeDebugRadius() {
        return probeDebugRadius;
    }

    public void setProbeDebugRadius(float probeDebugRadius) {
        this.probeDebugRadius = probeDebugRadius;
    }

    public float getProbeDebugMetallic() {
        return probeDebugMetallic;
    }

    public void setProbeDebugMetallic(float probeDebugMetallic) {
        this.probeDebugMetallic = probeDebugMetallic;
    }

    public float getProbeDebugRoughness() {
        return probeDebugRoughness;
    }

    public void setProbeDebugRoughness(float probeDebugRoughness) {
        this.probeDebugRoughness = probeDebugRoughness;
    }

    public Vector3f getProbeInnerRange() {
        return probeInnerRange;
    }

    public void setProbeInnerRange(Vector3f probeInnerRange) {
        this.probeInnerRange = probeInnerRange;
    }

    public Vector3f getProbeOuterRange() {
        return probeOuterRange;
    }

    public void setProbeOuterRange(Vector3f probeOuterRange) {
        this.probeOuterRange = probeOuterRange;
    }

    public float getProbeInnerRadius() {
        return probeInnerRadius;
    }

    public void setProbeInnerRadius(float probeInnerRadius) {
        this.probeInnerRadius = probeInnerRadius;
    }

    public float getProbeOuterRadius() {
        return probeOuterRadius;
    }

    public void setProbeOuterRadius(float probeOuterRadius) {
        this.probeOuterRadius = probeOuterRadius;
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

    public boolean isUpdateDirLight() {
        return updateDirLight;
    }

    public void setUpdateDirLight(boolean updateDirLight) {
        this.updateDirLight = updateDirLight;
    }

    public int getToneMappingType() {
        return toneMappingType;
    }

    public void setToneMappingType(int toneMappingType) {
        this.toneMappingType = toneMappingType;
    }
}
