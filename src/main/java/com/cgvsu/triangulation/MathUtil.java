package com.cgvsu.triangulation;

/**
 * Математические утилиты для триангуляции
 */
public class MathUtil {
    
    /**
     * Решает систему линейных уравнений методом Крамера
     * a*x + b*y = v1
     * c*x + d*y = v2
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
     * Вычисляет определитель матрицы 2x2
     */
    private static double calcDetermination(double a11, double a12, double a21, double a22) {
        return a11 * a22 - a12 * a21;
    }
    
    /**
     * Вычисляет площадь треугольника по формуле Герона по координатам вершин
     */
    public static double calcSquareByGeroneByVertices(double x0, double y0, double x1, double y1, double x2, double y2) {
        double AB = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
        double AC = Math.sqrt(Math.pow(x0 - x2, 2) + Math.pow(y0 - y2, 2));
        double BC = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        
        double semiPerimeter = (AB + AC + BC) / 2;
        return Math.sqrt(semiPerimeter * (semiPerimeter - AB) * (semiPerimeter - AC) * (semiPerimeter - BC));
    }
}
