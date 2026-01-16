package com.cgvsu.render_engine;

import com.cgvsu.camera.Camera;
import com.cgvsu.camera.CameraProjection;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки преобразования Z-координаты после перспективной проекции.
 * Помогает понять, какое направление Z используется в нашей системе координат.
 */
public class PerspectiveProjectionZTest {

    @Test
    public void testPerspectiveProjectionZTransformation() {
        float near = 0.01f;
        float far = 100.0f;
        float fov = (float) Math.toRadians(60.0);
        
        Matrix4f projection = CameraProjection.perspective(fov, 1.0f, near, far);
        
        Vector4f nearVertex = new Vector4f(0, 0, -near, 1.0f);
        Vector4f farVertex = new Vector4f(0, 0, -far, 1.0f);
        
        Vector4f nearTransformed = projection.multiply(nearVertex);
        Vector4f farTransformed = projection.multiply(farVertex);
        
        if (Math.abs(nearTransformed.w) > 1e-7f) {
            nearTransformed = nearTransformed.divide(nearTransformed.w);
        }
        if (Math.abs(farTransformed.w) > 1e-7f) {
            farTransformed = farTransformed.divide(farTransformed.w);
        }
        
        float nearZ = nearTransformed.z;
        float farZ = farTransformed.z;
        
        assertTrue(nearZ > farZ, 
            "Near plane Z (" + nearZ + ") should be greater than far plane Z (" + farZ + ")");
    }

    @Test
    public void testZBufferWithProjectedZ() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        
        float nearZ = 100.0f;
        float farZ = -50.0f;
        
        assertTrue(zBuffer.testAndSet(50, 50, farZ));
        assertEquals(farZ, zBuffer.get(50, 50), 1e-6f);
        
        assertTrue(zBuffer.testAndSet(50, 50, nearZ));
        assertEquals(nearZ, zBuffer.get(50, 50), 1e-6f);
        
        assertFalse(zBuffer.testAndSet(50, 50, farZ));
        assertEquals(nearZ, zBuffer.get(50, 50), 1e-6f);
    }
}
