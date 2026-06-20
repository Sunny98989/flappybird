package com.sunny.geegeetyflap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Loads TrueType fonts via FreeType so they render crisp at any size.
 *
 * Font file needed in assets/:
 *   Nunito-Bold.ttf      ← download from https://fonts.google.com/specimen/Nunito
 *   Nunito-Regular.ttf   ← same page, different weight
 *
 * Place both .ttf files inside:
 *   GeegeetyFlap/assets/
 */
public class FontManager {

    // Public font references — use these directly in Main
    public BitmapFont title;      // 64px  — "GeeGeetyFlap", "GAME OVER"
    public BitmapFont score;      // 80px  — in-game score number
    public BitmapFont heading;    // 46px  — menu heading ("PAUSED")
    public BitmapFont button;     // 34px  — button labels
    public BitmapFont sub;        // 24px  — secondary text (best score, hints)

    private FreeTypeFontGenerator boldGen;
    private FreeTypeFontGenerator regularGen;

    public void load() {
        boldGen    = new FreeTypeFontGenerator(Gdx.files.internal("Nunito-VariableFont_wght.ttf"));
        regularGen = new FreeTypeFontGenerator(Gdx.files.internal("Nunito-VariableFont_wght.ttf"));

        title   = make(boldGen,    64, Color.WHITE);
        score   = make(boldGen,    80, Color.WHITE);
        heading = make(boldGen,    46, Color.WHITE);
        button  = make(boldGen,    34, Color.WHITE);
        sub     = make(regularGen, 24, new Color(0.8f, 0.9f, 1f, 1f));

        // Generators are no longer needed after baking bitmaps
        boldGen.dispose();
        regularGen.dispose();
    }

    private BitmapFont make(FreeTypeFontGenerator gen, int size, Color color) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size            = size;
        p.color           = color;
        p.shadowOffsetX   = 2;
        p.shadowOffsetY   = -2;
        p.shadowColor     = new Color(0f, 0f, 0f, 0.35f);
        p.minFilter       = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        p.magFilter       = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        return gen.generateFont(p);
    }

    public void dispose() {
        if (title   != null) title.dispose();
        if (score   != null) score.dispose();
        if (heading != null) heading.dispose();
        if (button  != null) button.dispose();
        if (sub     != null) sub.dispose();
    }
}
