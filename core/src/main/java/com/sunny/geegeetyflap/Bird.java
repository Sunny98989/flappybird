package com.sunny.geegeetyflap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Bird {
    public float x = 200;
    public float y = 300;
    public float radius = 30;

    private float velocity = 0;
    private final float gravity = -900f;
    private final float flapForce = 350f;

    // ── Animation ────────────────────────────────────────────────────
    // Frame 0 = wings up, Frame 1 = neutral/down
    public int animFrame = 1;

    private float flapTimer = 0f;
    private static final float FLAP_DURATION = 0.2f;

    // ── Rotation ─────────────────────────────────────────────────────
    // In LibGDX: +degrees = CCW = nose up | -degrees = CW = nose down
    public float rotation = 0f;

    private static final float ROT_FLAP_MAX  =  25f;  // nose-up on flap
    private static final float ROT_FALL_MAX  = -50f;  // nose-down when falling
    // Velocity at which full nose-down rotation is reached
    private static final float FALL_VELOCITY = -500f;

    public boolean update(float delta, boolean gameOver) {
        if (gameOver) {
            // Dead bird animation: fall to the ground and rotate straight down
            if (y - radius > 0) {
                velocity += gravity * 1.5f * delta; // Fall slightly faster when dead
                y += velocity * delta;
            }
            if (y - radius <= 0) {
                y = radius;
                velocity = 0;
            }
            rotation += (-90f - rotation) * Math.min(1f, 15f * delta);
            return false;
        }

        if (Gdx.input.justTouched()) {
            velocity = flapForce;
            flapTimer = FLAP_DURATION;
        }

        velocity += gravity * delta;
        y        += velocity * delta;

        // ── Wing animation: wings up on flap, down otherwise ────────
        if (flapTimer > 0) {
            flapTimer -= delta;
            animFrame = 0;
        } else {
            animFrame = 1;
        }

        // ── Rotation based on velocity ────────────────────────────────
        float targetRotation;
        if (velocity >= 0) {
            // Rising or just tapped: interpolate 0° → +25° based on upward velocity
            targetRotation = (velocity / flapForce) * ROT_FLAP_MAX;
        } else {
            // Falling: interpolate 0° → -50° based on fall speed
            targetRotation = MathUtils.clamp(
                (velocity / FALL_VELOCITY) * ROT_FALL_MAX,
                ROT_FALL_MAX, 0f
            );
        }
        // Smooth the rotation transition (lerp at 10x/s)
        rotation += (targetRotation - rotation) * Math.min(1f, 10f * delta);

        // Floor → game over
        if (y - radius <= 0) {
            y = radius;
            return true;
        }

        // Ceiling → clamp only, no game over
        float screenHeight = Gdx.graphics.getHeight();
        if (y + radius >= screenHeight) {
            y        = screenHeight - radius;
            velocity = 0;
        }

        return false;
    }

    public void render(ShapeRenderer renderer) {
        renderer.circle(x, y, radius);
    }
}
