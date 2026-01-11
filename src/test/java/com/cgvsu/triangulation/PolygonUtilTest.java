package com.cgvsu.triangulation;

import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Тесты для PolygonUtil
 */
class PolygonUtilTest {
    
    @Test
    void testCreateNewPolygon() {
        List<Integer> vertexIndexes = Arrays.asList(0, 1, 2);
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        textureIndexesMap.put(0, 10);
        textureIndexesMap.put(1, 11);
        textureIndexesMap.put(2, 12);
        
        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        normalsIndexesMap.put(0, 100);
        normalsIndexesMap.put(1, 101);
        normalsIndexesMap.put(2, 102);
        
        Polygon newPolygon = PolygonUtil.createNewPolygon(vertexIndexes, textureIndexesMap, normalsIndexesMap);
        
        Assertions.assertEquals(vertexIndexes, newPolygon.getVertexIndices());
        
        List<Integer> expectedTextureIndices = Arrays.asList(10, 11, 12);
        Assertions.assertEquals(expectedTextureIndices, newPolygon.getTextureVertexIndices());
        
        List<Integer> expectedNormalIndices = Arrays.asList(100, 101, 102);
        Assertions.assertEquals(expectedNormalIndices, newPolygon.getNormalIndices());
    }
    
    @Test
    void testDeepCopyOfPolygon_ShouldCreateIndependentCopy() {
        ArrayList<Integer> originalVertices = new ArrayList<>(Arrays.asList(0, 1, 2));
        ArrayList<Integer> originalTextures = new ArrayList<>(Arrays.asList(10, 11, 12));
        ArrayList<Integer> originalNormals = new ArrayList<>(Arrays.asList(100, 101, 102));
        
        Polygon originalPolygon = new Polygon();
        originalPolygon.setVertexIndices(originalVertices);
        originalPolygon.setTextureVertexIndices(originalTextures);
        originalPolygon.setNormalIndices(originalNormals);
        
        Polygon copiedPolygon = PolygonUtil.deepCopyOfPolygon(originalPolygon);
        
        Assertions.assertEquals(originalVertices, copiedPolygon.getVertexIndices());
        Assertions.assertEquals(originalTextures, copiedPolygon.getTextureVertexIndices());
        Assertions.assertEquals(originalNormals, copiedPolygon.getNormalIndices());
        
        // Изменяем оригинальные списки
        originalVertices.set(0, 999);
        originalPolygon.setVertexIndices(originalVertices);
        
        // Копия не должна измениться
        Assertions.assertNotEquals(999, copiedPolygon.getVertexIndices().get(0));
        Assertions.assertEquals(0, copiedPolygon.getVertexIndices().get(0).intValue());
    }
    
    @Test
    void testCreateNewPolygon_WithoutTexturesAndNormals() {
        List<Integer> vertexIndexes = Arrays.asList(0, 1, 2);
        Map<Integer, Integer> emptyTextureMap = new HashMap<>();
        Map<Integer, Integer> emptyNormalsMap = new HashMap<>();
        
        Polygon newPolygon = PolygonUtil.createNewPolygon(vertexIndexes, emptyTextureMap, emptyNormalsMap);
        
        Assertions.assertEquals(vertexIndexes, newPolygon.getVertexIndices());
        Assertions.assertTrue(newPolygon.getTextureVertexIndices().isEmpty());
        Assertions.assertTrue(newPolygon.getNormalIndices().isEmpty());
    }
}
