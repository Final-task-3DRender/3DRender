package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Калькулятор нормалей для моделей.
 * Пересчитывает нормали для полигонов и вершин после триангуляции.
 */
public class NormalCalculator {
    
    /**
     * Пересчитывает нормали для всех полигонов модели.
     * Нормали полигонов вычисляются как векторное произведение двух сторон треугольника.
     * 
     * @param model модель для пересчета нормалей
     */
    public static void recalculateNormals(Model model) {
        if (model == null || model.polygons == null || model.vertices == null) {
            return;
        }
        
        // Очищаем существующие нормали (пересчитываем даже если они были в файле)
        model.normals.clear();
        
        // Карта для хранения нормалей вершин (для сглаживания)
        Map<Integer, Vector3f> vertexNormals = new HashMap<>();
        
        // Вычисляем нормали для каждого полигона
        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            
            // Полигон должен иметь минимум 3 вершины
            if (vertexIndices.size() < 3) {
                continue;
            }
            
            // Получаем вершины полигона
            Vector3f v0 = model.vertices.get(vertexIndices.get(0));
            Vector3f v1 = model.vertices.get(vertexIndices.get(1));
            Vector3f v2 = model.vertices.get(vertexIndices.get(2));
            
            // Вычисляем два вектора сторон треугольника
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            
            // Нормаль полигона = векторное произведение edge1 x edge2
            Vector3f polygonNormal = edge1.cross(edge2);
            
            // Нормализуем нормаль
            try {
                polygonNormal = polygonNormal.normalize();
            } catch (ArithmeticException e) {
                // Если вектор нулевой (вырожденный треугольник), используем единичный вектор по умолчанию
                polygonNormal = new Vector3f(0, 0, 1);
            }
            
            // Добавляем нормаль в список нормалей модели
            int normalIndex = model.normals.size();
            model.normals.add(polygonNormal);
            
            // Обновляем индексы нормалей в полигоне
            ArrayList<Integer> normalIndices = new ArrayList<>();
            for (int i = 0; i < vertexIndices.size(); i++) {
                normalIndices.add(normalIndex);
            }
            polygon.setNormalIndices(normalIndices);
            
            // Для сглаживания: добавляем нормаль полигона к каждой вершине
            for (Integer vertexIndex : vertexIndices) {
                vertexNormals.merge(vertexIndex, polygonNormal, Vector3f::add);
            }
        }
        
        // Нормализуем нормали вершин (для будущего использования в smooth shading)
        for (Map.Entry<Integer, Vector3f> entry : vertexNormals.entrySet()) {
            try {
                vertexNormals.put(entry.getKey(), entry.getValue().normalize());
            } catch (ArithmeticException e) {
                // Если вектор нулевой, используем единичный вектор по умолчанию
                vertexNormals.put(entry.getKey(), new Vector3f(0, 0, 1));
            }
        }
    }
    
    /**
     * Вычисляет нормаль для одного полигона.
     * 
     * @param model модель, содержащая вершины
     * @param polygon полигон
     * @return нормаль полигона (нормализованная)
     */
    public static Vector3f calculatePolygonNormal(Model model, Polygon polygon) {
        if (model == null || polygon == null || model.vertices == null) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }
        
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        
        if (vertexIndices.size() < 3) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }
        
        Vector3f v0 = model.vertices.get(vertexIndices.get(0));
        Vector3f v1 = model.vertices.get(vertexIndices.get(1));
        Vector3f v2 = model.vertices.get(vertexIndices.get(2));
        
        Vector3f edge1 = v1.subtract(v0);
        Vector3f edge2 = v2.subtract(v0);
        Vector3f normal = edge1.cross(edge2);
        
        try {
            return normal.normalize();
        } catch (ArithmeticException e) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию для вырожденного треугольника
        }
    }
}
