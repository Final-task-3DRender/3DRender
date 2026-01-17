package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.*;

/**
 * Триангуляция полигонов алгоритмом Ear-Cutting.
 */
public class EarCuttingTriangulator implements Triangulator {
    
    private static final int MAX_LOOP_SIZE = 1000000;
    
    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (polygon == null) {
            throw new IllegalArgumentException("Polygon cannot be null");
        }
        if (model.getVertexCount() == 0) {
            throw new IllegalArgumentException("Model must have at least one vertex");
        }
        
        float[] barycentric = new float[3];
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        
        // Если полигон уже является треугольником или меньше, возвращаем его копию
        if (vertexIndices.size() < 4) {
            return List.of(PolygonUtil.deepCopyOfPolygon(polygon));
        }
        
        int indexOfVertexInPolygon = 0;
        // Получаем данные из оригинального объекта
        Queue<Integer> verticesIndexes = new LinkedList<>(vertexIndices);
        
        // Создаем ассоциативные коллекции, которые связывают индексы, используемые в полигонах с объектами меша
        Map<Integer, Vector3f> vertices = new HashMap<>();
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        
        // Вспомогательный список вершин, хранимый для определения направления задания полигона
        List<Vector3f> verticesList = new ArrayList<>();
        
        List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();
        
        for (Integer vertexIndex : verticesIndexes) {
            vertices.put(vertexIndex, model.getVertex(vertexIndex));
            if (indexOfVertexInPolygon < textureVertexIndices.size()) {
                textureIndexesMap.put(vertexIndex, textureVertexIndices.get(indexOfVertexInPolygon));
            }
            if (indexOfVertexInPolygon < normalIndices.size()) {
                normalsIndexesMap.put(vertexIndex, normalIndices.get(indexOfVertexInPolygon));
            }
            verticesList.add(model.getVertex(vertexIndex));
            indexOfVertexInPolygon++;
        }
        
        // Подготовка. Подбираю оси, по которым буду триангулировать
        List<Function<Vector3f, Float>> axes = chooseAxes(verticesList);
        // Если невозможно триангулировать
        if (axes == null) {
            System.err.println("Unable to triangulate polygon");
            return List.of(PolygonUtil.deepCopyOfPolygon(polygon));
        }
        
        ByPassDirection polygonDirection = findDirection(verticesList, axes.get(0), axes.get(1));
        List<Polygon> newPolygons = new ArrayList<>();
        
        int leftPointIndex = verticesIndexes.poll();
        int middlePointIndex = verticesIndexes.poll();
        int rightPointIndex = verticesIndexes.poll();
        int iterationsCount = 0;
        
        while (!verticesIndexes.isEmpty()) {
            // Есть два условия, когда я не могу отрезать ухо:
            // 1) Одна из оставшихся вершин в треугольнике
            // 2) Направления обхода полигона и текущего треугольника не совпадают
            if (
                    isAnyVertexInsideTriangleByBarycentric(barycentric, leftPointIndex, middlePointIndex, rightPointIndex, vertices,
                            axes.get(0), axes.get(1))
                            || findDirection(
                            List.of(
                                    vertices.get(leftPointIndex),
                                    vertices.get(middlePointIndex),
                                    vertices.get(rightPointIndex)), axes.get(0), axes.get(1))
                            != polygonDirection
            ) {
                verticesIndexes.add(leftPointIndex);
                leftPointIndex = middlePointIndex;
                middlePointIndex = rightPointIndex;
                rightPointIndex = verticesIndexes.poll();
                iterationsCount++;
                if (iterationsCount > MAX_LOOP_SIZE) {
                    throw new RuntimeException("Bad polygons model. Unable to triangulate. Try SimpleTriangulator");
                }
                continue;
            }
            
            newPolygons.add(PolygonUtil.createNewPolygon(
                    List.of(leftPointIndex, middlePointIndex, rightPointIndex),
                    textureIndexesMap,
                    normalsIndexesMap
            ));
            
            middlePointIndex = rightPointIndex;
            rightPointIndex = verticesIndexes.poll();
            iterationsCount = 0;
        }
        
