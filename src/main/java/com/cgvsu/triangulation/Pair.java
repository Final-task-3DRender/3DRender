package com.cgvsu.triangulation;

/**
 * Универсальный класс для хранения пары значений разных типов.
 * 
 * <p>Используется для возврата двух связанных значений из методов.
 * 
 * @param <T> тип первого значения
 * @param <R> тип второго значения
 * @author CGVSU Team
 * @version 1.0
 */
public class Pair<T, R> {
    
    /**
     * Первое значение пары.
     */
    public T first;
    
    /**
     * Второе значение пары.
     */
    public R second;
    
    /**
     * Создает новую пару значений.
     * 
     * @param first первое значение
     * @param second второе значение
     */
    public Pair(T first, R second) {
        this.first = first;
        this.second = second;
    }
}
