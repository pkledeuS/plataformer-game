package com.cristian.plataformas;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Plataformas extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;

    //Posicion del jugador
    private float x, y;

    //Velocidad del jugador
    private float velX, velY;

    //Constantes
    private static final float SPEED = 200f;
    private static final float GRAVITY =  -600f;
    private static final float JUMP_FORCE = 350f;
    private static final float GROUND_Y = 50f;

    private boolean onGround = false;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        x = 100;
        y = GROUND_Y;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // --INPUT--
        velX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) velX = -SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velX = SPEED;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && onGround) {
            velY = JUMP_FORCE;
            onGround = false;
        }

        // --FISICA--
        velY += GRAVITY * delta;
        x += velX * delta;
        y += velY * delta;

        // Suelo simple
        if (y <= GROUND_Y){
            y = GROUND_Y;
            velY = 0;
            onGround = true;
        }

        // --RENDER--
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //Suelo
        shapeRenderer.setColor(0.4f, 0.8f, 0.4f, 1f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), GROUND_Y);

        //Jugador
        shapeRenderer.setColor(0.9f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(x, y, 40, 60);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
