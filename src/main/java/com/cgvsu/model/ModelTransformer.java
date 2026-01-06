package com.cgvsu.model;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

import java.util.ArrayList;

/**
 * Утилитный класс для применения матрицы аффинного преобразования к модели.
 *
 * Используется для получения "преобразованной" версии модели перед сохранением:
 * - вершины перемножаются с матрицей M (с w = 1);
 * - нормали, если есть, преобразуются как направления (w = 0) без переноса.
 *
 * Исходный объект {@link Model} не изменяется, возвращается новая копия.
 */
public class ModelTransformer {

    /**
     * Применяет матрицу аффинного преобразования к модели и возвращает новую модель.
     *
     * @param source исходная модель (не должна быть null)
     * @param transform матрица 4x4 (не должна быть null)
     * @return новая модель с преобразованными вершинами (и нормалями, если заданы)
     */
    public static Model applyTransform(Model source, Matrix4f transform) {
        if (source == null) {
            throw new IllegalArgumentException("Source model не должна быть null");
        }
        if (transform == null) {
            throw new IllegalArgumentException("Matrix transform не должна быть null");
        }

        Model result = new Model();

        // Копируем полигоны и текстурные координаты как есть
        result.textureVertices = new ArrayList<>(source.textureVertices);
        result.polygons = new ArrayList<>(source.polygons);

        // Преобразуем вершины (w = 1)
        result.vertices = new ArrayList<>(source.vertices.size());
        for (Vector3f v : source.vertices) {
            if (v == null) {
                result.vertices.add(null);
                continue;
            }

            Vector4f v4 = new Vector4f(v, 1.0f);
            Vector4f transformed = transform.multiply(v4);

            // Нормализация однородных координат
            float w = transformed.w;
            if (Math.abs(w) > 1e-7f && w != 1.0f) {
                transformed = new Vector4f(
                        transformed.x / w,
                        transformed.y / w,
                        transformed.z / w,
                        1.0f
                );
            }

            result.vertices.add(new Vector3f(transformed.x, transformed.y, transformed.z));
        }

        // Преобразуем нормали, если есть (как направления: w = 0, без переноса)
        result.normals = new ArrayList<>(source.normals.size());
        for (Vector3f n : source.normals) {
            if (n == null) {
                result.normals.add(null);
                continue;
            }

            // Трактуем нормаль как вектор-направление (w = 0)
            Vector4f n4 = new Vector4f(n, 0.0f);
            Vector4f transformed = transform.multiply(n4);

            // w здесь либо 0, либо что-то, не влияющее на направление — нормализуем по длине
            Vector3f dir = new Vector3f(transformed.x, transformed.y, transformed.z).normalize();
            result.normals.add(dir);
        }

        return result;
    }
}


