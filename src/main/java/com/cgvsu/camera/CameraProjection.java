package com.cgvsu.camera;

import com.cgvsu.math.Matrix4f;

/**
 * Утилиты для создания матрицы проекции камеры.
 * Отвечает за преобразование координат из пространства камеры в пространство экрана.
 */
public class CameraProjection {
    
    /**
     * Создает матрицу перспективной проекции.
     * 
     * @param fov поле зрения (field of view) в радианах. Если значение > 10, 
     *             предполагается, что это градусы, и выполняется преобразование
     * @param aspectRatio соотношение сторон (ширина / высота)
     * @param nearPlane расстояние до ближней плоскости отсечения
     * @param farPlane расстояние до дальней плоскости отсечения
     * @return матрица перспективной проекции
     * @throws IllegalArgumentException если параметры некорректны
     */
    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = Matrix4f.zero();
        
        float fovRad = fov;
        if (fov > 10.0f) {
            fovRad = (float) Math.toRadians(fov);
        }
        
        float tanHalfFov = (float) Math.tan(fovRad * 0.5F);
        
        if (tanHalfFov < 1e-6f || aspectRatio < 1e-6f || Math.abs(farPlane - nearPlane) < 1e-6f) {
            throw new IllegalArgumentException("Invalid perspective parameters");
        }
        
        result.set(0, 0, 1.0f / (tanHalfFov * aspectRatio));
        result.set(1, 1, 1.0f / tanHalfFov);
        result.set(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.set(2, 3, 1.0f);
        result.set(3, 2, -2.0f * nearPlane * farPlane / (farPlane - nearPlane));
        
        return result;
    }
}

