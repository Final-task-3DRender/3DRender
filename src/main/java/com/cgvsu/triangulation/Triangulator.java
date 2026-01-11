package com.cgvsu.triangulation;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Интерфейс для триангуляции моделей
 */
public interface Triangulator {
    
    /**
     * Триангулирует переданную модель (модифицирует исходную модель)
     */
    default void triangulateModel(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>();
        for (Polygon polygon : model.polygons) {
            List<Polygon> triangulated = triangulatePolygon(model, polygon);
            newPolygons.addAll(triangulated);
        }
        model.polygons = newPolygons;
    }
    
    /**
     * Создает новую триангулированную модель (не модифицирует исходную)
     */
    default Model createTriangulatedModel(Model model) {
        Model triangulatedModel = new Model();
        triangulatedModel.vertices = new ArrayList<>(model.vertices);
        triangulatedModel.normals = new ArrayList<>(model.normals);
        triangulatedModel.textureVertices = new ArrayList<>(model.textureVertices);
        triangulatedModel.polygons = new ArrayList<>(model.polygons);
        triangulateModel(triangulatedModel);
        return triangulatedModel;
    }
    
    /**
     * Триангулирует один полигон, возвращая список треугольников
     */
    List<Polygon> triangulatePolygon(Model model, Polygon polygon);
}
