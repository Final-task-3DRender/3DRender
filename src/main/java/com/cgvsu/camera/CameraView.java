package com.cgvsu.camera;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

/**
 * Утилитный класс для создания матрицы вида (view matrix) камеры.
 * 
 * <p>Отвечает за преобразование координат из мирового пространства в пространство камеры.
 * Матрица вида позиционирует камеру в сцене, определяя направление взгляда.
 * 
 * <p>Использует алгоритм lookAt для создания матрицы вида на основе:
 * <ul>
 *   <li>Позиции камеры (eye)</li>
 *   <li>Точки, на которую смотрит камера (target)</li>
 *   <li>Вектора "вверх" (up) для определения ориентации камеры</li>
 * </ul>
 * 
 * <p>После применения view матрицы, камера находится в начале координат (0, 0, 0)
 * и смотрит вдоль отрицательной оси Z.
 * 
 * @author CGVSU Team
 * @version 1.0
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
        Vector3f forward = target.subtract(eye);
        float forwardLength = forward.length();
        if (forwardLength < 1e-6f) {
            return Matrix4f.identity();
        }
        forward = forward.normalize();
        
        Vector3f right = up.cross(forward);
        float rightLength = right.length();
        if (rightLength < 1e-6f) {
            if (Math.abs(forward.y) > 0.9f) {
                up = new Vector3f(0, 0, 1);
            } else {
                up = new Vector3f(0, 1, 0);
            }
            right = up.cross(forward);
            rightLength = right.length();
            if (rightLength < 1e-6f) {
                return Matrix4f.identity();
            }
        }
        right = right.normalize();
        
        Vector3f upCorrected = forward.cross(right).normalize();

        Matrix4f rotation = Matrix4f.identity();
        rotation.set(0, 0, right.x);
        rotation.set(0, 1, right.y);
        rotation.set(0, 2, right.z);
        rotation.set(1, 0, upCorrected.x);
        rotation.set(1, 1, upCorrected.y);
        rotation.set(1, 2, upCorrected.z);
        rotation.set(2, 0, -forward.x);
        rotation.set(2, 1, -forward.y);
        rotation.set(2, 2, -forward.z);

        Matrix4f translation = Matrix4f.identity();
        translation.set(0, 3, -eye.x);
        translation.set(1, 3, -eye.y);
        translation.set(2, 3, -eye.z);

        return rotation.multiply(translation);
    }
}

