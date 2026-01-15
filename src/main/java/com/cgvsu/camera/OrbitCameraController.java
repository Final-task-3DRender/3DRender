package com.cgvsu.camera;

import com.cgvsu.math.Vector3f;

/**
 * Контроллер для управления орбитальной камерой.
 * Камера вращается вокруг фиксированной точки (target) в сферических координатах.
 * 
 * Поддерживает:
 * - Движение камеры в 6 направлениях (вперед/назад, влево/вправо, вверх/вниз)
 * - Поворот камеры вокруг цели с помощью мыши (орбитальное движение)
 * - Зум камеры (приближение/отдаление путем изменения радиуса)
 * - Сброс камеры в начальное положение
 * - Настраиваемые параметры чувствительности
 */
public class OrbitCameraController {
    
    private final Camera camera;
    
    private final Vector3f initialPosition;
    private final Vector3f initialTarget;
    
    private float translationSpeed = 0.5f;
    private float rotationSensitivity = 0.01f;
    private float zoomSensitivity = 5.0f;
    
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isMousePressed = false;
    
    /**
     * Создает контроллер для управления орбитальной камерой
     * 
     * @param camera камера для управления
     * @param initialPosition начальная позиция камеры
     * @param initialTarget начальная цель камеры (точка, вокруг которой вращается камера)
     */
    public OrbitCameraController(Camera camera, Vector3f initialPosition, Vector3f initialTarget) {
        if (camera == null) {
            throw new IllegalArgumentException("Camera не может быть null");
        }
        if (initialPosition == null || initialTarget == null) {
            throw new IllegalArgumentException("Initial position и target не могут быть null");
        }
        
        this.camera = camera;
        this.initialPosition = new Vector3f(initialPosition);
        this.initialTarget = new Vector3f(initialTarget);
    }
    
    /**
     * Создает контроллер с камерой и устанавливает начальные значения из самой камеры
     * 
     * @param camera камера для управления
     */
    public OrbitCameraController(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException("Camera не может быть null");
        }
        
