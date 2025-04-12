package com.modelviewer.util;

public class GeometryUtils {
    public static float[] calculateNormal(float[] v1, float[] v2, float[] v3) {
        float[] normal = new float[3];
        
        // Calculate vectors from v1 to v2 and v1 to v3
        float[] vector1 = new float[] {
            v2[0] - v1[0],
            v2[1] - v1[1],
            v2[2] - v1[2]
        };
        
        float[] vector2 = new float[] {
            v3[0] - v1[0],
            v3[1] - v1[1],
            v3[2] - v1[2]
        };
        
        // Cross product
        normal[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
        normal[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
        normal[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];
        
        // Normalize
        float length = (float) Math.sqrt(
            normal[0] * normal[0] +
            normal[1] * normal[1] +
            normal[2] * normal[2]
        );
        
        if (length != 0) {
            normal[0] /= length;
            normal[1] /= length;
            normal[2] /= length;
        }
        
        return normal;
    }
    
    public static float[] calculateCenter(float[] bounds) {
        return new float[] {
            (bounds[0] + bounds[1]) / 2.0f,  // X center
            (bounds[2] + bounds[3]) / 2.0f,  // Y center
            (bounds[4] + bounds[5]) / 2.0f   // Z center
        };
    }
    
    public static float calculateScale(float[] bounds, float targetSize) {
        float xSize = Math.abs(bounds[1] - bounds[0]);
        float ySize = Math.abs(bounds[3] - bounds[2]);
        float zSize = Math.abs(bounds[5] - bounds[4]);
        
        float maxSize = Math.max(Math.max(xSize, ySize), zSize);
        return targetSize / maxSize;
    }
    
    public static float[] calculateBoundingSphere(float[] bounds) {
        float[] center = calculateCenter(bounds);
        float radius = 0.0f;
        
        // Calculate the maximum distance from center to any corner
        float[] corners = new float[] {
            bounds[0], bounds[2], bounds[4],  // min x, y, z
            bounds[0], bounds[2], bounds[5],  // min x, y, max z
            bounds[0], bounds[3], bounds[4],  // min x, max y, min z
            bounds[0], bounds[3], bounds[5],  // min x, max y, max z
            bounds[1], bounds[2], bounds[4],  // max x, min y, min z
            bounds[1], bounds[2], bounds[5],  // max x, min y, max z
            bounds[1], bounds[3], bounds[4],  // max x, max y, min z
            bounds[1], bounds[3], bounds[5]   // max x, max y, max z
        };
        
        for (int i = 0; i < corners.length; i += 3) {
            float dx = corners[i] - center[0];
            float dy = corners[i + 1] - center[1];
            float dz = corners[i + 2] - center[2];
            
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            radius = Math.max(radius, distance);
        }
        
        return new float[] { center[0], center[1], center[2], radius };
    }
    
    public static boolean rayIntersectsSphere(
        float[] rayOrigin,
        float[] rayDirection,
        float[] sphere  // x, y, z, radius
    ) {
        float dx = rayOrigin[0] - sphere[0];
        float dy = rayOrigin[1] - sphere[1];
        float dz = rayOrigin[2] - sphere[2];
        
        float a = rayDirection[0] * rayDirection[0] +
                 rayDirection[1] * rayDirection[1] +
                 rayDirection[2] * rayDirection[2];
        
        float b = 2.0f * (dx * rayDirection[0] +
                         dy * rayDirection[1] +
                         dz * rayDirection[2]);
        
        float c = dx * dx + dy * dy + dz * dz - sphere[3] * sphere[3];
        
        float discriminant = b * b - 4 * a * c;
        return discriminant >= 0;
    }
} 