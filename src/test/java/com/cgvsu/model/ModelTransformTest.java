package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты для класса ModelTransform
 */
class ModelTransformTest {
    
    private static final float EPSILON = 1e-5f;
    
    /**
     * Тест конструктора по умолчанию (дефолтные значения).
     */
    @Test
    void testDefaultConstructor() {
        ModelTransform transform = new ModelTransform();
        
        // Дефолтные значения: position=(0,0,0), rotation=(0,0,0), scale=(1,1,1)
        Vector3f position = transform.getPosition();
        Vector3f rotation = transform.getRotation();
        Vector3f scale = transform.getScale();
        
        Assertions.assertEquals(0.0f, position.x, EPSILON);
        Assertions.assertEquals(0.0f, position.y, EPSILON);
        Assertions.assertEquals(0.0f, position.z, EPSILON);
        
        Assertions.assertEquals(0.0f, rotation.x, EPSILON);
        Assertions.assertEquals(0.0f, rotation.y, EPSILON);
        Assertions.assertEquals(0.0f, rotation.z, EPSILON);
        
        Assertions.assertEquals(1.0f, scale.x, EPSILON);
        Assertions.assertEquals(1.0f, scale.y, EPSILON);
        Assertions.assertEquals(1.0f, scale.z, EPSILON);
    }
    
    /**
     * Тест конструктора с параметрами.
     */
    @Test
    void testParameterizedConstructor() {
        Vector3f position = new Vector3f(1, 2, 3);
        Vector3f rotation = new Vector3f(10, 20, 30);
        Vector3f scale = new Vector3f(2, 3, 4);
        
        ModelTransform transform = new ModelTransform(position, rotation, scale);
        
        Vector3f resultPosition = transform.getPosition();
        Vector3f resultRotation = transform.getRotation();
        Vector3f resultScale = transform.getScale();
        
        Assertions.assertEquals(1.0f, resultPosition.x, EPSILON);
        Assertions.assertEquals(2.0f, resultPosition.y, EPSILON);
        Assertions.assertEquals(3.0f, resultPosition.z, EPSILON);
        
        Assertions.assertEquals(10.0f, resultRotation.x, EPSILON);
        Assertions.assertEquals(20.0f, resultRotation.y, EPSILON);
        Assertions.assertEquals(30.0f, resultRotation.z, EPSILON);
        
        Assertions.assertEquals(2.0f, resultScale.x, EPSILON);
        Assertions.assertEquals(3.0f, resultScale.y, EPSILON);
        Assertions.assertEquals(4.0f, resultScale.z, EPSILON);
    }
    
    /**
     * Тест геттера и сеттера для position.
     */
    @Test
    void testPositionGetterAndSetter() {
        ModelTransform transform = new ModelTransform();
        
        Vector3f newPosition = new Vector3f(5, 10, 15);
        transform.setPosition(newPosition);
        
        Vector3f result = transform.getPosition();
        Assertions.assertEquals(5.0f, result.x, EPSILON);
        Assertions.assertEquals(10.0f, result.y, EPSILON);
        Assertions.assertEquals(15.0f, result.z, EPSILON);
    }
    
    /**
     * Тест геттера и сеттера для rotation.
     */
    @Test
    void testRotationGetterAndSetter() {
        ModelTransform transform = new ModelTransform();
        
        Vector3f newRotation = new Vector3f(45, 90, 135);
        transform.setRotation(newRotation);
        
        Vector3f result = transform.getRotation();
        Assertions.assertEquals(45.0f, result.x, EPSILON);
        Assertions.assertEquals(90.0f, result.y, EPSILON);
        Assertions.assertEquals(135.0f, result.z, EPSILON);
    }
    
    /**
     * Тест геттера и сеттера для scale.
     */
    @Test
    void testScaleGetterAndSetter() {
        ModelTransform transform = new ModelTransform();
        
        Vector3f newScale = new Vector3f(2, 3, 4);
        transform.setScale(newScale);
        
        Vector3f result = transform.getScale();
        Assertions.assertEquals(2.0f, result.x, EPSILON);
        Assertions.assertEquals(3.0f, result.y, EPSILON);
        Assertions.assertEquals(4.0f, result.z, EPSILON);
    }
    
