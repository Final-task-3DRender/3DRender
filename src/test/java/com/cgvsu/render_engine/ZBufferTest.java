package com.cgvsu.render_engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для Z-buffer (буфера глубины).
 * Проверяют правильность работы алгоритма Z-buffer для корректной отрисовки 3D объектов.
 */
public class ZBufferTest {

    @Test
    public void testZBufferInitialization() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        assertEquals(100, zBuffer.getWidth());
        assertEquals(100, zBuffer.getHeight());
    }

    @Test
    public void testFirstPixelAlwaysPasses() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Первый пиксель всегда должен проходить проверку
        assertTrue(zBuffer.testAndSet(50, 50, 0.5f));
        assertEquals(0.5f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testNearerPixelWins() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Рисуем дальний пиксель (z = 0.2, дальше от камеры в нашей системе)
        assertTrue(zBuffer.testAndSet(50, 50, 0.2f));
        assertEquals(0.2f, zBuffer.get(50, 50), 1e-6f);
        
        // Пытаемся нарисовать ближний пиксель (z = 0.8, ближе к камере в нашей системе)
        // В нашей системе координат: БОЛЬШИЕ значения z означают ближе к камере
        assertTrue(zBuffer.testAndSet(50, 50, 0.8f));
        assertEquals(0.8f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testFartherPixelRejected() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Рисуем ближний пиксель (большое значение z)
        assertTrue(zBuffer.testAndSet(50, 50, 0.8f));
        assertEquals(0.8f, zBuffer.get(50, 50), 1e-6f);
        
        // Пытаемся нарисовать дальний пиксель (меньшее значение z) - должен быть отклонен
        assertFalse(zBuffer.testAndSet(50, 50, 0.2f));
        // Значение должно остаться прежним
        assertEquals(0.8f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testNegativeZValues() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // В нашей системе координат:
        // БОЛЬШИЕ значения z (включая положительные) означают ближе к камере
        // МЕНЬШИЕ значения z (включая отрицательные) означают дальше от камеры
        
        // Рисуем пиксель с z = -0.5 (дальше)
        assertTrue(zBuffer.testAndSet(50, 50, -0.5f));
        
        // Пытаемся нарисовать пиксель с z = 0.5 (ближе) - должен пройти
        assertTrue(zBuffer.testAndSet(50, 50, 0.5f));
        assertEquals(0.5f, zBuffer.get(50, 50), 1e-6f);
        
        // Пытаемся нарисовать пиксель с z = -0.8 (дальше) - должен быть отклонен
        assertFalse(zBuffer.testAndSet(50, 50, -0.8f));
    }

    @Test
    public void testBoundaryConditions() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Тест границ
        assertFalse(zBuffer.testAndSet(-1, 50, 0.5f)); // x < 0
        assertFalse(zBuffer.testAndSet(100, 50, 0.5f)); // x >= width
        assertFalse(zBuffer.testAndSet(50, -1, 0.5f)); // y < 0
        assertFalse(zBuffer.testAndSet(50, 100, 0.5f)); // y >= height
    }

    @Test
    public void testInvalidZValues() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // NaN и Infinity должны быть отклонены
        assertFalse(zBuffer.testAndSet(50, 50, Float.NaN));
        assertFalse(zBuffer.testAndSet(50, 50, Float.POSITIVE_INFINITY));
        assertFalse(zBuffer.testAndSet(50, 50, Float.NEGATIVE_INFINITY));
    }

    @Test
    public void testClear() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Рисуем пиксель
        assertTrue(zBuffer.testAndSet(50, 50, 0.5f));
        assertEquals(0.5f, zBuffer.get(50, 50), 1e-6f);
        
        // Очищаем буфер
        zBuffer.clear();
        
        // Теперь снова должен проходить первый пиксель
        assertTrue(zBuffer.testAndSet(50, 50, 0.3f));
        assertEquals(0.3f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testDepthOrdering() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Тест правильного порядка глубины
        // В нашей системе координат: БОЛЬШИЕ значения z означают ближе к камере
        // Рисуем пиксели в порядке от дальних (меньшие z) к ближним (большие z)
        float[] depths = {-0.8f, -0.5f, -0.2f, 0.2f, 0.5f, 0.8f};
        
        for (int i = 0; i < depths.length; i++) {
            float depth = depths[i];
            boolean shouldPass = zBuffer.testAndSet(50, 50, depth);
            if (i == 0) {
                // Первый всегда проходит
                assertTrue(shouldPass, "First depth " + depth + " should always pass");
            } else {
                // Более близкие (большие z) должны проходить
                // Более дальние (меньшие z) должны быть отклонены
                float currentZ = zBuffer.get(50, 50);
                if (depth > currentZ) {
                    assertTrue(shouldPass, "Depth " + depth + " should pass (closer than " + currentZ + ")");
                } else {
                    // Если depth <= currentZ, не рисуем (дальше или такой же)
                    assertFalse(shouldPass, "Depth " + depth + " should be rejected (farther than or equal to " + currentZ + ")");
                }
            }
        }
    }
}
