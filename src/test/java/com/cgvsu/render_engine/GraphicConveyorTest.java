package com.cgvsu.render_engine;

import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса GraphicConveyor
 */
class GraphicConveyorTest {
    
    private static final float EPSILON = 1e-5f;
    
    /**
     * Тест преобразования вершины из центра координат (0, 0, z, 1) в центр экрана.
     */
    @Test
    void testVertexToPointCenter() {
        Vector4f vertex = new Vector4f(0, 0, 0, 1);
        int width = 800;
        int height = 600;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
        
        // Центр координат должен быть в центре экрана
        Assertions.assertEquals(400.0f, result.x, EPSILON); // width / 2
        Assertions.assertEquals(300.0f, result.y, EPSILON); // height / 2
    }
    
    /**
     * Тест преобразования вершины из (-1, -1, z, 1) в левый верхний угол экрана.
     */
    @Test
    void testVertexToPointTopLeft() {
        Vector4f vertex = new Vector4f(-1, 1, 0, 1); // -1 по X, 1 по Y (но Y инвертируется)
        int width = 800;
        int height = 600;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
        
        // Левый верхний угол: x = 0, y = 0 (после инверсии Y)
        Assertions.assertEquals(0.0f, result.x, EPSILON);
        Assertions.assertEquals(0.0f, result.y, EPSILON);
    }
    
    /**
     * Тест преобразования вершины из (1, -1, z, 1) в правый нижний угол экрана.
     */
    @Test
    void testVertexToPointBottomRight() {
        Vector4f vertex = new Vector4f(1, -1, 0, 1); // 1 по X, -1 по Y (но Y инвертируется)
        int width = 800;
        int height = 600;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
        
        // Правый нижний угол: x = width, y = height
        Assertions.assertEquals(800.0f, result.x, EPSILON);
        Assertions.assertEquals(600.0f, result.y, EPSILON);
    }
    
    /**
     * Тест преобразования вершины с произвольными координатами.
     */
    @Test
    void testVertexToPointArbitrary() {
        Vector4f vertex = new Vector4f(0.5f, -0.3f, 0, 1);
        int width = 1000;
        int height = 800;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
        
        // x = 0.5 * 1000 / 2 + 1000 / 2 = 250 + 500 = 750
        Assertions.assertEquals(750.0f, result.x, EPSILON);
        // y = -(-0.3) * 800 / 2 + 800 / 2 = 0.3 * 400 + 400 = 120 + 400 = 520
        Assertions.assertEquals(520.0f, result.y, EPSILON);
    }
    
    /**
     * Тест проверки инверсии Y-координаты.
     */
    @Test
    void testYCoordinateInversion() {
        int width = 800;
        int height = 600;
        
        // Вершина с положительным Y в пространстве проекции должна быть выше по экрану
        Vector4f vertexPositiveY = new Vector4f(0, 0.5f, 0, 1);
        Point2f resultPositiveY = GraphicConveyor.vertexToPoint(vertexPositiveY, width, height);
        
        // Вершина с отрицательным Y в пространстве проекции должна быть ниже по экрану
        Vector4f vertexNegativeY = new Vector4f(0, -0.5f, 0, 1);
        Point2f resultNegativeY = GraphicConveyor.vertexToPoint(vertexNegativeY, width, height);
        
        // После инверсии Y, positiveY должна быть меньше (выше), а negativeY больше (ниже)
        Assertions.assertTrue(resultPositiveY.y < resultNegativeY.y, 
            "Positive Y in projection space should map to smaller screen Y (higher on screen)");
        
        // Проверяем конкретные значения
        // y_positive = -0.5 * 600 / 2 + 600 / 2 = -150 + 300 = 150
        Assertions.assertEquals(150.0f, resultPositiveY.y, EPSILON);
        // y_negative = -(-0.5) * 600 / 2 + 600 / 2 = 150 + 300 = 450
        Assertions.assertEquals(450.0f, resultNegativeY.y, EPSILON);
    }
    
