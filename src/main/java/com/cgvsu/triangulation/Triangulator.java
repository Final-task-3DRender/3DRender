package com.cgvsu.triangulation;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

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
        for (Polygon polygon : model.getPolygons()) {
            List<Polygon> triangulated = triangulatePolygon(model, polygon);
            newPolygons.addAll(triangulated);
        }
        model.clearPolygons();
        for (Polygon p : newPolygons) {
            model.addPolygon(p);
        }
    }
    
    /**
     * Создает новую триангулированную модель (не модифицирует исходную)
     */
    default Model createTriangulatedModel(Model model) {
        Model triangulatedModel = new Model();
        for (Vector3f v : model.getVertices()) {
            triangulatedModel.addVertex(v);
        }
        for (Vector2f tv : model.getTextureVertices()) {
            triangulatedModel.addTextureVertex(tv);
        }
        for (Vector3f n : model.getNormals()) {
            triangulatedModel.addNormal(n);
        }
        for (Polygon p : model.getPolygons()) {
            triangulatedModel.addPolygon(p);
        }
        triangulateModel(triangulatedModel);
        return triangulatedModel;
    }
    
    /**
     * Триангулирует один полигон, возвращая список треугольников
     */
    List<Polygon> triangulatePolygon(Model model, Polygon polygon);
}
