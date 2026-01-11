package com.cgvsu.triangulation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Тесты для MathUtil
 */
class MathUtilTest {
    
    private static final double EPSILON = 1e-5;
    
    @Test
    void testStandardRightTriangle() {
        double x0 = 0.0, y0 = 0.0;
        double x1 = 3.0, y1 = 0.0;
        double x2 = 0.0, y2 = 4.0;
        double expectedArea = 6.0;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, EPSILON);
    }
    
    @Test
    void testArbitraryTriangle() {
        double x0 = 1.0, y0 = 1.0;
        double x1 = 4.0, y1 = 5.0;
        double x2 = 6.0, y2 = 2.0;
        double expectedArea = 8.5;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, EPSILON);
    }
    
    @Test
    void testCollinearPoints() {
        double x0 = 0.0, y0 = 0.0;
        double x1 = 1.0, y1 = 0.0;
        double x2 = 5.0, y2 = 0.0;
        double expectedArea = 0.0;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, EPSILON);
    }
    
    @Test
    void testAllPointsSame() {
        double x0 = 1.0, y0 = 1.0;
        double x1 = 1.0, y1 = 1.0;
        double x2 = 1.0, y2 = 1.0;
        double expectedArea = 0.0;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, EPSILON);
    }
    
    @Test
    void testFractionalCoordinates() {
        double x0 = 0.0, y0 = 0.0;
        double x1 = 2.5, y1 = 0.0;
        double x2 = 0.0, y2 = 3.0;
        double expectedArea = 3.75;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, EPSILON);
    }
    
    @Test
    void testSolveByKramer() {
        // Система: 2x + 3y = 8
        //           4x + 5y = 14
        // Решение: x = 1, y = 2
        double[] result = MathUtil.solveByKramer(2, 3, 4, 5, 8, 14);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.length);
        Assertions.assertEquals(1.0, result[0], EPSILON);
        Assertions.assertEquals(2.0, result[1], EPSILON);
    }
    
    @Test
    void testSolveByKramer_NoSolution() {
        // Система: 2x + 4y = 8
        //           1x + 2y = 4
        // Определитель = 0, нет единственного решения
        double[] result = MathUtil.solveByKramer(2, 4, 1, 2, 8, 4);
        Assertions.assertNotNull(result);
        // При deltaMain = 0 возвращаются специальные значения
        Assertions.assertEquals(2, result.length);
    }
}