    /**
     * Тест метода translate() - добавление к текущей позиции.
     */
    @Test
    void testTranslate() {
        ModelTransform transform = new ModelTransform();
        
        // Начальная позиция (0, 0, 0)
        Vector3f translation1 = new Vector3f(1, 2, 3);
        transform.translate(translation1);
        
        Vector3f result1 = transform.getPosition();
        Assertions.assertEquals(1.0f, result1.x, EPSILON);
        Assertions.assertEquals(2.0f, result1.y, EPSILON);
        Assertions.assertEquals(3.0f, result1.z, EPSILON);
        
        // Добавляем еще один перевод
        Vector3f translation2 = new Vector3f(5, 10, 15);
        transform.translate(translation2);
        
        Vector3f result2 = transform.getPosition();
        Assertions.assertEquals(6.0f, result2.x, EPSILON); // 1 + 5
        Assertions.assertEquals(12.0f, result2.y, EPSILON); // 2 + 10
        Assertions.assertEquals(18.0f, result2.z, EPSILON); // 3 + 15
    }
    
    /**
     * Тест метода rotate() - добавление к текущему вращению.
     */
    @Test
    void testRotate() {
        ModelTransform transform = new ModelTransform();
        
        // Начальное вращение (0, 0, 0)
        Vector3f rotation1 = new Vector3f(10, 20, 30);
        transform.rotate(rotation1);
        
        Vector3f result1 = transform.getRotation();
        Assertions.assertEquals(10.0f, result1.x, EPSILON);
        Assertions.assertEquals(20.0f, result1.y, EPSILON);
        Assertions.assertEquals(30.0f, result1.z, EPSILON);
        
        // Добавляем еще одно вращение
        Vector3f rotation2 = new Vector3f(15, 25, 35);
        transform.rotate(rotation2);
        
        Vector3f result2 = transform.getRotation();
        Assertions.assertEquals(25.0f, result2.x, EPSILON); // 10 + 15
        Assertions.assertEquals(45.0f, result2.y, EPSILON); // 20 + 25
        Assertions.assertEquals(65.0f, result2.z, EPSILON); // 30 + 35
    }
    
    /**
     * Тест метода scale() - умножение текущего масштаба.
     */
    @Test
    void testScale() {
        ModelTransform transform = new ModelTransform();
        
        // Начальный масштаб (1, 1, 1)
        Vector3f scale1 = new Vector3f(2, 3, 4);
        transform.scale(scale1);
        
        Vector3f result1 = transform.getScale();
        Assertions.assertEquals(2.0f, result1.x, EPSILON);
        Assertions.assertEquals(3.0f, result1.y, EPSILON);
        Assertions.assertEquals(4.0f, result1.z, EPSILON);
        
        // Умножаем на еще один масштаб
        Vector3f scale2 = new Vector3f(0.5f, 2.0f, 1.5f);
        transform.scale(scale2);
        
        Vector3f result2 = transform.getScale();
        Assertions.assertEquals(1.0f, result2.x, EPSILON); // 2 * 0.5
        Assertions.assertEquals(6.0f, result2.y, EPSILON); // 3 * 2.0
        Assertions.assertEquals(6.0f, result2.z, EPSILON); // 4 * 1.5
    }
    
    /**
     * Тест метода reset() - сброс к дефолтным значениям.
     */
    @Test
    void testReset() {
        ModelTransform transform = new ModelTransform();
        
        // Изменяем значения
        transform.setPosition(new Vector3f(10, 20, 30));
        transform.setRotation(new Vector3f(45, 90, 135));
        transform.setScale(new Vector3f(2, 3, 4));
        
        // Сбрасываем
        transform.reset();
        
        // Проверяем, что вернулись дефолтные значения
        Vector3f position = transform.getPosition();
        Vector3f rotation = transform.getRotation();
        Vector3f scale = transform.getScale();
        
        Assertions.assertEquals(0.0f, position.x, EPSILON);
        Assertions.assertEquals(0.0f, position.y, EPSILON);
        Assertions.assertEquals(0.0f, position.z, EPSILON);
        
        Assertions.assertEquals(0.0f, rotation.x, EPSILON);
        Assertions.assertEquals(0.0f, rotation.y, EPSILON);
        Assertions.assertEquals(0.0f, rotation.z, EPSILON);
        
        Assertions.assertEquals(1.0f, scale.x, EPSILON);
        Assertions.assertEquals(1.0f, scale.y, EPSILON);
        Assertions.assertEquals(1.0f, scale.z, EPSILON);
    }
    
