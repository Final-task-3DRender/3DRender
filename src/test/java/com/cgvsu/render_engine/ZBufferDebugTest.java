package com.cgvsu.render_engine;

import com.cgvsu.camera.Camera;
import com.cgvsu.camera.CameraProjection;
import com.cgvsu.camera.CameraView;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Отладочные тесты для проверки реального поведения Z-buffer.
 */
public class ZBufferDebugTest {

    @Test
    public void testRealWorldZValues() {
        // Создаем реальную камеру
        Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f,
            0.01f,
            100.0f
        );
        
        Matrix4f view = camera.getViewMatrix();
        Matrix4f projection = camera.getProjectionMatrix();
        Matrix4f mvp = projection.multiply(view);
        
        // Тестируем реальные вершины модели
        // Вершина близко к камере (z = 10 в мировом пространстве)
        Vector4f nearVertex = new Vector4f(0, 0, 10, 1.0f);
        // Вершина далеко от камеры (z = -10 в мировом пространстве, дальше от камеры)
        Vector4f farVertex = new Vector4f(0, 0, -10, 1.0f);
        
        Vector4f nearTransformed = mvp.multiply(nearVertex);
        Vector4f farTransformed = mvp.multiply(farVertex);
        
        // Перспективное деление
        if (Math.abs(nearTransformed.w) > 1e-7f) {
            nearTransformed = nearTransformed.divide(nearTransformed.w);
        }
        if (Math.abs(farTransformed.w) > 1e-7f) {
            farTransformed = farTransformed.divide(farTransformed.w);
        }
        
        float nearZ = nearTransformed.z;
        float farZ = farTransformed.z;
        
        System.out.println("=== Real World Z Values ===");
        System.out.println("Near vertex (world z=10) -> NDC z: " + nearZ);
        System.out.println("Far vertex (world z=-10) -> NDC z: " + farZ);
        System.out.println("Near Z > Far Z: " + (nearZ > farZ));
        
        // Тестируем Z-buffer с реальными значениями
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Рисуем дальний объект первым
        boolean farPassed = zBuffer.testAndSet(50, 50, farZ);
        System.out.println("Far Z passed: " + farPassed + ", stored: " + zBuffer.get(50, 50));
        
        // Пытаемся нарисовать ближний объект
        boolean nearPassed = zBuffer.testAndSet(50, 50, nearZ);
        System.out.println("Near Z passed: " + nearPassed + ", stored: " + zBuffer.get(50, 50));
        
        // Если nearZ > farZ, то near должен пройти
        if (nearZ > farZ) {
            assertTrue(nearPassed, "Near Z should pass when nearZ > farZ");
            assertEquals(nearZ, zBuffer.get(50, 50), 1e-3f);
        } else {
            assertFalse(nearPassed, "Near Z should be rejected when nearZ <= farZ");
        }
    }
}
