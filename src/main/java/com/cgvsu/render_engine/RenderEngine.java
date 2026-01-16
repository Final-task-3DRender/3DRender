package com.cgvsu.render_engine;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

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
import com.cgvsu.triangulation.EarCuttingTriangulator;
import com.cgvsu.triangulation.Triangulator;
import static com.cgvsu.render_engine.GraphicConveyor.vertexToPoint;

/**
 * Основной класс рендер-движка для отрисовки 3D моделей.
 * 
 * <p>Реализует полный графический конвейер согласно теории из методички:
 * <ol>
 *   <li>Преобразование из локальных координат в мировые (Model Matrix) - аффинные преобразования</li>
 *   <li>Преобразование из мировых координат в координаты камеры (View Matrix) - LookAt алгоритм</li>
 *   <li>Перспективная проекция (Projection Matrix) - перспективное преобразование</li>
 *   <li>Преобразование в экранные координаты - NDC к экранным координатам</li>
 *   <li>Растеризация треугольников с поддержкой Z-buffer и текстур</li>
 * </ol>
 * 
 * <p>Поддерживает режимы отрисовки (задание для третьего человека):
 * <ul>
 *   <li>Растеризация полигонов одним цветом с Z-buffer</li>
 *   <li>Наложение текстуры (текстура загружается через меню)</li>
 *   <li>Wireframe режим (рисование полигональной сетки)</li>
 *   <li>Filled режим (заливка треугольников)</li>
 * </ul>
 * 
 * <p>Технические особенности:
 * <ul>
 *   <li>Z-buffer для правильной отрисовки глубины (задние полигоны не вылазят на передние)</li>
 *   <li>Backface culling для оптимизации (опционально)</li>
 *   <li>Текстуры с perspective-correct interpolation (перспективно-корректная интерполяция UV)</li>
 *   <li>Динамическая триангуляция полигонов (Ear-Cutting Triangulator)</li>
 *   <li>Пересчет нормалей при загрузке модели (всегда, даже если есть в файле)</li>
 *   <li>Оптимизация для больших моделей (пропуск полигонов при большом количестве)</li>
 * </ul>
 * 
 * <p>Использует векторы-столбцы. Порядок умножения матриц: P * V * M.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class RenderEngine {

    private static final Logger logger = Logger.getLogger(RenderEngine.class.getName());
    
    // ThreadLocal для переиспользования ZBuffer между кадрами
    private static final ThreadLocal<ZBuffer> zBufferCache = new ThreadLocal<>();
    
    // Кэш для MVP матрицы с проверкой параметров
    private static final class MatrixCache {
        Matrix4f mvpMatrix;
        // Параметры камеры
        Vector3f cachedCameraPosition;
        Vector3f cachedCameraTarget;
        float cachedFov;
        float cachedAspectRatio;
        float cachedNearPlane;
        float cachedFarPlane;
        // Параметры трансформации
        Vector3f cachedTransformPosition;
        Vector3f cachedTransformRotation;
        Vector3f cachedTransformScale;
        // Размеры экрана
        int cachedWidth;
        int cachedHeight;
    }
    private static final ThreadLocal<MatrixCache> matrixCache = new ThreadLocal<>();
    
    // Пороги для оптимизации пропуска полигонов при большом количестве
    private static final int POLYGON_SKIP_THRESHOLD_1 = 10000;
    private static final int POLYGON_SKIP_THRESHOLD_2 = 50000;
    private static final int POLYGON_SKIP_THRESHOLD_3 = 100000;
    
    // Эпсилон для проверки деления на w (перспективное деление)
    private static final float W_EPSILON = 1e-7f;
    
    // Эпсилон для сравнения параметров камеры и трансформации
    private static final float PARAMETER_EPSILON = 1e-5f;
    
    /**
     * Сравнивает два вектора с учетом эпсилона.
     */
    private static boolean vectorsEqual(Vector3f v1, Vector3f v2, float epsilon) {
        if (v1 == null && v2 == null) return true;
        if (v1 == null || v2 == null) return false;
        return Math.abs(v1.x - v2.x) < epsilon &&
               Math.abs(v1.y - v2.y) < epsilon &&
               Math.abs(v1.z - v2.z) < epsilon;
    }

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
        
        // Валидация модели
        if (mesh.getVertexCount() == 0) {
            logger.log(Level.FINE, "Попытка рендеринга пустой модели (нет вершин)");
            return; // Нет смысла рендерить пустую модель
        }
        if (mesh.getPolygonCount() == 0) {
            logger.log(Level.FINE, "Попытка рендеринга модели без полигонов");
            return; // Нет смысла рендерить модель без полигонов
        }
        
        if (settings == null) {
            render(graphicsContext, camera, mesh, transform, width, height);
            return;
        }

        // Вычисляем model и view матрицы (нужны для backface culling)
        Matrix4f modelMatrix = ModelMatrixBuilder.build(transform);
        Matrix4f viewMatrix = camera.getViewMatrix();
        
        // Кэширование MVP матрицы с проверкой параметров
        MatrixCache cache = matrixCache.get();
        
        // Получаем текущие параметры для сравнения
        Vector3f cameraPos = camera.getPosition();
        Vector3f cameraTarget = camera.getTarget();
        float fov = camera.getFov();
        float aspectRatio = camera.getAspectRatio();
        float nearPlane = camera.getNearPlane();
        float farPlane = camera.getFarPlane();
        
        Vector3f transformPos = transform != null ? transform.getPosition() : new Vector3f(0, 0, 0);
        Vector3f transformRot = transform != null ? transform.getRotation() : new Vector3f(0, 0, 0);
        Vector3f transformScale = transform != null ? transform.getScale() : new Vector3f(1, 1, 1);
        
        Matrix4f modelViewProjectionMatrix = null;
        boolean useCache = false;
        
        if (cache != null && cache.mvpMatrix != null) {
            // Проверяем, изменились ли параметры
            boolean cameraParamsMatch = 
                vectorsEqual(cache.cachedCameraPosition, cameraPos, PARAMETER_EPSILON) &&
                vectorsEqual(cache.cachedCameraTarget, cameraTarget, PARAMETER_EPSILON) &&
                Math.abs(cache.cachedFov - fov) < PARAMETER_EPSILON &&
                Math.abs(cache.cachedAspectRatio - aspectRatio) < PARAMETER_EPSILON &&
                Math.abs(cache.cachedNearPlane - nearPlane) < PARAMETER_EPSILON &&
                Math.abs(cache.cachedFarPlane - farPlane) < PARAMETER_EPSILON;
            
            boolean transformParamsMatch =
                vectorsEqual(cache.cachedTransformPosition, transformPos, PARAMETER_EPSILON) &&
                vectorsEqual(cache.cachedTransformRotation, transformRot, PARAMETER_EPSILON) &&
                vectorsEqual(cache.cachedTransformScale, transformScale, PARAMETER_EPSILON);
            
            boolean sizeMatch = cache.cachedWidth == width && cache.cachedHeight == height;
            
            if (cameraParamsMatch && transformParamsMatch && sizeMatch) {
                // Используем кэшированную матрицу
                modelViewProjectionMatrix = cache.mvpMatrix;
                useCache = true;
            }
        }
        
        if (!useCache) {
            // Пересчитываем MVP матрицу
            Matrix4f projectionMatrix = camera.getProjectionMatrix();
            modelViewProjectionMatrix = projectionMatrix
                    .multiply(viewMatrix)
                    .multiply(modelMatrix);
            
            // Сохраняем в кэш
            if (cache == null) {
                cache = new MatrixCache();
                matrixCache.set(cache);
            }
            cache.mvpMatrix = modelViewProjectionMatrix;
            // Сохраняем параметры камеры
            cache.cachedCameraPosition = new Vector3f(cameraPos);
            cache.cachedCameraTarget = new Vector3f(cameraTarget);
            cache.cachedFov = fov;
            cache.cachedAspectRatio = aspectRatio;
            cache.cachedNearPlane = nearPlane;
            cache.cachedFarPlane = farPlane;
            // Сохраняем параметры трансформации
            cache.cachedTransformPosition = new Vector3f(transformPos);
            cache.cachedTransformRotation = new Vector3f(transformRot);
            cache.cachedTransformScale = new Vector3f(transformScale);
            // Сохраняем размеры
            cache.cachedWidth = width;
            cache.cachedHeight = height;
        }
        
        // modelViewProjectionMatrix гарантированно инициализирована здесь
        assert modelViewProjectionMatrix != null;

        // Задание для третьего человека: "Режимы отрисовки"
        // Статический цвет для заливки (используется, если текстура не включена)
        Color triangleColor = settings.getFillColor();
        Color wireframeColor = settings.getWireframeColor();

        // Fix for transparency: Log Z-buffer status only once (FINE level to avoid spam)
        // Checked at FINE level to avoid excessive logging during rendering loop
        logger.log(Level.FINE, "Z-buffer enabled: {0}", settings.isEnableZBuffer());
        
        ZBuffer zBuffer = null;
        if (settings.isEnableZBuffer()) {
            // Переиспользуем ZBuffer из кэша, если размер совпадает
            ZBuffer cached = zBufferCache.get();
            if (cached != null && cached.getWidth() == width && cached.getHeight() == height) {
                zBuffer = cached;
                zBuffer.clear();
            } else {
                // Создаем новый ZBuffer при первом использовании или при изменении размера
                zBuffer = new ZBuffer(width, height);
                zBufferCache.set(zBuffer);
            }
        }

        final int nPolygons = mesh.getPolygonCount();
        
        int polygonSkip = 1;
        if (nPolygons > POLYGON_SKIP_THRESHOLD_3) {
            polygonSkip = 4;
        } else if (nPolygons > POLYGON_SKIP_THRESHOLD_2) {
            polygonSkip = 3;
        } else if (nPolygons > POLYGON_SKIP_THRESHOLD_1) {
            polygonSkip = 2;
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
                if (Math.abs(transformed.w) > W_EPSILON) {
                    invW = 1.0f / transformed.w;
                    transformed = transformed.divide(transformed.w);
                } else {
                    invW = 1e7f;
                }
                
                // Fix for transparency: Clamp Z values to [-1, 1] NDC range to ensure valid Z-buffer values
                // This prevents overflow issues and ensures correct depth testing.
                // Note: We clamp instead of skipping to avoid losing entire polygons when vertices
                // are slightly outside the range. Invalid values (NaN, Infinity) are still filtered.
                float ndcZ = transformed.z;
                if (Float.isNaN(ndcZ) || Float.isInfinite(ndcZ)) {
                    continue; // Skip invalid Z values (NaN, Infinity)
                }
                // Clamp Z to valid NDC range [-1, 1]
                ndcZ = Math.max(-1.0f, Math.min(1.0f, ndcZ));
                
                transformedVertices.add(transformed);
                resultZ.add(ndcZ);
                resultInvW.add(invW);
                
                Point2f resultPoint = vertexToPoint(transformed, width, height);
                resultPoints.add(resultPoint);
            }
            
            // Fix for transparency: Skip polygon if we don't have enough valid vertices after clipping
            // This prevents rendering degenerate polygons that could cause transparency issues
            if (resultPoints.size() < 3) {
                continue; // Skip polygon with insufficient vertices after clipping
            }

            if (settings.isEnableBackfaceCulling() && nVerticesInPolygon >= 3) {
                try {
                    if (!isFrontFacingByNormal(mesh, polygonInd, modelMatrix, viewMatrix)) {
                        continue;
                    }
                } catch (Exception e) {
                    // Если не удалось определить ориентацию полигона, пропускаем его
                    // Это может произойти при некорректных нормалях или вырожденных полигонах
                    logger.log(Level.FINE, "Не удалось определить ориентацию полигона {0}: {1}", 
                        new Object[]{polygonInd, e.getMessage()});
                    continue;
                }
            }

            // Задание для третьего человека: "Растеризация полигонов"
            // Если полигон имеет больше 3 вершин, триангулируем его динамически
            // Триангуляция выполняется во время рендеринга, а не при загрузке модели
            if (nVerticesInPolygon > 3 && settings.isEnableTriangulation()) {
                Triangulator triangulator = new EarCuttingTriangulator();
                java.util.List<Polygon> triangles = triangulator.triangulatePolygon(mesh, polygon);
                
                // Рендерим каждый треугольник отдельно
                for (Polygon triangle : triangles) {
                    ArrayList<Integer> triVertexIndices = triangle.getVertexIndices();
                    if (triVertexIndices.size() != 3) continue;
                    
                    // Находим позиции вершин треугольника в исходном полигоне
                    int idx0 = -1, idx1 = -1, idx2 = -1;
                    ArrayList<Integer> polygonVertexIndices = polygon.getVertexIndices();
                    for (int i = 0; i < polygonVertexIndices.size(); i++) {
                        if (polygonVertexIndices.get(i).equals(triVertexIndices.get(0))) idx0 = i;
                        if (polygonVertexIndices.get(i).equals(triVertexIndices.get(1))) idx1 = i;
                        if (polygonVertexIndices.get(i).equals(triVertexIndices.get(2))) idx2 = i;
                    }
                    
                    if (idx0 < 0 || idx1 < 0 || idx2 < 0 || 
                        idx0 >= resultPoints.size() || idx1 >= resultPoints.size() || idx2 >= resultPoints.size()) {
                        continue;
                    }
                    
                    // Получаем UV координаты для треугольника
                    ArrayList<Integer> triTextureIndices = triangle.getTextureVertexIndices();
                    int[] triUVIndices = {0, 1, 2};
                    float[] uv = getUVCoordinates(mesh, triangle, triTextureIndices, triUVIndices);
                    
                    // Создаем списки точек, Z и invW для треугольника
                    ArrayList<Point2f> triPoints = new ArrayList<>();
                    ArrayList<Float> triZ = new ArrayList<>();
                    ArrayList<Float> triInvW = new ArrayList<>();
                    triPoints.add(resultPoints.get(idx0));
                    triPoints.add(resultPoints.get(idx1));
                    triPoints.add(resultPoints.get(idx2));
                    triZ.add(resultZ.get(idx0));
                    triZ.add(resultZ.get(idx1));
                    triZ.add(resultZ.get(idx2));
                    triInvW.add(resultInvW.get(idx0));
                    triInvW.add(resultInvW.get(idx1));
                    triInvW.add(resultInvW.get(idx2));
                    
                    // Рендерим треугольник
                    renderTriangle(graphicsContext, zBuffer, settings, triPoints, triZ, triInvW, uv, triangleColor, wireframeColor);
                }
                continue; // Пропускаем дальнейшую обработку полигона, так как уже обработали треугольники
            }
            
            if (nVerticesInPolygon == 3) {
                // Треугольник - рендерим напрямую (задание для третьего человека: "Растеризация полигонов")
                ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
                int[] vertexIndices = {0, 1, 2};
                float[] uv = getUVCoordinates(mesh, polygon, textureIndices, vertexIndices);
                
                renderTriangle(graphicsContext, zBuffer, settings, resultPoints, resultZ, resultInvW, uv, triangleColor, wireframeColor);
            } else {
                // Полигон с >3 вершинами - используем fan triangulation (без динамической триангуляции)
                // Это fallback для случая, когда триангуляция отключена в настройках
                // Задание для третьего человека: "Растеризация полигонов" - заполнение полигонов одним цветом
                ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
                
                // Fan triangulation: разбиваем на треугольники от первой вершины
                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon - 1; ++vertexInPolygonInd) {
                    int[] triVertexIndices = {0, vertexInPolygonInd, vertexInPolygonInd + 1};
                    float[] uv = getUVCoordinates(mesh, polygon, textureIndices, triVertexIndices);
                    
                    // Создаем списки для треугольника
                    ArrayList<Point2f> triPoints = new ArrayList<>();
                    ArrayList<Float> triZ = new ArrayList<>();
                    ArrayList<Float> triInvW = new ArrayList<>();
                    triPoints.add(resultPoints.get(0));
                    triPoints.add(resultPoints.get(vertexInPolygonInd));
                    triPoints.add(resultPoints.get(vertexInPolygonInd + 1));
                    triZ.add(resultZ.get(0));
                    triZ.add(resultZ.get(vertexInPolygonInd));
                    triZ.add(resultZ.get(vertexInPolygonInd + 1));
                    triInvW.add(resultInvW.get(0));
                    triInvW.add(resultInvW.get(vertexInPolygonInd));
                    triInvW.add(resultInvW.get(vertexInPolygonInd + 1));
                    
                    renderTriangle(graphicsContext, zBuffer, settings, triPoints, triZ, triInvW, uv, triangleColor, wireframeColor);
                }
                
                // Отрисовываем wireframe для всего полигона
                if (settings.isShowWireframe()) {
                    drawWireframePolygon(graphicsContext, wireframeColor, resultPoints);
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
     * Получает UV координаты для трех вершин из полигона.
     * 
     * <p>Извлекает текстурные координаты из модели для указанных индексов вершин полигона.
     * Если текстура не используется или координаты недоступны, возвращает нули.
     * 
     * @param mesh модель с текстурными координатами
     * @param polygon полигон с индексами текстурных вершин (не используется, но оставлен для ясности)
     * @param textureIndices индексы текстурных вершин из полигона (может быть null)
     * @param vertexIndicesInPolygon массив из 3 индексов вершин в полигоне (например, [0, 1, 2] или [0, i, i+1])
     * @return массив из 6 float значений: [u0, v0, u1, v1, u2, v2]
     */
    private static float[] getUVCoordinates(
            Model mesh, 
            Polygon polygon, 
            ArrayList<Integer> textureIndices,
            int[] vertexIndicesInPolygon) {
        float[] uv = new float[6]; // [u0, v0, u1, v1, u2, v2]
        
        if (textureIndices != null && textureIndices.size() > 0 && mesh.getTextureVertexCount() > 0) {
            for (int i = 0; i < 3; i++) {
                int vertexIdxInPolygon = vertexIndicesInPolygon[i];
                if (vertexIdxInPolygon >= 0 && vertexIdxInPolygon < textureIndices.size()) {
                    int texIdx = textureIndices.get(vertexIdxInPolygon);
                    if (texIdx >= 0 && texIdx < mesh.getTextureVertexCount()) {
                        Vector2f tex = mesh.getTextureVertex(texIdx);
                        uv[i * 2] = tex.x;
                        uv[i * 2 + 1] = tex.y;
                    }
                }
            }
        }
        
        return uv;
    }
    
    /**
     * Отрисовывает wireframe (обводку) для треугольника.
     * 
     * @param graphicsContext контекст для отрисовки
     * @param wireframeColor цвет обводки
     * @param p0, p1, p2 точки треугольника в экранных координатах
     */
    private static void drawWireframeTriangle(
            GraphicsContext graphicsContext,
            Color wireframeColor,
            Point2f p0, Point2f p1, Point2f p2) {
        graphicsContext.setStroke(wireframeColor);
        graphicsContext.strokeLine(p0.x, p0.y, p1.x, p1.y);
        graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
        graphicsContext.strokeLine(p2.x, p2.y, p0.x, p0.y);
    }
    
    /**
     * Отрисовывает wireframe (обводку) для полигона с произвольным количеством вершин.
     * 
     * @param graphicsContext контекст для отрисовки
     * @param wireframeColor цвет обводки
     * @param points точки полигона в экранных координатах
     */
    private static void drawWireframePolygon(
            GraphicsContext graphicsContext,
            Color wireframeColor,
            ArrayList<Point2f> points) {
        if (points.size() < 2) return;
        
        graphicsContext.setStroke(wireframeColor);
        for (int i = 1; i < points.size(); i++) {
            graphicsContext.strokeLine(
                points.get(i - 1).x, points.get(i - 1).y,
                points.get(i).x, points.get(i).y
            );
        }
        graphicsContext.strokeLine(
            points.get(points.size() - 1).x, points.get(points.size() - 1).y,
            points.get(0).x, points.get(0).y
        );
    }
    
    /**
     * Рендерит треугольник с учетом всех настроек (заливка, текстура, wireframe).
     * 
     * @param graphicsContext контекст для отрисовки
     * @param zBuffer Z-buffer для проверки глубины
     * @param settings настройки рендеринга
     * @param resultPoints точки треугольника в экранных координатах
     * @param resultZ значения Z для вершин
     * @param resultInvW обратные значения w для вершин
     * @param uv UV координаты [u0, v0, u1, v1, u2, v2]
     * @param triangleColor цвет треугольника
     * @param wireframeColor цвет обводки
     */
    private static void renderTriangle(
            GraphicsContext graphicsContext,
            ZBuffer zBuffer,
            RenderSettings settings,
            ArrayList<Point2f> resultPoints,
            ArrayList<Float> resultZ,
            ArrayList<Float> resultInvW,
            float[] uv,
            Color triangleColor,
            Color wireframeColor) {
        
        Texture texture = settings.isUseTexture() ? settings.getTexture() : null;
        
        if (settings.isShowFilled()) {
            TriangleRasterizer.fillTriangle(
                graphicsContext,
                zBuffer,
                texture,
                resultPoints.get(0).x, resultPoints.get(0).y, resultZ.get(0), resultInvW.get(0), 
                uv[0], uv[1], triangleColor,
                resultPoints.get(1).x, resultPoints.get(1).y, resultZ.get(1), resultInvW.get(1), 
                uv[2], uv[3], triangleColor,
                resultPoints.get(2).x, resultPoints.get(2).y, resultZ.get(2), resultInvW.get(2), 
                uv[4], uv[5], triangleColor
            );
        }
        
        if (settings.isShowWireframe()) {
            drawWireframeTriangle(graphicsContext, wireframeColor, 
                resultPoints.get(0), resultPoints.get(1), resultPoints.get(2));
        }
    }
    
    /**
     * Проверяет, является ли треугольник передней гранью, используя нормаль в пространстве камеры.
     * Использует сохраненные нормали из модели (пересчитанные NormalCalculator), что более надежно.
     * 
     * <p>Fix for backface culling to prevent transparency: Always compute normal from vertices if stored
     * normal is invalid (null or length < 1e-6f). Change fallback returns to false (skip polygon)
     * for degenerate cases instead of true to prevent rendering invalid polygons.
     * 
     * @param mesh модель
     * @param polygonIndex индекс полигона
     * @param modelMatrix матрица модели
     * @param viewMatrix матрица вида
     * @return true если треугольник front-facing (видим), false если back-facing (невидим) или вырожденный
     */
    private static boolean isFrontFacingByNormal(
            Model mesh, int polygonIndex, Matrix4f modelMatrix, Matrix4f viewMatrix) {
        if (polygonIndex >= mesh.getPolygonCount()) {
            return false; // Fix: Skip invalid polygon indices to prevent transparency issues
        }
        
        Polygon polygon = mesh.getPolygon(polygonIndex);
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();
        
        Vector3f normal = null;
        boolean useNormalFromArray = false;
        
        if (mesh.getNormalCount() > 0 && 
            normalIndices != null && !normalIndices.isEmpty() && 
            normalIndices.get(0) >= 0 && normalIndices.get(0) < mesh.getNormalCount()) {
            normal = mesh.getNormal(normalIndices.get(0));
            // Проверка на null нормаль
            if (normal == null) {
                logger.log(Level.FINE, "Нормаль полигона {0} равна null, вычисляем из вершин", polygonIndex);
            } else {
                // Проверка на валидность нормали (не должна быть нулевым вектором)
                if (normal.length() < 1e-6f) {
                    logger.log(Level.FINE, "Нормаль полигона {0} является нулевым вектором, вычисляем из вершин", polygonIndex);
                    normal = null; // Будет вычислена ниже
                } else {
                    useNormalFromArray = true;
                }
            }
        }
        
        // Вычисляем нормаль из вершин, если она не была получена из массива нормалей
        if (!useNormalFromArray) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) {
                return false; // Fix: Skip polygons with <3 vertices instead of rendering them
            }
            
            // Проверка на null вершины
            Vector3f v0 = mesh.getVertex(vertexIndices.get(0));
            Vector3f v1 = mesh.getVertex(vertexIndices.get(1));
            Vector3f v2 = mesh.getVertex(vertexIndices.get(2));
            
            if (v0 == null || v1 == null || v2 == null) {
                logger.log(Level.FINE, "Полигон {0} содержит null вершины, пропускаем", polygonIndex);
                return false; // Fix: Skip polygons with null vertices to prevent transparency issues
            }
            
            // Проверка на вырожденный треугольник (вершины на одной прямой)
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            normal = edge1.cross(edge2);
            
            // Проверка на нулевой вектор (вырожденный треугольник)
            if (normal.length() < 1e-6f) {
                logger.log(Level.FINE, "Полигон {0} является вырожденным (вершины на одной прямой)", polygonIndex);
                return false; // Fix: Skip degenerate polygons instead of rendering them
            }
            
            try {
                normal = normal.normalize();
            } catch (ArithmeticException e) {
                logger.log(Level.FINE, "Вырожденный полигон {0}: невозможно нормализовать нормаль (нулевой вектор)", polygonIndex);
                return false; // Fix: Skip polygons that cannot be normalized
            }
        }
        
        Vector4f normal4 = new Vector4f(normal, 0.0f);
        Vector4f normalModel = modelMatrix.multiply(normal4);
        Vector4f normalView = viewMatrix.multiply(normalModel);
        
        Vector3f normalView3 = new Vector3f(normalView.x, normalView.y, normalView.z);
        try {
            normalView3 = normalView3.normalize();
        } catch (ArithmeticException e) {
            logger.log(Level.FINE, "Вырожденная нормаль после трансформации: невозможно нормализовать");
            return false; // Fix: Skip polygons with invalid transformed normals
        }
        
        // Assuming camera looks down -Z: front-facing if normalView3.z < 0
        return normalView3.z < 0;
    }

}