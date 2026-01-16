package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * @version 2.0
 */
public class Model {

    /**
     * Список вершин модели в трехмерном пространстве.
     * Каждая вершина представлена как Vector3f (x, y, z).
     */
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    
    /**
     * Список текстурных координат (UV координаты).
     * Каждая координата представлена как Vector2f (u, v), где значения обычно в диапазоне [0, 1].
     */
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    
    /**
     * Список нормалей вершин.
     * Нормали используются для расчета освещения и определения ориентации граней.
     * Каждая нормаль представлена как Vector3f и должна быть нормализована.
     */
    public ArrayList<Vector3f> normals = new ArrayList<>();
    
    /**
     * Список полигонов (граней) модели.
     * Каждый полигон ссылается на индексы вершин, текстурных координат и нормалей.
     * Полигоны должны быть триангулированы (состоять из треугольников) перед рендерингом.
     */
    public ArrayList<Polygon> polygons = new ArrayList<>();

    /**
     * Возвращает список вершин модели.
     * 
     * @return список вершин (неизменяемый вид для чтения)
     */
    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    /**
     * Возвращает список текстурных координат модели.
     * 
     * @return список текстурных координат (неизменяемый вид для чтения)
     */
    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    /**
     * Возвращает список нормалей модели.
     * 
     * @return список нормалей (неизменяемый вид для чтения)
     */
    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    /**
     * Возвращает список полигонов модели.
     * 
     * @return список полигонов (неизменяемый вид для чтения)
     */
    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    /**
     * Добавляет вершину в модель.
     * 
     * @param vertex вершина для добавления (может быть null, но не рекомендуется)
     */
    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
    }

    /**
     * Добавляет текстурную координату в модель.
     * 
     * @param textureVertex текстурная координата для добавления (может быть null, но не рекомендуется)
     */
    public void addTextureVertex(Vector2f textureVertex) {
        textureVertices.add(textureVertex);
    }

    /**
     * Добавляет нормаль в модель.
     * 
     * @param normal нормаль для добавления (может быть null, но не рекомендуется)
     */
    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    /**
     * Добавляет полигон в модель.
     * 
     * @param polygon полигон для добавления (может быть null, но не рекомендуется)
     */
    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    /**
     * Очищает список нормалей модели.
     * Используется при пересчете нормалей.
     */
    public void clearNormals() {
        normals.clear();
    }

    /**
     * Очищает список полигонов модели.
     * Используется при триангуляции и других операциях.
     */
    public void clearPolygons() {
        polygons.clear();
    }

    /**
     * Возвращает количество вершин в модели.
     * 
     * @return количество вершин
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * Возвращает количество текстурных координат в модели.
     * 
     * @return количество текстурных координат
     */
    public int getTextureVertexCount() {
        return textureVertices.size();
    }

    /**
     * Возвращает количество нормалей в модели.
     * 
     * @return количество нормалей
     */
    public int getNormalCount() {
        return normals.size();
    }

    /**
     * Возвращает количество полигонов в модели.
     * 
     * @return количество полигонов
     */
    public int getPolygonCount() {
        return polygons.size();
    }

    /**
     * Проверяет, пуста ли модель (не содержит вершин или полигонов).
     * 
     * @return true если модель пуста
     */
    public boolean isEmpty() {
        return vertices.isEmpty() || polygons.isEmpty();
    }

    /**
     * Получает вершину по индексу.
     * 
     * @param index индекс вершины
     * @return вершина по указанному индексу
     * @throws IndexOutOfBoundsException если индекс выходит за границы
     */
    public Vector3f getVertex(int index) {
        return vertices.get(index);
    }

    /**
     * Получает текстурную координату по индексу.
     * 
     * @param index индекс текстурной координаты
     * @return текстурная координата по указанному индексу
     * @throws IndexOutOfBoundsException если индекс выходит за границы
     */
    public Vector2f getTextureVertex(int index) {
        return textureVertices.get(index);
    }

    /**
     * Получает нормаль по индексу.
     * 
     * @param index индекс нормали
     * @return нормаль по указанному индексу
     * @throws IndexOutOfBoundsException если индекс выходит за границы
     */
    public Vector3f getNormal(int index) {
        return normals.get(index);
    }

    /**
     * Получает полигон по индексу.
     * 
     * @param index индекс полигона
     * @return полигон по указанному индексу
     * @throws IndexOutOfBoundsException если индекс выходит за границы
     */
    public Polygon getPolygon(int index) {
        return polygons.get(index);
    }

    /**
     * Возвращает изменяемый список вершин модели.
     * Используется для внутренних операций модификации модели.
     * 
     * @return изменяемый список вершин
     */
    public ArrayList<Vector3f> getVerticesMutable() {
        return vertices;
    }

    /**
     * Возвращает изменяемый список текстурных координат модели.
     * Используется для внутренних операций модификации модели.
     * 
     * @return изменяемый список текстурных координат
     */
    public ArrayList<Vector2f> getTextureVerticesMutable() {
        return textureVertices;
    }

    /**
     * Возвращает изменяемый список нормалей модели.
     * Используется для внутренних операций модификации модели.
     * 
     * @return изменяемый список нормалей
     */
    public ArrayList<Vector3f> getNormalsMutable() {
        return normals;
    }

    /**
     * Возвращает изменяемый список полигонов модели.
     * Используется для внутренних операций модификации модели.
     * 
     * @return изменяемый список полигонов
     */
    public ArrayList<Polygon> getPolygonsMutable() {
        return polygons;
    }
}
