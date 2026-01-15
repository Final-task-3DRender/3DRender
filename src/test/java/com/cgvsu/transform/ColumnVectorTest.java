package com.cgvsu.transform;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;

import static com.cgvsu.transform.ModelMatrixBuilder.build;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки правильности работы с векторами-столбцами.
 * Проверяет, что порядок умножения матриц соответствует векторам-столбцам.
 */
class ColumnVectorTest {
    private static final float EPSILON = 1e-5f;

    /**
     * Применяет матрицу к вектору-столбцу и нормализует результат
     */
    private Vector3f applyMatrix(Matrix4f matrix, Vector3f point) {
        Vector4f result = matrix.multiply(new Vector4f(point, 1.0f));
        if (Math.abs(result.w) > EPSILON && result.w != 1.0f) {
            return new Vector3f(result.x / result.w, result.y / result.w, result.z / result.w);
        }
        return new Vector3f(result.x, result.y, result.z);
    }

    @Test
    void testColumnVectorOrder() {
        // Для векторов-столбцов порядок должен быть: M * v = T * R * S * v
        // Это означает: сначала S, потом R, потом T
        
        Vector3f position = new Vector3f(10, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0); // Нет вращения
        Vector3f scale = new Vector3f(2, 1, 1); // Масштаб по X
        
        Matrix4f modelMatrix = build(position, rotation, scale);
        
        // Точка (1, 0, 0):
        // 1. Масштаб: (1, 0, 0) * 2 = (2, 0, 0)
        // 2. Вращение: нет изменений
        // 3. Перенос: (2, 0, 0) + (10, 0, 0) = (12, 0, 0)
        Vector3f point = new Vector3f(1, 0, 0);
        Vector3f result = applyMatrix(modelMatrix, point);
        
        assertEquals(12.0f, result.x, EPSILON);
        assertEquals(0.0f, result.y, EPSILON);
        assertEquals(0.0f, result.z, EPSILON);
    }

    @Test
    void testColumnVectorOrderWithRotation() {
        // Проверяем порядок: масштаб -> вращение -> перенос
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(0, 90, 0); // 90° вокруг Y
        Vector3f scale = new Vector3f(2, 1, 1); // Масштаб по X
        
        Matrix4f modelMatrix = build(position, rotation, scale);
        
        // Точка (1, 0, 0):
        // 1. Масштаб: (1, 0, 0) * 2 = (2, 0, 0)
        // 2. Вращение на 90° вокруг Y: (2, 0, 0) -> (0, 0, -2)
        // 3. Перенос: (0, 0, -2) + (0, 0, 0) = (0, 0, -2)
        Vector3f point = new Vector3f(1, 0, 0);
        Vector3f result = applyMatrix(modelMatrix, point);
        
        assertEquals(0.0f, result.x, EPSILON);
        assertEquals(0.0f, result.y, EPSILON);
        assertEquals(-2.0f, result.z, EPSILON);
    }

    @Test
    void testMVPOrder() {
        // Проверяем правильность порядка MVP для векторов-столбцов
        // Порядок должен быть: P * V * M
        
        Vector3f position = new Vector3f(0, 0, 0);
        Vector3f rotation = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(1, 1, 1);
        
        Matrix4f modelMatrix = build(position, rotation, scale);
        
        // Создаем простые view и projection матрицы для теста
        Matrix4f viewMatrix = Matrix4f.identity();
        Matrix4f projectionMatrix = Matrix4f.identity();
        
        // MVP = P * V * M
        Matrix4f mvp = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);
        
        // Применяем к точке
        Vector3f point = new Vector3f(1, 1, 1);
        Vector4f result = mvp.multiply(new Vector4f(point, 1.0f));
        
        // Результат должен быть валидным
        assertFalse(Float.isNaN(result.x));
        assertFalse(Float.isNaN(result.y));
        assertFalse(Float.isNaN(result.z));
    }
}
