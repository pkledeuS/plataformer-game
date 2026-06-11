package com.cristian.plataformas;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Plataformas extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    //Jugador
    private Rectangle player;
    private float velX, velY;
    private boolean onGround = false;

    //Plataformas
    private Array<Rectangle> platforms;

    //Constantes
    private static final float SPEED     = 200f;
    private static final float GRAVITY   = -600f;
    private static final float JUMP_FORCE = 400f;
    private static final float GROUND_Y  = 50f;

    //Tamaño del mundo
    private static final float WORLD_WIDTH = 1600f;
    private static final float WORLD_HEIGHT = 600f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        //Camara
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Jugador: x, y, ancho, alto
        player = new Rectangle(100, GROUND_Y, 40, 60);

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
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // --- INPUT ---
        velX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  velX = -SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velX =  SPEED;
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
        }

        // --- CAMARA ---
        float camX = player.x + player.width / 2f;
        float camY = player.y + player.height / 2f;

        //Limitar la camara a los bordes del mundo
        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;
        camX = Math.max(halfW, Math.min(camX, WORLD_WIDTH - halfW));
        camY = Math.max(halfH, Math.min(camY, WORLD_HEIGHT - halfH));

        camera.position.set(camX, camY, 0);
        camera.update();

        // --- RENDER ---
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //Suelo
        shapeRenderer.setColor(0.4f, 0.8f, 0.4f, 1f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), GROUND_Y);

        //Plataformas
        shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 1f);
        for (Rectangle platform : platforms) {
            shapeRenderer.rect(platform.x, platform.y, platform.width, platform.height);
        }

        //Jugador
        shapeRenderer.setColor(0.9f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
