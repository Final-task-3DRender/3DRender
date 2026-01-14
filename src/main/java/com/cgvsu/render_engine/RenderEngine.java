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
import com.cgvsu.model.Polygon;
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

        // Создаем Z-buffer только если он включен в настройках
        ZBuffer zBuffer = null;
        if (settings.isEnableZBuffer()) {
            zBuffer = new ZBuffer(width, height);
            zBuffer.clear();
        }

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
            Polygon polygon = mesh.polygons.get(polygonInd);
            final int nVerticesInPolygon = polygon.getVertexIndices().size();

            // Полигоны должны быть триангулированы (3 вершины)
            if (nVerticesInPolygon < 3) {
                continue;
            }

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            ArrayList<Float> resultZ = new ArrayList<>();
            ArrayList<Float> resultInvW = new ArrayList<>(); // 1/w для perspective-correct interpolation
            ArrayList<Vector4f> transformedVertices = new ArrayList<>(); // Для backface culling
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(polygon.getVertexIndices().get(vertexInPolygonInd));

                // Преобразуем вершину в однородные координаты (w=1)
                Vector4f homogeneousVertex = new Vector4f(vertex, 1.0f);
                
                // Применяем матричные преобразования (получаем clip space)
                Vector4f transformed = modelViewProjectionMatrix.multiply(homogeneousVertex);
                
                // КРИТИЧЕСКИ ВАЖНО: Сохраняем clip space Z ДО perspective divide для Z-buffer
                // Clip space Z используется для правильного сравнения глубины
                float clipZ = transformed.z;
                
                // КРИТИЧЕСКИ ВАЖНО: Сохраняем 1/w ДО perspective divide для perspective-correct interpolation
                float invW = 0.0f;
                if (Math.abs(transformed.w) > 1e-7f) {
                    invW = 1.0f / transformed.w;
                    transformed = transformed.divide(transformed.w);
                } else {
                    // Если w слишком мал, используем большое значение для 1/w
                    invW = 1e7f;
                }
                
                // Сохраняем преобразованную вершину для backface culling
                transformedVertices.add(transformed);
                
                // Сохраняем clip space Z для Z-buffer (НЕ NDC Z!)
                // Clip space Z правильно работает с сравнением: большие значения = ближе к камере
                resultZ.add(clipZ);
                resultInvW.add(invW);
                
                // Преобразуем в экранные координаты
                Point2f resultPoint = vertexToPoint(transformed, width, height);
                resultPoints.add(resultPoint);
            }

            // Backface culling: проверяем, видна ли передняя грань треугольника
            if (settings.isEnableBackfaceCulling() && nVerticesInPolygon >= 3) {
                try {
                    // Используем проверку по нормали в пространстве камеры (более надежно)
                    if (!isFrontFacingByNormal(mesh, polygonInd, modelMatrix, viewMatrix)) {
                        continue; // Пропускаем заднюю грань
                    }
                } catch (Exception e) {
                    // В случае ошибки пропускаем проверку и рендерим треугольник
                    // Это защита от возможных проблем с вычислениями
                }
            }

            // Рендерим треугольник (модель уже триангулирована, поэтому nVerticesInPolygon = 3)
            if (nVerticesInPolygon == 3) {
                // Извлекаем UV координаты для вершин треугольника
                float u0 = 0.0f, v0 = 0.0f, u1 = 0.0f, v1 = 0.0f, u2 = 0.0f, v2 = 0.0f;
                Texture texture = settings.isUseTexture() ? settings.getTexture() : null;
                if (texture != null) {
                    ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
                    if (textureIndices != null && textureIndices.size() >= 3) {
                        if (textureIndices.get(0) >= 0 && textureIndices.get(0) < mesh.textureVertices.size()) {
                            u0 = mesh.textureVertices.get(textureIndices.get(0)).x;
                            v0 = mesh.textureVertices.get(textureIndices.get(0)).y;
                        }
                        if (textureIndices.get(1) >= 0 && textureIndices.get(1) < mesh.textureVertices.size()) {
                            u1 = mesh.textureVertices.get(textureIndices.get(1)).x;
                            v1 = mesh.textureVertices.get(textureIndices.get(1)).y;
                        }
                        if (textureIndices.get(2) >= 0 && textureIndices.get(2) < mesh.textureVertices.size()) {
                            u2 = mesh.textureVertices.get(textureIndices.get(2)).x;
                            v2 = mesh.textureVertices.get(textureIndices.get(2)).y;
                        }
                    }
                }
                
                // Заливаем треугольник (если включено)
                if (settings.isShowFilled()) {
                    TriangleRasterizer.fillTriangle(
                        graphicsContext,
                        zBuffer,
                        texture,
                        resultPoints.get(0).x, resultPoints.get(0).y, resultZ.get(0), resultInvW.get(0), u0, v0, triangleColor,
                        resultPoints.get(1).x, resultPoints.get(1).y, resultZ.get(1), resultInvW.get(1), u1, v1, triangleColor,
                        resultPoints.get(2).x, resultPoints.get(2).y, resultZ.get(2), resultInvW.get(2), u2, v2, triangleColor
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
                    Texture texture = settings.isUseTexture() ? settings.getTexture() : null;
                    ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
                    
                    for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon - 1; ++vertexInPolygonInd) {
                        // Извлекаем UV координаты для вершин треугольника
                        float u0 = 0.0f, v0 = 0.0f, u1 = 0.0f, v1 = 0.0f, u2 = 0.0f, v2 = 0.0f;
                        if (texture != null && textureIndices != null && textureIndices.size() >= vertexInPolygonInd + 2) {
                            if (textureIndices.get(0) >= 0 && textureIndices.get(0) < mesh.textureVertices.size()) {
                                u0 = mesh.textureVertices.get(textureIndices.get(0)).x;
                                v0 = mesh.textureVertices.get(textureIndices.get(0)).y;
                            }
                            if (textureIndices.get(vertexInPolygonInd) >= 0 && textureIndices.get(vertexInPolygonInd) < mesh.textureVertices.size()) {
                                u1 = mesh.textureVertices.get(textureIndices.get(vertexInPolygonInd)).x;
                                v1 = mesh.textureVertices.get(textureIndices.get(vertexInPolygonInd)).y;
                            }
                            if (textureIndices.get(vertexInPolygonInd + 1) >= 0 && textureIndices.get(vertexInPolygonInd + 1) < mesh.textureVertices.size()) {
                                u2 = mesh.textureVertices.get(textureIndices.get(vertexInPolygonInd + 1)).x;
                                v2 = mesh.textureVertices.get(textureIndices.get(vertexInPolygonInd + 1)).y;
                            }
                        }
                        
                        TriangleRasterizer.fillTriangle(
                            graphicsContext,
                            zBuffer,
                            texture,
                            resultPoints.get(0).x, resultPoints.get(0).y, resultZ.get(0), resultInvW.get(0), u0, v0, triangleColor,
                            resultPoints.get(vertexInPolygonInd).x, resultPoints.get(vertexInPolygonInd).y, resultZ.get(vertexInPolygonInd), resultInvW.get(vertexInPolygonInd), u1, v1, triangleColor,
                            resultPoints.get(vertexInPolygonInd + 1).x, resultPoints.get(vertexInPolygonInd + 1).y, resultZ.get(vertexInPolygonInd + 1), resultInvW.get(vertexInPolygonInd + 1), u2, v2, triangleColor
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

    /**
     * Проверяет, является ли треугольник передней гранью (front-facing).
     * Использует проверку порядка вершин (winding order) в экранных координатах.
     * 
     * @param v0 первая вершина треугольника в NDC пространстве
     * @param v1 вторая вершина треугольника в NDC пространстве
     * @param v2 третья вершина треугольника в NDC пространстве
     * @return true если треугольник front-facing (видим), false если back-facing (невидим)
     */
    private static boolean isFrontFacing(Vector4f v0, Vector4f v1, Vector4f v2) {
        // Вычисляем векторы двух сторон треугольника в экранных координатах
        float edge1x = v1.x - v0.x;
        float edge1y = v1.y - v0.y;
        float edge2x = v2.x - v0.x;
        float edge2y = v2.y - v0.y;
        
        // Вычисляем Z-компоненту векторного произведения (cross product)
        // Это эквивалентно определителю матрицы 2x2 для проверки порядка вершин
        float crossZ = edge1x * edge2y - edge1y * edge2x;
        
        // В нашей системе координат после перспективной проекции:
        // Проверяем порядок вершин (winding order) в экранных координатах
        // Из-за инверсии Y в экранных координатах (GraphicConveyor.vertexToPoint):
        // crossZ < 0 означает front-facing (видимый треугольник)
        // crossZ > 0 означает back-facing (невидимый треугольник)
        // Если crossZ == 0, треугольник вырожденный (все вершины на одной линии) - считаем видимым
        return crossZ < 0;
    }

    /**
     * Проверяет, является ли треугольник передней гранью, используя нормаль в пространстве камеры.
     * Использует сохраненные нормали из модели (пересчитанные NormalCalculator), что более надежно.
     * 
     * @param mesh модель
     * @param polygonIndex индекс полигона
     * @param modelMatrix матрица модели
     * @param viewMatrix матрица вида
     * @return true если треугольник front-facing (видим), false если back-facing (невидим)
     */
    private static boolean isFrontFacingByNormal(
            Model mesh, int polygonIndex, Matrix4f modelMatrix, Matrix4f viewMatrix) {
        if (mesh.polygons == null || polygonIndex >= mesh.polygons.size()) {
            return true; // Если не можем проверить, считаем видимым
        }
        
        Polygon polygon = mesh.polygons.get(polygonIndex);
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();
        
        Vector3f normal;
        
        // Пытаемся использовать сохраненную нормаль из модели (пересчитанную NormalCalculator)
        if (mesh.normals != null && !mesh.normals.isEmpty() && 
            normalIndices != null && !normalIndices.isEmpty() && 
            normalIndices.get(0) < mesh.normals.size()) {
            normal = mesh.normals.get(normalIndices.get(0));
        } else {
            // Если нормали нет, вычисляем ее из вершин треугольника
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) {
                return true;
            }
            
            Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
            Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
            Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));
            
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            normal = edge1.cross(edge2);
            
            try {
                normal = normal.normalize();
            } catch (ArithmeticException e) {
                return true; // Вырожденный треугольник, считаем видимым
            }
        }
        
        // Преобразуем нормаль в пространство камеры
        // Нормаль - это вектор направления, поэтому w=0 (не применяется перенос)
        Vector4f normal4 = new Vector4f(normal, 0.0f);
        // Сначала применяем матрицу модели, затем матрицу вида
        Vector4f normalModel = modelMatrix.multiply(normal4);
        Vector4f normalView = viewMatrix.multiply(normalModel);
        
        // Нормализуем нормаль в пространстве камеры (на случай, если матрицы изменили длину)
        Vector3f normalView3 = new Vector3f(normalView.x, normalView.y, normalView.z);
        try {
            normalView3 = normalView3.normalize();
        } catch (ArithmeticException e) {
            return true; // Если не можем нормализовать, считаем видимым
        }
        
        // В пространстве камеры камера смотрит по направлению -Z (вперед)
        // Если нормаль направлена к камере, треугольник front-facing (видим)
        // Если нормаль направлена от камеры, треугольник back-facing (невидим)
        // Пробуем оба варианта, так как направление может зависеть от системы координат
        // Сначала пробуем: если Z < 0, нормаль направлена к камере (front-facing)
        return normalView3.z < 0;
    }

}