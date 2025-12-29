package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class ObjWriter {

    public static void saveModel(Model model, String filename) throws IOException {
        if (model == null) {
            throw new IOException("Invalid model provided!");
        }

        try (FileWriter fileWriter = new FileWriter(filename)) {
            writeHeader(fileWriter, model);

            writeVertices(fileWriter, model.vertices);
            writeTextureCoordinates(fileWriter, model.textureVertices);
            writeNormals(fileWriter, model.normals);

            writePolygons(fileWriter, model);
        }
    }

    private static void writeHeader(FileWriter writer, Model model) throws IOException {
        writer.write("# Created by ObjWriter\n");
        writer.write("# Vertices: " + model.vertices.size() + "\n");
        writer.write("# Texture coordinates: " + model.textureVertices.size() + "\n");
        writer.write("# Normals: " + model.normals.size() + "\n");
        writer.write("# Polygons: " + model.polygons.size() + "\n\n");
    }

    private static void writeVertices(FileWriter writer, ArrayList<Vector3f> vertices) throws IOException {
        for (Vector3f v : vertices) {
            writer.write(String.format(Locale.US, "v %.6f %.6f %.6f\n", v.x, v.y, v.z));
        }
        if (!vertices.isEmpty()) writer.write("\n");
    }

    private static void writeTextureCoordinates(FileWriter writer, ArrayList<Vector2f> textures) throws IOException {
        for (Vector2f uv : textures) {
            writer.write(String.format(Locale.US, "vt %.6f %.6f\n", uv.x, uv.y));
        }
        if (!textures.isEmpty()) writer.write("\n");
    }

    private static void writeNormals(FileWriter writer, ArrayList<Vector3f> normals) throws IOException {
        for (Vector3f n : normals) {
            writer.write(String.format(Locale.US, "vn %.6f %.6f %.6f\n", n.x, n.y, n.z));
        }
        if (!normals.isEmpty()) writer.write("\n");
    }

    private static void writePolygons(FileWriter writer, Model model) throws IOException {
        for (int i = 0; i < model.polygons.size(); i++) {
            Polygon polygon = model.polygons.get(i);

            if (polygon == null) {
                throw new IOException("Polygon " + i + " is invalid");
            }

            ArrayList<Integer> vIndices = polygon.getVertexIndices();
            ArrayList<Integer> tIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> nIndices = polygon.getNormalIndices();

            validatePolygon(i, vIndices, tIndices, nIndices,
                    model.vertices.size(), model.textureVertices.size(), model.normals.size());

            writer.write(constructPolygonString(vIndices, tIndices, nIndices) + "\n");
        }
    }

    private static void validatePolygon(int polygonIndex,
                                        ArrayList<Integer> vIndices,
                                        ArrayList<Integer> tIndices,
                                        ArrayList<Integer> nIndices,
                                        int totalVertices, int totalTextures, int totalNormals) throws IOException {

        if (vIndices == null || vIndices.isEmpty()) {
            throw new IOException("Polygon " + polygonIndex + " has no vertices");
        }

        int vertexCount = vIndices.size();

        if (tIndices != null && !tIndices.isEmpty() && tIndices.size() != vertexCount) {
            throw new IOException("Polygon " + polygonIndex + ": UV count mismatch (" +
                    tIndices.size() + " vs " + vertexCount + ")");
        }

        if (nIndices != null && !nIndices.isEmpty() && nIndices.size() != vertexCount) {
            throw new IOException("Polygon " + polygonIndex + ": Normal count mismatch (" +
                    nIndices.size() + " vs " + vertexCount + ")");
        }

        // Валидация границ индексов
        validateIndexRange(polygonIndex, vIndices, "vertex", totalVertices);
        if (tIndices != null && !tIndices.isEmpty()) {
            validateIndexRange(polygonIndex, tIndices, "texture", totalTextures);
        }
        if (nIndices != null && !nIndices.isEmpty()) {
            validateIndexRange(polygonIndex, nIndices, "normal", totalNormals);
        }
    }

    private static void validateIndexRange(int polygonIndex, ArrayList<Integer> indices,
                                           String type, int maxValue) throws IOException {
        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            if (idx < 0 || idx >= maxValue) {
                throw new IOException(String.format("Polygon %d, %s %d: index %d out of range [0, %d]",
                        polygonIndex, type, i, idx, maxValue - 1));
            }
        }
    }

    private static String constructPolygonString(ArrayList<Integer> vIndices,
                                                 ArrayList<Integer> tIndices,
                                                 ArrayList<Integer> nIndices) {

        StringBuilder polygonBuilder = new StringBuilder("f");

        for (int i = 0; i < vIndices.size(); i++) {
            polygonBuilder.append(" ");
            polygonBuilder.append(vIndices.get(i) + 1);

            boolean hasTex = tIndices != null && !tIndices.isEmpty() && i < tIndices.size();
            boolean hasNorm = nIndices != null && !nIndices.isEmpty() && i < nIndices.size();

            if (hasTex && hasNorm) {
                polygonBuilder.append("/").append(tIndices.get(i) + 1)
                        .append("/").append(nIndices.get(i) + 1);
            } else if (hasTex) {
                polygonBuilder.append("/").append(tIndices.get(i) + 1);
            } else if (hasNorm) {
                polygonBuilder.append("//").append(nIndices.get(i) + 1);
            }
        }

        return polygonBuilder.toString();
    }
}