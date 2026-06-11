package com.cristian.plataformas;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Plataformas extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private SpriteBatch uiBatch;
    private BitmapFont font;

    //Estado del juego
    private enum GameState { PLAYING, GAME_OVER }
    private GameState state = GameState.PLAYING;

    //Jugador
    private Rectangle player;
    private float velX, velY;
    private boolean onGround = false;
    private int lives = 3;
    private float invincibleTimer = 0f;

    //Enemigos
    private Array<Rectangle> enemies;
    private Array<Float> enemyVelX;
    private Array<Boolean> enemyAlive;

    //Plataformas
    private Array<Rectangle> platforms;

    //Constantes
    private static final float SPEED     = 200f;
    private static final float GRAVITY   = -600f;
    private static final float JUMP_FORCE = 400f;
    private static final float GROUND_Y  = 50f;
    private static final float ENEMY_SPEED = 80f;
    private static final float WORLD_WIDTH  = 1600f;
    private static final float WORLD_HEIGHT = 600f;

    @Override
    public void create(){
        shapeRenderer = new ShapeRenderer();

        // Cámara del mundo
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Cámara fija para el HUD (no se mueve con el jugador)
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        uiBatch = new SpriteBatch();
        font    = new BitmapFont();
        font.getData().setScale(2f);

        initGame();
    }

    public void initGame() {
        state          = GameState.PLAYING;
        lives          = 3;
        invincibleTimer = 0f;

        player = new Rectangle(100, GROUND_Y, 40, 60);
        velX   = 0;
        velY   = 0;

        //Crear plataformas
        platforms = new Array<>();
        platforms.add(new Rectangle(200,  150, 150, 20));
        platforms.add(new Rectangle(450,  250, 150, 20));
        platforms.add(new Rectangle(150,  350, 150, 20));
        platforms.add(new Rectangle(500,  420, 120, 20));
        platforms.add(new Rectangle(700,  180, 160, 20));
        platforms.add(new Rectangle(900,  300, 150, 20));
        platforms.add(new Rectangle(1100, 200, 180, 20));
        platforms.add(new Rectangle(1300, 350, 150, 20));

        //Enemigos
        enemies = new Array<>();
        enemyVelX = new Array<>();
        enemyAlive = new Array<>();

        spawnEnemy(210, 170);
        spawnEnemy(460, 270);
        spawnEnemy(910, 320);
        spawnEnemy(1110, 220);
    }

    private void spawnEnemy(float x, float y) {
        enemies.add(new Rectangle(x, y, 35, 35));
        enemyVelX.add(ENEMY_SPEED);
        enemyAlive.add(true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (state == GameState.GAME_OVER) {
            renderGameOver();
            return;
        }

        float delta = Gdx.graphics.getDeltaTime();
        updatePlayer(delta);
        updateEnemies(delta);
        updateCamera();
        renderWorld();
        renderHUD();
    }

    // --- INPUT ---
    private void updatePlayer(float delta) {
        velX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) velX = -SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velX = SPEED;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && onGround) {
            velY = JUMP_FORCE;
            onGround = false;
        }

        // --- FÍSICA ---
        velY += GRAVITY * delta;

        //Mover en X
        player.x += velX * delta;
        player.x = Math.max(0, Math.min(player.x, WORLD_WIDTH - player.width));
        for (Rectangle platform : platforms) {
            if (player.overlaps(platform)) {
                if (velX > 0) player.x = platform.x - player.width;
                if (velX < 0) player.x = platform.x + platform.width;
                velX = 0;
            }
        }

        //Mover en Y
        player.y += velY * delta;
        onGround = false;

        //Colisión con suelo
        if (player.y <= GROUND_Y) {
            player.y = GROUND_Y;
            velY = 0;
            onGround = true;
        }

        //Colisión con plataformas
        for (Rectangle platform : platforms) {
            if (player.overlaps(platform)) {
                if (velY < 0) {
                    //Cayendo: aterrizar encima
                    player.y = platform.y + platform.height;
                    velY = 0;
                    onGround = true;
                } else if (velY > 0) {
                    //Subiendo: chocar con el borde inferior
                    player.y = platform.y - player.height;
                    velY = 0;
                }
            }
            if (invincibleTimer > 0f) invincibleTimer -= delta;
        }
    }

    // --- LÓGICA ENEMIGOS ---
    public void updateEnemies(float delta) {
        for (int i = 0; i < enemies.size; i++) {
            if (!enemyAlive.get(i)) continue;

            Rectangle enemy = enemies.get(i);
            float eVel = enemyVelX.get(i);
            enemy.x += eVel * delta;

            // Rebotar en los bordes del mundo
            if (enemy.x <= 0 || enemy.x + enemy.width >= WORLD_WIDTH) {
                enemyVelX.set(i, -eVel);
            }

            // Rebotar en los bordes de la plataforma debajo
            for (Rectangle platform : platforms) {
                boolean encima = enemy.y >= platform.y + platform.height - 5 &&
                    enemy.y <= platform.y + platform.height + 5;
                if (encima) {
                    if (enemy.x < platform.x || enemy.x + enemy.width > platform.x + platform.width) {
                        enemyVelX.set(i, -eVel);
                        enemy.x += enemyVelX.get(i) * delta;
                    }
                }
            }

            // --- COLISIÓN JUGADOR - ENEMIGO ---
            if (player.overlaps(enemy)) {
                // Jugador cae encima → enemigo muere
                if (velY < 0 && player.y >= enemy.y + enemy.height * 0.5f) {
                    enemyAlive.set(i, false);
                    velY = JUMP_FORCE * 0.6f; // pequeño rebote al matar
                } else if (invincibleTimer <= 0f) {
                    // Jugador tocó al enemigo de lado → pierde vida
                        lives--;
                        invincibleTimer = 1.5f; // 1.5 segundos de invencibilidad
                        // Empujar al jugador hacia atrás
                        velY = 250f;
                        velX = (player.x < enemy.x) ? -300f : 300f;

                        if (lives <= 0) state = GameState.GAME_OVER;
                    }
                }
            }
        }

        // --- CAMARA ---
        private void updateCamera() {
            float camX = player.x + player.width / 2f;
            float camY = player.y + player.height / 2f;

            //Limitar la camara a los bordes del mundo
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            camX = Math.max(halfW, Math.min(camX, WORLD_WIDTH - halfW));
            camY = Math.max(halfH, Math.min(camY, WORLD_HEIGHT - halfH));

            camera.position.set(camX, camY, 0);
            camera.update();
        }

        // --- RENDER ---
    private void renderWorld() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //Suelo
        shapeRenderer.setColor(0.4f, 0.8f, 0.4f, 1f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, GROUND_Y);

        //Plataformas
        shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 1f);
        for (Rectangle platform : platforms) {
            shapeRenderer.rect(platform.x, platform.y, platform.width, platform.height);
        }

        // Enemigos
        for (int i = 0; i < enemies.size; i++) {
            if (!enemyAlive.get(i)) continue;
            shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 1f);
            Rectangle e = enemies.get(i);
            shapeRenderer.rect(e.x, e.y, e.width, e.height);
        }

        // Jugador (parpadea cuando es invencible)
        boolean visible = invincibleTimer <= 0f || ((int)(invincibleTimer * 10) % 2 == 0);
        if (visible) {
            shapeRenderer.setColor(0.9f, 0.5f, 0.2f, 1f);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        shapeRenderer.end();
    }

    private void renderHUD() {
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(uiBatch, "Vidas: " + lives, 20, Gdx.graphics.getHeight() - 20);
        uiBatch.end();
    }

    private void renderGameOver() {
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();
        font.setColor(0.9f, 0.2f, 0.2f, 1f);
        font.draw(uiBatch, "GAME OVER", Gdx.graphics.getWidth() / 2f - 80, Gdx.graphics.getHeight() / 2f + 20);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(uiBatch, "Presiona R para reiniciar", Gdx.graphics.getWidth() / 2f - 160, Gdx.graphics.getHeight() / 2f - 30);
        uiBatch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) initGame();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        uiBatch.dispose();
        font.dispose();
    }
}
