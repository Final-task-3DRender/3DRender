package com.cgvsu.camera;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

/**
 * Утилиты для создания матрицы вида камеры.
 * Отвечает за преобразование координат из мирового пространства в пространство камеры.
 */
public class CameraView {
    
    /**
     * Создает матрицу вида (view matrix) для камеры, смотрящей на цель.
     * Использует стандартный up вектор (0, 1, 0).
     * 
     * @param eye позиция камеры
     * @param target точка, на которую смотрит камера
     * @return матрица вида
     */
    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    /**
     * Создает матрицу вида (view matrix) для камеры, смотрящей на цель.
     * 
     * @param eye позиция камеры
     * @param target точка, на которую смотрит камера
     * @param up вектор "вверх" для камеры
     * @return матрица вида
     */
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        // Вычисляем направление камеры (forward vector) - от камеры к цели
        Vector3f forward = target.subtract(eye);
        float forwardLength = forward.length();
        if (forwardLength < 1e-6f) {
            // Если камера находится в точке цели, возвращаем единичную матрицу
            return Matrix4f.identity();
        }
        forward = forward.normalize();
        
        // Для левосторонней системы координат (JavaFX):
        // Вычисляем правый вектор (right vector) = up × forward
        Vector3f right = up.cross(forward);
        float rightLength = right.length();
        if (rightLength < 1e-6f) {
            // Если forward и up коллинеарны, используем альтернативный up
            if (Math.abs(forward.y) > 0.9f) {
                up = new Vector3f(0, 0, 1);
            } else {
                up = new Vector3f(0, 1, 0);
            }
            right = up.cross(forward);
            rightLength = right.length();
            if (rightLength < 1e-6f) {
                // Если все еще коллинеарны, используем единичную матрицу
                return Matrix4f.identity();
            }
        }
        right = right.normalize();
        
        // Вычисляем истинный up вектор = forward × right (для левосторонней системы)
        Vector3f upCorrected = forward.cross(right).normalize();

        // Создаем матрицу поворота (rotation matrix)
        // В OpenGL/JavaFX используется column-major порядок, но здесь row-major
        Matrix4f rotation = Matrix4f.identity();
        // Первая строка - right vector (X ось камеры)
        rotation.set(0, 0, right.x);
        rotation.set(0, 1, right.y);
        rotation.set(0, 2, right.z);
        // Вторая строка - up vector (Y ось камеры)
        rotation.set(1, 0, upCorrected.x);
        rotation.set(1, 1, upCorrected.y);
        rotation.set(1, 2, upCorrected.z);
        // Третья строка - отрицательный forward vector (Z ось камеры, смотрит по -Z)
        rotation.set(2, 0, -forward.x);
        rotation.set(2, 1, -forward.y);
        rotation.set(2, 2, -forward.z);

        // Создаем матрицу переноса (перемещаем камеру в начало координат)
        Matrix4f translation = Matrix4f.identity();
        translation.set(0, 3, -eye.x);
        translation.set(1, 3, -eye.y);
        translation.set(2, 3, -eye.z);

        // Комбинируем: сначала поворот, потом перенос
        // View = Rotation * Translation
        return rotation.multiply(translation);
    }
}

