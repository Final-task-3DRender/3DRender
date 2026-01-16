package com.cgvsu.triangulation;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Простая реализация триангулятора для разбиения полигонов на треугольники.
 * 
 * <p>Использует последовательный алгоритм: разбивает полигон на треугольники,
 * беря первые три вершины, затем следующие три и т.д. (fan triangulation).
 * 
 * <p>Алгоритм работает корректно только для выпуклых полигонов.
 * Для вогнутых полигонов может давать некорректные результаты.
 * 
 * <p>Сохраняет текстурные координаты и нормали при триангуляции.
 * 
 * @author CGVSU Team
 * @version 1.0
 * @see Triangulator
 */
public class SimpleTriangulator implements Triangulator {
    
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
        
        ArrayList<Integer> verticesIndexes = polygon.getVertexIndices();
        
        if (verticesIndexes.size() <= 3) {
            return List.of(PolygonUtil.deepCopyOfPolygon(polygon));
        }
        
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
