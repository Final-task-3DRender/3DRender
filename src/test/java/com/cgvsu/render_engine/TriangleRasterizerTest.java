package com.cgvsu.render_engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса TriangleRasterizer
 * 
 * Примечание: Тесты с реальным Canvas требуют JavaFX Application Thread,
 * поэтому здесь только базовые проверки без GUI компонентов.
 * Интеграционные тесты можно выполнить через UI приложения.
 */
class TriangleRasterizerTest {

    @Test
    void testTriangleRasterizerClassExists() {
        // Проверяем, что класс существует и имеет метод fillTriangle
        assertNotNull(TriangleRasterizer.class, "Класс TriangleRasterizer должен существовать");
        
        // Проверяем наличие метода fillTriangle через рефлексию
        try {
            TriangleRasterizer.class.getMethod("fillTriangle",
                javafx.scene.canvas.GraphicsContext.class,
                double.class, double.class, javafx.scene.paint.Color.class,
                double.class, double.class, javafx.scene.paint.Color.class,
                double.class, double.class, javafx.scene.paint.Color.class);
        } catch (NoSuchMethodException e) {
            fail("Метод fillTriangle должен существовать");
        }
    }

    @Test
    void testTriangleRasterizerIsPublic() {
        // Проверяем, что класс публичный
        assertTrue(java.lang.reflect.Modifier.isPublic(TriangleRasterizer.class.getModifiers()),
            "Класс TriangleRasterizer должен быть публичным");
    }
}
