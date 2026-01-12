package com.cgvsu.render_engine;

import java.util.ArrayList;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelTransform;
import com.cgvsu.camera.Camera;
import com.cgvsu.transform.ModelMatrixBuilder;
import static com.cgvsu.render_engine.GraphicConveyor.vertexToPoint;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        render(graphicsContext, camera, mesh, null, width, height);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final ModelTransform transform,
            final int width,
            final int height)
    {
        RenderSettings defaultSettings = new RenderSettings();
        render(graphicsContext, camera, mesh, transform, width, height, defaultSettings);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final ModelTransform transform,
            final int width,
            final int height,
            final RenderSettings settings)
    {
        if (settings == null) {
            render(graphicsContext, camera, mesh, transform, width, height);
            return;
        }

        // Создаем матрицу модели (преобразование из локальных в мировые координаты)
        Matrix4f modelMatrix = ModelMatrixBuilder.build(transform);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // Для векторов-столбцов порядок: P * V * M
        Matrix4f modelViewProjectionMatrix = projectionMatrix
                .multiply(viewMatrix)
                .multiply(modelMatrix);

        // Цвет для заливки треугольников из настроек
        Color triangleColor = settings.getFillColor();
        Color wireframeColor = settings.getWireframeColor();

        final int nPolygons = mesh.polygons.size();
        
        // Оптимизация для больших моделей: пропускаем каждый N-й полигон
        // Это снижает нагрузку на рендеринг для моделей с большим количеством полигонов
        int polygonSkip = 1;
        if (nPolygons > 10000) {
            polygonSkip = 2; // Пропускаем каждый второй полигон
        } else if (nPolygons > 50000) {
            polygonSkip = 3; // Пропускаем каждые два из трех полигонов
        } else if (nPolygons > 100000) {
            polygonSkip = 4; // Пропускаем каждые три из четырех полигонов
        }
        
        for (int polygonInd = 0; polygonInd < nPolygons; polygonInd += polygonSkip) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            // Полигоны должны быть триангулированы (3 вершины)
            if (nVerticesInPolygon < 3) {
                continue;
            }

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                // Преобразуем вершину в однородные координаты (w=1)
                Vector4f homogeneousVertex = new Vector4f(vertex, 1.0f);
                
                // Применяем матричные преобразования
                Vector4f transformed = modelViewProjectionMatrix.multiply(homogeneousVertex);
                
                // Нормализуем однородные координаты
                if (Math.abs(transformed.w) > 1e-7f) {
                    transformed = transformed.divide(transformed.w);
                }
                
                // Преобразуем в экранные координаты
                Point2f resultPoint = vertexToPoint(transformed, width, height);
                resultPoints.add(resultPoint);
            }

            // Рендерим треугольник (модель уже триангулирована, поэтому nVerticesInPolygon = 3)
            if (nVerticesInPolygon == 3) {
                // Заливаем треугольник (если включено)
                if (settings.isShowFilled()) {
                    TriangleRasterizer.fillTriangle(
                        graphicsContext,
                        resultPoints.get(0).x, resultPoints.get(0).y, triangleColor,
                        resultPoints.get(1).x, resultPoints.get(1).y, triangleColor,
                        resultPoints.get(2).x, resultPoints.get(2).y, triangleColor
                    );
                }
                
                // Добавляем обводку (wireframe) (если включено)
                if (settings.isShowWireframe()) {
                    graphicsContext.setStroke(wireframeColor);
                    graphicsContext.strokeLine(
                        resultPoints.get(0).x, resultPoints.get(0).y,
                        resultPoints.get(1).x, resultPoints.get(1).y
                    );
                    graphicsContext.strokeLine(
                        resultPoints.get(1).x, resultPoints.get(1).y,
                        resultPoints.get(2).x, resultPoints.get(2).y
                    );
                    graphicsContext.strokeLine(
                        resultPoints.get(2).x, resultPoints.get(2).y,
                        resultPoints.get(0).x, resultPoints.get(0).y
                    );
                }
            } else {
                // Для полигонов с более чем 3 вершинами (на случай, если триангуляция не сработала)
                // используем заливку треугольников через fan-триангуляцию
                if (settings.isShowFilled()) {
                    for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon - 1; ++vertexInPolygonInd) {
                        TriangleRasterizer.fillTriangle(
                            graphicsContext,
                            resultPoints.get(0).x, resultPoints.get(0).y, triangleColor,
                            resultPoints.get(vertexInPolygonInd).x, resultPoints.get(vertexInPolygonInd).y, triangleColor,
                            resultPoints.get(vertexInPolygonInd + 1).x, resultPoints.get(vertexInPolygonInd + 1).y, triangleColor
                        );
                    }
                }
                
                // Добавляем обводку для полигона (если включено)
                if (settings.isShowWireframe()) {
                    graphicsContext.setStroke(wireframeColor);
                    for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                        graphicsContext.strokeLine(
                            resultPoints.get(vertexInPolygonInd - 1).x, resultPoints.get(vertexInPolygonInd - 1).y,
                            resultPoints.get(vertexInPolygonInd).x, resultPoints.get(vertexInPolygonInd).y
                        );
                    }
                    graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x, resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x, resultPoints.get(0).y
                    );
                }
            }
        }
    }
}