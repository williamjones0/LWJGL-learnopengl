package io.william.renderer;

public class ShaderSettings {

    private boolean specularOcclusion = true;
    private boolean horizonSpecularOcclusion = true;

    // Shadows

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
