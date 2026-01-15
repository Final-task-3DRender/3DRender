package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Утилиты для работы с полигонами при триангуляции
 */
public class PolygonUtil {
    
    /**
     * Создает новый полигон с указанными индексами вершин и соответствующими индексами текстур и нормалей
     */
    public static Polygon createNewPolygon(
            List<Integer> vertexIndexes,
            Map<Integer, Integer> textureIndexesMap,
            Map<Integer, Integer> normalsIndexesMap
    ) {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(vertexIndexes));
        
        if (!textureIndexesMap.isEmpty() && vertexIndexes.size() >= 3) {
            ArrayList<Integer> textureIndices = new ArrayList<>();
            for (Integer index : vertexIndexes) {
                Integer textureIndex = textureIndexesMap.get(index);
                if (textureIndex != null) {
                    textureIndices.add(textureIndex);
                }
            }
            if (!textureIndices.isEmpty() && textureIndices.size() >= 3) {
                polygon.setTextureVertexIndices(textureIndices);
            }
        }
        
        if (!normalsIndexesMap.isEmpty() && vertexIndexes.size() >= 3) {
            ArrayList<Integer> normalsIndices = new ArrayList<>();
            for (Integer index : vertexIndexes) {
                Integer normalIndex = normalsIndexesMap.get(index);
                if (normalIndex != null) {
                    normalsIndices.add(normalIndex);
                }
            }
            if (!normalsIndices.isEmpty() && normalsIndices.size() >= 3) {
                polygon.setNormalIndices(normalsIndices);
            }
        }
        
        return polygon;
    }
    
    /**
     * Создает глубокую копию полигона
     */
    public static Polygon deepCopyOfPolygon(Polygon polygon) {
        Polygon copy = new Polygon();
        copy.setVertexIndices(new ArrayList<>(polygon.getVertexIndices()));
        if (!polygon.getTextureVertexIndices().isEmpty()) {
            copy.setTextureVertexIndices(new ArrayList<>(polygon.getTextureVertexIndices()));
        }
        if (!polygon.getNormalIndices().isEmpty()) {
            copy.setNormalIndices(new ArrayList<>(polygon.getNormalIndices()));
        }
        return copy;
    }
    
    /**
     * Вычисляет площадь треугольника (полигон должен иметь 3 вершины)
     */
    public static float calcTrianglePolygonSquare(Polygon polygon, Model model) {
        List<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() != 3) {
            throw new IllegalArgumentException("Method works only with triangles. Given polygon vertices count: " + vertexIndices.size());
        }
        
        Vector3f v0 = model.vertices.get(vertexIndices.get(0));
        Vector3f v1 = model.vertices.get(vertexIndices.get(1));
        Vector3f v2 = model.vertices.get(vertexIndices.get(2));
        
        Vector3f first = new Vector3f(
                v0.x - v1.x,
                v0.y - v1.y,
                v0.z - v1.z
        );
        Vector3f second = new Vector3f(
                v2.x - v1.x,
                v2.y - v1.y,
                v2.z - v1.z
        );
        
        Vector3f cross = first.cross(second);
        float area = 0.5f * cross.length();
        return area;
    }
}
