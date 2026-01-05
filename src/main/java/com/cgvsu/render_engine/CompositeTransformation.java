package com.cgvsu.render_engine;

import com.cgvsu.render_engine.transformations.Transformation;
import com.cgvsu.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Композитное преобразование - комбинация нескольких преобразований
 */
public class CompositeTransformation implements Transformation {
    private final List<Transformation> transformations;
    private Matrix4f cachedMatrix;
    private boolean isDirty;

    public CompositeTransformation() {
        this.transformations = new ArrayList<>();
        this.cachedMatrix = Matrix4f.identity();
        this.isDirty = false;
    }

    public void add(Transformation transformation) {
        if (transformation == null) {
            throw new IllegalArgumentException("Transformation не может быть null");
        }
        this.transformations.add(transformation);
        this.isDirty = true;
    }

    private void updateCachedMatrix() {
        Matrix4f result = Matrix4f.identity();

        // Для векторов-столбцов: последнее добавленное преобразование применяется первым
        // Если список [S, T], то результат = T * S (translate применяется к уже масштабированной точке)
        for (Transformation transformation : transformations) {
            result = transformation.getMatrix().multiply(result);
        }
        
        this.cachedMatrix = result;
        this.isDirty = false;
    }

    @Override
    public Matrix4f getMatrix() {
        if (isDirty) {
            updateCachedMatrix();
        }
        return new Matrix4f(cachedMatrix);
    }
}

