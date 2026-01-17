package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

/**
 * Класс для хранения настроек рендеринга 3D моделей.
 * 
 * <p>Содержит параметры отображения:
 * <ul>
 *   <li>Режимы отрисовки (wireframe/filled)</li>
 *   <li>Цвета для заливки и обводки</li>
 *   <li>Настройки Z-buffer для правильной отрисовки глубины</li>
 *   <li>Настройки backface culling</li>
 *   <li>Поддержка текстур</li>
 *   <li>Триангуляция и растеризация</li>
 * </ul>
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class RenderSettings {
    
    /**
     * Показывать ли обводку (wireframe) полигонов.
     */
    private boolean showWireframe = true;
    
    /**
     * Показывать ли заливку (filled) полигонов.
     */
    private boolean showFilled = true;
    
    /**
     * Цвет для заливки полигонов.
     */
    private Color fillColor = Color.LIGHTGRAY;
    
    /**
     * Цвет для обводки (wireframe) полигонов.
     */
    private Color wireframeColor = Color.DARKGRAY;
    
    private boolean enableZBuffer = true;
    private boolean enableBackfaceCulling = false;
    private boolean useTexture = false;
    private Texture texture = null;
    private boolean enableTriangulation = true;
    private boolean enableRasterization = true;

    private boolean enableLighting = true;
    private Vector3f lightDirection = new Vector3f(0.57735f, 0.57735f, -0.57735f);
    private float lightingCoefficient = 0.7f;

    /**
     * Создает настройки рендеринга со значениями по умолчанию.
     */
    public RenderSettings() {
    }

    /**
     * Создает настройки рендеринга с указанными параметрами.
     * 
     * @param showWireframe показывать ли обводку
     * @param showFilled показывать ли заливку
     * @param fillColor цвет заливки
     * @param wireframeColor цвет обводки
     */
    public RenderSettings(boolean showWireframe, boolean showFilled, Color fillColor, Color wireframeColor) {
        this.showWireframe = showWireframe;
        this.showFilled = showFilled;
        this.fillColor = fillColor;
        this.wireframeColor = wireframeColor;
    }

    /**
     * Проверяет, включена ли отрисовка обводки (wireframe).
     * 
     * @return true если обводка включена
     */
    public boolean isShowWireframe() {
        return showWireframe;
    }

    /**
     * Устанавливает, показывать ли обводку полигонов.
     * 
     * @param showWireframe true для включения обводки
     */
    public void setShowWireframe(boolean showWireframe) {
        this.showWireframe = showWireframe;
    }

    /**
     * Проверяет, включена ли отрисовка заливки.
     * 
     * @return true если заливка включена
     */
    public boolean isShowFilled() {
        return showFilled;
    }

    /**
     * Устанавливает, показывать ли заливку полигонов.
     * 
     * @param showFilled true для включения заливки
     */
    public void setShowFilled(boolean showFilled) {
        this.showFilled = showFilled;
    }

    /**
     * Возвращает цвет заливки полигонов.
     * 
     * @return цвет заливки
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Устанавливает цвет заливки полигонов.
     * 
     * @param fillColor новый цвет заливки
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Возвращает цвет обводки (wireframe) полигонов.
     * 
     * @return цвет обводки
     */
    public Color getWireframeColor() {
        return wireframeColor;
    }

    /**
     * Устанавливает цвет обводки полигонов.
     * 
     * @param wireframeColor новый цвет обводки
     */
    public void setWireframeColor(Color wireframeColor) {
        this.wireframeColor = wireframeColor;
    }

    /**
     * Проверяет, включен ли Z-buffer.
     * 
     * @return true если Z-buffer включен
     */
    public boolean isEnableZBuffer() {
        return enableZBuffer;
    }

    /**
     * Включает или выключает Z-buffer.
     * 
     * @param enableZBuffer true для включения Z-buffer
     */
    public void setEnableZBuffer(boolean enableZBuffer) {
        this.enableZBuffer = enableZBuffer;
    }

    /**
     * Проверяет, включен ли backface culling.
     * 
     * @return true если backface culling включен
     */
    public boolean isEnableBackfaceCulling() {
        return enableBackfaceCulling;
    }

    /**
     * Включает или выключает backface culling.
     * 
     * @param enableBackfaceCulling true для включения backface culling
     */
    public void setEnableBackfaceCulling(boolean enableBackfaceCulling) {
        this.enableBackfaceCulling = enableBackfaceCulling;
    }

    /**
     * Проверяет, используется ли текстура.
     * 
     * @return true если текстура используется
     */
    public boolean isUseTexture() {
        return useTexture;
    }

    /**
     * Устанавливает, использовать ли текстуру.
     * 
     * @param useTexture true для использования текстуры
     */
    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    /**
     * Возвращает загруженную текстуру.
     * 
     * @return текстура или null, если текстура не загружена
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Устанавливает текстуру для наложения на модель.
     * 
     * @param texture текстура для использования (может быть null)
     */
    public void setTexture(Texture texture) {
        this.texture = texture;
    }
    
    /**
     * Проверяет, включена ли триангуляция полигонов.
     * 
     * @return true если триангуляция включена
     */
    public boolean isEnableTriangulation() {
        return enableTriangulation;
    }
    
    /**
     * Включает или выключает триангуляцию полигонов.
     * 
     * @param enableTriangulation true для включения триангуляции
     */
    public void setEnableTriangulation(boolean enableTriangulation) {
        this.enableTriangulation = enableTriangulation;
    }
    
    /**
     * Проверяет, включена ли растеризация (заливка треугольников).
     * 
     * @return true если растеризация включена
     */
    public boolean isEnableRasterization() {
        return enableRasterization;
    }
    
    /**
     * Включает или выключает растеризацию.
     * 
     * @param enableRasterization true для включения растеризации
     */
    public void setEnableRasterization(boolean enableRasterization) {
        this.enableRasterization = enableRasterization;
    }
    
    public boolean isEnableLighting() {
        return enableLighting;
    }
    
    public void setEnableLighting(boolean enableLighting) {
        this.enableLighting = enableLighting;
    }
    
    public Vector3f getLightDirection() {
        return lightDirection;
    }
    
    public void setLightDirection(Vector3f lightDirection) {
        this.lightDirection = lightDirection != null ? lightDirection.normalize() : new Vector3f(0, 0, -1);
    }
    
    public float getLightingCoefficient() {
        return lightingCoefficient;
    }
    
    public void setLightingCoefficient(float lightingCoefficient) {
        this.lightingCoefficient = lightingCoefficient;
    }
}
