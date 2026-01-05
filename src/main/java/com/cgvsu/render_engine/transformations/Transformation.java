package com.cgvsu.render_engine.transformations;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

/**
 * Интерфейс для аффинных преобразований
 */
public interface Transformation {
    /**
     * Получить матрицу преобразования
     * @return матрица 4x4
     */
    Matrix4f getMatrix();

    /**
     * Применить преобразование к точке
     * @param point исходная точка
     * @return преобразованная точка
     */
    default Vector3f apply(Vector3f point) {
        if (point == null) {
            throw new IllegalArgumentException("Point не может быть null");
        }
        Vector4f v4 = new Vector4f(point, 1.0f);
        Vector4f result = this.getMatrix().multiply(v4);
        
        // Нормализация однородных координат
        if (Math.abs(result.w) > 1e-7f) {
            return new Vector3f(result.x / result.w, result.y / result.w, result.z / result.w);
        }
        return new Vector3f(result.x, result.y, result.z);
    }
}

