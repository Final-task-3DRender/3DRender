package com.cgvsu.render_engine;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Тесты для класса RenderSettings.
 * 
 * Проверяют настройки рендеринга: режимы отрисовки, цвета, Z-buffer, текстуры.
 */
class RenderSettingsTest {
    
    @Test
    void testDefaultSettings() {
        RenderSettings settings = new RenderSettings();
        
        Assertions.assertTrue(settings.isShowWireframe());
        Assertions.assertTrue(settings.isShowFilled());
        Assertions.assertTrue(settings.isEnableZBuffer());
        Assertions.assertFalse(settings.isEnableBackfaceCulling());
        Assertions.assertFalse(settings.isUseTexture());
        Assertions.assertTrue(settings.isEnableTriangulation());
        Assertions.assertTrue(settings.isEnableRasterization());
        Assertions.assertEquals(Color.LIGHTGRAY, settings.getFillColor());
        Assertions.assertEquals(Color.DARKGRAY, settings.getWireframeColor());
    }
    
    @Test
    void testWireframeSettings() {
        RenderSettings settings = new RenderSettings();
        
        settings.setShowWireframe(false);
        Assertions.assertFalse(settings.isShowWireframe());
        
        settings.setShowWireframe(true);
        Assertions.assertTrue(settings.isShowWireframe());
    }
    
    @Test
    void testFilledSettings() {
        RenderSettings settings = new RenderSettings();
        
        settings.setShowFilled(false);
        Assertions.assertFalse(settings.isShowFilled());
        
        settings.setShowFilled(true);
        Assertions.assertTrue(settings.isShowFilled());
    }
    
    @Test
    void testColorSettings() {
        RenderSettings settings = new RenderSettings();
        
        Color newFillColor = Color.RED;
        Color newWireframeColor = Color.BLUE;
        
        settings.setFillColor(newFillColor);
        settings.setWireframeColor(newWireframeColor);
        
        Assertions.assertEquals(newFillColor, settings.getFillColor());
        Assertions.assertEquals(newWireframeColor, settings.getWireframeColor());
    }
    
    @Test
    void testZBufferSettings() {
        RenderSettings settings = new RenderSettings();
        
        settings.setEnableZBuffer(false);
        Assertions.assertFalse(settings.isEnableZBuffer());
        
        settings.setEnableZBuffer(true);
        Assertions.assertTrue(settings.isEnableZBuffer());
    }
    
    @Test
    void testBackfaceCullingSettings() {
        RenderSettings settings = new RenderSettings();
        
        settings.setEnableBackfaceCulling(true);
        Assertions.assertTrue(settings.isEnableBackfaceCulling());
        
        settings.setEnableBackfaceCulling(false);
        Assertions.assertFalse(settings.isEnableBackfaceCulling());
    }
    
    @Test
    void testTextureSettings() {
        RenderSettings settings = new RenderSettings();
        
        Assertions.assertFalse(settings.isUseTexture());
        Assertions.assertNull(settings.getTexture());
        
        settings.setUseTexture(true);
        Assertions.assertTrue(settings.isUseTexture());
        
        settings.setUseTexture(false);
        Assertions.assertFalse(settings.isUseTexture());
        Assertions.assertNull(settings.getTexture());
    }
    
    @Test
    void testTriangulationSettings() {
        RenderSettings settings = new RenderSettings();
        
        settings.setEnableTriangulation(false);
        Assertions.assertFalse(settings.isEnableTriangulation());
        
        settings.setEnableTriangulation(true);
        Assertions.assertTrue(settings.isEnableTriangulation());
    }
    
    @Test
    void testRasterizationSettings() {
        RenderSettings settings = new RenderSettings();
        
        settings.setEnableRasterization(false);
        Assertions.assertFalse(settings.isEnableRasterization());
        
        settings.setEnableRasterization(true);
        Assertions.assertTrue(settings.isEnableRasterization());
    }
    
    @Test
    void testConstructorWithParameters() {
        Color fillColor = Color.GREEN;
        Color wireframeColor = Color.YELLOW;
        
        RenderSettings settings = new RenderSettings(false, true, fillColor, wireframeColor);
        
        Assertions.assertFalse(settings.isShowWireframe());
        Assertions.assertTrue(settings.isShowFilled());
        Assertions.assertEquals(fillColor, settings.getFillColor());
        Assertions.assertEquals(wireframeColor, settings.getWireframeColor());
    }
}
