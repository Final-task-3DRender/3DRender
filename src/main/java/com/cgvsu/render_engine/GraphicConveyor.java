package com.cgvsu.render_engine;

import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector4f;

/**
 * Утилиты для конвертации координат в графическом конвейере.
 * Отвечает за преобразование координат из пространства проекции в экранные координаты.
 * 
 * Методы создания матриц аффинных преобразований перенесены в пакет transform:
 * - AffineMatrixFactory - создание базовых матриц (translation, rotation, scale)
 * - ModelMatrixBuilder - построение модели матрицы
 * 
 * Методы создания матриц камеры находятся в пакете camera:
 * - CameraView.lookAt() - для создания матрицы вида
 * - CameraProjection.perspective() - для создания матрицы проекции
 */
public class GraphicConveyor {

    /**
     * Преобразует вершину из пространства проекции в экранные координаты.
     * 
     * @param vertex вершина в пространстве проекции (после MVP преобразования и перспективного деления)
     * @param width ширина экрана
     * @param height высота экрана
     * @return точка в экранных координатах
     */
    public static Point2f vertexToPoint(final Vector4f vertex, final int width, final int height) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }
        
        float x = vertex.x * width / 2.0f + width / 2.0f;
        float y = -vertex.y * height / 2.0f + height / 2.0f;
        return new Point2f(x, y);
    }
}
