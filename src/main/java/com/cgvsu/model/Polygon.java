package com.cgvsu.model;

import java.util.ArrayList;

/**
 * Полигон (грань) модели с индексами вершин, текстурных координат и нормалей.
 */
public class Polygon {

    private ArrayList<Integer> vertexIndices;
    private ArrayList<Integer> textureVertexIndices;
    private ArrayList<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<Integer>();
        textureVertexIndices = new ArrayList<Integer>();
        normalIndices = new ArrayList<Integer>();
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

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
