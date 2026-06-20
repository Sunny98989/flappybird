package com.sunny.geegeetyflap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class Main extends ApplicationAdapter {

    // ── Assets ───────────────────────────────────────────────────
    private Texture[] birdTextures; // bird1=up, bird2=mid
    private Texture birdDeathTexture;
    private Texture pipeTexture;
    private Texture pauseTexture;

    // ── Renderers ────────────────────────────────────────────────
    private SpriteBatch batch;
    private FontManager fontManager;
    private NinePatch roundedRectPatch;
    private GlyphLayout layout;     // for centering text
    private ShapeRenderer shapeRenderer;

    // ── Game objects ─────────────────────────────────────────────
    private Bird bird;
    private Pipe pipe;

    // ── State ────────────────────────────────────────────────────
    private enum GameState { START, PLAYING, PAUSED, GAME_OVER }
    private GameState state = GameState.START;

    private boolean scoredThisPipe = false;
    private int score     = 0;
    private int bestScore = 0;

    // ── Screen dimensions (resolved at create/resize time) ────────
    private float W, H;

    // ── Pause button position / scale ─────────────────────────────
    private float pauseX, pauseY;
    private float pauseBtnScale = 1.0f;
    private static final float PAUSE_BTN_SIZE = 54f;

    // ── UI Buttons ───────────────────────────────────────────────
    private UIButton pauseResumeBtn;
    private UIButton pauseRestartBtn;
    private UIButton pauseExitBtn;

    private UIButton gameOverRestartBtn;
    private UIButton gameOverExitBtn;

    // ── Transition animation ──────────────────────────────────────
    private float menuTransition = 0f;

    // ── UI layout constants (all relative to W/H) ─────────────────
    // Popup box
    private static final float BOX_W = 320f;
    private static final float BOX_H = 300f; // Increased height to prevent overlaps

    // Button inside popup
    private static final float BTN_W  = 240f;
    private static final float BTN_H  = 52f;

    // Colors
    private static final Color SKY_COLOR      = new Color(0.35f, 0.75f, 1f,  1f);
    private static final Color DEAD_SKY_COLOR = new Color(0.9f,  0.25f, 0.2f, 1f);
    private static final Color OVERLAY_COLOR  = new Color(0f,    0f,    0f,  0.65f);
    private static final Color BOX_COLOR      = new Color(0.12f, 0.12f, 0.18f, 1f);
    private static final Color BTN_BLUE       = new Color(0.15f, 0.55f, 1f,  1f);
    private static final Color BTN_RED        = new Color(0.85f, 0.2f,  0.2f, 1f);
    private static final Color BTN_GREEN      = new Color(0.1f,  0.7f,  0.3f, 1f);
    private static final Color BTN_DARK       = new Color(0.25f, 0.25f, 0.32f, 1f);
    private static final Color TEXT_WHITE     = Color.WHITE;
    private static final Color TEXT_YELLOW    = new Color(1f, 0.9f, 0.2f, 1f);

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        layout        = new GlyphLayout();

        // Load modern crisp fonts
        fontManager = new FontManager();
        fontManager.load();

        // Generate NinePatch rounded rect texture
        roundedRectPatch = UIUtils.createRoundedRectNinePatch(64, 16);

        // Load 2-frame bird animation: wings up / neutral
        birdTextures = new Texture[] {
            new Texture("bird1.png"),   // frame 0 – wings UP (on flap)
            new Texture("bird2.png")    // frame 1 – wings DOWN (neutral/falling)
        };
        birdDeathTexture = new Texture("bird-death.png");
        // Enable linear filtering for smooth pixel scaling
        for (Texture t : birdTextures) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        birdDeathTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pipeTexture  = new Texture("pipe.png");
        pauseTexture = new Texture("pause.png");

        bird = new Bird();
        pipe = new Pipe();

        rebuildUI();
    }

    private void changeState(GameState newState) {
        if (newState == GameState.PAUSED || newState == GameState.GAME_OVER) {
            if (state != newState) {
                menuTransition = 0f;
            }
        }
        state = newState;
    }

    private void rebuildUI() {
        W = Gdx.graphics.getWidth();
        H = Gdx.graphics.getHeight();

        pauseX = W - PAUSE_BTN_SIZE - 14f;
        pauseY = H - PAUSE_BTN_SIZE - 14f;

        float boxX = (W - BOX_W) / 2f;
        float boxY = (H - BOX_H) / 2f;
        float bx   = boxX + (BOX_W - BTN_W) / 2f;

        // Initialize pause menu buttons with actions (spaced out)
        pauseResumeBtn = new UIButton(bx, boxY + 155f, BTN_W, BTN_H, "Resume", BTN_BLUE, new Runnable() {
            @Override
            public void run() {
                changeState(GameState.PLAYING);
            }
        });

        pauseRestartBtn = new UIButton(bx, boxY + 90f, BTN_W, BTN_H, "Restart", BTN_DARK, new Runnable() {
            @Override
            public void run() {
                restartGame();
            }
        });

        pauseExitBtn = new UIButton(bx, boxY + 25f, BTN_W, BTN_H, "Exit", BTN_RED, new Runnable() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        });

        // Initialize game over menu buttons with actions (spaced out)
        gameOverRestartBtn = new UIButton(bx, boxY + 85f, BTN_W, BTN_H, "Restart", BTN_GREEN, new Runnable() {
            @Override
            public void run() {
                restartGame();
            }
        });

        gameOverExitBtn = new UIButton(bx, boxY + 20f, BTN_W, BTN_H, "Exit", BTN_RED, new Runnable() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        rebuildUI();
    }

    // ─────────────────────────────────────────────────────────────
    // MAIN RENDER LOOP
    // ─────────────────────────────────────────────────────────────
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Smoothly animate menu card transition
        if (state == GameState.PAUSED || state == GameState.GAME_OVER) {
            menuTransition += (1f - menuTransition) * Math.min(1f, 12f * delta);
        } else {
            menuTransition = 0f;
        }

        switch (state) {
            case START:
                renderBackground(false);
                renderWorld(false);
                renderStartMenu();
                handleStartInput();
                break;

            case PLAYING:
                updateGame(delta);
                renderBackground(false);
                renderWorld(false);
                renderHUD();
                handlePauseButtonClick();
                break;

            case PAUSED:
                updatePausedMenu(delta);
                renderBackground(false);
                renderWorld(false);
                renderHUD();
                renderPauseMenu();
                break;

            case GAME_OVER:
                updateGameOverMenu(delta);
                renderBackground(true);
                renderWorld(true);
                renderGameOverMenu();
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GAME UPDATE
    // ─────────────────────────────────────────────────────────────
    private void updateGame(float delta) {
        boolean hitFloor = bird.update(delta, false);
        pipe.update(delta, false);

        if (hitFloor) { triggerGameOver(); return; }

        // Scoring
        if (!scoredThisPipe && pipe.x + pipe.width < bird.x) {
            score++;
            scoredThisPipe = true;
        }
        if (pipe.x > bird.x) scoredThisPipe = false;

        // Pipe collision
        if (pipe.collidesWith(bird)) triggerGameOver();
    }

    private void triggerGameOver() {
        if (score > bestScore) bestScore = score;
        changeState(GameState.GAME_OVER);
    }

    // ─────────────────────────────────────────────────────────────
    // INPUT HANDLERS
    // ─────────────────────────────────────────────────────────────
    private void handleStartInput() {
        if (Gdx.input.justTouched()) {
            changeState(GameState.PLAYING);
        }
    }

    private void handlePauseButtonClick() {
        int mx = Gdx.input.getX();
        int my = (int)(H - Gdx.input.getY());

        boolean pauseHover = mx >= pauseX && mx <= pauseX + PAUSE_BTN_SIZE &&
                             my >= pauseY && my <= pauseY + PAUSE_BTN_SIZE;
        float targetPauseScale = 1.0f;
        if (pauseHover && Gdx.input.isTouched()) {
            targetPauseScale = 0.85f;
        }
        pauseBtnScale += (targetPauseScale - pauseBtnScale) * Math.min(1f, 15f * Gdx.graphics.getDeltaTime());

        if (Gdx.input.justTouched() && pauseHover) {
            changeState(GameState.PAUSED);
        }
    }

    private void updatePausedMenu(float delta) {
        int mx = Gdx.input.getX();
        int my = (int)(H - Gdx.input.getY());
        boolean screenTouched = Gdx.input.isTouched();
        boolean justTouched = Gdx.input.justTouched();

        pauseResumeBtn.update(delta, mx, my, screenTouched, justTouched);
        pauseRestartBtn.update(delta, mx, my, screenTouched, justTouched);
        pauseExitBtn.update(delta, mx, my, screenTouched, justTouched);
    }

    private void updateGameOverMenu(float delta) {
        int mx = Gdx.input.getX();
        int my = (int)(H - Gdx.input.getY());
        boolean screenTouched = Gdx.input.isTouched();
        boolean justTouched = Gdx.input.justTouched();

        gameOverRestartBtn.update(delta, mx, my, screenTouched, justTouched);
        gameOverExitBtn.update(delta, mx, my, screenTouched, justTouched);
    }

    // ─────────────────────────────────────────────────────────────
    // RENDER HELPERS
    // ─────────────────────────────────────────────────────────────
    private void renderBackground(boolean dead) {
        if (dead) Gdx.gl.glClearColor(DEAD_SKY_COLOR.r, DEAD_SKY_COLOR.g, DEAD_SKY_COLOR.b, 1f);
        else      Gdx.gl.glClearColor(SKY_COLOR.r, SKY_COLOR.g, SKY_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderWorld(boolean dead) {
        batch.begin();

        // Pipe bottom
        batch.draw(pipeTexture, pipe.x, 0, pipe.width, pipe.gapY);
        // Pipe top
        batch.draw(pipeTexture, pipe.x, pipe.gapY + pipe.gapHeight,
            pipe.width, H - (pipe.gapY + pipe.gapHeight));

        // Animated bird with rotation (hidden on start screen)
        if (state != GameState.START) {
            Texture frame = dead ? birdDeathTexture : birdTextures[bird.animFrame];
            float birdSize = bird.radius * 2;
            float birdX    = bird.x - bird.radius;
            float birdY    = bird.y - bird.radius;
            // Draw with rotation around bird's center
            batch.draw(
                frame,
                birdX, birdY,              // position
                bird.radius, bird.radius,  // origin (center for rotation)
                birdSize, birdSize,        // size
                1f, 1f,                    // scale
                bird.rotation,             // rotation in degrees
                0, 0,                      // srcX, srcY
                frame.getWidth(), frame.getHeight(),
                false, false               // flipX, flipY
            );
        }

        batch.end();
    }

    private void renderHUD() {
        batch.begin();

        // Pause icon (drawn with hover/click scaling animation)
        float pW = PAUSE_BTN_SIZE * pauseBtnScale;
        float pH = PAUSE_BTN_SIZE * pauseBtnScale;
        float pX = pauseX + (PAUSE_BTN_SIZE - pW) / 2f;
        float pY = pauseY + (PAUSE_BTN_SIZE - pH) / 2f;
        batch.draw(pauseTexture, pX, pY, pW, pH);

        // Score – centered at top. The Nunito-Bold font already includes a vector shadow!
        fontManager.score.setColor(TEXT_WHITE);
        layout.setText(fontManager.score, String.valueOf(score));
        fontManager.score.draw(batch, String.valueOf(score), W / 2f - layout.width / 2f, H - 30f);

        batch.end();
    }

    // ─────────────────────────────────────────────────────────────
    // START MENU
    // ─────────────────────────────────────────────────────────────
    private void renderStartMenu() {
        float cx = W / 2f;
        float cy = H / 2f;

        // Draw modern rounded panel using NinePatch
        batch.begin();
        batch.setColor(BOX_COLOR.r, BOX_COLOR.g, BOX_COLOR.b, 0.92f);
        roundedRectPatch.draw(batch, cx - 190f, cy - 120f, 380f, 250f);
        batch.setColor(Color.WHITE);

        // Title
        fontManager.title.setColor(TEXT_YELLOW);
        drawCenteredLarge("GeeGeetyFlap", cx, cy + 85f);

        // Divider hint
        fontManager.heading.setColor(TEXT_WHITE);
        drawCenteredMedium("Tap to Start", cx, cy + 20f);

        // High Score
        fontManager.sub.setColor(0.75f, 0.9f, 1f, 1f);
        drawCenteredSmall("Best Score: " + bestScore, cx, cy - 30f);

        // Description
        fontManager.sub.setColor(0.6f, 0.6f, 0.65f, 1f);
        drawCenteredSmall("Tap the screen to flap!", cx, cy - 75f);

        batch.end();
    }

    // ─────────────────────────────────────────────────────────────
    // PAUSE MENU
    // ─────────────────────────────────────────────────────────────
    private void renderPauseMenu() {
        drawOverlay(menuTransition);

        float s = 0.8f + 0.2f * menuTransition; // scale transition
        float boxW = BOX_W * s;
        float boxH = BOX_H * s;
        float boxX = (W - boxW) / 2f;
        float boxY = (H - boxH) / 2f;

        // Draw panel (NinePatch)
        batch.begin();
        batch.setColor(BOX_COLOR.r, BOX_COLOR.g, BOX_COLOR.b, BOX_COLOR.a * menuTransition);
        roundedRectPatch.draw(batch, boxX, boxY, boxW, boxH);
        batch.setColor(Color.WHITE);
        batch.end();

        // Draw heading title (PAUSED)
        batch.begin();
        fontManager.heading.setColor(1f, 1f, 1f, menuTransition);
        float origScaleX = fontManager.heading.getScaleX();
        float origScaleY = fontManager.heading.getScaleY();
        fontManager.heading.getData().setScale(origScaleX * s, origScaleY * s);

        GlyphLayout titleLayout = new GlyphLayout(fontManager.heading, "PAUSED");
        // Spaced safely to prevent overlaps
        fontManager.heading.draw(batch, "PAUSED", W / 2f - titleLayout.width / 2f, boxY + 260f * s);
        fontManager.heading.getData().setScale(origScaleX, origScaleY);
        batch.end();

        // Layout and draw buttons
        float bx = (W - BTN_W * s) / 2f;
        float bw = BTN_W * s;
        float bh = BTN_H * s;

        pauseResumeBtn.x = bx;
        pauseResumeBtn.y = boxY + 155f * s;
        pauseResumeBtn.w = bw;
        pauseResumeBtn.h = bh;

        pauseRestartBtn.x = bx;
        pauseRestartBtn.y = boxY + 90f * s;
        pauseRestartBtn.w = bw;
        pauseRestartBtn.h = bh;

        pauseExitBtn.x = bx;
        pauseExitBtn.y = boxY + 25f * s;
        pauseExitBtn.w = bw;
        pauseExitBtn.h = bh;

        batch.begin();
        pauseResumeBtn.draw(batch, roundedRectPatch, fontManager.button, menuTransition);
        pauseRestartBtn.draw(batch, roundedRectPatch, fontManager.button, menuTransition);
        pauseExitBtn.draw(batch, roundedRectPatch, fontManager.button, menuTransition);
        batch.end();
    }

    // ─────────────────────────────────────────────────────────────
    // GAME OVER MENU
    // ─────────────────────────────────────────────────────────────
    private void renderGameOverMenu() {
        drawOverlay(menuTransition);

        float s = 0.8f + 0.2f * menuTransition; // scale transition
        float boxW = BOX_W * s;
        float boxH = BOX_H * s;
        float boxX = (W - boxW) / 2f;
        float boxY = (H - boxH) / 2f;

        // Draw panel (NinePatch)
        batch.begin();
        batch.setColor(BOX_COLOR.r, BOX_COLOR.g, BOX_COLOR.b, BOX_COLOR.a * menuTransition);
        roundedRectPatch.draw(batch, boxX, boxY, boxW, boxH);
        batch.setColor(Color.WHITE);
        batch.end();

        // Draw title & scores
        batch.begin();

        // GAME OVER Title
        fontManager.heading.setColor(1f, 0.3f, 0.3f, menuTransition);
        float origScaleX = fontManager.heading.getScaleX();
        float origScaleY = fontManager.heading.getScaleY();
        fontManager.heading.getData().setScale(origScaleX * s, origScaleY * s);
        GlyphLayout titleLayout = new GlyphLayout(fontManager.heading, "GAME OVER");
        // Spaced safely to prevent overlaps
        fontManager.heading.draw(batch, "GAME OVER", W / 2f - titleLayout.width / 2f, boxY + 270f * s);
        fontManager.heading.getData().setScale(origScaleX, origScaleY);

        // Score: X
        fontManager.button.setColor(TEXT_YELLOW.r, TEXT_YELLOW.g, TEXT_YELLOW.b, menuTransition);
        float origBtnScaleX = fontManager.button.getScaleX();
        float origBtnScaleY = fontManager.button.getScaleY();
        fontManager.button.getData().setScale(origBtnScaleX * s, origBtnScaleY * s);
        GlyphLayout scoreLayout = new GlyphLayout(fontManager.button, "Score: " + score);
        fontManager.button.draw(batch, "Score: " + score, W / 2f - scoreLayout.width / 2f, boxY + 215f * s);
        fontManager.button.getData().setScale(origBtnScaleX, origBtnScaleY);
        // Reset color to prevent leaking to other UI buttons
        fontManager.button.setColor(Color.WHITE);

        // Best: Y
        fontManager.sub.setColor(0.7f, 0.85f, 1f, menuTransition);
        float origSubScaleX = fontManager.sub.getScaleX();
        float origSubScaleY = fontManager.sub.getScaleY();
        fontManager.sub.getData().setScale(origSubScaleX * s, origSubScaleY * s);
        GlyphLayout bestLayout = new GlyphLayout(fontManager.sub, "Best: " + bestScore);
        fontManager.sub.draw(batch, "Best: " + bestScore, W / 2f - bestLayout.width / 2f, boxY + 165f * s);
        fontManager.sub.getData().setScale(origSubScaleX, origSubScaleY);

        batch.end();

        // Layout and draw buttons
        float bx = (W - BTN_W * s) / 2f;
        float bw = BTN_W * s;
        float bh = BTN_H * s;

        gameOverRestartBtn.x = bx;
        gameOverRestartBtn.y = boxY + 85f * s;
        gameOverRestartBtn.w = bw;
        gameOverRestartBtn.h = bh;

        gameOverExitBtn.x = bx;
        gameOverExitBtn.y = boxY + 20f * s;
        gameOverExitBtn.w = bw;
        gameOverExitBtn.h = bh;

        batch.begin();
        gameOverRestartBtn.draw(batch, roundedRectPatch, fontManager.button, menuTransition);
        gameOverExitBtn.draw(batch, roundedRectPatch, fontManager.button, menuTransition);
        batch.end();
    }

    // ─────────────────────────────────────────────────────────────
    // UI PRIMITIVES
    // ─────────────────────────────────────────────────────────────
    private void drawOverlay(float alpha) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(OVERLAY_COLOR.r, OVERLAY_COLOR.g, OVERLAY_COLOR.b, OVERLAY_COLOR.a * alpha);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // Centers text horizontally around cx, baseline at y
    private void drawCenteredLarge(String text, float cx, float y) {
        layout.setText(fontManager.title, text);
        fontManager.title.draw(batch, text, cx - layout.width / 2f, y);
    }

    private void drawCenteredMedium(String text, float cx, float y) {
        layout.setText(fontManager.heading, text);
        fontManager.heading.draw(batch, text, cx - layout.width / 2f, y);
    }

    private void drawCenteredSmall(String text, float cx, float y) {
        layout.setText(fontManager.sub, text);
        fontManager.sub.draw(batch, text, cx - layout.width / 2f, y);
    }

    // ─────────────────────────────────────────────────────────────
    // RESTART
    // ─────────────────────────────────────────────────────────────
    private void restartGame() {
        bird           = new Bird();
        pipe           = new Pipe();
        score          = 0;
        scoredThisPipe = false;
        changeState(GameState.PLAYING);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        if (fontManager != null) {
            fontManager.dispose();
        }
        if (roundedRectPatch != null && roundedRectPatch.getTexture() != null) {
            roundedRectPatch.getTexture().dispose();
        }
        if (birdTextures != null) {
            for (Texture t : birdTextures) if (t != null) t.dispose();
        }
        if (birdDeathTexture != null) birdDeathTexture.dispose();
        pipeTexture.dispose();
        pauseTexture.dispose();
    }
}
