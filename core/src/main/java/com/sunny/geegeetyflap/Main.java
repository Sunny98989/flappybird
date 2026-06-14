package com.sunny.geegeetyflap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;


public class Main extends ApplicationAdapter {

    private Texture birdTexture;
    private Texture pipeTexture;
    private Texture pauseTexture;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private Bird bird;
    private Pipe pipe;

    private boolean paused = false;
    private boolean gameOver = false;
    private boolean scoredThisPipe = false;
    private boolean hitFloor = false;

    private int score = 0;

    private float pauseX;
    private float pauseY;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(3);
        font.setColor(Color.BLACK);

        birdTexture = new Texture("bird.png");
        pipeTexture = new Texture("pipe.png");
        pauseTexture = new Texture("pause.png");

        bird = new Bird();
        pipe = new Pipe();

        pauseX = Gdx.graphics.getWidth() - 70;
        pauseY = Gdx.graphics.getHeight() - 70;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Pause button click
        if (!gameOver && Gdx.input.justTouched()) {
            if (mouseX >= pauseX && mouseX <= pauseX + 50 &&
                mouseY >= pauseY && mouseY <= pauseY + 50) {
                paused = !paused;
                return;
            }
        }

        // Pause menu input
        if (paused) {
            handlePauseMenuClicks();
            renderWorld();
            renderPauseMenu();
            return;
        }

        // Game Over menu input
        if (gameOver && Gdx.input.justTouched()) {

            // Restart
            if (mouseX >= 300 && mouseX <= 400 &&
                mouseY >= 280 && mouseY <= 320) {
                restartGame();
                return;
            }

            // Exit
            if (mouseX >= 440 && mouseX <= 520 &&
                mouseY >= 280 && mouseY <= 320) {
                Gdx.app.exit();
            }

            renderWorld();
            renderGameOverMenu();
            return;
        }

        // ======================
        // GAME UPDATE
        // ======================
        hitFloor = bird.update(delta, gameOver);
        pipe.update(delta, gameOver);

        if (hitFloor) {
            gameOver = true;
            System.out.println("GAME OVER - FLOOR");
        }

        if (!scoredThisPipe && pipe.x + pipe.width < bird.x) {
            score++;
            scoredThisPipe = true;
            System.out.println("Score: " + score);
        }

        if (pipe.x > bird.x) {
            scoredThisPipe = false;
        }

        if (!gameOver && pipe.collidesWith(bird)) {
            gameOver = true;
            System.out.println("GAME OVER");
        }

        renderWorld();

        if (gameOver) {
            renderGameOverMenu();
        }
    }

    private void renderWorld() {
        if (gameOver) {
            Gdx.gl.glClearColor(1f, 0.2f, 0.2f, 1);
        } else {
            Gdx.gl.glClearColor(0.4f, 0.8f, 1f, 1);
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Pipe bottom
        batch.draw(pipeTexture, pipe.x, 0, pipe.width, pipe.gapY);

        // Pipe top
        batch.draw(
            pipeTexture,
            pipe.x,
            pipe.gapY + pipe.gapHeight,
            pipe.width,
            800 - (pipe.gapY + pipe.gapHeight)
        );

        // Bird
        batch.draw(
            birdTexture,
            bird.x - bird.radius,
            bird.y - bird.radius,
            bird.radius * 2,
            bird.radius * 2
        );

        // Pause button
        batch.draw(pauseTexture, pauseX, pauseY, 50, 50);

        // Score
        font.draw(batch, "Score: " + score, 20, 580);

        batch.end();
    }

    private void renderPauseMenu() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Dark overlay
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, 800, 600);

        // Menu box
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapeRenderer.rect(250, 150, 300, 300);

        // Buttons
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(310, 340, 180, 50); // Resume
        shapeRenderer.rect(310, 270, 180, 50); // Restart
        shapeRenderer.rect(310, 200, 180, 50); // Exit

        shapeRenderer.end();

        batch.begin();
        font.draw(batch, "PAUSED", 365, 420);
        font.draw(batch, "Resume", 375, 370);
        font.draw(batch, "Restart", 375, 300);
        font.draw(batch, "Exit", 390, 230);
        batch.end();
    }

    private void renderGameOverMenu() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, 800, 600);

        shapeRenderer.setColor(0f, 0.5f, 0f, 1f);
        shapeRenderer.rect(220, 170, 360, 240);

        shapeRenderer.end();

        batch.begin();
        font.draw(batch, "GAME OVER", 340, 360);
        font.draw(batch, "Restart", 320, 300);
        font.draw(batch, "Exit", 460, 300);
        batch.end();
    }

    private void handlePauseMenuClicks() {
        if (!Gdx.input.justTouched()) return;

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Resume
        if (mouseX >= 310 && mouseX <= 490 &&
            mouseY >= 340 && mouseY <= 390) {
            paused = false;
        }

        // Restart
        if (mouseX >= 310 && mouseX <= 490 &&
            mouseY >= 270 && mouseY <= 320) {
            restartGame();
            paused = false;
        }

        // Exit
        if (mouseX >= 310 && mouseX <= 490 &&
            mouseY >= 200 && mouseY <= 250) {
            Gdx.app.exit();
        }
    }

    private void restartGame() {
        bird = new Bird();
        pipe = new Pipe();
        paused = false;
        gameOver = false;
        score = 0;
        scoredThisPipe = false;
        hitFloor = false;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        birdTexture.dispose();
        pipeTexture.dispose();
        pauseTexture.dispose();
    }
}
