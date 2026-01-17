package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 3D модель с вершинами, текстурными координатами, нормалями и полигонами.
 */
public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();

    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
    }

    public void addTextureVertex(Vector2f textureVertex) {
        textureVertices.add(textureVertex);
    }

    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    public void clearNormals() {
        normals.clear();
    }

    public void clearPolygons() {
        polygons.clear();
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getTextureVertexCount() {
        return textureVertices.size();
    }

    public int getNormalCount() {
        return normals.size();
    }

    public int getPolygonCount() {
        return polygons.size();
    }

    public boolean isEmpty() {
        return vertices.isEmpty() || polygons.isEmpty();
    }

    public Vector3f getVertex(int index) {
        return vertices.get(index);
    }

    public Vector2f getTextureVertex(int index) {
        return textureVertices.get(index);
    }

    public Vector3f getNormal(int index) {
        return normals.get(index);
    }

    public Polygon getPolygon(int index) {
        return polygons.get(index);
    }

    public ArrayList<Vector3f> getVerticesMutable() {
        return vertices;
    }

    public ArrayList<Vector2f> getTextureVerticesMutable() {
        return textureVertices;
    }

    public ArrayList<Vector3f> getNormalsMutable() {
        return normals;
    }

    public ArrayList<Polygon> getPolygonsMutable() {
        return polygons;
    }
}
