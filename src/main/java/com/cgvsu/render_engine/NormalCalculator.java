package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Вычисление нормалей для полигонов и вершин моделей.
 */
public class NormalCalculator {
    
    public static void recalculateNormals(Model model) {
        if (model == null || model.getPolygonCount() == 0 || model.getVertexCount() == 0) {
            return;
        }
        
        model.clearNormals();
        
        Map<Integer, Vector3f> vertexNormals = new HashMap<>();
        
        for (Polygon polygon : model.getPolygons()) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            
            if (vertexIndices.size() < 3) {
                continue;
            }
            
            int idx0 = vertexIndices.get(0);
            int idx1 = vertexIndices.get(1);
            int idx2 = vertexIndices.get(2);
            
            if (idx0 < 0 || idx0 >= model.getVertexCount() ||
                idx1 < 0 || idx1 >= model.getVertexCount() ||
                idx2 < 0 || idx2 >= model.getVertexCount()) {
                continue;
            }
            
            Vector3f v0 = model.getVertex(idx0);
            Vector3f v1 = model.getVertex(idx1);
            Vector3f v2 = model.getVertex(idx2);
            
            if (v0 == null || v1 == null || v2 == null) {
                continue;
            }
            
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            
            Vector3f polygonNormal = edge1.cross(edge2);
            
            try {
                polygonNormal = polygonNormal.normalize();
            } catch (ArithmeticException e) {
                polygonNormal = new Vector3f(0, 0, 1);
            }
            
            int normalIndex = model.getNormalCount();
            model.addNormal(polygonNormal);
            
            ArrayList<Integer> normalIndices = new ArrayList<>();
            for (int i = 0; i < vertexIndices.size(); i++) {
                normalIndices.add(normalIndex);
            }
            polygon.setNormalIndices(normalIndices);
            
            for (Integer vertexIndex : vertexIndices) {
                vertexNormals.merge(vertexIndex, polygonNormal, Vector3f::add);
            }
        }
        
        for (Map.Entry<Integer, Vector3f> entry : vertexNormals.entrySet()) {
            try {
                vertexNormals.put(entry.getKey(), entry.getValue().normalize());
            } catch (ArithmeticException e) {
                vertexNormals.put(entry.getKey(), new Vector3f(0, 0, 1));
            }
        }
    }
    
    public static Vector3f calculatePolygonNormal(Model model, Polygon polygon) {
        if (model == null || polygon == null || model.getVertexCount() == 0) {
            return new Vector3f(0, 0, 1);
        }
        
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        
        if (vertexIndices.size() < 3) {
            return new Vector3f(0, 0, 1);
        }
        
        int idx0 = vertexIndices.get(0);
        int idx1 = vertexIndices.get(1);
        int idx2 = vertexIndices.get(2);
        
        if (idx0 < 0 || idx0 >= model.getVertexCount() ||
            idx1 < 0 || idx1 >= model.getVertexCount() ||
            idx2 < 0 || idx2 >= model.getVertexCount()) {
            return new Vector3f(0, 0, 1);
        }
        
        Vector3f v0 = model.getVertex(idx0);
        Vector3f v1 = model.getVertex(idx1);
        Vector3f v2 = model.getVertex(idx2);
        
        if (v0 == null || v1 == null || v2 == null) {
            return new Vector3f(0, 0, 1);
        }
        
        Vector3f edge1 = v1.subtract(v0);
        Vector3f edge2 = v2.subtract(v0);
        Vector3f normal = edge1.cross(edge2);
        
        try {
            return normal.normalize();
        } catch (ArithmeticException e) {
            return new Vector3f(0, 0, 1);
        }
    }
}
