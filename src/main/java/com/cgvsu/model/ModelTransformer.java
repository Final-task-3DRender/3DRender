package com.cgvsu.model;

import com.cgvsu.exceptions.TransformationException;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

import java.util.ArrayList;

/**
 * Утилитный класс для применения матрицы аффинного преобразования к модели.
 * 
 * <p>Используется для получения "преобразованной" версии модели перед сохранением.
 * Применяет трансформации к вершинам и нормалям модели.
 * 
 * <p>Особенности преобразования:
 * <ul>
 *   <li>Вершины преобразуются как точки (w = 1) с нормализацией однородных координат</li>
 *   <li>Нормали преобразуются как векторы-направления (w = 0) без переноса, затем нормализуются</li>
 *   <li>Текстурные координаты и полигоны копируются без изменений</li>
 * </ul>
 * 
 * <p>Исходный объект {@link Model} не изменяется, возвращается новая копия.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class ModelTransformer {

    /**
     * Применяет матрицу аффинного преобразования к модели и возвращает новую модель.
     * 
     * <p>Преобразует все вершины и нормали модели согласно матрице трансформации.
     * Создает новую модель, исходная модель не изменяется.
     * 
     * @param source исходная модель (не должна быть null)
     * @param transform матрица 4x4 аффинного преобразования (не должна быть null)
     * @return новая модель с преобразованными вершинами и нормалями
     * @throws IllegalArgumentException если source или transform равны null
     */
    public static Model applyTransform(Model source, Matrix4f transform) {
        if (source == null) {
            throw new TransformationException("Source model cannot be null");
        }
        if (transform == null) {
            throw new TransformationException("Transform matrix cannot be null");
        }
        if (source.getVertexCount() == 0 && source.getPolygonCount() > 0) {
            throw new TransformationException("Source model has polygons but no vertices");
        }

        Model result = new Model();

        for (com.cgvsu.math.Vector2f tv : source.getTextureVertices()) {
            result.addTextureVertex(tv);
        }
        for (Polygon p : source.getPolygons()) {
            result.addPolygon(p);
        }

        for (Vector3f v : source.getVertices()) {
            if (v == null) {
                result.addVertex(new Vector3f(0, 0, 0));
                continue;
            }

            Vector4f v4 = new Vector4f(v, 1.0f);
            Vector4f transformed = transform.multiply(v4);

            float w = transformed.w;
            if (Math.abs(w) > 1e-7f && w != 1.0f) {
                transformed = new Vector4f(
                        transformed.x / w,
                        transformed.y / w,
                        transformed.z / w,
                        1.0f
                );
            }

            result.addVertex(new Vector3f(transformed.x, transformed.y, transformed.z));
        }

        for (Vector3f n : source.getNormals()) {
            if (n == null) {
                result.addNormal(new Vector3f(0, 0, 1));
                continue;
            }

            Vector4f n4 = new Vector4f(n, 0.0f);
            Vector4f transformed = transform.multiply(n4);

            Vector3f dir = new Vector3f(transformed.x, transformed.y, transformed.z).normalize();
            result.addNormal(dir);
        }

        return result;
    }
}


