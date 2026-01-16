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
        
        assertTrue(zBuffer.testAndSet(50, 50, 0.2f));
        assertEquals(0.2f, zBuffer.get(50, 50), 1e-6f);
        
        assertTrue(zBuffer.testAndSet(50, 50, 0.8f));
        assertEquals(0.8f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testFartherPixelRejected() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        assertTrue(zBuffer.testAndSet(50, 50, 0.8f));
        assertEquals(0.8f, zBuffer.get(50, 50), 1e-6f);
        
        assertFalse(zBuffer.testAndSet(50, 50, 0.2f));
        assertEquals(0.8f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testNegativeZValues() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        assertTrue(zBuffer.testAndSet(50, 50, -0.5f));
        
        assertTrue(zBuffer.testAndSet(50, 50, 0.5f));
        assertEquals(0.5f, zBuffer.get(50, 50), 1e-6f);
        
        assertFalse(zBuffer.testAndSet(50, 50, -0.8f));
    }

    @Test
    public void testBoundaryConditions() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        assertFalse(zBuffer.testAndSet(-1, 50, 0.5f));
        assertFalse(zBuffer.testAndSet(100, 50, 0.5f)); // x >= width
        assertFalse(zBuffer.testAndSet(50, -1, 0.5f)); // y < 0
        assertFalse(zBuffer.testAndSet(50, 100, 0.5f)); // y >= height
    }

    @Test
    public void testInvalidZValues() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        assertFalse(zBuffer.testAndSet(50, 50, Float.NaN));
        assertFalse(zBuffer.testAndSet(50, 50, Float.POSITIVE_INFINITY));
        assertFalse(zBuffer.testAndSet(50, 50, Float.NEGATIVE_INFINITY));
    }

    @Test
    public void testClear() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        assertTrue(zBuffer.testAndSet(50, 50, 0.5f));
        assertEquals(0.5f, zBuffer.get(50, 50), 1e-6f);
        
        zBuffer.clear();
        
        assertTrue(zBuffer.testAndSet(50, 50, 0.3f));
        assertEquals(0.3f, zBuffer.get(50, 50), 1e-6f);
    }

    @Test
    public void testDepthOrdering() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        float[] depths = {-0.8f, -0.5f, -0.2f, 0.2f, 0.5f, 0.8f};
        
        for (int i = 0; i < depths.length; i++) {
            float depth = depths[i];
            float currentZ = zBuffer.get(50, 50);
            boolean shouldPass = zBuffer.testAndSet(50, 50, depth);
            
            if (i == 0) {
                assertTrue(shouldPass, "First depth " + depth + " should always pass (currentZ=" + currentZ + ")");
            } else {
                if (depth > currentZ) {
                    assertTrue(shouldPass, "Depth " + depth + " should pass (closer than " + currentZ + ")");
                } else {
                    assertFalse(shouldPass, "Depth " + depth + " should be rejected (farther than or equal to " + currentZ + ")");
                }
            }
        }
    }
}
