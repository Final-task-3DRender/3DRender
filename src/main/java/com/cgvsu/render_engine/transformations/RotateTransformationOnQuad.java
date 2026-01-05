package com.cgvsu.render_engine.transformations;

import com.cgvsu.math.Matrix4f;

/**
 * Преобразование вращения через кватернионы
 * Примечание: упрощенная реализация без полной поддержки кватернионов
 */
public class RotateTransformationOnQuad implements Transformation {
    private final Matrix4f rotation;

    public RotateTransformationOnQuad(Axis axis, float angle) {
        this.rotation = createRotationMatrixFromQuaternion(axis, angle);
    }

    /**
     * Создает матрицу вращения из кватерниона
     * Упрощенная реализация - используем матричное представление
     */
    private Matrix4f createRotationMatrixFromQuaternion(Axis axis, float angle) {
        // Для упрощения используем ту же логику, что и RotateTransformation
        // В полной реализации здесь была бы конвертация кватерниона в матрицу
        Matrix4f result = Matrix4f.identity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        switch (axis) {
            case X:
                result.set(1, 1, cos);
                result.set(1, 2, -sin);
                result.set(2, 1, sin);
                result.set(2, 2, cos);
                break;
            case Y:
                result.set(0, 0, cos);
                result.set(0, 2, sin);
                result.set(2, 0, -sin);
                result.set(2, 2, cos);
                break;
            case Z:
                result.set(0, 0, cos);
                result.set(0, 1, -sin);
                result.set(1, 0, sin);
                result.set(1, 1, cos);
                break;
            default:
                throw new IllegalArgumentException("Unknown axis: " + axis);
        }
        return result;
    }

    @Override
    public Matrix4f getMatrix() {
        return new Matrix4f(rotation);
    }
}