        this.camera = camera;
        this.initialPosition = new Vector3f(camera.getPosition());
        this.initialTarget = new Vector3f(camera.getTarget());
    }
    
    // ========== Настройка параметров ==========
    
    /**
     * Устанавливает скорость движения камеры
     * 
     * @param speed скорость движения (по умолчанию 0.5)
     */
    public void setTranslationSpeed(float speed) {
        this.translationSpeed = speed;
    }
    
    /**
     * Устанавливает чувствительность поворота камеры
     * 
     * @param sensitivity чувствительность (по умолчанию 0.01)
     */
    public void setRotationSensitivity(float sensitivity) {
        this.rotationSensitivity = sensitivity;
    }
    
    /**
     * Устанавливает чувствительность зума камеры
     * 
     * @param sensitivity чувствительность (по умолчанию 5.0)
     */
    public void setZoomSensitivity(float sensitivity) {
        this.zoomSensitivity = sensitivity;
    }
    
    public float getTranslationSpeed() {
        return translationSpeed;
    }
    
    public float getRotationSensitivity() {
        return rotationSensitivity;
    }
    
    public float getZoomSensitivity() {
        return zoomSensitivity;
    }
    
    /**
     * Движение камеры вперед (к цели)
     */
    public void moveForward() {
        Vector3f direction = camera.getTarget().subtract(camera.getPosition()).normalize();
        direction = direction.multiply(translationSpeed);
        camera.movePosition(direction);
    }
    
    /**
     * Движение камеры назад (от цели)
     */
    public void moveBackward() {
        Vector3f direction = camera.getPosition().subtract(camera.getTarget()).normalize();
        direction = direction.multiply(translationSpeed);
        camera.movePosition(direction);
    }
    
    /**
     * Движение камеры влево
     */
    public void moveLeft() {
        Vector3f forward = camera.getTarget().subtract(camera.getPosition());
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = forward.cross(up).normalize();
        right = right.multiply(-translationSpeed);
        camera.movePosition(right);
    }
    
    /**
     * Движение камеры вправо
     */
    public void moveRight() {
        Vector3f forward = camera.getTarget().subtract(camera.getPosition());
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = forward.cross(up).normalize();
        right = right.multiply(translationSpeed);
        camera.movePosition(right);
    }
    
    /**
     * Движение камеры вверх
     */
    public void moveUp() {
        camera.movePosition(new Vector3f(0, translationSpeed, 0));
    }
    
    /**
     * Движение камеры вниз
     */
    public void moveDown() {
        camera.movePosition(new Vector3f(0, -translationSpeed, 0));
    }
    
    /**
     * Движение камеры в заданном направлении
     * 
     * @param direction направление движения (будет нормализовано)
     */
    public void moveInDirection(Vector3f direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction не может быть null");
        }
        Vector3f normalized = direction.normalize();
        normalized = normalized.multiply(translationSpeed);
        camera.movePosition(normalized);
    }
    
    /**
     * Поворот камеры вокруг цели (орбитальное движение)
     * Используется для управления мышью
     * 
     * @param deltaX изменение по X (в пикселях)
     * @param deltaY изменение по Y (в пикселях)
     */
    public void rotateAroundTarget(double deltaX, double deltaY) {
        rotateAroundTarget((float) deltaX * rotationSensitivity, -(float) deltaY * rotationSensitivity);
    }
    
    /**
     * Поворот камеры вокруг цели (орбитальное движение)
     * 
     * @param deltaX изменение угла по X (в радианах) - горизонтальный поворот
     * @param deltaY изменение угла по Y (в радианах) - вертикальный поворот
     */
    public void rotateAroundTarget(float deltaX, float deltaY) {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        
        Vector3f offset = position.subtract(target);
        
        float radius = offset.length();
        if (radius < 1e-6f) {
            radius = 1.0f;
            offset = new Vector3f(0, 0, radius);
        }
        
        float theta = (float) Math.atan2(offset.x, offset.z);
        float phi = (float) Math.acos(Math.max(-1.0, Math.min(1.0, offset.y / radius)));
        
        theta += deltaX;
        phi += deltaY;
        
        phi = Math.max(0.01f, Math.min((float) Math.PI - 0.01f, phi));
        
        float sinPhi = (float) Math.sin(phi);
        float cosPhi = (float) Math.cos(phi);
        float sinTheta = (float) Math.sin(theta);
        float cosTheta = (float) Math.cos(theta);
        
        float newX = radius * sinPhi * sinTheta;
        float newY = radius * cosPhi;
        float newZ = radius * sinPhi * cosTheta;
        
        Vector3f newOffset = new Vector3f(newX, newY, newZ);
        Vector3f newPosition = target.add(newOffset);
        
        camera.setPosition(newPosition);
    }
    
    /**
     * Приближение/отдаление камеры (зум)
     * Для орбитальной камеры изменяем радиус напрямую
     * 
     * @param delta изменение зума (положительное - приближение, отрицательное - отдаление)
     */
    public void zoom(double delta) {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        
        Vector3f offset = position.subtract(target);
        float radius = offset.length();
        
        if (radius < 1e-6f) {
            radius = 1.0f;
        }
        
        float zoomFactor = (float) (delta * zoomSensitivity * 0.01);
        float newRadius = Math.max(0.1f, radius - zoomFactor);
        
        if (Math.abs(newRadius - radius) < 1e-6f) {
            return;
        }
        
        Vector3f normalizedOffset = offset.normalize();
        Vector3f newOffset = normalizedOffset.multiply(newRadius);
        Vector3f newPosition = target.add(newOffset);
        
        camera.setPosition(newPosition);
    }
    
    /**
     * Приближение камеры
     */
    public void zoomIn() {
        zoom(1.0);
    }
    
    /**
     * Отдаление камеры
     */
    public void zoomOut() {
        zoom(-1.0);
    }
    
    /**
     * Сбрасывает камеру в начальное положение
     */
    public void reset() {
        camera.setPosition(new Vector3f(initialPosition));
        camera.setTarget(new Vector3f(initialTarget));
    }
    
    /**
     * Устанавливает новые начальные значения для сброса
     * 
     * @param position новая начальная позиция
     * @param target новая начальная цель
     */
    public void setInitialValues(Vector3f position, Vector3f target) {
        if (position == null || target == null) {
            throw new IllegalArgumentException("Position и target не могут быть null");
        }
        this.initialPosition.x = position.x;
        this.initialPosition.y = position.y;
        this.initialPosition.z = position.z;
        this.initialTarget.x = target.x;
        this.initialTarget.y = target.y;
        this.initialTarget.z = target.z;
    }
    
    /**
     * Обработка нажатия мыши
     * Сохраняет позицию мыши для последующего поворота
     * 
     * @param mouseX координата X мыши
     * @param mouseY координата Y мыши
     */
    public void onMousePressed(double mouseX, double mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.isMousePressed = true;
    }
    
    /**
     * Обработка перетаскивания мыши
     * Поворачивает камеру вокруг цели (орбитальное движение)
     * 
     * @param mouseX текущая координата X мыши
     * @param mouseY текущая координата Y мыши
     */
    public void onMouseDragged(double mouseX, double mouseY) {
        if (isMousePressed) {
            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;
            
            rotateAroundTarget(deltaX, deltaY);
            
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }
    
    /**
     * Обработка отпускания мыши
     */
    public void onMouseReleased() {
        isMousePressed = false;
    }
    
    /**
     * Обработка прокрутки колесика мыши
     * 
     * @param deltaY изменение прокрутки (положительное - вверх, отрицательное - вниз)
     */
    public void onMouseScroll(double deltaY) {
        zoom(deltaY);
    }
    
    /**
     * Получить управляемую камеру
     */
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * Получить начальную позицию
     */
    public Vector3f getInitialPosition() {
        return new Vector3f(initialPosition);
    }
    
    /**
     * Получить начальную цель
     */
    public Vector3f getInitialTarget() {
        return new Vector3f(initialTarget);
    }
    
    /**
     * Проверка, нажата ли кнопка мыши
     */
    public boolean isMousePressed() {
        return isMousePressed;
    }
}