        // Добавляем последний треугольник
        newPolygons.add(PolygonUtil.createNewPolygon(
                List.of(leftPointIndex, middlePointIndex, rightPointIndex),
                textureIndexesMap,
                normalsIndexesMap
        ));
        
        return newPolygons;
    }
    
    protected boolean isAnyVertexInsideTriangleByBarycentric(
            float[] barycentric,
            int leftPointIndex, int middlePointIndex, int rightPointIndex,
            Map<Integer, Vector3f> vertices,
            Function<Vector3f, Float> getterFirst,
            Function<Vector3f, Float> getterSecond
    ) {
        for (int i : vertices.keySet()) {
            if (i == leftPointIndex || i == middlePointIndex || i == rightPointIndex) {
                continue;
            }
            
            float point0cord0 = getterFirst.apply(vertices.get(leftPointIndex));
            float point0cord1 = getterSecond.apply(vertices.get(leftPointIndex));
            
            float point1cord0 = getterFirst.apply(vertices.get(middlePointIndex));
            float point1cord1 = getterSecond.apply(vertices.get(middlePointIndex));
            
            float point2cord0 = getterFirst.apply(vertices.get(rightPointIndex));
            float point2cord1 = getterSecond.apply(vertices.get(rightPointIndex));
            
            float pointCurrentCord0 = getterFirst.apply(vertices.get(i));
            float pointCurrentCord1 = getterSecond.apply(vertices.get(i));
            
            findBarycentricCords(
                    barycentric, pointCurrentCord0, pointCurrentCord1,
                    point0cord0, point0cord1,
                    point1cord0, point1cord1,
                    point2cord0, point2cord1
            );
            
            if (barycentric[0] < 0 || barycentric[1] < 0 || barycentric[2] < 0) {
                continue;
            }
            if (abs(1 - (barycentric[0] + barycentric[1] + barycentric[2])) > Constants.EPS) {
                continue;
            }
            return true;
        }
        return false;
    }
    
    protected List<Function<Vector3f, Float>> chooseAxes(List<Vector3f> vertices) {
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        
        boolean XYbad = false;
        boolean XZbad = false;
        boolean YZbad = false;
        
        for (int i = 0; i < vertices.size() - 1; i++) {
            Vector3f v1 = vertices.get(i);
            maxX = max(maxX, v1.x);
            maxY = max(maxY, v1.y);
            maxZ = max(maxZ, v1.z);
            minX = min(minX, v1.x);
            minY = min(minY, v1.y);
            minZ = min(minZ, v1.z);
            
            for (int j = i + 1; j < vertices.size(); j++) {
                Vector3f v2 = vertices.get(j);
                if (
                        abs(v1.x - v2.x) <= Constants.EPS
                                && abs(v1.y - v2.y) <= Constants.EPS
                                && abs(v1.z - v2.z) <= Constants.EPS
                ) {
                    return null; // Дублирующиеся вершины
                }
                if (abs(v1.x - v2.x) <= Constants.EPS
                        && abs(v1.y - v2.y) <= Constants.EPS) {
                    XYbad = true;
                }
                if (abs(v1.x - v2.x) <= Constants.EPS
                        && abs(v1.z - v2.z) <= Constants.EPS) {
                    XZbad = true;
                }
                if (abs(v1.y - v2.y) <= Constants.EPS
                        && abs(v1.z - v2.z) <= Constants.EPS) {
                    YZbad = true;
                }
            }
        }
        
        if (XYbad && XZbad && YZbad) {
            return null;
        }
        
        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;
        
        if (dx <= Constants.EPS && dy <= Constants.EPS) return null;
        if (dy <= Constants.EPS && dz <= Constants.EPS) return null;
        if (dx <= Constants.EPS && dz <= Constants.EPS) return null;
        
        Function<Vector3f, Float> dxGetter = v -> v.x;
        Function<Vector3f, Float> dyGetter = v -> v.y;
        Function<Vector3f, Float> dzGetter = v -> v.z;
        
        if (dx > Constants.EPS && dy > Constants.EPS && !XYbad) {
            return List.of(dxGetter, dyGetter);
        }
        if (dx > Constants.EPS && dz > Constants.EPS && !XZbad) {
            return List.of(dxGetter, dzGetter);
        }
        return List.of(dyGetter, dzGetter);
    }
    
    public ByPassDirection findDirection(List<Vector3f> vertices, Function<Vector3f, Float> getterFirst,
                                         Function<Vector3f, Float> getterSecond) {
        int indexOfFoundingVertex = 0;
        Vector3f bottomLeftVertex = vertices.get(0);
        
        for (int i = 1; i < vertices.size(); i++) {
            Vector3f currentVertex = vertices.get(i);
            if (getterFirst.apply(currentVertex) <= getterFirst.apply(bottomLeftVertex)) {
                if (Objects.equals(getterFirst.apply(currentVertex), getterFirst.apply(bottomLeftVertex))
                        && getterSecond.apply(currentVertex) > getterSecond.apply(bottomLeftVertex)) {
                    continue;
                }
                indexOfFoundingVertex = i;
                bottomLeftVertex = currentVertex;
            }
        }
        
        int leftVertexIndex = indexOfFoundingVertex - 1 < 0 ? vertices.size() - 1 : indexOfFoundingVertex - 1;
        int rightVertexIndex = indexOfFoundingVertex + 1 >= vertices.size() ? 0 : indexOfFoundingVertex + 1;
        
        Vector3f vectorA = new Vector3f(
                vertices.get(leftVertexIndex).x - bottomLeftVertex.x,
                vertices.get(leftVertexIndex).y - bottomLeftVertex.y,
                vertices.get(leftVertexIndex).z - bottomLeftVertex.z
        );
        
        Vector3f vectorB = new Vector3f(
                vertices.get(rightVertexIndex).x - bottomLeftVertex.x,
                vertices.get(rightVertexIndex).y - bottomLeftVertex.y,
                vertices.get(rightVertexIndex).z - bottomLeftVertex.z
        );
        
        float cross = getterFirst.apply(vectorA) * getterSecond.apply(vectorB)
                - getterSecond.apply(vectorA) * getterFirst.apply(vectorB);
        
        return cross > 0 ? ByPassDirection.SECOND : ByPassDirection.FIRST;
    }
    
    public static void findBarycentricCords(float[] barycentric, float xCur, float yCur,
                                            float x0, float y0, float x1, float y1, float x2, float y2) {
        float mainDet = findThirdOrderDeterminant(
                x0, x1, x2,
                y0, y1, y2,
                1, 1, 1
        );
        
        if (mainDet == 0) {
            barycentric[0] = 0;
            barycentric[1] = 0;
            barycentric[2] = 0;
            return;
        }
        
        float detForAlpha = findThirdOrderDeterminant(
                xCur, x1, x2,
                yCur, y1, y2,
                1, 1, 1
        );
        float detForBeta = findThirdOrderDeterminant(
                x0, xCur, x2,
                y0, yCur, y2,
                1, 1, 1
        );
        float detForLambda = findThirdOrderDeterminant(
                x0, x1, xCur,
                y0, y1, yCur,
                1, 1, 1
        );
        
        barycentric[0] = detForAlpha / mainDet;
        barycentric[1] = detForBeta / mainDet;
        barycentric[2] = detForLambda / mainDet;
    }
    
    public static float findThirdOrderDeterminant(
            float a00, float a01, float a02,
            float a10, float a11, float a12,
            float a20, float a21, float a22
    ) {
        return ((a00 * a11 * a22) + (a10 * a21 * a02) + (a01 * a12 * a20))
                - ((a02 * a11 * a20) + (a01 * a10 * a22) + (a12 * a21 * a00));
    }
}