    /**
     * Тест проверки immutability - геттеры возвращают копии.
     */
    @Test
    void testImmutability() {
        ModelTransform transform = new ModelTransform();
        transform.setPosition(new Vector3f(1, 2, 3));
        transform.setRotation(new Vector3f(10, 20, 30));
        transform.setScale(new Vector3f(2, 3, 4));
        
        // Получаем значения
        Vector3f position1 = transform.getPosition();
        Vector3f rotation1 = transform.getRotation();
        Vector3f scale1 = transform.getScale();
        
        // Изменяем полученные векторы
        position1.x = 100;
        rotation1.y = 200;
        scale1.z = 300;
        
        // Получаем значения снова
        Vector3f position2 = transform.getPosition();
        Vector3f rotation2 = transform.getRotation();
        Vector3f scale2 = transform.getScale();
        
        // Значения не должны измениться (геттеры возвращают копии)
        Assertions.assertEquals(1.0f, position2.x, EPSILON,
            "Position should not be modified through getter");
        Assertions.assertEquals(2.0f, position2.y, EPSILON);
        Assertions.assertEquals(3.0f, position2.z, EPSILON);
        
        Assertions.assertEquals(10.0f, rotation2.x, EPSILON);
        Assertions.assertEquals(20.0f, rotation2.y, EPSILON,
            "Rotation should not be modified through getter");
        Assertions.assertEquals(30.0f, rotation2.z, EPSILON);
        
        Assertions.assertEquals(2.0f, scale2.x, EPSILON);
        Assertions.assertEquals(3.0f, scale2.y, EPSILON);
        Assertions.assertEquals(4.0f, scale2.z, EPSILON,
            "Scale should not be modified through getter");
    }
    
    /**
     * Тест проверки immutability - сеттеры создают копии.
     */
    @Test
    void testSetImmutability() {
        ModelTransform transform = new ModelTransform();
        
        Vector3f originalPosition = new Vector3f(1, 2, 3);
        Vector3f originalRotation = new Vector3f(10, 20, 30);
        Vector3f originalScale = new Vector3f(2, 3, 4);
        
        transform.setPosition(originalPosition);
        transform.setRotation(originalRotation);
        transform.setScale(originalScale);
        
        // Изменяем оригинальные векторы
        originalPosition.x = 100;
        originalRotation.y = 200;
        originalScale.z = 300;
        
        // Проверяем, что значения в transform не изменились
        Vector3f position = transform.getPosition();
        Vector3f rotation = transform.getRotation();
        Vector3f scale = transform.getScale();
        
        Assertions.assertEquals(1.0f, position.x, EPSILON,
            "Position should not be modified when original vector is changed");
        Assertions.assertEquals(2.0f, position.y, EPSILON);
        Assertions.assertEquals(3.0f, position.z, EPSILON);
        
        Assertions.assertEquals(10.0f, rotation.x, EPSILON);
        Assertions.assertEquals(20.0f, rotation.y, EPSILON,
            "Rotation should not be modified when original vector is changed");
        Assertions.assertEquals(30.0f, rotation.z, EPSILON);
        
        Assertions.assertEquals(2.0f, scale.x, EPSILON);
        Assertions.assertEquals(3.0f, scale.y, EPSILON);
        Assertions.assertEquals(4.0f, scale.z, EPSILON,
            "Scale should not be modified when original vector is changed");
    }
    
    /**
     * Тест translate() с отрицательными значениями.
     */
    @Test
    void testTranslateNegative() {
        ModelTransform transform = new ModelTransform();
        transform.setPosition(new Vector3f(10, 20, 30));
        
        Vector3f translation = new Vector3f(-5, -10, -15);
        transform.translate(translation);
        
        Vector3f result = transform.getPosition();
        Assertions.assertEquals(5.0f, result.x, EPSILON); // 10 + (-5)
        Assertions.assertEquals(10.0f, result.y, EPSILON); // 20 + (-10)
        Assertions.assertEquals(15.0f, result.z, EPSILON); // 30 + (-15)
    }
    
