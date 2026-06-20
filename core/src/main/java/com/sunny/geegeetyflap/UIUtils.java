package com.sunny.geegeetyflap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;

public class UIUtils {

    /**
     * Generates a white rounded rectangle NinePatch texture.
     * Drawing it with batch.setColor(color) allows tinting it to any color/alpha.
     *
     * @param size   Total texture size (width and height). E.g., 64.
     * @param radius Corner radius. E.g., 16.
     * @return A NinePatch instance with margins set to radius.
     */
    public static NinePatch createRoundedRectNinePatch(int size, int radius) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        // Clear with transparent
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        // Fill with white
        pixmap.setColor(Color.WHITE);
        
        // Draw the core rectangles
        pixmap.fillRectangle(radius, 0, size - 2 * radius, size);
        pixmap.fillRectangle(0, radius, radius, size - 2 * radius);
        pixmap.fillRectangle(size - radius, radius, radius, size - 2 * radius);

        // Draw the corner circles
        // We use (size - 1 - radius) to center the circles perfectly in the 0-indexed coordinate system
        int edge = size - 1 - radius;
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(edge, radius, radius);
        pixmap.fillCircle(radius, edge, radius);
        pixmap.fillCircle(edge, edge, radius);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Dispose pixmap as it is now loaded in GPU memory
        pixmap.dispose();

        // NinePatch splits: left, right, top, bottom
        return new NinePatch(texture, radius, radius, radius, radius);
    }
}
