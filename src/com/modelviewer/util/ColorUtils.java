package com.modelviewer.util;

import java.awt.Color;

public class ColorUtils {
    public static float[] toFloatArray(Color color) {
        float[] components = color.getComponents(null);
        return new float[] {
                components[0],
                components[1],
                components[2],
                components[3]
        };
    }

    public static float[] toFloatArray(Color color, float alpha) {
        float[] components = color.getComponents(null);
        return new float[] {
                components[0],
                components[1],
                components[2],
                alpha
        };
    }

    public static Color brighten(Color color, float factor) {
        float[] hsb = Color.RGBtoHSB(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                null);
        hsb[2] = Math.min(1.0f, hsb[2] * (1.0f + factor));
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color darken(Color color, float factor) {
        float[] hsb = Color.RGBtoHSB(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                null);
        hsb[2] = Math.max(0.0f, hsb[2] * (1.0f - factor));
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static int getBrightness(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
    }

    public static Color getContrastColor(Color color) {
        return getBrightness(color) > 128 ? Color.BLACK : Color.WHITE;
    }

    public static Color interpolate(Color c1, Color c2, float ratio) {
        float[] comp1 = c1.getComponents(null);
        float[] comp2 = c2.getComponents(null);
        float[] result = new float[4];

        for (int i = 0; i < 4; i++) {
            result[i] = comp1[i] + (comp2[i] - comp1[i]) * ratio;
        }

        return new Color(result[0], result[1], result[2], result[3]);
    }
}