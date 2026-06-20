package com.sunny.geegeetyflap;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class Pipe {
    public float x         = 900;
    public float width     = 120;
    public float gapHeight = 220;
    public float gapY      = 280;
    public float speed     = 250;

    private final Random random = new Random();

    public void update(float delta, boolean gameOver) {
        if (gameOver) return;

        x -= speed * delta;

        if (x < -width) {
            x    = 900;
            gapY = 100 + random.nextInt(300);
        }
    }

    public void render(ShapeRenderer renderer) {
        renderer.rect(x, 0,              width, gapY);
        renderer.rect(x, gapY + gapHeight, width, 800);
    }

    public boolean collidesWith(Bird bird) {
        float birdLeft   = bird.x - bird.radius;
        float birdRight  = bird.x + bird.radius;
        float birdBottom = bird.y - bird.radius;
        float birdTop    = bird.y + bird.radius;

        boolean horizontalCollision = birdRight > x && birdLeft < x + width;
        boolean verticalCollision   = birdBottom < gapY || birdTop > gapY + gapHeight;

        return horizontalCollision && verticalCollision;
    }
}
