package com.cgvsu.transform;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

/**
 * Фабрика для создания матриц аффинных преобразований.
 * Отвечает за создание базовых матриц: переноса, вращения и масштабирования.
 */
public class AffineMatrixFactory {

    /**
     * Создает матрицу переноса (translation matrix).
     * 
     * @param translation вектор переноса
     * @return матрица переноса 4x4
     */
    public static Matrix4f createTranslationMatrix(Vector3f translation) {
        if (translation == null) {
            throw new IllegalArgumentException("Translation vector cannot be null");
        }
        
        Matrix4f result = Matrix4f.identity();
        result.set(0, 3, translation.x);
        result.set(1, 3, translation.y);
        result.set(2, 3, translation.z);
        return result;
    }

    /**
     * Создает матрицу вращения вокруг оси X.
     * 
     * @param angleRad угол вращения в радианах
     * @return матрица вращения вокруг оси X
     */
    public static Matrix4f createRotationXMatrix(float angleRad) {
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        
        result.set(1, 1, cos);
        result.set(1, 2, -sin);
        result.set(2, 1, sin);
        result.set(2, 2, cos);
        
        return result;
    }

    /**
     * Создает матрицу вращения вокруг оси Y.
     * 
     * @param angleRad угол вращения в радианах
     * @return матрица вращения вокруг оси Y
     */
    public static Matrix4f createRotationYMatrix(float angleRad) {
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        
        result.set(0, 0, cos);
        result.set(0, 2, sin);
        result.set(2, 0, -sin);
        result.set(2, 2, cos);
        
        return result;
    }

    /**
     * Создает матрицу вращения вокруг оси Z.
     * 
     * @param angleRad угол вращения в радианах
     * @return матрица вращения вокруг оси Z
     */
    public static Matrix4f createRotationZMatrix(float angleRad) {
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        
        result.set(0, 0, cos);
        result.set(0, 1, -sin);
        result.set(1, 0, sin);
        result.set(1, 1, cos);
        
        return result;
    }

    /**
     * Создает матрицу вращения по осям X, Y, Z (в градусах).
     * Порядок вращения: сначала вокруг X, потом Y, потом Z.
     * 
     * @param rotationDegrees вектор углов вращения в градусах (x, y, z)
     * @return комбинированная матрица вращения
     */
    public static Matrix4f createRotationMatrix(Vector3f rotationDegrees) {
        if (rotationDegrees == null) {
            throw new IllegalArgumentException("Rotation vector cannot be null");
        }
        
        float rx = (float) Math.toRadians(rotationDegrees.x);
        float ry = (float) Math.toRadians(rotationDegrees.y);
        float rz = (float) Math.toRadians(rotationDegrees.z);

        Matrix4f rotX = createRotationXMatrix(rx);
        Matrix4f rotY = createRotationYMatrix(ry);
        Matrix4f rotZ = createRotationZMatrix(rz);

        // Порядок: сначала X, потом Y, потом Z
        return rotX.multiply(rotY).multiply(rotZ);
    }

    /**
     * Создает матрицу масштабирования (scale matrix).
     * 
     * @param scale вектор масштабирования по осям (x, y, z)
     * @return матрица масштабирования 4x4
     */
    public static Matrix4f createScaleMatrix(Vector3f scale) {
        if (scale == null) {
            throw new IllegalArgumentException("Scale vector cannot be null");
        }
        
        Matrix4f result = Matrix4f.identity();
        result.set(0, 0, scale.x);
        result.set(1, 1, scale.y);
        result.set(2, 2, scale.z);
        return result;
    }
}

