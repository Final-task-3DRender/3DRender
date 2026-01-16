package com.cgvsu.render_engine;

import java.util.ArrayList;

import com.cgvsu.math.Vector2f;
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

/**
 * Основной класс рендер-движка для отрисовки 3D моделей.
 * 
 * <p>Реализует полный графический конвейер:
 * <ol>
 *   <li>Преобразование из локальных координат в мировые (Model Matrix)</li>
 *   <li>Преобразование из мировых координат в координаты камеры (View Matrix)</li>
 *   <li>Перспективная проекция (Projection Matrix)</li>
 *   <li>Преобразование в экранные координаты</li>
 *   <li>Растеризация треугольников с поддержкой Z-buffer и текстур</li>
 * </ol>
 * 
 * <p>Поддерживает:
 * <ul>
 *   <li>Z-buffer для правильной отрисовки глубины</li>
 *   <li>Backface culling для оптимизации</li>
 *   <li>Текстуры с perspective-correct interpolation</li>
 *   <li>Wireframe и filled режимы отрисовки</li>
 *   <li>Оптимизацию для больших моделей (пропуск полигонов)</li>
 * </ul>
 * 
 * <p>Использует векторы-столбцы. Порядок умножения матриц: P * V * M.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class RenderEngine {

    /**
     * Рендерит модель без трансформаций и с настройками по умолчанию.
     * 
     * @param graphicsContext контекст графики JavaFX для отрисовки
     * @param camera камера для определения точки зрения
     * @param mesh модель для отрисовки
     * @param width ширина области отрисовки
     * @param height высота области отрисовки
     */
    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        render(graphicsContext, camera, mesh, null, width, height);
    }

    /**
     * Рендерит модель с трансформациями и настройками по умолчанию.
     * 
     * @param graphicsContext контекст графики JavaFX для отрисовки
     * @param camera камера для определения точки зрения
     * @param mesh модель для отрисовки
     * @param transform трансформации модели (позиция, вращение, масштаб), может быть null
     * @param width ширина области отрисовки
     * @param height высота области отрисовки
     */
    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final ModelTransform transform,
            final int width,
            final int height)
    {
        if (graphicsContext == null) {
            throw new IllegalArgumentException("GraphicsContext cannot be null");
        }
        if (camera == null) {
            throw new IllegalArgumentException("Camera cannot be null");
        }
        if (mesh == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive, got: " + width + "x" + height);
        }
        RenderSettings defaultSettings = new RenderSettings();
        render(graphicsContext, camera, mesh, transform, width, height, defaultSettings);
    }

    /**
     * Рендерит модель с полным контролем над настройками.
     * 
     * <p>Основной метод рендеринга, выполняющий полный графический конвейер:
     * <ol>
     *   <li>Строит матрицы преобразования (Model, View, Projection)</li>
     *   <li>Комбинирует их в MVP матрицу (P * V * M для векторов-столбцов)</li>
     *   <li>Преобразует вершины через MVP матрицу</li>
     *   <li>Выполняет backface culling (если включен)</li>
     *   <li>Растеризует треугольники с поддержкой Z-buffer и текстур</li>
     * </ol>
     * 
     * @param graphicsContext контекст графики JavaFX для отрисовки
     * @param camera камера для определения точки зрения
     * @param mesh модель для отрисовки
     * @param transform трансформации модели (позиция, вращение, масштаб), может быть null
     * @param width ширина области отрисовки
     * @param height высота области отрисовки
     * @param settings настройки рендеринга (цвета, режимы отрисовки, Z-buffer и т.д.)
     */
    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final ModelTransform transform,
            final int width,
            final int height,
            final RenderSettings settings)
    {
        if (graphicsContext == null) {
            throw new IllegalArgumentException("GraphicsContext cannot be null");
        }
        if (camera == null) {
            throw new IllegalArgumentException("Camera cannot be null");
        }
        if (mesh == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive, got: " + width + "x" + height);
        }
        
        if (settings == null) {
            render(graphicsContext, camera, mesh, transform, width, height);
            return;
        }

        Matrix4f modelMatrix = ModelMatrixBuilder.build(transform);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = projectionMatrix
                .multiply(viewMatrix)
                .multiply(modelMatrix);

        Color triangleColor = settings.getFillColor();
        Color wireframeColor = settings.getWireframeColor();

        ZBuffer zBuffer = null;
        if (settings.isEnableZBuffer()) {
            zBuffer = new ZBuffer(width, height);
            zBuffer.clear();
        }

        final int nPolygons = mesh.getPolygonCount();
        
        int polygonSkip = 1;
        if (nPolygons > 10000) {
            polygonSkip = 2;
        } else if (nPolygons > 50000) {
            polygonSkip = 3;
        } else if (nPolygons > 100000) {
            polygonSkip = 4;
        }
        
        // Переиспользуем списки для всех полигонов вместо создания новых
        ArrayList<Point2f> resultPoints = new ArrayList<>();
        ArrayList<Float> resultZ = new ArrayList<>();
        ArrayList<Float> resultInvW = new ArrayList<>();
        ArrayList<Vector4f> transformedVertices = new ArrayList<>();
        
        for (int polygonInd = 0; polygonInd < nPolygons; polygonInd += polygonSkip) {
            Polygon polygon = mesh.getPolygon(polygonInd);
            final int nVerticesInPolygon = polygon.getVertexIndices().size();

            if (nVerticesInPolygon < 3) {
                continue;
            }

            // Очищаем списки для переиспользования
            resultPoints.clear();
            resultZ.clear();
            resultInvW.clear();
            transformedVertices.clear();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                int vertexIndex = polygon.getVertexIndices().get(vertexInPolygonInd);
                if (vertexIndex < 0 || vertexIndex >= mesh.getVertexCount()) {
                    continue; // Пропускаем некорректные индексы
                }
                Vector3f vertex = mesh.getVertex(vertexIndex);
                if (vertex == null) {
                    continue; // Пропускаем null вершины
                }

                Vector4f homogeneousVertex = new Vector4f(vertex, 1.0f);
                Vector4f transformed = modelViewProjectionMatrix.multiply(homogeneousVertex);
                
                float invW = 0.0f;
                if (Math.abs(transformed.w) > 1e-7f) {
                    invW = 1.0f / transformed.w;
                    transformed = transformed.divide(transformed.w);
                } else {
                    invW = 1e7f;
                }
                
                // Используем Z после перспективного деления (NDC координаты) для Z-buffer
                float ndcZ = transformed.z;
                
                transformedVertices.add(transformed);
                resultZ.add(ndcZ);
                resultInvW.add(invW);
                
                Point2f resultPoint = vertexToPoint(transformed, width, height);
                resultPoints.add(resultPoint);
            }

            if (settings.isEnableBackfaceCulling() && nVerticesInPolygon >= 3) {
                try {
                    if (!isFrontFacingByNormal(mesh, polygonInd, modelMatrix, viewMatrix)) {
                        continue;
                    }
                } catch (Exception e) {
                    // Если не удалось определить ориентацию полигона, пропускаем его
                    // Это может произойти при некорректных нормалях или вырожденных полигонах
                    continue;
                }
            }

            if (nVerticesInPolygon == 3) {
                float u0 = 0.0f, v0 = 0.0f, u1 = 0.0f, v1 = 0.0f, u2 = 0.0f, v2 = 0.0f;
                Texture texture = settings.isUseTexture() ? settings.getTexture() : null;
                if (texture != null) {
                    ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
                    if (textureIndices != null && textureIndices.size() >= 3) {
                        if (textureIndices.get(0) >= 0 && textureIndices.get(0) < mesh.getTextureVertexCount()) {
                            Vector2f tex0 = mesh.getTextureVertex(textureIndices.get(0));
                            u0 = tex0.x;
                            v0 = tex0.y;
                        }
                        if (textureIndices.get(1) >= 0 && textureIndices.get(1) < mesh.getTextureVertexCount()) {
                            Vector2f tex1 = mesh.getTextureVertex(textureIndices.get(1));
                            u1 = tex1.x;
                            v1 = tex1.y;
                        }
                        if (textureIndices.get(2) >= 0 && textureIndices.get(2) < mesh.getTextureVertexCount()) {
                            Vector2f tex2 = mesh.getTextureVertex(textureIndices.get(2));
                            u2 = tex2.x;
                            v2 = tex2.y;
                        }
                    }
                }
                
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
                if (settings.isShowFilled()) {
                    Texture texture = settings.isUseTexture() ? settings.getTexture() : null;
                    ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
                    
                    for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon - 1; ++vertexInPolygonInd) {
                        float u0 = 0.0f, v0 = 0.0f, u1 = 0.0f, v1 = 0.0f, u2 = 0.0f, v2 = 0.0f;
                        if (texture != null && textureIndices != null && textureIndices.size() >= vertexInPolygonInd + 2) {
                            if (textureIndices.get(0) >= 0 && textureIndices.get(0) < mesh.getTextureVertexCount()) {
                                Vector2f tex0 = mesh.getTextureVertex(textureIndices.get(0));
                                u0 = tex0.x;
                                v0 = tex0.y;
                            }
                            if (textureIndices.get(vertexInPolygonInd) >= 0 && textureIndices.get(vertexInPolygonInd) < mesh.getTextureVertexCount()) {
                                Vector2f tex1 = mesh.getTextureVertex(textureIndices.get(vertexInPolygonInd));
                                u1 = tex1.x;
                                v1 = tex1.y;
                            }
                            if (textureIndices.get(vertexInPolygonInd + 1) >= 0 && textureIndices.get(vertexInPolygonInd + 1) < mesh.getTextureVertexCount()) {
                                Vector2f tex2 = mesh.getTextureVertex(textureIndices.get(vertexInPolygonInd + 1));
                                u2 = tex2.x;
                                v2 = tex2.y;
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
        float edge1x = v1.x - v0.x;
        float edge1y = v1.y - v0.y;
        float edge2x = v2.x - v0.x;
        float edge2y = v2.y - v0.y;
        
        float crossZ = edge1x * edge2y - edge1y * edge2x;
        
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
        if (polygonIndex >= mesh.getPolygonCount()) {
            return true; // Если не можем проверить, считаем видимым
        }
        
        Polygon polygon = mesh.getPolygon(polygonIndex);
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();
        
        Vector3f normal;
        
        if (mesh.getNormalCount() > 0 && 
            normalIndices != null && !normalIndices.isEmpty() && 
            normalIndices.get(0) < mesh.getNormalCount()) {
            normal = mesh.getNormal(normalIndices.get(0));
        } else {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) {
                return true;
            }
            
            Vector3f v0 = mesh.getVertex(vertexIndices.get(0));
            Vector3f v1 = mesh.getVertex(vertexIndices.get(1));
            Vector3f v2 = mesh.getVertex(vertexIndices.get(2));
            
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            normal = edge1.cross(edge2);
            
            try {
                normal = normal.normalize();
            } catch (ArithmeticException e) {
                return true;
            }
        }
        
        Vector4f normal4 = new Vector4f(normal, 0.0f);
        Vector4f normalModel = modelMatrix.multiply(normal4);
        Vector4f normalView = viewMatrix.multiply(normalModel);
        
        Vector3f normalView3 = new Vector3f(normalView.x, normalView.y, normalView.z);
        try {
            normalView3 = normalView3.normalize();
        } catch (ArithmeticException e) {
            return true;
        }
        
        return normalView3.z < 0;
    }

}