    /**
     * Тест обработки null vertex.
     */
    @Test
    void testNullVertex() {
        int width = 800;
        int height = 600;
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            GraphicConveyor.vertexToPoint(null, width, height);
        }, "Should throw IllegalArgumentException for null vertex");
    }
    
    /**
     * Тест для разных размеров экрана.
     */
    @Test
    void testDifferentScreenSizes() {
        Vector4f vertex = new Vector4f(0, 0, 0, 1);
        
        // Маленький экран
        Point2f smallScreen = GraphicConveyor.vertexToPoint(vertex, 100, 100);
        Assertions.assertEquals(50.0f, smallScreen.x, EPSILON);
        Assertions.assertEquals(50.0f, smallScreen.y, EPSILON);
        
        // Большой экран
        Point2f largeScreen = GraphicConveyor.vertexToPoint(vertex, 1920, 1080);
        Assertions.assertEquals(960.0f, largeScreen.x, EPSILON);
        Assertions.assertEquals(540.0f, largeScreen.y, EPSILON);
    }
    
    /**
     * Тест для граничных значений (края экрана).
     */
    @Test
    void testBoundaryValues() {
        int width = 800;
        int height = 600;
        
        // Левый край (x = -1)
        Vector4f leftEdge = new Vector4f(-1, 0, 0, 1);
        Point2f leftResult = GraphicConveyor.vertexToPoint(leftEdge, width, height);
        Assertions.assertEquals(0.0f, leftResult.x, EPSILON);
        
        // Правый край (x = 1)
        Vector4f rightEdge = new Vector4f(1, 0, 0, 1);
        Point2f rightResult = GraphicConveyor.vertexToPoint(rightEdge, width, height);
        Assertions.assertEquals(800.0f, rightResult.x, EPSILON);
        
        // Верхний край (y = 1, но инвертируется)
        Vector4f topEdge = new Vector4f(0, 1, 0, 1);
        Point2f topResult = GraphicConveyor.vertexToPoint(topEdge, width, height);
        Assertions.assertEquals(0.0f, topResult.y, EPSILON);
        
        // Нижний край (y = -1, но инвертируется)
        Vector4f bottomEdge = new Vector4f(0, -1, 0, 1);
        Point2f bottomResult = GraphicConveyor.vertexToPoint(bottomEdge, width, height);
        Assertions.assertEquals(600.0f, bottomResult.y, EPSILON);
    }
    
    /**
     * Тест для вершин с w != 1 (хотя vertexToPoint использует только x и y).
     */
    @Test
    void testVertexWithDifferentW() {
        Vector4f vertexW2 = new Vector4f(0.5f, 0.5f, 0, 2.0f);
        Vector4f vertexW1 = new Vector4f(0.5f, 0.5f, 0, 1.0f);
        int width = 800;
        int height = 600;
        
        Point2f resultW2 = GraphicConveyor.vertexToPoint(vertexW2, width, height);
        Point2f resultW1 = GraphicConveyor.vertexToPoint(vertexW1, width, height);
        
        // vertexToPoint использует только x и y, поэтому результат должен быть одинаковым
        Assertions.assertEquals(resultW1.x, resultW2.x, EPSILON);
        Assertions.assertEquals(resultW1.y, resultW2.y, EPSILON);
    }
    
    /**
     * Тест для проверки формулы преобразования координат.
     */
    @Test
    void testTransformationFormula() {
        Vector4f vertex = new Vector4f(0.8f, -0.6f, 5.0f, 1.0f);
        int width = 640;
        int height = 480;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
        
        // Формула: x = vertex.x * width / 2 + width / 2
        float expectedX = 0.8f * width / 2.0f + width / 2.0f;
        // Формула: y = -vertex.y * height / 2 + height / 2
        float expectedY = -(-0.6f) * height / 2.0f + height / 2.0f;
        
        Assertions.assertEquals(expectedX, result.x, EPSILON);
        Assertions.assertEquals(expectedY, result.y, EPSILON);
    }
    
    /**
     * Тест для квадратного экрана.
     */
    @Test
    void testSquareScreen() {
        Vector4f vertex = new Vector4f(0.5f, 0.5f, 0, 1);
        int size = 512;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, size, size);
        
        // Для квадратного экрана x и y должны быть симметричны после инверсии Y
        // x = 0.5 * 512 / 2 + 512 / 2 = 128 + 256 = 384
        // y = -0.5 * 512 / 2 + 512 / 2 = -128 + 256 = 128
        Assertions.assertEquals(384.0f, result.x, EPSILON);
        Assertions.assertEquals(128.0f, result.y, EPSILON);
    }
    
    /**
     * Тест для очень маленьких размеров экрана.
     */
    @Test
    void testVerySmallScreen() {
        Vector4f vertex = new Vector4f(0, 0, 0, 1);
        int width = 2;
        int height = 2;
        
        Point2f result = GraphicConveyor.vertexToPoint(vertex, width, height);
        
        // Центр должен быть в (1, 1) для экрана 2x2
        Assertions.assertEquals(1.0f, result.x, EPSILON);
        Assertions.assertEquals(1.0f, result.y, EPSILON);
    }
}