package com.cgvsu.ui;

import com.cgvsu.model.Model;
import com.cgvsu.model.ModelTransform;

/**
 * Представляет модель в сцене с ее трансформациями.
 * 
 * <p>Хранит модель, ее трансформации (позиция, вращение, масштаб),
 * имя модели и флаг активности (отображается ли модель в сцене).
 * 
 * @author CGVSU Team
 * @version 1.0
 */
public class SceneModel {
    
    /**
     * 3D модель.
     */
    private final Model model;
    
    /**
     * Трансформации модели (позиция, вращение, масштаб).
     */
    private final ModelTransform transform;
    
    /**
     * Имя модели (обычно имя файла).
     */
    private final String name;
    
    /**
     * Флаг активности модели (отображается ли в сцене).
     */
    private boolean active;

    /**
     * Создает новую модель в сцене.
     * 
     * @param model 3D модель (не должна быть null)
     * @param name имя модели (не должно быть null)
     */
    public SceneModel(Model model, String name) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.model = model;
        this.transform = new ModelTransform();
        this.name = name;
        this.active = true;
    }

    /**
     * Возвращает модель.
     * 
     * @return 3D модель
     */
    public Model getModel() {
        return model;
    }

    /**
     * Возвращает трансформации модели.
     * 
     * @return объект трансформаций
     */
    public ModelTransform getTransform() {
        return transform;
    }

    /**
     * Возвращает имя модели.
     * 
     * @return имя модели
     */
    public String getName() {
        return name;
    }

    /**
     * Проверяет, активна ли модель (отображается ли в сцене).
     * 
     * @return true если модель активна
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Устанавливает активность модели.
     * 
     * @param active true для отображения модели в сцене
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
