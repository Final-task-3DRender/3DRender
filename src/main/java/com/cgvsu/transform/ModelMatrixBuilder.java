package com.cgvsu.transform;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.ModelTransform;

import static com.cgvsu.transform.AffineMatrixFactory.*;

/**
 * Построитель матрицы модели (Model Matrix).
 * Отвечает за создание матрицы преобразования из локальных координат в мировые.
 * 
 * Для векторов-столбцов порядок применения трансформаций: сначала масштаб (S), 
 * потом вращение (R), потом перенос (T). Матрица модели: M = T * R * S
 * 
 * При применении к вектору-столбцу v: M * v = T * R * S * v
 * Это означает, что сначала применяется S, потом R, потом T.
 */
public class ModelMatrixBuilder {

    /**
     * Создает единичную матрицу модели (без преобразований).
     * 
     * @return единичная матрица 4x4
     */
    public static Matrix4f identity() {
        return Matrix4f.identity();
    }

    /**
     * Создает матрицу модели из отдельных компонентов трансформации.
     * 
     * Для векторов-столбцов порядок: M = T * R * S
     * Это означает, что при применении к вектору сначала масштаб, потом вращение, потом перенос.
     * 
     * @param position позиция в мировом пространстве
     * @param rotation вращение в градусах (x, y, z)
     * @param scale масштаб по осям (x, y, z)
     * @return матрица модели 4x4
     */
    public static Matrix4f build(Vector3f position, Vector3f rotation, Vector3f scale) {
        if (position == null || rotation == null || scale == null) {
            throw new IllegalArgumentException("Position, rotation and scale cannot be null");
        }

        Matrix4f scaleMatrix = createScaleMatrix(scale);
        Matrix4f rotationMatrix = createRotationMatrix(rotation);
        Matrix4f translationMatrix = createTranslationMatrix(position);

        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    /**
     * Создает матрицу модели из объекта ModelTransform.
     * 
     * @param transform объект трансформации модели
     * @return матрица модели 4x4, или единичная матрица если transform == null
     */
    public static Matrix4f build(ModelTransform transform) {
        if (transform == null) {
            return identity();
        }

        return build(
            transform.getPosition(),
            transform.getRotation(),
            transform.getScale()
        );
    }
}

