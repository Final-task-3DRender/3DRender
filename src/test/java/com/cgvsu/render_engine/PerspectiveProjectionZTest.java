package com.cgvsu.render_engine;

import com.cgvsu.camera.Camera;
import com.cgvsu.camera.CameraProjection;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки преобразования Z-координаты после перспективной проекции.
 * Помогает понять, какое направление Z используется в нашей системе координат.
 */
public class PerspectiveProjectionZTest {

    @Test
    public void testPerspectiveProjectionZTransformation() {
        // Создаем камеру с параметрами, аналогичными реальной
        float near = 0.01f;
        float far = 100.0f;
        float fov = (float) Math.toRadians(60.0);
        
        Matrix4f projection = CameraProjection.perspective(fov, 1.0f, near, far);
        
        // Тестируем преобразование Z для ближней и дальней плоскостей
        // В пространстве камеры после lookAt: Z отрицательное для объектов перед камерой
        // Объекты перед камерой имеют отрицательную Z в пространстве камеры
        Vector4f nearVertex = new Vector4f(0, 0, -near, 1.0f); // Ближняя плоскость (отрицательная Z)
        Vector4f farVertex = new Vector4f(0, 0, -far, 1.0f);  // Дальняя плоскость (отрицательная Z)
        
        Vector4f nearTransformed = projection.multiply(nearVertex);
        Vector4f farTransformed = projection.multiply(farVertex);
        
        // Перспективное деление
        if (Math.abs(nearTransformed.w) > 1e-7f) {
            nearTransformed = nearTransformed.divide(nearTransformed.w);
        }
        if (Math.abs(farTransformed.w) > 1e-7f) {
            farTransformed = farTransformed.divide(farTransformed.w);
        }
        
        float nearZ = nearTransformed.z;
        float farZ = farTransformed.z;
        
        System.out.println("Near plane Z (camera space: " + near + ") -> NDC: " + nearZ);
        System.out.println("Far plane Z (camera space: " + far + ") -> NDC: " + farZ);
        
        // В нашей системе координат после перспективной проекции:
        // Ближние объекты имеют БОЛЬШИЕ значения Z (положительные, большие числа)
        // Дальние объекты имеют МЕНЬШИЕ значения Z (отрицательные или маленькие)
        // Проверяем, что nearZ > farZ (ближняя плоскость имеет большее Z)
        assertTrue(nearZ > farZ, 
            "Near plane Z (" + nearZ + ") should be greater than far plane Z (" + farZ + ")");
        
        // Проверяем, что значения в разумном диапазоне [-1, 1]
        assertTrue(Math.abs(nearZ) <= 1.1f, "Near Z should be in range [-1, 1], got: " + nearZ);
        assertTrue(Math.abs(farZ) <= 1.1f, "Far Z should be in range [-1, 1], got: " + farZ);
    }

    @Test
    public void testZBufferWithProjectedZ() {
        // Тест Z-buffer с реальными значениями после перспективной проекции
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        // Симулируем значения Z после перспективной проекции
        // В нашей системе: Ближние объекты имеют БОЛЬШИЕ положительные значения Z
        // Дальние объекты имеют МЕНЬШИЕ (отрицательные) значения Z
        
        float nearZ = 100.0f;  // Ближний объект (большое положительное значение)
        float farZ = -50.0f;   // Дальний объект (отрицательное значение)
        
        // Рисуем дальний объект первым
        assertTrue(zBuffer.testAndSet(50, 50, farZ));
        assertEquals(farZ, zBuffer.get(50, 50), 1e-6f);
        
        // Пытаемся нарисовать ближний объект - должен пройти (nearZ > farZ)
        assertTrue(zBuffer.testAndSet(50, 50, nearZ));
        assertEquals(nearZ, zBuffer.get(50, 50), 1e-6f);
        
        // Пытаемся снова нарисовать дальний объект - должен быть отклонен
        assertFalse(zBuffer.testAndSet(50, 50, farZ));
        assertEquals(nearZ, zBuffer.get(50, 50), 1e-6f);
    }
}
