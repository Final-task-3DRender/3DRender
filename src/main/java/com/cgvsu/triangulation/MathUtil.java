package com.cgvsu.triangulation;

/**
 * Утилитный класс для математических операций, используемых при триангуляции.
 * 
 * <p>Содержит методы для решения систем линейных уравнений и вычисления
 * геометрических характеристик.
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class MathUtil {
    
    /**
     * Решает систему линейных уравнений методом Крамера.
     * 
     * <p>Система уравнений:
     * <pre>
     * a*x + b*y = v1
     * c*x + d*y = v2
     * </pre>
     * 
     * @param a коэффициент при x в первом уравнении
     * @param b коэффициент при y в первом уравнении
     * @param c коэффициент при x во втором уравнении
     * @param d коэффициент при y во втором уравнении
     * @param v1 правая часть первого уравнения
     * @param v2 правая часть второго уравнения
     * @return массив из двух элементов [x, y] - решение системы.
     *         Если система вырожденная (определитель равен 0), возвращает [Double.MIN_VALUE, Double.MIN_VALUE]
     */
    public static double[] solveByKramer(double a, double b, double c, double d, double v1, double v2) {
        double deltaMain = calcDetermination(a, b, c, d);
        if (Math.abs(deltaMain) < Constants.EPS) {
            return new double[]{Double.MIN_VALUE, Double.MIN_VALUE};
        }
        double delta1 = calcDetermination(v1, b, v2, d);
        double delta2 = calcDetermination(a, v1, c, v2);
        return new double[]{delta1 / deltaMain, delta2 / deltaMain};
    }
    
    /**
     * Вычисляет определитель матрицы 2x2.
     * 
     * <p>Формула: det = a11 * a22 - a12 * a21
     * 
     * @param a11 элемент матрицы [0,0]
     * @param a12 элемент матрицы [0,1]
     * @param a21 элемент матрицы [1,0]
     * @param a22 элемент матрицы [1,1]
     * @return определитель матрицы
     */
    private static double calcDetermination(double a11, double a12, double a21, double a22) {
        return a11 * a22 - a12 * a21;
    }
    
    /**
     * Вычисляет площадь треугольника по формуле Герона по координатам вершин.
     * 
     * <p>Формула Герона: S = sqrt(p * (p - a) * (p - b) * (p - c)),
     * где p - полупериметр, a, b, c - длины сторон треугольника.
     * 
     * @param x0 координата X первой вершины
     * @param y0 координата Y первой вершины
     * @param x1 координата X второй вершины
     * @param y1 координата Y второй вершины
     * @param x2 координата X третьей вершины
     * @param y2 координата Y третьей вершины
     * @return площадь треугольника
     */
    public static double calcSquareByGeroneByVertices(double x0, double y0, double x1, double y1, double x2, double y2) {
        double AB = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
        double AC = Math.sqrt(Math.pow(x0 - x2, 2) + Math.pow(y0 - y2, 2));
        double BC = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        
        double semiPerimeter = (AB + AC + BC) / 2;
        return Math.sqrt(semiPerimeter * (semiPerimeter - AB) * (semiPerimeter - AC) * (semiPerimeter - BC));
    }
}
