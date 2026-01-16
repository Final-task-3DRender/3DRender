package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Утилитный класс для вычисления нормалей полигонов и вершин 3D моделей.
 * 
 * <p>Реализует задание для третьего человека: "Нормали следует пересчитывать даже если те сохранены в файле,
 * потому что мы не можем им доверять."
 * 
 * <p>Нормали необходимы для:
 * <ul>
 *   <li>Правильного расчета освещения (задание для третьего человека)</li>
 *   <li>Определения ориентации граней (front-facing/back-facing) для backface culling</li>
 *   <li>Визуализации сглаженных поверхностей (smooth shading) - сглаживание нормалей</li>
 * </ul>
 * 
 * <p>Алгоритм вычисления нормалей (согласно теории):
 * <ol>
 *   <li>Для каждого полигона вычисляется нормаль как векторное произведение двух сторон треугольника</li>
 *   <li>Нормаль нормализуется (приводится к единичной длине)</li>
 *   <li>Для каждой вершины накапливаются нормали всех полигонов, содержащих эту вершину</li>
 *   <li>Нормали вершин нормализуются (для smooth shading)</li>
 * </ol>
 * 
 * <p>Нормали полигонов вычисляются как векторное произведение двух сторон треугольника.
 * Нормали вершин вычисляются как среднее значение нормалей всех полигонов, содержащих эту вершину.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class NormalCalculator {
    
    /**
     * Пересчитывает нормали для всех полигонов модели.
     * 
     * <p>Алгоритм:
     * <ol>
     *   <li>Очищает существующие нормали модели</li>
     *   <li>Для каждого полигона вычисляет нормаль как векторное произведение двух сторон</li>
     *   <li>Нормализует нормаль полигона</li>
     *   <li>Для каждой вершины накапливает нормали всех полигонов, содержащих эту вершину</li>
     *   <li>Нормализует нормали вершин (для будущего использования в smooth shading)</li>
     * </ol>
     * 
     * <p>Модифицирует переданную модель, перезаписывая существующие нормали.
     * 
     * @param model модель для пересчета нормалей (может быть null, в этом случае метод ничего не делает)
     */
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
                continue; // Пропускаем полигоны с некорректными индексами
            }
            
            Vector3f v0 = model.getVertex(idx0);
            Vector3f v1 = model.getVertex(idx1);
            Vector3f v2 = model.getVertex(idx2);
            
            if (v0 == null || v1 == null || v2 == null) {
                continue; // Пропускаем полигоны с null вершинами
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
    
    /**
     * Вычисляет нормаль для одного полигона.
     * 
     * <p>Нормаль вычисляется как векторное произведение двух сторон треугольника,
     * образованного первыми тремя вершинами полигона.
     * 
     * <p>Если полигон вырожденный (нулевая площадь) или не может быть обработан,
     * возвращается нормаль по умолчанию (0, 0, 1).
     * 
     * @param model модель, содержащая вершины (может быть null)
     * @param polygon полигон для вычисления нормали (может быть null)
     * @return нормализованная нормаль полигона или (0, 0, 1) по умолчанию
     */
    public static Vector3f calculatePolygonNormal(Model model, Polygon polygon) {
        if (model == null || polygon == null || model.getVertexCount() == 0) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }
        
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        
        if (vertexIndices.size() < 3) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }
        
        int idx0 = vertexIndices.get(0);
        int idx1 = vertexIndices.get(1);
        int idx2 = vertexIndices.get(2);
        
        if (idx0 < 0 || idx0 >= model.getVertexCount() ||
            idx1 < 0 || idx1 >= model.getVertexCount() ||
            idx2 < 0 || idx2 >= model.getVertexCount()) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }
        
        Vector3f v0 = model.getVertex(idx0);
        Vector3f v1 = model.getVertex(idx1);
        Vector3f v2 = model.getVertex(idx2);
        
        if (v0 == null || v1 == null || v2 == null) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }
        
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
