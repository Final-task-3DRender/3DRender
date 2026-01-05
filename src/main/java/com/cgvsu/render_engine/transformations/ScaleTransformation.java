package com.cgvsu.render_engine.transformations;

import com.cgvsu.math.Matrix4f;

/**
 * Преобразование масштабирования
 */
public class ScaleTransformation implements Transformation {
    private final float sx, sy, sz;

    public ScaleTransformation(float sx, float sy, float sz) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
    }

    public ScaleTransformation(float uniformScale) {
        this(uniformScale, uniformScale, uniformScale);
    }

    @Override
    public Matrix4f getMatrix() {
        Matrix4f result = Matrix4f.identity();
        result.set(0, 0, sx);
        result.set(1, 1, sy);
        result.set(2, 2, sz);
        return result;
    }
}

