package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;

/**
 * Представляет 3D модель, загруженную из файла OBJ.
 * 
 * <p>Модель содержит:
 * <ul>
 *   <li>Вершины (vertices) - трехмерные точки в пространстве</li>
 *   <li>Текстурные координаты (textureVertices) - двумерные координаты для наложения текстур</li>
 *   <li>Нормали (normals) - векторы нормалей для расчета освещения</li>
 *   <li>Полигоны (polygons) - грани модели, определяющие форму</li>
 * </ul>
 * 
 * <p>Все полигоны должны быть триангулированы (состоять из треугольников) перед рендерингом.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class Model {

    /**
     * Список вершин модели в трехмерном пространстве.
     * Каждая вершина представлена как Vector3f (x, y, z).
     */
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    
    /**
     * Список текстурных координат (UV координаты).
     * Каждая координата представлена как Vector2f (u, v), где значения обычно в диапазоне [0, 1].
     */
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    
    /**
     * Список нормалей вершин.
     * Нормали используются для расчета освещения и определения ориентации граней.
     * Каждая нормаль представлена как Vector3f и должна быть нормализована.
     */
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    
    /**
     * Список полигонов (граней) модели.
     * Каждый полигон ссылается на индексы вершин, текстурных координат и нормалей.
     * Полигоны должны быть триангулированы (состоять из треугольников) перед рендерингом.
     */
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();
}
