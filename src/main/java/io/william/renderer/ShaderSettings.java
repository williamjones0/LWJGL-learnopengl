package io.william.renderer;

public class ShaderSettings {

    private boolean specularOcclusion = true;
    private boolean horizonSpecularOcclusion = true;

    // Shadows

    // Directional shadows
    private float shadowMinBias = 0.005f;
    private float shadowMaxBias = 0.05f;

    // Point shadows
    private boolean pointShadows = true;
    private float pointShadowBias = 0.05f;

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
