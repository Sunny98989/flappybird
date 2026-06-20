package com.sunny.geegeetyflap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class UIButton {

    public float x, y;
    public float w, h;
    public String text;
    public Runnable onClick;

    // Colors
    public Color baseColor;
    public Color textColor = Color.WHITE;
    private Color hoverColor;
    private Color pressedColor;

    // Animation state
    private float currentScale = 1.0f;
    private float targetScale = 1.0f;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean wasPressedInside = false;

    public UIButton(float x, float y, float w, float h, String text, Color baseColor, Runnable onClick) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.text = text;
        this.onClick = onClick;
        setBaseColor(baseColor);
    }

    public void setBaseColor(Color color) {
        this.baseColor = color;
        // Generate hover and pressed colors automatically
        this.hoverColor = new Color(color).lerp(Color.WHITE, 0.12f);
        this.pressedColor = new Color(color).lerp(Color.BLACK, 0.18f);
    }

    /**
     * Updates the button's hover/click state and runs animations.
     */
    public void update(float delta, int mx, int my, boolean screenTouched, boolean justTouched) {
        boolean inside = mx >= x && mx <= x + w && my >= y && my <= y + h;
        isHovered = inside;

        if (justTouched && inside) {
            wasPressedInside = true;
        }

        if (screenTouched) {
            if (wasPressedInside && inside) {
                isPressed = true;
                targetScale = 0.92f; // Click shrink effect
            } else {
                isPressed = false;
                targetScale = 1.0f;
            }
        } else {
            if (wasPressedInside && inside && isPressed) {
                if (onClick != null) {
                    onClick.run();
                }
            }
            isPressed = false;
            wasPressedInside = false;
            targetScale = 1.0f;
        }

        // Framerate-independent smooth scale interpolation
        currentScale += (targetScale - currentScale) * Math.min(1f, 15f * delta);
    }

    /**
     * Renders the button with dynamic scaling, drop shadow, centered text, and custom transparency.
     */
    public void draw(SpriteBatch batch, NinePatch roundedRectPatch, BitmapFont font, float parentAlpha) {
        float s = currentScale;
        float drawW = w * s;
        float drawH = h * s;
        float drawX = x + (w - drawW) / 2f;
        float drawY = y + (h - drawH) / 2f;

        // 1. Draw drop shadow (gives modern 3D depth)
        // Shrinks when button is pressed to look like it pushes down
        float shadowOffset = 6f * (s - 0.8f) / 0.2f;
        if (shadowOffset < 0) shadowOffset = 0;

        batch.setColor(0f, 0f, 0f, 0.2f * parentAlpha); // Translucent black shadow
        roundedRectPatch.draw(batch, drawX, drawY - shadowOffset, drawW, drawH);

        // 2. Draw button body (shaded based on hover/press)
        Color bodyColor = isPressed ? pressedColor : (isHovered ? hoverColor : baseColor);
        batch.setColor(bodyColor.r, bodyColor.g, bodyColor.b, bodyColor.a * parentAlpha);
        roundedRectPatch.draw(batch, drawX, drawY, drawW, drawH);

        // Reset batch color to draw text normally
        batch.setColor(Color.WHITE);

        // 3. Draw text centered
        float fontScaleX = font.getScaleX();
        float fontScaleY = font.getScaleY();
        
        // Scale font to match the button's size animation
        font.getData().setScale(fontScaleX * s, fontScaleY * s);

        // Copy font color to prevent mutating the internal color reference
        Color oldFontColor = new Color(font.getColor());
        font.setColor(textColor.r, textColor.g, textColor.b, textColor.a * parentAlpha);

        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = drawX + (drawW - layout.width) / 2f;
        // Correctly centers the text using capHeight as the top-alignment offset
        float textY = drawY + (drawH + font.getCapHeight()) / 2f;

        font.draw(batch, text, textX, textY);

        // Restore original font scale and color
        font.getData().setScale(fontScaleX, fontScaleY);
        font.setColor(oldFontColor);
    }
}
