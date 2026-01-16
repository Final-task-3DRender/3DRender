package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Класс для записи 3D моделей в формат OBJ.
 * 
 * <p>Поддерживает запись:
 * <ul>
 *   <li>Вершин (v)</li>
 *   <li>Текстурных координат (vt)</li>
 *   <li>Нормалей (vn)</li>
 *   <li>Полигонов (f) с поддержкой текстурных координат и нормалей</li>
 * </ul>
 * 
 * <p>Формат записи соответствует стандарту OBJ файлов.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class ObjWriter {

    /**
     * Сохраняет модель в файл формата OBJ.
     * 
     * @param model модель для сохранения (не должна быть null)
     * @param filename путь к файлу для сохранения
     * @throws IOException если произошла ошибка при записи файла
     * @throws IllegalArgumentException если model равен null
     */
    public static void saveModel(Model model, String filename) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try (FileWriter fileWriter = new FileWriter(filename)) {
            writeHeader(fileWriter, model);

            writeVertices(fileWriter, model.vertices);
            writeTextureCoordinates(fileWriter, model.textureVertices);
            writeNormals(fileWriter, model.normals);

            writePolygons(fileWriter, model);
        }
    }

    /**
     * Записывает заголовок OBJ файла с информацией о модели.
     * 
     * @param writer FileWriter для записи
     * @param model модель для получения статистики
     * @throws IOException если произошла ошибка записи
     */
    private static void writeHeader(FileWriter writer, Model model) throws IOException {
        writer.write("# Created by ObjWriter\n");
        writer.write("# Vertices: " + model.vertices.size() + "\n");
        writer.write("# Texture coordinates: " + model.textureVertices.size() + "\n");
        writer.write("# Normals: " + model.normals.size() + "\n");
        writer.write("# Polygons: " + model.polygons.size() + "\n\n");
    }

    /**
     * Записывает вершины модели в OBJ формат.
     * 
     * @param writer FileWriter для записи
     * @param vertices список вершин для записи
     * @throws IOException если произошла ошибка записи
     */
    private static void writeVertices(FileWriter writer, ArrayList<Vector3f> vertices) throws IOException {
        for (Vector3f v : vertices) {
            writer.write(String.format(Locale.US, "v %.6f %.6f %.6f\n", v.x, v.y, v.z));
        }
        if (!vertices.isEmpty()) writer.write("\n");
    }

    /**
     * Записывает текстурные координаты модели в OBJ формат.
     * 
     * @param writer FileWriter для записи
     * @param textures список текстурных координат для записи
     * @throws IOException если произошла ошибка записи
     */
    private static void writeTextureCoordinates(FileWriter writer, ArrayList<Vector2f> textures) throws IOException {
        for (Vector2f uv : textures) {
            writer.write(String.format(Locale.US, "vt %.6f %.6f\n", uv.x, uv.y));
        }
        if (!textures.isEmpty()) writer.write("\n");
    }

    /**
     * Записывает нормали модели в OBJ формат.
     * 
     * @param writer FileWriter для записи
     * @param normals список нормалей для записи
     * @throws IOException если произошла ошибка записи
     */
    private static void writeNormals(FileWriter writer, ArrayList<Vector3f> normals) throws IOException {
        for (Vector3f n : normals) {
            writer.write(String.format(Locale.US, "vn %.6f %.6f %.6f\n", n.x, n.y, n.z));
        }
        if (!normals.isEmpty()) writer.write("\n");
    }

    /**
     * Записывает полигоны модели в OBJ формат.
     * 
     * <p>Формат: f v1[/vt1][/vn1] v2[/vt2][/vn2] ...
     * где v - индекс вершины, vt - индекс текстурной координаты, vn - индекс нормали.
     * 
     * @param writer FileWriter для записи
     * @param model модель с полигонами для записи
     * @throws IOException если произошла ошибка записи или полигон невалиден
     */
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

    /**
     * Валидирует полигон перед записью.
     * 
     * <p>Проверяет:
     * <ul>
     *   <li>Наличие вершин</li>
     *   <li>Согласованность количества текстурных координат и нормалей с количеством вершин</li>
     *   <li>Валидность индексов (не выходят за границы массивов)</li>
     * </ul>
     * 
     * @param polygonIndex индекс полигона (для сообщений об ошибках)
     * @param vIndices индексы вершин
     * @param tIndices индексы текстурных координат (может быть null)
     * @param nIndices индексы нормалей (может быть null)
     * @param totalVertices общее количество вершин в модели
     * @param totalTextures общее количество текстурных координат в модели
     * @param totalNormals общее количество нормалей в модели
     * @throws IOException если полигон невалиден
     */
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

        validateIndexRange(polygonIndex, vIndices, "vertex", totalVertices);
        if (tIndices != null && !tIndices.isEmpty()) {
            validateIndexRange(polygonIndex, tIndices, "texture", totalTextures);
        }
        if (nIndices != null && !nIndices.isEmpty()) {
            validateIndexRange(polygonIndex, nIndices, "normal", totalNormals);
        }
    }

    /**
     * Валидирует диапазон индексов для полигона.
     * 
     * @param polygonIndex индекс полигона (для сообщений об ошибках)
     * @param indices список индексов для проверки
     * @param type тип индексов ("vertex", "texture", "normal") для сообщений об ошибках
     * @param maxValue максимальное допустимое значение индекса
     * @throws IOException если какой-либо индекс выходит за границы [0, maxValue)
     */
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

    /**
     * Строит строку полигона в формате OBJ.
     * 
     * <p>Формат: f v1[/vt1][/vn1] v2[/vt2][/vn2] v3[/vt3][/vn3] ...
     * Индексы в OBJ формате начинаются с 1, поэтому к каждому индексу добавляется 1.
     * 
     * @param vIndices индексы вершин
     * @param tIndices индексы текстурных координат (может быть null или пустым)
     * @param nIndices индексы нормалей (может быть null или пустым)
     * @return строка полигона в формате OBJ
     */
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