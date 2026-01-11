package com.cgvsu.triangulation;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Простая триангуляция: разбивает полигон на треугольники последовательно,
 * беря первые три вершины, затем следующие три и т.д.
 * Работает только для выпуклых полигонов.
 */
public class SimpleTriangulator implements Triangulator {
    
    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        ArrayList<Integer> verticesIndexes = polygon.getVertexIndices();
        
        // Если полигон уже треугольник или меньше, возвращаем копию
        if (verticesIndexes.size() <= 3) {
            return List.of(PolygonUtil.deepCopyOfPolygon(polygon));
        }
        
        // Создаем карты для текстурных координат и нормалей
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        
        List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();
        
        for (int i = 0; i < verticesIndexes.size(); i++) {
            Integer vertexIndex = verticesIndexes.get(i);
            if (i < textureVertexIndices.size()) {
                textureIndexesMap.put(vertexIndex, textureVertexIndices.get(i));
            }
            if (i < normalIndices.size()) {
                normalsIndexesMap.put(vertexIndex, normalIndices.get(i));
            }
        }
        
        // Создаем треугольники последовательно
        List<Polygon> newPolygons = new ArrayList<>();
        int n = verticesIndexes.size();
        int firstVertexIndex = 0;
        int secondVertexIndex = 1;
        int thirdVertexIndex = 2;
        
        while (thirdVertexIndex < n) {
            Polygon newPolygon = PolygonUtil.createNewPolygon(
                    List.of(
                            verticesIndexes.get(firstVertexIndex),
                            verticesIndexes.get(secondVertexIndex),
                            verticesIndexes.get(thirdVertexIndex)
                    ),
                    textureIndexesMap,
                    normalsIndexesMap
            );
            newPolygons.add(newPolygon);
            secondVertexIndex++;
            thirdVertexIndex++;
        }
        
        return newPolygons;
    }
}
