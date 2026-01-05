package com.cgvsu.render_engine.transformations;

import com.cgvsu.math.Matrix4f;

/**
 * Преобразование переноса
 */
public class TranslationTransformation implements Transformation {
    private final float tx, ty, tz;

    public TranslationTransformation(float tx, float ty, float tz) {
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
    }

    @Override
    public Matrix4f getMatrix() {
        Matrix4f result = Matrix4f.identity();
        result.set(0, 3, tx);
        result.set(1, 3, ty);
        result.set(2, 3, tz);
        return result;
    }
}

