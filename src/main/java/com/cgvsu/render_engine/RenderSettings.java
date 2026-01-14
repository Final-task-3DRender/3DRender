package com.cgvsu.render_engine;

import javafx.scene.paint.Color;

/**
 * Настройки рендеринга модели.
 * Хранит параметры отображения: режим отрисовки (wireframe/filled), цвет и т.д.
 */
public class RenderSettings {
    private boolean showWireframe = true;
    private boolean showFilled = true;
    private Color fillColor = Color.LIGHTGRAY;
    private Color wireframeColor = Color.DARKGRAY;
    private boolean enableZBuffer = true; // По умолчанию включен для правильной отрисовки

    public RenderSettings() {
    }

    public RenderSettings(boolean showWireframe, boolean showFilled, Color fillColor, Color wireframeColor) {
        this.showWireframe = showWireframe;
        this.showFilled = showFilled;
        this.fillColor = fillColor;
        this.wireframeColor = wireframeColor;
    }

    public boolean isShowWireframe() {
        return showWireframe;
    }

    public void setShowWireframe(boolean showWireframe) {
        this.showWireframe = showWireframe;
    }

    public boolean isShowFilled() {
        return showFilled;
    }

    public void setShowFilled(boolean showFilled) {
        this.showFilled = showFilled;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color getWireframeColor() {
        return wireframeColor;
    }

    public void setWireframeColor(Color wireframeColor) {
        this.wireframeColor = wireframeColor;
    }

    public boolean isEnableZBuffer() {
        return enableZBuffer;
    }

    public void setEnableZBuffer(boolean enableZBuffer) {
        this.enableZBuffer = enableZBuffer;
    }
}
