package com.cgvsu.removers;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;

public class PolygonRemover {

    public static void deletePolygons(
            Model model,
            Set<Integer> polygonIndicesToDelete,
            boolean deleteFreeVertices
    ) {

        ArrayList<Polygon> polygons = model.getPolygonsMutable();
        ArrayList<Vector3f> vertices = model.getVerticesMutable();
        ArrayList<Vector2f> textureVertices = model.getTextureVerticesMutable();
        ArrayList<Vector3f> normals = model.getNormalsMutable();

        Set<Integer> allInitiallyUsedVertexes = new HashSet<>();
        for (Polygon p : polygons) {
            allInitiallyUsedVertexes.addAll(p.getVertexIndices());
        }

        Set<Integer> allInitiallyUsedTextureVertexes = new HashSet<>();
        for (Polygon p : polygons) {
            allInitiallyUsedTextureVertexes.addAll(p.getTextureVertexIndices());
        }

        Set<Integer> allInitiallyUsedNormals = new HashSet<>();
        for (Polygon p : polygons) {
            allInitiallyUsedNormals.addAll(p.getNormalIndices());
        }

        ArrayList<Polygon> newPolygons = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            if (!polygonIndicesToDelete.contains(i)) {
                newPolygons.add(polygons.get(i));
            }
        }
        polygons.clear();
        polygons.addAll(newPolygons);

        if (!deleteFreeVertices) return;

        Set<Integer> currentlyUsedVertices = new HashSet<>();
        for (Polygon p : polygons) {
            currentlyUsedVertices.addAll(p.getVertexIndices());
        }

        Set<Integer> currentlyUsedVerticesT = new HashSet<>();
        for (Polygon p : polygons) {
            currentlyUsedVerticesT.addAll(p.getTextureVertexIndices());
        }

        Set<Integer> currentlyUsedNormal = new HashSet<>();
        for (Polygon p : polygons) {
            currentlyUsedNormal.addAll(p.getNormalIndices());
        }

        Set<Integer> verticesToKeep = new HashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            if (currentlyUsedVertices.contains(i) || !allInitiallyUsedVertexes.contains(i)) {
                verticesToKeep.add(i);
            }
        }

        Set<Integer> verticesTToKeep = new HashSet<>();
        for (int i = 0; i < textureVertices.size(); i++) {
            if (currentlyUsedVerticesT.contains(i) || !allInitiallyUsedTextureVertexes.contains(i)) {
                verticesTToKeep.add(i);
            }
        }

        Set<Integer> normalToKeep = new HashSet<>();
        for (int i = 0; i < normals.size(); i++) {
            if (currentlyUsedNormal.contains(i) || !allInitiallyUsedNormals.contains(i)) {
                normalToKeep.add(i);
            }
        }

        Map<Integer, Integer> vMap = rebuildList(vertices, verticesToKeep);
        Map<Integer, Integer> vtMap = rebuildList(textureVertices, verticesTToKeep);
        Map<Integer, Integer> vnMap = rebuildList(normals, normalToKeep);

        for (Polygon p : polygons) {
            remapVertices(p.getVertexIndices(), vMap);
            remapVertices(p.getTextureVertexIndices(), vtMap);
            remapVertices(p.getNormalIndices(), vnMap);
        }
    }

    private static <T> Map<Integer, Integer> rebuildList(
            ArrayList<T> list,
            Set<Integer> toKeep
    ) {
        ArrayList<T> newList = new ArrayList<>();
        Map<Integer, Integer> map = new HashMap<>();
        int newIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (toKeep.contains(i)) {
                newList.add(list.get(i));
                map.put(i, newIndex++);
            }
        }
        list.clear();
        list.addAll(newList);
        return map;
    }

    private static void remapVertices(ArrayList<Integer> indices, Map<Integer, Integer> map) {
        if (indices.isEmpty()) return;
        for (int i = 0; i < indices.size(); i++) {
            Integer newIndex = map.get(indices.get(i));
            if (newIndex != null) {
                indices.set(i, newIndex);
            }
        }
    }
}
