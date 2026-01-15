package com.cgvsu.model;

import java.util.ArrayList;

/**
 * Представляет полигон (грань) 3D модели.
 * 
 * <p>Полигон состоит из вершин, которые могут иметь текстурные координаты и нормали.
 * Все индексы ссылаются на соответствующие массивы в объекте {@link Model}.
 * 
 * <p>Полигон должен содержать минимум 3 вершины (треугольник). Для рендеринга
 * все полигоны должны быть триангулированы.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class Polygon {

    /**
     * Индексы вершин полигона в массиве vertices модели.
     * Должно быть минимум 3 индекса.
     */
    private ArrayList<Integer> vertexIndices;
    
    /**
     * Индексы текстурных координат в массиве textureVertices модели.
     * Может быть пустым, если полигон не имеет текстурных координат.
     * Если не пусто, размер должен совпадать с размером vertexIndices.
     */
    private ArrayList<Integer> textureVertexIndices;
    
    /**
     * Индексы нормалей в массиве normals модели.
     * Может быть пустым, если полигон не имеет нормалей.
     * Если не пусто, размер должен совпадать с размером vertexIndices.
     */
    private ArrayList<Integer> normalIndices;

    /**
     * Создает новый пустой полигон.
     */
    public Polygon() {
        vertexIndices = new ArrayList<Integer>();
        textureVertexIndices = new ArrayList<Integer>();
        normalIndices = new ArrayList<Integer>();
    }

    /**
     * Устанавливает индексы вершин полигона.
     * 
     * @param vertexIndices список индексов вершин (должно быть минимум 3)
     * @throws AssertionError если количество вершин меньше 3
     */
    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    /**
     * Устанавливает индексы текстурных координат полигона.
     * 
     * @param textureVertexIndices список индексов текстурных координат (должно быть минимум 3)
     * @throws AssertionError если количество индексов меньше 3
     */
    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    /**
     * Устанавливает индексы нормалей полигона.
     * 
     * @param normalIndices список индексов нормалей (должно быть минимум 3)
     * @throws AssertionError если количество индексов меньше 3
     */
    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    /**
     * Возвращает индексы вершин полигона.
     * 
     * @return список индексов вершин
     */
    public ArrayList<Integer> getVertexIndices() {
        return vertexIndices;
    }

    /**
     * Возвращает индексы текстурных координат полигона.
     * 
     * @return список индексов текстурных координат (может быть пустым)
     */
    public ArrayList<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    /**
     * Возвращает индексы нормалей полигона.
     * 
     * @return список индексов нормалей (может быть пустым)
     */
    public ArrayList<Integer> getNormalIndices() {
        return normalIndices;
    }
}