    /**
     * Тест rotate() с отрицательными значениями.
     */
    @Test
    void testRotateNegative() {
        ModelTransform transform = new ModelTransform();
        transform.setRotation(new Vector3f(45, 90, 135));
        
        Vector3f rotation = new Vector3f(-10, -20, -30);
        transform.rotate(rotation);
        
        Vector3f result = transform.getRotation();
        Assertions.assertEquals(35.0f, result.x, EPSILON); // 45 + (-10)
        Assertions.assertEquals(70.0f, result.y, EPSILON); // 90 + (-20)
        Assertions.assertEquals(105.0f, result.z, EPSILON); // 135 + (-30)
    }
    
    /**
     * Тест scale() с дробными значениями.
     */
    @Test
    void testScaleFractional() {
        ModelTransform transform = new ModelTransform();
        transform.setScale(new Vector3f(4, 6, 8));
        
        Vector3f scale = new Vector3f(0.25f, 0.5f, 0.125f);
        transform.scale(scale);
        
        Vector3f result = transform.getScale();
        Assertions.assertEquals(1.0f, result.x, EPSILON); // 4 * 0.25
        Assertions.assertEquals(3.0f, result.y, EPSILON); // 6 * 0.5
        Assertions.assertEquals(1.0f, result.z, EPSILON); // 8 * 0.125
    }
    
    /**
     * Тест scale() с нулевыми значениями.
     */
    @Test
    void testScaleZero() {
        ModelTransform transform = new ModelTransform();
        transform.setScale(new Vector3f(2, 3, 4));
        
        Vector3f scale = new Vector3f(0, 0, 0);
        transform.scale(scale);
        
        Vector3f result = transform.getScale();
        Assertions.assertEquals(0.0f, result.x, EPSILON); // 2 * 0
        Assertions.assertEquals(0.0f, result.y, EPSILON); // 3 * 0
        Assertions.assertEquals(0.0f, result.z, EPSILON); // 4 * 0
    }
    
    /**
     * Тест комбинированных операций (translate, rotate, scale).
     */
    @Test
    void testCombinedOperations() {
        ModelTransform transform = new ModelTransform();
        
        // Последовательные операции
        transform.translate(new Vector3f(1, 2, 3));
        transform.rotate(new Vector3f(10, 20, 30));
        transform.scale(new Vector3f(2, 2, 2));
        
        Vector3f position = transform.getPosition();
        Vector3f rotation = transform.getRotation();
        Vector3f scale = transform.getScale();
        
        Assertions.assertEquals(1.0f, position.x, EPSILON);
        Assertions.assertEquals(2.0f, position.y, EPSILON);
        Assertions.assertEquals(3.0f, position.z, EPSILON);
        
        Assertions.assertEquals(10.0f, rotation.x, EPSILON);
        Assertions.assertEquals(20.0f, rotation.y, EPSILON);
        Assertions.assertEquals(30.0f, rotation.z, EPSILON);
        
        Assertions.assertEquals(2.0f, scale.x, EPSILON);
        Assertions.assertEquals(2.0f, scale.y, EPSILON);
        Assertions.assertEquals(2.0f, scale.z, EPSILON);
    }
    
    /**
     * Тест конструктора с null параметрами (должен бросить исключение).
     */
    @Test
    void testConstructorWithNull() {
        Vector3f position = new Vector3f(1, 2, 3);
        Vector3f rotation = new Vector3f(10, 20, 30);
        Vector3f scale = new Vector3f(2, 3, 4);
        
        // Конструктор должен обработать null, если есть проверка, или создать копии
        // В данном случае конструктор создает копии, поэтому null вызовет исключение в конструкторе Vector3f
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ModelTransform(null, rotation, scale);
        }, "Should throw exception for null position");
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ModelTransform(position, null, scale);
        }, "Should throw exception for null rotation");
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ModelTransform(position, rotation, null);
        }, "Should throw exception for null scale");
    }
}
