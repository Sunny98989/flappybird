package com.sunny.geegeetyflap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Bird {
    public float x = 200;
    public float y = 300;
    public float radius = 30;

    private float velocity = 0;
    private final float gravity = -900f;
    private final float flapForce = 350f;

    public boolean update(float delta, boolean gameOver) {
        if (gameOver) return false;

        if (Gdx.input.justTouched()) {
            velocity = flapForce;
        }

        velocity += gravity * delta;
        y += velocity * delta;

        // FLOOR = GAME OVER
        if (y - radius <= 0) {
            y = radius;
            return true;
        }

        // CEILING = CLAMP ONLY
        float screenHeight = Gdx.graphics.getHeight();
        if (y + radius >= screenHeight) {
            y = screenHeight - radius;
            velocity = 0;
        }

        return false;
    }

    public void render(ShapeRenderer renderer) {
        renderer.circle(x, y, radius);
    }
}
