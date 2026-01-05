package com.cgvsu.render_engine.transformations;

import com.cgvsu.math.Matrix4f;

/**
 * Сохранение состояния трансформации
 */
public class SaveTransformation implements Transformation {
    private final Matrix4f savedMatrix;

    public SaveTransformation(Matrix4f matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix не может быть null");
        }
        this.savedMatrix = new Matrix4f(matrix);
    }

    @Override
    public Matrix4f getMatrix() {
        return new Matrix4f(savedMatrix);
    }
}

